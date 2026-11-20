import {type ReactNode, useCallback, useContext, useState} from 'react';
import {request} from '../api/request.ts';
import {BoardViewsContext} from './BoardViewsContext.ts';
import type {TaskViewData} from '../components/types/TaskViewData.ts';
import {TasksContext, type NewTask} from './TasksContext.ts';
import {useAsyncResource} from './useAsyncResource.ts';

export function TasksProvider({
                                  boardId,
                                  children,
                              }: {
    readonly boardId: number;
    readonly children: ReactNode;
}) {

    const [reloadCount, setReloadCount] = useState(0);

    const viewsContext = useContext(BoardViewsContext);
    const selectedViewId = viewsContext?.selectedView?.id ?? null;
    const settingsRevision = viewsContext?.settingsRevision ?? 0;
    const requestKey = `${boardId}:${selectedViewId}:${reloadCount}:${settingsRevision}`;

    const loadTasks = useCallback(async (signal: AbortSignal): Promise<TaskViewData> => {
        const response = await request(`/views/${selectedViewId}/tasks`, {signal}, {
            fallbackMessage: 'Could not load tasks',
        });
        return response.json();
    }, [selectedViewId]);
    const {data: tasks, loading, error} = useAsyncResource(
        selectedViewId === null ? null : requestKey, loadTasks, 'Could not load tasks',
    );

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

        await request('/tasks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...input,
                title,
                boardId,
            }),
        }, {fallbackMessage: 'Could not create task'});

        // Reload groups and task ordering from the selected view after saving.
        setReloadCount(current => current + 1);
    }

    async function moveTask(taskId: number, statusId: number | null): Promise<void> {
        if (selectedViewId === null) {
            throw new Error('Select a board view before moving a task');
        }

        await request(`/tasks/${taskId}/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                statusId,
            }),
        }, {fallbackMessage: 'Could not update task status'});

        // Reload view tasks after status change
        setReloadCount(current => current + 1);
    }

    return (
        <TasksContext.Provider value={{tasks, loading, error, createTask, moveTask}}>
            {children}
        </TasksContext.Provider>
    );
}
