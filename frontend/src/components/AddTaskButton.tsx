import {useContext, useState} from 'react';
import {
    Alert,
    Button,
    Group,
    Modal,
    Stack,
    Textarea,
    TextInput,
} from '@mantine/core';
import {TasksContext} from '../providers/TasksProvider.tsx';

export default function AddTaskButton() {
    const context = useContext(TasksContext);

    const [opened, setOpened] = useState(false);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    if (!context) {
        throw new Error('AddTaskButton must be inside TasksProvider');
    }

    const {createTask, loading, error: loadError} = context;

    function openModal() {
        setTitle('');
        setDescription('');
        setError(null);
        setOpened(true);
    }

    function closeModal() {
        if (!saving) {
            setOpened(false);
        }
    }

    async function submitTask() {
        if (saving) return;

        if (!title.trim()) {
            setError('Enter a task title');
            return;
        }

        setSaving(true);
        setError(null);

        try {
            await createTask({
                title: title.trim(),
                description: description.trim(),
                status: 'TODO',
            });

            setOpened(false);
            setTitle('');
            setDescription('');
        } catch (exception) {
            setError(
                exception instanceof Error
                    ? exception.message
                    : 'Could not create task',
            );
        } finally {
            setSaving(false);
        }
    }

    return (
        <>
            <Button
                onClick={openModal}
                disabled={loading || Boolean(loadError)}
            >
                + Add task
            </Button>

            <Modal
                opened={opened}
                onClose={closeModal}
                title="Add task"
                centered
                closeOnClickOutside={!saving}
                closeOnEscape={!saving}
                withCloseButton={!saving}
            >
                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        void submitTask();
                    }}
                >
                    <Stack>
                        {error && (
                            <Alert color="red" role="alert">
                                {error}
                            </Alert>
                        )}

                        <TextInput
                            label="Title"
                            placeholder="What needs doing?"
                            value={title}
                            onChange={(event) =>
                                setTitle(event.currentTarget.value)
                            }
                            required
                            maxLength={255}
                            disabled={saving}
                            data-autofocus
                        />

                        <Textarea
                            label="Description"
                            placeholder="Optional details"
                            value={description}
                            onChange={(event) =>
                                setDescription(event.currentTarget.value)
                            }
                            minRows={3}
                            autosize
                            disabled={saving}
                        />

                        <Group justify="flex-end">
                            <Button
                                type="button"
                                variant="default"
                                onClick={closeModal}
                                disabled={saving}
                            >
                                Cancel
                            </Button>

                            <Button
                                type="submit"
                                loading={saving}
                                disabled={
                                    loading
                                    || Boolean(loadError)
                                    || !title.trim()
                                }
                            >
                                Create task
                            </Button>
                        </Group>
                    </Stack>
                </form>
            </Modal>
        </>
    );
}