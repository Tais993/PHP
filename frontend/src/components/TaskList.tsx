import { Stack, Text } from '@mantine/core'
import { Task } from './Task'
import type {TaskData} from "./types/TaskData.ts";

type TaskListProps = {
    tasks: TaskData[]
}

export function TaskList({ tasks }: Readonly<TaskListProps>) {
    if (tasks.length === 0) {
        return (
            <Text c="dimmed">
                No tasks to display.
            </Text>
        )
    }

    return (
        <Stack gap="sm" align="flex-start">
            {tasks.map(task => (
                <Task key={task.id} task={task} />
            ))}
        </Stack>
    )
}
