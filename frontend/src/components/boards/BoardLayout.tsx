import { useContext } from 'react';
import { Alert, Box, Divider, Group, Paper, ScrollArea, Stack, Text, Title } from '@mantine/core';
import { TaskList } from '../TaskList.tsx';
import { BoardsContext } from '../../providers/BoardsContext.ts';
import { TasksContext } from '../../providers/TasksContext.ts';
import AddTaskButton from '../buttons/AddTaskButton.tsx';
import { BoardViewsContext } from '../../providers/BoardViewsContext.ts';
import BoardTopBar from './BoardTopBar.tsx';
import { useBoardDragAndDrop } from '../../hooks/useBoardDragAndDrop.ts';

export default function BoardLayout() {
    const boardsContext = useContext(BoardsContext);
    const boardViewsContext = useContext(BoardViewsContext);
    const tasksContext = useContext(TasksContext);

    if (!boardsContext) {
        throw new Error('TopBar must be inside BoardsProvider');
    }

    if (!tasksContext) {
        throw new Error('TopBar must be inside TasksProvider');
    }

    if (!boardViewsContext) {
        throw new Error('TopBar must be inside BoardViewsProvider');
    }

    const { tasks, loading: tasksLoading, error: tasksError, moveTask } = tasksContext;
    const { selectedView } = boardViewsContext;

    const {
        draggedTaskId,
        displayedGroups,
        handleDragEnter,
        handleDragOver,
        handleDragLeave,
        handleDrop,
        handleTaskDragStart,
        handleTaskDragEnd,
    } = useBoardDragAndDrop({
        groupsRecord: tasks?.groups,
        moveTask,
    });

    return (
        <>
            <header>
                <BoardTopBar />
            </header>

            <Box aria-label="Tasks" p="lg">
                <Group justify="space-between" my="lg">
                    <Title order={1} size="h2">{selectedView?.name}</Title>
                    <AddTaskButton label="New task" />
                </Group>

                {tasksLoading && <Text c="dimmed" role="status">Loading tasks...</Text>}
                {tasksError && <Alert color="red" role="alert">{tasksError}</Alert>}

                <ScrollArea type="auto" offsetScrollbars>
                    <Box style={{ display: 'flex', alignItems: 'stretch', gap: 16 }}>
                        {displayedGroups.map(group => (
                            <Paper
                                component="section"
                                aria-label={group.name}
                                key={group.id ?? 'unassigned'}
                                withBorder
                                radius="sm"
                                bg="gray.0"
                                onDragEnter={() => handleDragEnter(group.id)}
                                onDragOver={(event) => handleDragOver(event, group.id)}
                                onDragLeave={handleDragLeave}
                                onDrop={(event) => void handleDrop(event, group.id)}
                                style={{
                                    flex: '1 0 300px',
                                    minWidth: 300,
                                    minHeight: 610,
                                    display: 'flex',
                                    flexDirection: 'column',
                                }}
                            >
                                <Group justify="space-between" p="sm">
                                    <Title order={2} size="md" fw={600}>{group.name}</Title>
                                    <Text size="sm" c="dimmed">{group.items.length}</Text>
                                </Group>

                                <Divider />

                                <Stack gap="sm" p="xs" style={{ flex: 1 }}>
                                    <TaskList
                                        tasks={group.items}
                                        draggedTaskId={draggedTaskId}
                                        onDragStart={handleTaskDragStart}
                                        onDragEnd={handleTaskDragEnd}
                                    />
                                    <AddTaskButton statusId={group.id} fullWidth mt="auto" />
                                </Stack>
                            </Paper>
                        ))}
                    </Box>
                </ScrollArea>
            </Box>
        </>
    );
}