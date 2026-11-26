import {type ReactNode, useCallback, useMemo} from 'react';
import type {BoardData} from "../components/types/BoardData.ts";
import {request} from '../api/request.ts';
import {BoardsContext, type NewBoard} from './BoardsContext.ts';
import {useAsyncResource} from './useAsyncResource.ts';

export function BoardsProvider({children}: { readonly children: ReactNode }) {
    const loadBoards = useCallback(async (signal: AbortSignal): Promise<BoardData[]> => {
        const response = await request('/boards', {signal}, {fallbackMessage: 'Could not load boards'});
        return response.json();
    }, []);
    const {data, loading, error, updateData} = useAsyncResource('boards', loadBoards, 'Could not load boards');

    const createBoard = useCallback(async (input: NewBoard): Promise<void> => {
        // Prevent the initial load from overwriting a newly created board.
        if (loading) {
            throw new Error('Wait until boards have finished loading');
        }

        if (error) {
            throw new Error('Reload the boards before creating a board');
        }

        const title = input.title.trim();

        if (!title || title.length > 255) {
            throw new Error('Board title must contain 1–255 characters');
        }

        const response = await request('/boards', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...input,
                title,
            }),
        }, {fallbackMessage: 'Could not create board'});

        const createdBoard: BoardData = await response.json();

        updateData(current =>
            current.some(board => board.id === createdBoard.id)
                ? current
                : [...current, createdBoard],
        );
    }, [loading, error, updateData]);

    const value = useMemo(() => ({boards: data ?? [], loading, error, createBoard}),
        [data, loading, error, createBoard]);

    return (
        <BoardsContext.Provider value={value}>
            {children}
        </BoardsContext.Provider>
    );
}
