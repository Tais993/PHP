import {type ReactNode, type SubmitEvent, useState} from 'react';
import {
    Alert,
    Button,
    type ButtonProps,
    Fieldset,
    Group,
    Modal,
    Stack,
    Textarea,
    TextInput,
} from '@mantine/core';

type AddItemButtonProps = {
    itemName: string;
    label?: string;
    buttonProps?: Pick<ButtonProps, 'variant' | 'color' | 'size' | 'fullWidth' | 'mt'>;
    dataCy?: {
        trigger: string;
        title?: string;
        submit?: string;
    };
    titleLabel?: string;
    titlePlaceholder?: string;
    showDescription?: boolean;
    fields?: ReactNode;
    disabled: boolean;
    onCreate: (input: {title: string; description: string}, fields: FormData) => Promise<void>;
};

export default function AddItemButton({
    itemName,
    label = `+ Add ${itemName}`,
    buttonProps,
    dataCy,
    titleLabel = 'Title',
    titlePlaceholder = 'What needs doing?',
    showDescription = true,
    fields,
    disabled,
    onCreate,
}: Readonly<AddItemButtonProps>) {
    const [opened, setOpened] = useState(false);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const trimmedTitle = title.trim();

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

    async function submit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();
        if (saving || disabled) return;

        if (!trimmedTitle) {
            setError(`Enter a ${itemName} ${titleLabel.toLowerCase()}`);
            return;
        }

        setSaving(true);
        setError(null);

        try {
            await onCreate({
                title: trimmedTitle,
                description: description.trim(),
            }, new FormData(event.currentTarget));

            setOpened(false);
            setTitle('');
            setDescription('');
        } catch (exception) {
            setError(
                exception instanceof Error
                    ? exception.message
                    : `Could not create ${itemName}`,
            );
        } finally {
            setSaving(false);
        }
    }

    return (
        <>
            <Button {...buttonProps} data-cy={dataCy?.trigger} onClick={openModal} disabled={disabled}>
                {label}
            </Button>

            <Modal
                opened={opened}
                onClose={closeModal}
                title={`Add ${itemName}`}
                centered
                closeOnClickOutside={!saving}
                closeOnEscape={!saving}
                withCloseButton={!saving}
            >
                <form onSubmit={submit}>
                    <Stack>
                        {error && (
                            <Alert color="red" role="alert">
                                {error}
                            </Alert>
                        )}

                        <TextInput
                            label={titleLabel}
                            placeholder={titlePlaceholder}
                            value={title}
                            onChange={(event) => setTitle(event.currentTarget.value)}
                            required
                            maxLength={255}
                            disabled={saving}
                            data-autofocus
                            data-cy={dataCy?.title}
                        />

                        {showDescription && (
                            <Textarea
                                label="Description"
                                placeholder="Optional details"
                                value={description}
                                onChange={(event) => setDescription(event.currentTarget.value)}
                                minRows={3}
                                autosize
                                disabled={saving}
                            />
                        )}

                        {fields && (
                            <Fieldset variant="unstyled" disabled={saving}>
                                <Stack>{fields}</Stack>
                            </Fieldset>
                        )}

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
                                data-cy={dataCy?.submit}
                                loading={saving}
                                disabled={disabled || !trimmedTitle}
                            >
                                Create {itemName}
                            </Button>
                        </Group>
                    </Stack>
                </form>
            </Modal>
        </>
    );
}
