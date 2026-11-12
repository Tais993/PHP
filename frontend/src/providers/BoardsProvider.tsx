import {createContext, type ReactNode, useEffect, useState} from 'react';
import type {BoardData} from "../components/types/BoardData.ts";
import {apiUrl} from '../config.ts';

type BoardsContextValue = {
    boards: BoardData[];
    loading: boolean;
    error: string | null;
};

export const BoardsContext = createContext<BoardsContextValue | null>(null);

export function BoardsProvider({children}: { children: ReactNode }) {
    const [boards, setBoards] = useState<BoardData[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string | null>(null)


    useEffect(() => {
        const controller = new AbortController();

        setLoading(true);
        setError(null);
        fetch(apiUrl('/boards'), {
            signal: controller.signal,
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Could not load boards')
                }
                return response.json()
            })
            .then((data: BoardData[]) => {
                if (!controller.signal.aborted) {
                    setBoards(data)
                }
            })
            .catch((exception) => {
                if (controller.signal.aborted) return;
                setError('Could not load boards')
                console.log(exception)
            })
            .finally(() => {
                if (!controller.signal.aborted) {
                    setLoading(false)
                }
            })

        return () => controller.abort();
    }, [])

    return (
        <BoardsContext.Provider value={{boards, loading, error}}>
            {children}
        </BoardsContext.Provider>
    );
}
