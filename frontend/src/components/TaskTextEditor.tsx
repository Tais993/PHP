import {Box, Button, Group, Stack, Textarea, TextInput} from '@mantine/core';

export type TaskTextField = 'title' | 'description';

type TaskTextEditorProps = {
    field: TaskTextField;
    value: string;
    error?: string;
    saving: boolean;
    canSave: boolean;
    onChange: (value: string) => void;
    onSave: (value: string) => void;
    onCancel: () => void;
};

export function TaskTextEditor({
    field,
    value,
    error,
    saving,
    canSave,
    onChange,
    onSave,
    onCancel,
}: Readonly<TaskTextEditorProps>) {
    const Input = field === 'title' ? TextInput : Textarea;

    return (
        <Box component="form" w="100%" onSubmit={event => {
            event.preventDefault();
            onSave(value);
        }}>
            <Stack gap="xs">
                <Input
                    {...(field === 'description' ? { rows: 9, resize: 'vertical' } : { size: 'lg' })}
                    aria-label={field === 'title' ? 'Title' : 'Description'}
                    value={value}
                    onChange={event => onChange(event.currentTarget.value)}
                    error={error}
                    readOnly={saving}
                    autoFocus
                />
                <Group gap="xs">
                    <Button
                        type="submit"
                        size="sm"
                        aria-label={`Save ${field}`}
                        loading={saving}
                        disabled={!canSave}
                    >
                        Save
                    </Button>
                    <Button
                        type="button"
                        variant="default"
                        size="sm"
                        aria-label={`Cancel ${field}`}
                        disabled={saving}
                        onClick={onCancel}
                    >
                        Cancel
                    </Button>
                </Group>
            </Stack>
        </Box>
    );
}
