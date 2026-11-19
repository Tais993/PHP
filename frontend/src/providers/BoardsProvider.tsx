import {type ReactNode, useEffect, useState} from 'react';
import type {BoardData} from "../components/types/BoardData.ts";
import {apiUrl} from '../config.ts';
import {BoardsContext, type NewBoard} from './BoardsContext.ts';

export function BoardsProvider({children}: { children: ReactNode }) {
    const [boards, setBoards] = useState<BoardData[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string | null>(null)


    useEffect(() => {
        const controller = new AbortController();

        async function loadBoards() {
            try {
                const response = await fetch(
                    apiUrl(`/boards`),
                    {signal: controller.signal},
                );

                if (!response.ok) {
                    throw new Error('Could not load boards');
                }
                const data: BoardData[] = await response.json();

                if (!controller.signal.aborted) {
                    setBoards(data);
                }
            } catch (exception) {
                if (!controller.signal.aborted) {
                    setError('Could not load boards');
                    console.error(exception);
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        void loadBoards();

        return () => controller.abort();
    }, [])

    async function createBoard(input: NewBoard): Promise<void> {
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

        const response = await fetch(apiUrl('/boards'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...input,
                title,
            }),
        });

        if (!response.ok) {
            throw new Error('Could not create board');
        }

        const createdBoard: BoardData = await response.json();

        setBoards(current =>
            current.some(board => board.id === createdBoard.id)
                ? current
                : [...current, createdBoard],
        );
    }

    return (
        <BoardsContext.Provider value={{boards, loading, error, createBoard}}>
            {children}
        </BoardsContext.Provider>
    );
}
