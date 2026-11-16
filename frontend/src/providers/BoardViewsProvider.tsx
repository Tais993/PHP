import {type ReactNode, useEffect, useState} from 'react';
import {apiUrl} from '../config.ts';
import type {BoardViewData} from '../components/types/BoardViewData.ts';
import {BoardViewsContext} from './BoardViewsContext.ts';

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
    const selectedViewId = loading || error
        ? null
        : boardViewId ?? boardViews[0]?.id ?? null;

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

    return (
        <BoardViewsContext.Provider
            value={{
                boardViews,
                selectedViewId,
                loading,
                error,
            }}
        >
            {children}
        </BoardViewsContext.Provider>
    );
}
