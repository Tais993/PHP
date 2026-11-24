import {useContext, useState} from 'react';
import {
    Alert,
    Button,
    Drawer,
    Grid,
    Group,
    NativeSelect,
    Paper,
    Stack,
    Text,
    TextInput,
    VisuallyHidden
} from '@mantine/core';
import {ApiResponseError} from '../api/request.ts';
import {useTaskDetails} from '../hooks/useTaskDetails.ts';
import {type TaskChanges, TasksContext} from '../providers/TasksContext.ts';
import {TaskTextEditor, type TaskTextField} from './TaskTextEditor.tsx';

type TaskDetailPanelProps = {
    boardId: number;
    taskId?: number;
    onClose: () => void;
};

type TaskField = keyof TaskChanges;
// An editor exists while open. Its value follows the confirmed text until edited.
type TaskDraft = { value?: string };
type TaskDrafts = Partial<Record<TaskTextField, TaskDraft>>;

export default function TaskDetailPanel({ boardId, taskId, onClose }: Readonly<TaskDetailPanelProps>) {
    return (
        <Drawer.Root opened={taskId !== undefined} onClose={onClose} position="right" size={800} padding="lg">
            <Drawer.Overlay />
            <Drawer.Content>
                {taskId !== undefined && <TaskDetails key={taskId} boardId={boardId} taskId={taskId} />}
            </Drawer.Content>
        </Drawer.Root>
    );
}

