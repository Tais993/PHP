import { Divider, Group, Paper, Stack, Text, Title } from '@mantine/core';
import type { DragEvent } from 'react';
import { TaskList } from '../TaskList.tsx';
import AddTaskButton from '../buttons/AddTaskButton.tsx';
import type { TaskViewData } from '../types/TaskViewData.ts';

type BoardColumnProps = {
    group: TaskViewData['groups'][string];
    draggedTaskId: number | null;
    onDragEnter: (statusId: number | null) => void;
    onDragOver: (event: DragEvent<HTMLElement>, statusId: number | null) => void;
    onDragLeave: (event: DragEvent<HTMLElement>) => void;
    onDrop: (event: DragEvent<HTMLElement>, statusId: number | null) => Promise<void>;
    onTaskDragStart: (taskId: number) => void;
    onTaskDragEnd: () => void;
};

export default function BoardColumn({
                                        group: { id, name, items },
                                        draggedTaskId,
                                        onDragEnter,
                                        onDragOver,
                                        onDragLeave,
                                        onDrop,
                                        onTaskDragStart,
                                        onTaskDragEnd,
                                    }: Readonly<BoardColumnProps>) {
    return (
        <Paper
            component="section"
            aria-label={name}
            withBorder
            radius="sm"
            bg="gray.0"
            onDragEnter={() => onDragEnter(id)}
            onDragOver={(event) => onDragOver(event, id)}
            onDragLeave={onDragLeave}
            onDrop={(event) => void onDrop(event, id)}
            style={{
                flex: '1 0 300px',
                minWidth: 300,
                minHeight: 610,
                display: 'flex',
                flexDirection: 'column',
            }}
        >
            <Group justify="space-between" p="sm">
                <Title order={2} size="md" fw={600}>{name}</Title>
                <Text size="sm" c="dimmed">{items.length}</Text>
            </Group>

            <Divider />

            <Stack gap="sm" p="xs" style={{ flex: 1 }}>
                <TaskList
                    tasks={items}
                    draggedTaskId={draggedTaskId}
                    onDragStart={onTaskDragStart}
                    onDragEnd={onTaskDragEnd}
                />
                <AddTaskButton statusId={id} fullWidth mt="auto" />
            </Stack>
        </Paper>
    );
}
