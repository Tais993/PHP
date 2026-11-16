import { Paper, Stack, Text } from '@mantine/core'
import { Task } from './Task'
import type {TaskData} from "./types/TaskData.ts";

type TaskListProps = {
    tasks: TaskData[]
}

export function TaskList({ tasks }: Readonly<TaskListProps>) {
    if (tasks.length === 0) {
        return (
            <Paper withBorder radius="sm" p="lg">
                <Text size="sm" c="dimmed" ta="center">
                    No tasks with this status
                </Text>
            </Paper>
        )
    }

    return (
        <Stack gap="sm">
            {tasks.map(task => (
                <Task key={task.id} task={task} />
            ))}
        </Stack>
    )
}
