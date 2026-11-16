import type {TaskData} from "./TaskData.ts";

export type TaskViewData = {
    groupedBy: 'STATUS';
    groups: Record<string, {
        id: number | null;
        name: string;
        position: number;
        items: TaskData[];
    }>;
};