import {createContext, type ReactNode, useEffect, useState} from 'react';
import type {TaskData} from "../components/types/TaskData.ts";

type TasksContextValue = {
    tasks: TaskData[];
    loading: boolean;
    error: string | null;
};

export const TasksContext = createContext<TasksContextValue | null>(null);

export function TasksProvider({boardId, children}: { boardId: number; children: ReactNode; }) {
    const [tasks, setTasks] = useState<TaskData[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string | null>(null)


    useEffect(() => {
        const controller = new AbortController();

        setLoading(true);
        setError(null);
        fetch(`/api/boards/${boardId}/tasks`, {
            signal: controller.signal,
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Could not load tasks')
                }
                return response.json()
            })
            .then((data: TaskData[]) => {
                if (!controller.signal.aborted) {
                    setTasks(data)
                }
            })
            .catch((exception) => {
                if (controller.signal.aborted) return;
                setError('Could not load tasks')
                console.log(exception)
            })
            .finally(() => {
                if (!controller.signal.aborted) {
                    setLoading(false)
                }
            })

        return () => controller.abort();
    }, [boardId])

    return (
        <TasksContext.Provider value={{tasks, loading, error}}>
            {children}
        </TasksContext.Provider>
    );
}