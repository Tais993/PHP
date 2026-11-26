import {type ReactNode, useCallback, useContext, useMemo, useRef, useState} from 'react';
import {request} from '../api/request.ts';
import {BoardViewsContext} from './BoardViewsContext.ts';
import type {TaskData} from '../components/types/TaskData.ts';
import type {TaskViewData} from '../components/types/TaskViewData.ts';
import {type NewTask, type TaskChanges, TasksContext} from './TasksContext.ts';
import {useAsyncResource} from './useAsyncResource.ts';

export function TasksProvider({
                                  boardId,
                                  children,
                              }: {
    readonly boardId: number;
    readonly children: ReactNode;
}) {

    const [reloadCount, setReloadCount] = useState(0);
    const pendingTaskSaves = useRef(new Map<number, Promise<TaskData>>());
    const getPendingTaskSave = useCallback((taskId: number) => pendingTaskSaves.current.get(taskId), []);

    const viewsContext = useContext(BoardViewsContext);
    const selectedViewId = viewsContext?.selectedView?.id ?? null;
    const settingsRevision = viewsContext?.settingsRevision ?? 0;
    const requestKey = `${boardId}:${selectedViewId}:${reloadCount}:${settingsRevision}`;

    const loadTasks = useCallback(async (signal: AbortSignal): Promise<TaskViewData> => {
        const response = await request(`/views/${selectedViewId}/tasks`, {cache: 'no-store', signal}, {
            fallbackMessage: 'Could not load tasks',
        });
        return response.json();
    }, [selectedViewId]);
    const {data: tasks, loading, error} = useAsyncResource(
        selectedViewId === null ? null : requestKey, loadTasks, 'Could not load tasks',
    );

    const createTask = useCallback(async (input: NewTask): Promise<void> => {
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
    }, [selectedViewId, loading, error, boardId]);

    const updateTask = useCallback(async (taskId: number, changes: TaskChanges): Promise<TaskData> => {
        const previousSave = getPendingTaskSave(taskId);

        async function patchTask(): Promise<TaskData> {
            // A failed save must not prevent the next edit from being sent.
            await previousSave?.catch(() => undefined);
            const response = await request(`/tasks/${taskId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(changes),
            }, {
                fallbackMessage: 'Could not save task changes.',
                parseProblemDetail: true,
            });

            const task: TaskData = await response.json();
            setReloadCount(current => current + 1);
            return task;
        }

        const pendingSave = patchTask();
        pendingTaskSaves.current.set(taskId, pendingSave);
        try {
            return await pendingSave;
        } finally {
            if (getPendingTaskSave(taskId) === pendingSave) {
                pendingTaskSaves.current.delete(taskId);
            }
        }
    }, [getPendingTaskSave]);

    const moveTask = useCallback(async (taskId: number, statusId: number | null): Promise<void> => {
        if (selectedViewId === null) {
            throw new Error('Select a board view before moving a task');
        }

        await updateTask(taskId, {status: statusId});
    }, [selectedViewId, updateTask]);

    const value = useMemo(() => ({tasks, loading, error, createTask, updateTask, getPendingTaskSave, moveTask}),
        [tasks, loading, error, createTask, updateTask, getPendingTaskSave, moveTask]);

    return (
        <TasksContext.Provider value={value}>
            {children}
        </TasksContext.Provider>
    );
}
