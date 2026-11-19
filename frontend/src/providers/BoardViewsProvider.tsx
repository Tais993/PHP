import {type ReactNode, useEffect, useState} from 'react';
import {apiUrl} from '../config.ts';
import type {BoardViewData} from '../components/types/BoardViewData.ts';
import {BoardViewsContext, type NewBoardView} from './BoardViewsContext.ts';

type BoardViewsResult = {
    boardId: number;
    data: BoardViewData[];
    error: string | null;
};

export function BoardViewsProvider({
                                       boardId,
                                       boardViewId,
                                       children,
                                   }: {
    readonly boardId: number;
    readonly boardViewId?: number;
    readonly children: ReactNode;
}) {

    const [result, setResult] = useState<BoardViewsResult | null>(null);
    const currentResult = result?.boardId === boardId ? result : null;
    const boardViews = currentResult?.data ?? [];
    const loading = currentResult === null;
    const invalidView = !loading && currentResult.error === null &&
        boardViewId !== undefined && !boardViews.some(view => view.id === boardViewId);
    const error = currentResult?.error ?? (invalidView ? 'View does not belong to this board' : null);
    const selectedView = loading || error
        ? null
        : boardViewId === undefined
            ? boardViews[0] ?? null
            : boardViews.find(view => view.id === boardViewId) ?? null;

    useEffect(() => {
        const controller = new AbortController();

        async function loadBoardViews() {
            try {
                const response = await fetch(
                    apiUrl(`/boards/${boardId}/views`),
                    {signal: controller.signal},
                );

                if (!response.ok) {
                    throw new Error('Could not load board views');
                }

                const data: BoardViewData[] = await response.json();

                if (!controller.signal.aborted) {
                    setResult({boardId, data, error: null});
                }

            } catch (exception) {
                if (!controller.signal.aborted) {
                    setResult({boardId, data: [], error: 'Could not load board views'});
                    console.error(exception);
                }
            }
        }

        void loadBoardViews();

        return () => controller.abort();
    }, [boardId]);

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

        const position = (boardViews.at(-1)?.position ?? -1) + 1;
        const response = await fetch(apiUrl('/views'), {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                ...input,
                boardId,
                name,
                position,
            }),
        });

        if (!response.ok) {
            throw new Error('Could not create view');
        }

        const createdView: BoardViewData = await response.json();

        setResult(current => {
            if (!current || current.boardId !== boardId || current.data.some(view => view.id === createdView.id)) {
                return current;
            }

            const data = [...current.data, createdView].sort((a, b) => a.position - b.position || a.id - b.id);
            return {...current, data};
        });
    }

    return (
        <BoardViewsContext.Provider
            value={{
                boardViews,
                selectedView,
                loading,
                error,
                createView,
            }}
        >
            {children}
        </BoardViewsContext.Provider>
    );
}
