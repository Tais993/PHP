import {Paper, Stack, Text} from '@mantine/core';
import {Task} from './Task.tsx';
import type {TaskData} from './types/TaskData.ts';

type TaskListProps = {
    tasks: TaskData[];
    draggedTaskId?: number | null;
    onDragStart?: (taskId: number) => void;
    onDragEnd?: () => void;
    onOpenTask: (taskId: number) => void;
};

export function TaskList({ tasks, draggedTaskId, onDragStart, onDragEnd, onOpenTask }: Readonly<TaskListProps>) {
    if (tasks.length === 0) {
        return (
            <Paper withBorder radius="sm" p="lg">
                <Text size="sm" c="dimmed" ta="center">
                    No tasks with this status
                </Text>
            </Paper>
        );
    }

    return (
        <Stack gap="sm">
            {tasks.map(task => (
                <Task
                    key={task.id}
                    task={task}
                    isDragging={draggedTaskId === task.id}
                    onDragStart={onDragStart}
                    onDragEnd={onDragEnd}
                    onOpen={onOpenTask}
                />
            ))}
        </Stack>
    );
}
