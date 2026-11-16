import {createContext} from 'react';
import type {TaskViewData} from '../components/types/TaskViewData.ts';

export type NewTask = {
    title: string;
    description: string;
    statusId: number | null;
};

type TasksContextValue = {
    tasks: TaskViewData | null;
    loading: boolean;
    error: string | null;
    createTask: (input: NewTask) => Promise<void>;
};

export const TasksContext = createContext<TasksContextValue | null>(null);
