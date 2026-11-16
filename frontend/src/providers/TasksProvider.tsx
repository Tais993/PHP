import {type ReactNode, useContext, useEffect, useState} from 'react';
import {apiUrl} from '../config.ts';
import {BoardViewsContext} from "./BoardViewsContext.ts";
import type {TaskViewData} from "../components/types/TaskViewData.ts";
import {TasksContext, type NewTask} from './TasksContext.ts';

type TaskResult = {
    requestKey: string;
    data: TaskViewData | null;
    error: string | null;
};

export function TasksProvider({
    boardId,
    children,
}: {
    readonly boardId: number;
    readonly children: ReactNode;
}) {
    const [result, setResult] = useState<TaskResult | null>(null);
    const [reloadCount, setReloadCount] = useState(0);

    const viewsContext = useContext(BoardViewsContext);
    const selectedViewId = viewsContext?.selectedView?.id ?? null;
    const requestKey = `${boardId}:${selectedViewId}:${reloadCount}`;
    const currentResult = result?.requestKey === requestKey ? result : null;

    const tasks = currentResult?.data ?? null;
    const error = currentResult?.error ?? null;
    const loading = selectedViewId !== null && currentResult === null;

    useEffect(() => {
        if (selectedViewId == null) {
            return;
        }

        const controller = new AbortController();

        async function loadTasks() {
            try {
                const response = await fetch(
                    apiUrl(`/views/${selectedViewId}/tasks`),
                    {signal: controller.signal},
                );

                if (!response.ok) {
                    throw new Error('Could not load tasks');
                }

                const data: TaskViewData = await response.json();

                if (!controller.signal.aborted) {
                    setResult({requestKey, data, error: null});
                }
            } catch (exception) {
                if (!controller.signal.aborted) {
                    setResult({requestKey, data: null, error: 'Could not load tasks'});
                    console.error(exception);
                }
            }
        }

        void loadTasks();

        return () => controller.abort();
    }, [selectedViewId, requestKey]);

    async function createTask(input: NewTask): Promise<void> {
        if (selectedViewId === null) {
            throw new Error('Select a board view before creating a task');
        }

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

        // Reload groups and task ordering from the selected view after saving.
        setReloadCount(current => current + 1);
    }

    return (
        <TasksContext.Provider value={{tasks, loading, error, createTask}}>
            {children}
        </TasksContext.Provider>
    );
}
