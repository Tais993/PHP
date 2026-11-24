import type {TaskDataStatus} from "./TaskDataStatus.ts";

export type TaskData = {
    id: number
    boardId: number
    title: string
    description: string
    createdAt: string
    status: TaskDataStatus | null
    dueDate: string | null
}
