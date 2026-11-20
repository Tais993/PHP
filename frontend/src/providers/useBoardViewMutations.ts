import type {BoardViewData} from '../components/types/BoardViewData.ts';
import {request} from '../api/request.ts';
import type {NewBoardView} from './BoardViewsContext.ts';

export function useBoardViewMutations({
    boardId,
    loading,
    error,
    boardViews,
    onCreated,
}: {
    boardId: number;
    loading: boolean;
    error: string | null;
    boardViews: BoardViewData[];
    onCreated: (view: BoardViewData) => void;
}) {
    return async function createView(input: NewBoardView): Promise<void> {
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
        const response = await request('/views', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                ...input,
                boardId,
                name,
                position,
            }),
        }, {fallbackMessage: 'Could not create view'});

        const createdView: BoardViewData = await response.json();
        onCreated(createdView);
    };
}