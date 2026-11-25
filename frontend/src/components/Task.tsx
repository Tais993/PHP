import {type DragEvent, useRef} from 'react';
import {Badge, Button, Group, Paper, Stack, Text} from '@mantine/core';
import type {TaskData} from './types/TaskData.ts';

type TaskProps = {
    task: TaskData;
    isDragging?: boolean;
    onDragStart?: (taskId: number) => void;
    onDragEnd?: () => void;
    onOpen: (taskId: number) => void;
};

export function Task({ task, isDragging, onDragStart, onDragEnd, onOpen }: Readonly<TaskProps>) {
    const openButton = useRef<HTMLButtonElement>(null);

    const handleDragStart = (event: DragEvent<HTMLDivElement>) => {
        event.dataTransfer.setData('text/plain', String(task.id));
        event.dataTransfer.setData('application/json', JSON.stringify({ taskId: task.id }));
        event.dataTransfer.effectAllowed = 'move';
        onDragStart?.(task.id);
    };

    const handleDragEnd = () => {
        onDragEnd?.();
    };

    return (
        <Paper
            data-cy="task-card"
            withBorder
            radius="sm"
            p="sm"
            w="100%"
            bg="white"
            shadow="xs"
            draggable
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
            onClick={() => {
                if (isDragging) return;
                openButton.current?.focus({ preventScroll: true });
                onOpen(task.id);
            }}
            style={{
                cursor: 'grab',
                userSelect: 'none',
                // Ensures smooth rendering of the drag image
                boxSizing: 'border-box',
                opacity: isDragging ? 0.5 : 1,
                borderStyle: isDragging ? 'dashed' : 'solid',
            }}
        >
            <Stack gap="sm">
                <Group justify="space-between" align="flex-start" wrap="nowrap">
                    <Group
                        gap="xs"
                        wrap="nowrap"
                        style={{ minWidth: 0 }}
                    >
                        <Text size="xs" c="dimmed" style={{ flexShrink: 0 }}>
                            #{task.id}
                        </Text>

                        <Text
                            size="sm"
                            fw={600}
                            style={{ overflowWrap: 'anywhere', minWidth: 0 }}
                        >
                            {task.title}
                        </Text>
                    </Group>

                    <Badge
                        variant="light"
                        color="gray"
                        radius="sm"
                        size="sm"
                        style={{ flexShrink: 0 }}
                    >
                        {task.status?.name ?? 'No status'}
                    </Badge>
                </Group>

                <Text
                    size="sm"
                    c="dimmed"
                    lineClamp={2}
                    style={{ overflowWrap: 'anywhere' }}
                >
                    {task.description || 'No description provided.'}
                </Text>

                <Group justify="space-between" mt="xs">
                    <Text size="xs" fw={600}>
                        Due: {task.dueDate || 'Not set'}
                    </Text>

                    <Button ref={openButton} variant="default" size="xs">
                        Open task
                    </Button>
                </Group>
            </Stack>
        </Paper>
    );
}
