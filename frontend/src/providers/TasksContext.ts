import {createContext} from 'react';
import type {TaskData} from '../components/types/TaskData.ts';
import type {TaskViewData} from '../components/types/TaskViewData.ts';

export type NewTask = {
    title: string;
    description: string;
    statusId: number | null;
};

export type TaskChanges = {
    title?: string;
    description?: string;
    dueDate?: string | null;
    status?: number | null;
};

type TasksContextValue = {
    tasks: TaskViewData | null;
    loading: boolean;
    error: string | null;
    createTask: (input: NewTask) => Promise<void>;
    updateTask: (taskId: number, changes: TaskChanges) => Promise<TaskData>;
    getPendingTaskSave: (taskId: number) => Promise<TaskData> | undefined;
    moveTask: (taskId: number, statusId: number | null) => Promise<void>;
};

export const TasksContext = createContext<TasksContextValue | null>(null);
