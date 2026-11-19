import { useContext } from 'react';
import { Alert, Box, Group, ScrollArea, Text, Title } from '@mantine/core';
import { BoardsContext } from '../../providers/BoardsContext.ts';
import { TasksContext } from '../../providers/TasksContext.ts';
import AddTaskButton from '../buttons/AddTaskButton.tsx';
import { BoardViewsContext } from '../../providers/BoardViewsContext.ts';
import BoardColumn from './BoardColumn.tsx';
import BoardTopBar from './BoardTopBar.tsx';
import BoardViewControls from './BoardViewControls.tsx';
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
    const { selectedView, settingsStatus, saveFilterRules, saveSorting, reloadViewSettings } = boardViewsContext;
    const boardTitle = boardsContext.boards.find(board => board.id === selectedView?.boardId)?.title;
    const taskCount = tasks && Object.values(tasks.groups).reduce((count, group) => count + group.items.length, 0);

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
                    <Box>
                        <Title order={1} size="h2">{selectedView?.name}</Title>
                        {taskCount !== null && (
                            <Text size="sm" c="dimmed" mt={4}>
                                {taskCount} {taskCount === 1 ? 'task' : 'tasks'}{boardTitle && ` in ${boardTitle}`}
                            </Text>
                        )}
                    </Box>
                    <AddTaskButton label="New task" />
                </Group>

                {selectedView && (
                    <BoardViewControls
                        key={selectedView.id}
                        settingsStatus={settingsStatus}
                        onReload={() => reloadViewSettings(selectedView.id)}
                        filter={{
                            rules: selectedView.filterRules,
                            groups: tasks?.groups,
                            onSave: rules => saveFilterRules(selectedView.id, rules),
                        }}
                        sort={{
                            sorting: selectedView,
                            onSave: sorting => saveSorting(selectedView.id, sorting),
                        }}
                    />
                )}

                {tasksLoading && <Text c="dimmed" role="status">Loading tasks...</Text>}
                {tasksError && <Alert color="red" role="alert">{tasksError}</Alert>}
                {taskCount === 0 && Boolean(selectedView?.filterRules.length) && (
                    <Text c="dimmed" role="status" mb="sm">No tasks match these filters.</Text>
                )}

                <ScrollArea type="auto" offsetScrollbars>
                    <Box style={{ display: 'flex', alignItems: 'stretch', gap: 16 }}>
                        {displayedGroups.map(group => (
                            <BoardColumn
                                key={group.id ?? 'unassigned'}
                                group={group}
                                draggedTaskId={draggedTaskId}
                                onDragEnter={handleDragEnter}
                                onDragOver={handleDragOver}
                                onDragLeave={handleDragLeave}
                                onDrop={handleDrop}
                                onTaskDragStart={handleTaskDragStart}
                                onTaskDragEnd={handleTaskDragEnd}
                            />
                        ))}
                    </Box>
                </ScrollArea>
            </Box>
        </>
    );
}
