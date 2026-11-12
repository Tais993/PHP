import {createContext, type ReactNode, useEffect, useState} from 'react';
import {apiUrl} from '../config.ts';
import type {TaskData} from "../components/types/TaskData.ts";

type NewTask = {
    title: string;
    description: string;
    status: string;
};

type TasksContextValue = {
    tasks: TaskData[];
    loading: boolean;
    error: string | null;
    createTask: (input: NewTask) => Promise<void>;
};

export const TasksContext = createContext<TasksContextValue | null>(null);

export function TasksProvider({
    boardId,
    children,
}: {
    boardId: number;
    children: ReactNode;
}) {
    const [tasks, setTasks] = useState<TaskData[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const controller = new AbortController();

        setLoading(true);
        setError(null);

        async function loadTasks() {
            try {
                const response = await fetch(
                    apiUrl(`/boards/${boardId}/tasks`),
                    {signal: controller.signal},
                );

                if (!response.ok) {
                    throw new Error('Could not load tasks');
                }

                const data: TaskData[] = await response.json();

                if (!controller.signal.aborted) {
                    setTasks(data);
                }
            } catch (exception) {
                if (!controller.signal.aborted) {
                    setError('Could not load tasks');
                    console.error(exception);
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        void loadTasks();

        return () => controller.abort();
    }, [boardId]);

    async function createTask(input: NewTask): Promise<void> {
        // Prevent the initial load from overwriting a newly created task.
        if (loading) {
            throw new Error('Wait until tasks have finished loading');
        }

        if (error) {
            throw new Error('Reload the board before creating a task');
        }

        const title = input.title.trim();

        if (!title || title.length > 255) {
            throw new Error('Task title must contain 1–255 characters');
        }

        const response = await fetch(apiUrl('/tasks'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...input,
                title,
                boardId,
            }),
        });

        if (!response.ok) {
            throw new Error('Could not create task');
        }

        const createdTask: TaskData = await response.json();

        setTasks(current =>
            current.some(task => task.id === createdTask.id)
                ? current
                : [...current, createdTask],
        );
    }

    return (
        <TasksContext.Provider value={{tasks, loading, error, createTask}}>
            {children}
        </TasksContext.Provider>
    );
}