function TaskDetails({ boardId, taskId }: Readonly<{ boardId: number; taskId: number }>) {
    const tasksContext = useContext(TasksContext);
    if (!tasksContext) {
        throw new Error('TaskDetails must be inside TasksProvider');
    }

    const { tasks, loading: tasksLoading } = tasksContext;
    const { task, loading, loadError, save } = useTaskDetails(boardId, taskId);
    const [drafts, setDrafts] = useState<TaskDrafts>({});
    const [fieldErrors, setFieldErrors] = useState<Partial<Record<TaskField, string>>>({});
    const [savingField, setSavingField] = useState<TaskField | null>(null);
    const titleValue = drafts.title?.value ?? task?.title ?? '';
    const descriptionValue = drafts.description?.value ?? task?.description ?? '';

    async function saveField<Field extends TaskField>(field: Field, value: TaskChanges[Field]) {
        if (savingField !== null) return;

        setSavingField(field);
        setFieldErrors(current => ({ ...current, [field]: undefined }));

        try {
            await save({ [field]: value });
            if (field === 'title' || field === 'description') {
                changeDraft(field);
            }
        } catch (exception) {
            const message = exception instanceof ApiResponseError ? exception.message : 'Could not save task changes.';
            setFieldErrors(current => ({ ...current, [field]: message }));
        } finally {
            setSavingField(null);
        }
    }

    const dueDay = task?.dueDate?.split('T')[0] ?? '';
    const statusValue = task?.status ? String(task.status.id) : 'unassigned';
    const statusOptions = Object.values(tasks?.groups ?? {})
        .filter(group => group.id !== null)
        .sort((a, b) => a.position - b.position)
        .map(group => ({ value: String(group.id), label: group.name }));

    if (task?.status && !statusOptions.some(option => option.value === statusValue)) {
        statusOptions.push({ value: statusValue, label: task.status.name });
    }
    statusOptions.push({ value: 'unassigned', label: 'Unassigned' });

    function changeDraft(field: TaskTextField, draft?: TaskDraft) {
        setDrafts(current => {
            const next = { ...current };
            if (draft === undefined) {
                delete next[field];
            } else {
                next[field] = draft;
            }
            return next;
        });
        setFieldErrors(current => ({ ...current, [field]: undefined }));
    }

    function changeDraftText(field: TaskTextField, value: string) {
        changeDraft(field, value === task?.[field] ? {} : { value });
    }

    function saveDraft(field: TaskTextField, value: string) {
        const updatedValue = field === 'title' ? value.trim() : value;
        if (field === 'title' && (!updatedValue || updatedValue.length > 255)) {
            setFieldErrors(current => ({ ...current, title: 'Enter a title with 1 to 255 characters.' }));
            return;
        }

        void saveField(field, updatedValue);
    }

    function changeDueDay(day: string) {
        if (day === dueDay) return;

        // Keep the API's local time of day when changing the calendar date.
        const time = task?.dueDate?.split('T')[1] ?? '00:00:00';
        void saveField('dueDate', day ? `${day}T${time}` : null);
    }

    return (
        <>
            <Drawer.Header style={{ alignItems: 'flex-start', gap: 'var(--mantine-spacing-sm)' }}>
                {task && drafts.title !== undefined ? (
                    <>
                        <VisuallyHidden><Drawer.Title>{task.title}</Drawer.Title></VisuallyHidden>
                        <TaskTextEditor
                            field="title"
                            value={titleValue}
                            error={fieldErrors.title}
                            saving={savingField === 'title'}
                            canSave={savingField === null && titleValue !== task.title}
                            onChange={value => changeDraftText('title', value)}
                            onSave={value => saveDraft('title', value)}
                            onCancel={() => changeDraft('title')}
                        />
                    </>
                ) : (
                    <Group gap="xs" wrap="nowrap" style={{ minWidth: 0 }}>
                        <Drawer.Title fz={32} fw={600} lh={1.2} style={{ overflowWrap: 'anywhere' }}>
                            {task?.title ?? 'Task details'}
                        </Drawer.Title>
                        {task && (
                            <Button
                                variant="subtle"
                                size="compact-xs"
                                aria-label="Edit title"
                                onClick={() => changeDraft('title', {})}
                            >
                                Edit
                            </Button>
                        )}
                    </Group>
                )}
                <Drawer.CloseButton aria-label="Close task details" />
            </Drawer.Header>

            <Drawer.Body>
                <Stack gap="lg">
                    {loadError && <Alert color="red" role="alert">{loadError}</Alert>}
                    {loading && <Text c="dimmed" role="status">Loading task...</Text>}

                    {task && (
                        <Grid gap="lg">
                            <Grid.Col span={{ base: 12, sm: 8 }}>
                                <Paper
                                    withBorder
                                    radius="sm"
                                    p="md"
                                    h={drafts.description === undefined ? '11rem' : undefined}
                                    style={drafts.description === undefined ? { resize: 'vertical', overflow: 'auto' } : undefined}
                                >
                                    {drafts.description === undefined ? (
                                        <Group justify="space-between" align="flex-start" wrap="nowrap" gap="sm">
                                            <Text
                                                c={task.description ? undefined : 'dimmed'}
                                                style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere', minWidth: 0 }}
                                            >
                                                {task.description || 'No description provided.'}
                                            </Text>
                                            <Button
                                                variant="subtle"
                                                size="compact-xs"
                                                aria-label="Edit description"
                                                onClick={() => changeDraft('description', {})}
                                            >
                                                Edit
                                            </Button>
                                        </Group>
                                    ) : (
                                        <TaskTextEditor
                                            field="description"
                                            value={descriptionValue}
                                            error={fieldErrors.description}
                                            saving={savingField === 'description'}
                                            canSave={savingField === null && descriptionValue !== task.description}
                                            onChange={value => changeDraftText('description', value)}
                                            onSave={value => saveDraft('description', value)}
                                            onCancel={() => changeDraft('description')}
                                        />
                                    )}
                                </Paper>
                            </Grid.Col>

                            <Grid.Col span={{ base: 12, sm: 4 }}>
                                <Paper component="aside" aria-label="Task properties" withBorder radius="sm" p="md">
                                    <Stack gap="md">
                                        <NativeSelect
                                            label="Status"
                                            data={statusOptions}
                                            value={statusValue}
                                            onChange={event => {
                                                const value = event.currentTarget.value;
                                                void saveField('status', value === 'unassigned' ? null : Number(value));
                                            }}
                                            error={fieldErrors.status}
                                            disabled={savingField !== null || !tasks}
                                            description={!tasks && (tasksLoading ? 'Loading statuses...' : 'Statuses unavailable.')}
                                        />
                                        <Stack gap="xs" align="flex-start">
                                            <TextInput
                                                label="Due date"
                                                type="date"
                                                value={dueDay}
                                                onChange={event => changeDueDay(event.currentTarget.value)}
                                                error={fieldErrors.dueDate}
                                                disabled={savingField !== null}
                                                w="100%"
                                            />
                                            {dueDay && (
                                                <Button
                                                    variant="subtle"
                                                    size="compact-sm"
                                                    disabled={savingField !== null}
                                                    onClick={() => changeDueDay('')}
                                                >
                                                    Clear due date
                                                </Button>
                                            )}
                                        </Stack>
                                    </Stack>
                                </Paper>
                            </Grid.Col>
                        </Grid>
                    )}
                </Stack>
            </Drawer.Body>
        </>
    );
}
