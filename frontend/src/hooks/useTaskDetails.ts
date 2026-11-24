import {useContext, useEffect, useRef, useState} from 'react';
import {ApiResponseError, request} from '../api/request.ts';
import type {TaskData} from '../components/types/TaskData.ts';
import {type TaskChanges, TasksContext} from '../providers/TasksContext.ts';

type TaskDetailsSession = {
    read?: AbortController;
};

export function useTaskDetails(boardId: number, taskId: number) {
    const tasksContext = useContext(TasksContext);
    if (!tasksContext) {
        throw new Error('useTaskDetails must be inside TasksProvider');
    }
    const {getPendingTaskSave, updateTask} = tasksContext;
    const [task, setTask] = useState<TaskData | null>(null);
    const [loadError, setLoadError] = useState<string | null>(null);
    const session = useRef<TaskDetailsSession | null>(null);

    useEffect(() => {
        const current: TaskDetailsSession = {};
        session.current = current;

        async function loadTask() {
            current.read?.abort();
            const read = new AbortController();
            current.read = read;

            try {
                // An edit can still be in flight after closing and reopening the drawer.
                await getPendingTaskSave(taskId)?.catch(() => undefined);
                read.signal.throwIfAborted();

                const response = await request(`/tasks/${taskId}`, {cache: 'no-store', signal: read.signal}, {
                    fallbackMessage: 'Could not load task.',
                    notFoundMessage: 'Task not found.',
                    parseProblemDetail: true,
                });
                const loaded: TaskData = await response.json();
                if (read.signal.aborted) return;

                if (loaded.boardId !== boardId) {
                    setTask(null);
                    setLoadError('This task belongs to a different board.');
                    return;
                }

                setTask(loaded);
                setLoadError(null);
            } catch (exception) {
                if (read.signal.aborted) return;

                if (exception instanceof ApiResponseError && exception.status === 404) {
                    setTask(null);
                }
                setLoadError(exception instanceof ApiResponseError ? exception.message : 'Could not load task.');
            }
        }

        function refreshOnFocus() {
            if (!getPendingTaskSave(taskId)) void loadTask();
        }

        void loadTask();
        window.addEventListener('focus', refreshOnFocus);
        return () => {
            window.removeEventListener('focus', refreshOnFocus);
            session.current = null;
            current.read?.abort();
        };
    }, [boardId, taskId, getPendingTaskSave]);

    async function save(changes: TaskChanges) {
        const current = session.current;
        current?.read?.abort();

        const saved = await updateTask(taskId, changes);
        if (current && session.current === current) {
            setTask(saved);
            setLoadError(null);
        }
    }

    return {task, loading: task === null && loadError === null, loadError, save};
}
