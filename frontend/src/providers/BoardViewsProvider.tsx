import {type ReactNode, useCallback, useRef, useSyncExternalStore} from 'react';
import {ApiResponseError, request} from '../api/request.ts';
import type {BoardViewData, FilterRule, ViewSorting} from '../components/types/BoardViewData.ts';
import {BoardViewsContext, type NewBoardView, type ViewDetails} from './BoardViewsContext.ts';
import {useAsyncResource} from './useAsyncResource.ts';
import {
    beginBoardViewMutation,
    finishBoardViewMutation,
    getBoardViewMutationStatus,
    getSettingsRevision,
    getViewSettings,
    hasUnsettledViewSettings,
    initialSettings,
    subscribeToViewSettings,
    updateViewSettings,
    waitForBoardViewMutation,
} from './boardViewSettings.ts';

type BoardViewsResult = {
    data: BoardViewData[];
    implicitSelectedViewId: number | null;
    settingsRevisionAtRead: number;
};

function orderBoardViews(views: BoardViewData[]) {
    return [...views].sort((a, b) => a.position - b.position || a.id - b.id);
}

function readBoardListRevision(revisions: Map<number, number>, boardId: number) {
    return revisions.get(boardId) ?? 0;
}

export function BoardViewsProvider({
    boardId,
    boardViewId,
    children,
}: {
    readonly boardId: number;
    readonly boardViewId?: number;
    readonly children: ReactNode;
}) {
    const boardListRevisions = useRef(new Map<number, number>());
    function invalidateBoardListReads(targetBoardId: number) {
        boardListRevisions.current.set(
            targetBoardId,
            readBoardListRevision(boardListRevisions.current, targetBoardId) + 1,
        );
    }

    const loadBoardViews = useCallback(async (signal: AbortSignal): Promise<BoardViewsResult> => {
        while (true) {
            await waitForBoardViewMutation(boardId, signal);
            const requestRevision = readBoardListRevision(boardListRevisions.current, boardId);
            const settingsRevisionAtRead = getSettingsRevision();
            const response = await request(`/boards/${boardId}/views`, {cache: 'no-store', signal}, {
                fallbackMessage: 'Could not load board views',
            });
            const data = orderBoardViews(await response.json() as BoardViewData[]);
            if (requestRevision === readBoardListRevision(boardListRevisions.current, boardId)) {
                return {data, implicitSelectedViewId: data[0]?.id ?? null, settingsRevisionAtRead};
            }
        }
    }, [boardId]);

    const {data: currentResult, loading, error: loadError, updateData} = useAsyncResource(
        String(boardId), loadBoardViews, 'Could not load board views',
    );

    const settings = useSyncExternalStore(subscribeToViewSettings, getViewSettings);
    const viewMutationStatus = useSyncExternalStore(
        subscribeToViewSettings,
        () => getBoardViewMutationStatus(boardId),
    );
    const boardViewsResult = currentResult;
    const boardViews = boardViewsResult ? boardViewsResult.data.map(view => {
        const updated = settings.get(view.id);
        // A save can finish while the board's view list is loading.
        return updated?.savedView && updated.revision > boardViewsResult.settingsRevisionAtRead
            ? updated.savedView
            : view;
    }) : [];

    const invalidView = !loading && loadError === null &&
        boardViewId !== undefined && !boardViews.some(view => view.id === boardViewId);
    const error = loadError ?? (invalidView ? 'View does not belong to this board' : null);
    const selectedView = loading || error
        ? null
        : boardViewId === undefined
            ? boardViews.find(view => view.id === currentResult?.implicitSelectedViewId) ?? boardViews[0] ?? null
            : boardViews.find(view => view.id === boardViewId) ?? null;

    const selectedSettings = (selectedView && settings.get(selectedView.id)) || initialSettings;

    async function fetchBoardViews(targetBoardId: number) {
        const response = await request(`/boards/${targetBoardId}/views`, {
            cache: 'no-store',
            signal: AbortSignal.timeout(5_000),
        }, {
            fallbackMessage: 'Could not reload board views.',
            parseProblemDetail: true,
        });
        return orderBoardViews(await response.json() as BoardViewData[]);
    }

    function applyBoardViews(targetBoardId: number, data: BoardViewData[]) {
        if (boardId !== targetBoardId) return;
        updateData(current => ({
            ...current,
            data,
            implicitSelectedViewId: current.implicitSelectedViewId ?? data[0]?.id ?? null,
            settingsRevisionAtRead: getSettingsRevision(),
        }));
    }

    async function recoverUncertainMutation(targetBoardId: number, errorMessage: string) {
        try {
            const data = await fetchBoardViews(targetBoardId);
            applyBoardViews(targetBoardId, data);
            finishBoardViewMutation(targetBoardId, {error: errorMessage});
            return true;
        } catch (exception) {
            const recoveryError = exception instanceof Error
                ? exception.message
                : 'Could not reload board views.';
            finishBoardViewMutation(targetBoardId, {
                needsRefresh: true,
                error: `${errorMessage} ${recoveryError} Refresh the board list before making another change.`,
            });
            return false;
        }
    }

    async function refreshBoardViews() {
        if (!getBoardViewMutationStatus(boardId).needsRefresh) return true;
        if (!beginBoardViewMutation(boardId, true)) return false;

        invalidateBoardListReads(boardId);
        try {
            const data = await fetchBoardViews(boardId);
            applyBoardViews(boardId, data);
            finishBoardViewMutation(boardId);
            return true;
        } catch (exception) {
            const message = exception instanceof Error ? exception.message : 'Could not reload board views.';
            finishBoardViewMutation(boardId, {needsRefresh: true, error: message});
            return false;
        }
    }

    function saveFilterRules(viewId: number, filterRules: FilterRule[]) {
        return boardViews.some(view => view.id === viewId)
            ? updateViewSettings(viewId, boardId, 'filter-rules', {filterRules})
            : Promise.resolve(false);
    }

    function saveSorting(viewId: number, sorting: ViewSorting) {
        return boardViews.some(view => view.id === viewId)
            ? updateViewSettings(viewId, boardId, 'sorting', sorting)
            : Promise.resolve(false);
    }

    function reloadViewSettings(viewId: number) {
        return boardViews.some(view => view.id === viewId)
            ? updateViewSettings(viewId, boardId)
            : Promise.resolve(false);
    }

    async function saveViewDetails(viewId: number, details: ViewDetails): Promise<boolean> {
        if (loading || error) {
            throw new Error('Reload the board before changing view details.');
        }

        const fromIndex = boardViews.findIndex(view => view.id === viewId);
        if (fromIndex === -1) {
            throw new Error('This view is no longer on the current board.');
        }

        const name = details.name.trim();
        if (!name || name.length > 255) {
            throw new Error('View name must contain 1–255 characters.');
        }
        if (!Number.isInteger(details.position) || details.position < 1 || details.position > boardViews.length) {
            throw new Error('Choose a position within the current view list.');
        }
        if (hasUnsettledViewSettings(boardId)) {
            throw new Error('Reload the affected view settings before changing view details.');
        }

        const status = getBoardViewMutationStatus(boardId);
        if (!beginBoardViewMutation(boardId)) {
            throw new Error(status.error ?? 'Another view change is in progress.');
        }

        invalidateBoardListReads(boardId);
        const signal = AbortSignal.timeout(15_000);
        const updatedViews = [...boardViews];
        const toIndex = details.position - 1;
        const moving = fromIndex !== toIndex;
        if (moving) {
            const [movedView] = updatedViews.splice(fromIndex, 1);
            updatedViews.splice(toIndex, 0, movedView);
        }
        let savedAny = false;

        try {
            // The details endpoint only updates one view. Assign distinct positions to
            // the reordered list, including when existing positions have ties or gaps.
            for (const [index, view] of updatedViews.entries()) {
                const position = moving ? index : view.position;
                const nextName = view.id === viewId ? name : view.name;
                if (position === view.position && nextName === view.name) continue;

                const response = await request(`/views/${view.id}/details`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({name: nextName, position}),
                    signal,
                }, {
                    fallbackMessage: 'Could not save view changes.',
                    parseProblemDetail: true,
                });
                savedAny = true;
                updatedViews[index] = await response.json() as BoardViewData;
            }

            applyBoardViews(boardId, orderBoardViews(updatedViews));
            finishBoardViewMutation(boardId);
            return true;
        } catch (exception) {
            if (exception instanceof ApiResponseError && exception.status < 500 && !savedAny) {
                finishBoardViewMutation(boardId, {error: exception.message});
                throw exception;
            }

            let uncertainMessage = 'The view update outcome is uncertain.';
            if (signal.aborted) {
                uncertainMessage = 'The view update timed out.';
            } else if (savedAny) {
                uncertainMessage = 'Some view changes may have been saved.';
            }
            const refreshed = await recoverUncertainMutation(boardId, uncertainMessage);
            if (refreshed) {
                throw new Error(`${uncertainMessage} The board list was refreshed; verify the result before trying again.`);
            }
            throw new Error(getBoardViewMutationStatus(boardId).error ?? uncertainMessage);
        }
    }

    async function duplicateView(viewId: number, requestedName: string): Promise<boolean> {
        if (loading || error) {
            throw new Error('Reload the board before duplicating a view.');
        }

        if (!boardViews.some(view => view.id === viewId)) {
            throw new Error('This view is no longer on the current board.');
        }

        const name = requestedName.trim();
        if (!name || name.length > 255) {
            throw new Error('View name must contain 1–255 characters.');
        }
        if (hasUnsettledViewSettings(boardId)) {
            throw new Error('Reload the affected view settings before duplicating a view.');
        }

        const status = getBoardViewMutationStatus(boardId);
        if (!beginBoardViewMutation(boardId)) {
            throw new Error(status.error ?? 'Another view change is in progress.');
        }

        invalidateBoardListReads(boardId);
        const signal = AbortSignal.timeout(15_000);

        try {
            const response = await request(`/views/${viewId}/duplicate`, {
                method: 'POST',
                headers: {'Content-Type': 'text/plain;charset=UTF-8'},
                body: name,
                signal,
            }, {
                fallbackMessage: 'Could not duplicate view.',
                parseProblemDetail: true,
            });

            const duplicate: BoardViewData = await response.json();
            applyBoardViews(boardId, orderBoardViews([...boardViews, duplicate]));
            finishBoardViewMutation(boardId);
            return true;
        } catch (exception) {
            if (exception instanceof ApiResponseError && exception.status < 500) {
                finishBoardViewMutation(boardId, {error: exception.message});
                throw exception;
            }

            const uncertainMessage = signal.aborted
                ? 'View duplication timed out.'
                : 'The view duplication outcome is uncertain.';
            const refreshed = await recoverUncertainMutation(boardId, uncertainMessage);
            throw new Error(refreshed
                ? `${uncertainMessage} The board list was refreshed; verify the result before trying again.`
                : getBoardViewMutationStatus(boardId).error ?? uncertainMessage);
        }
    }

    async function createView(input: NewBoardView): Promise<void> {
        if (loading) {
            throw new Error('Wait until board views have finished loading');
        }

        if (error) {
            throw new Error('Reload the board before creating a view');
        }

        const name = input.name.trim();

        if (!name || name.length > 255) {
            throw new Error('View name must contain 1–255 characters');
        }
        if (hasUnsettledViewSettings(boardId)) {
            throw new Error('Reload the affected view settings before creating a view.');
        }

        const status = getBoardViewMutationStatus(boardId);
        if (!beginBoardViewMutation(boardId)) {
            throw new Error(status.error ?? 'Another view change is in progress.');
        }

        const position = (boardViews.at(-1)?.position ?? -1) + 1;
        invalidateBoardListReads(boardId);
        const signal = AbortSignal.timeout(15_000);

        try {
            const response = await request('/views', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    ...input,
                    boardId,
                    name,
                    position,
                }),
                signal,
            }, {fallbackMessage: 'Could not create view.', parseProblemDetail: true});

            const createdView: BoardViewData = await response.json();
            updateData(current => current.data.some(view => view.id === createdView.id)
                ? current
                : {
                    ...current,
                    data: orderBoardViews([...current.data, createdView]),
                    settingsRevisionAtRead: getSettingsRevision(),
                });
            finishBoardViewMutation(boardId);
        } catch (exception) {
            if (exception instanceof ApiResponseError) {
                finishBoardViewMutation(boardId, {error: exception.message});
                throw exception;
            }

            const uncertainMessage = signal.aborted
                ? 'View creation timed out.'
                : 'The view creation outcome is uncertain.';
            const refreshed = await recoverUncertainMutation(boardId, uncertainMessage);
            throw new Error(refreshed
                ? `${uncertainMessage} The board list was refreshed; verify the result before trying again.`
                : getBoardViewMutationStatus(boardId).error ?? uncertainMessage);
        }
    }
    return (
        <BoardViewsContext.Provider
            value={{
                boardViews,
                selectedView,
                loading,
                error,
                createView,
                settingsStatus: selectedSettings,
                settingsRevision: selectedSettings.revision,
                viewMutationStatus,
                saveFilterRules,
                saveSorting,
                reloadViewSettings,
                saveViewDetails,
                duplicateView,
                refreshBoardViews,
            }}
        >
            {children}
        </BoardViewsContext.Provider>
    );
}
