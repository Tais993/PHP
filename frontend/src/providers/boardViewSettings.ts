import {request} from '../api/request.ts';
import type {BoardViewData, FilterRule, ViewSorting} from '../components/types/BoardViewData.ts';

export type ViewSettingsStatus = {
    pending: boolean;
    needsRefresh: boolean;
    error: string | null;
};

export type ViewMutationStatus = {
    pending: boolean;
    needsRefresh: boolean;
    error: string | null;
};

type ViewSettings = ViewSettingsStatus & {
    revision: number;
    boardId?: number;
    savedView?: BoardViewData;
};

export const initialSettings: ViewSettings = {
    revision: 0,
    pending: false,
    needsRefresh: false,
    error: null,
};

// Saves and their outcomes outlive the board provider.
let settings = new Map<number, ViewSettings>();
let revision = 0;
const listeners = new Set<() => void>();
const initialMutationStatus: ViewMutationStatus = {pending: false, needsRefresh: false, error: null};
const boardMutationStatuses = new Map<number, ViewMutationStatus>();

export const getViewSettings = () => settings;
export const getSettingsRevision = () => revision;
export const getBoardViewMutationStatus = (boardId: number) =>
    boardMutationStatuses.get(boardId) ?? initialMutationStatus;
export const hasUnsettledViewSettings = (boardId: number, exceptViewId?: number) =>
    Array.from(settings.entries()).some(([viewId, viewSettings]) =>
        viewId !== exceptViewId && viewSettings.boardId === boardId &&
        (viewSettings.pending || viewSettings.needsRefresh));

export function subscribeToViewSettings(listener: () => void) {
    listeners.add(listener);
    return () => {
        listeners.delete(listener);
    };
}

function updateSettings(viewId: number, next: ViewSettings) {
    settings = new Map(settings).set(viewId, next);
    listeners.forEach(listener => listener());
}

function updateBoardMutationStatus(boardId: number, next: ViewMutationStatus) {
    boardMutationStatuses.set(boardId, next);
    listeners.forEach(listener => listener());
}

export function beginBoardViewMutation(boardId: number, recovery = false) {
    const current = getBoardViewMutationStatus(boardId);
    if (current.pending || current.needsRefresh && !recovery) {
        return false;
    }

    updateBoardMutationStatus(boardId, {...current, pending: true, error: null});
    return true;
}

export function finishBoardViewMutation(
    boardId: number,
    {needsRefresh = false, error = null}: {needsRefresh?: boolean; error?: string | null} = {},
) {
    updateBoardMutationStatus(boardId, {pending: false, needsRefresh, error});
}

export async function updateViewSettings(
    viewId: number,
    boardId: number,
    ...change: [] | ['filter-rules', {filterRules: FilterRule[]}] | ['sorting', ViewSorting]
): Promise<boolean> {
    const current = settings.get(viewId) ?? initialSettings;
    const [kind, payload] = change;
    const reloading = kind === undefined;
    if (current.pending || reloading !== current.needsRefresh) {
        return false;
    }
    if (hasUnsettledViewSettings(boardId, viewId)) {
        return false;
    }
    if (!beginBoardViewMutation(boardId)) {
        return false;
    }

    const path = `/views/${viewId}`;
    const signal = AbortSignal.timeout(reloading ? 5_000 : 15_000);
    const fallbackError = reloading
        ? 'Could not reload the saved settings. Try again.'
        : 'Could not save view settings. Try again.';
    updateSettings(viewId, {...current, boardId, pending: true, error: null});

    try {
        const response = await request(reloading ? path : `${path}/${kind}`, reloading
            ? {cache: 'no-store', signal}
            : {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload),
                signal,
            }, {fallbackMessage: fallbackError, parseProblemDetail: true});

        const savedView: BoardViewData = await response.json();
        updateSettings(viewId, {...initialSettings, boardId, savedView, revision: ++revision});
        return true;

    } catch (exception) {
        const saveTimedOut = !reloading && signal.aborted;
        const errorMessage = exception instanceof Error ? exception.message : fallbackError;

        updateSettings(viewId, {
            ...current,
            boardId,
            needsRefresh: reloading || saveTimedOut,
            error: saveTimedOut
                ? 'The save timed out. Reload the saved settings before applying changes again.'
                : errorMessage,
        });
        return false;
    } finally {
        finishBoardViewMutation(boardId);
    }
}