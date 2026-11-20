import {type SubmitEvent, useState} from 'react';
import {Alert, Button, Divider, Group, NativeSelect, Popover, Stack, TextInput} from '@mantine/core';
import type {ViewDetails} from '../../providers/BoardViewsContext.ts';
import type {ViewMutationStatus} from '../../providers/boardViewSettings.ts';
import type {BoardViewData} from '../types/BoardViewData.ts';

type ViewOptionsPopoverProps = {
    view: BoardViewData;
    position: number;
    viewCount: number;
    mutationStatus: ViewMutationStatus;
    onSave: (details: ViewDetails) => Promise<boolean>;
    onDuplicate: (name: string) => Promise<boolean>;
    onRefresh: () => Promise<boolean>;
};

export default function ViewOptionsPopover({
    view,
    position,
    viewCount,
    mutationStatus,
    onSave,
    onDuplicate,
    onRefresh,
}: Readonly<ViewOptionsPopoverProps>) {
    const [draft, setDraft] = useState<ViewDetails | null>(null);
    const [saving, setSaving] = useState(false);
    const [duplicating, setDuplicating] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const trimmedName = draft?.name.trim() ?? '';
    const changed = draft !== null && (trimmedName !== view.name || draft.position !== position);
    const editingDisabled = saving || duplicating || refreshing || mutationStatus.pending || mutationStatus.needsRefresh;

    function openEditor() {
        setDraft({name: view.name, position});
        setError(null);
    }

    function closeEditor() {
        if (!saving && !duplicating && !refreshing) {
            setDraft(null);
            setError(null);
        }
    }

    async function saveDetails(event: SubmitEvent) {
        event.preventDefault();
        if (!draft || editingDisabled) return;

        if (!trimmedName) {
            setError('Enter a view name.');
            return;
        }

        if (trimmedName.length > 255) {
            setError('View names must be 255 characters or fewer.');
            return;
        }

        if (!changed) return;

        setSaving(true);
        setError(null);

        try {
            if (await onSave({name: trimmedName, position: draft.position})) {
                setDraft(null);
            } else {
                setError('Could not save the view changes. Please try again.');
            }
        } catch (exception) {
            setError(exception instanceof Error ? exception.message : 'Could not save the view changes.');
        } finally {
            setSaving(false);
        }
    }

    async function refreshList() {
        setRefreshing(true);
        setError(null);
        try {
            if (await onRefresh()) {
                setDraft(null);
            } else {
                setError('Could not refresh the board view list. Please try again.');
            }
        } catch (exception) {
            setError(exception instanceof Error ? exception.message : 'Could not refresh the board view list.');
        } finally {
            setRefreshing(false);
        }
    }

    async function duplicateView() {
        if (editingDisabled) return;

        setDuplicating(true);
        setError(null);
        try {
            const prefix = 'Copy of ';
            let sourceName = view.name.slice(0, 255 - prefix.length);
            const lastCodeUnit = sourceName.charCodeAt(sourceName.length - 1);
            if (lastCodeUnit >= 0xD800 && lastCodeUnit <= 0xDBFF) {
                sourceName = sourceName.slice(0, -1);
            }

            if (await onDuplicate(`${prefix}${sourceName}`)) {
                setDraft(null);
            } else {
                setError('Could not duplicate the view. Please try again.');
            }
        } catch (exception) {
            setError(exception instanceof Error ? exception.message : 'Could not duplicate the view.');
        } finally {
            setDuplicating(false);
        }
    }

    return (
        <Popover
            position="bottom-start"
            opened={draft !== null}
            onDismiss={closeEditor}
            trapFocus={!mutationStatus.needsRefresh}
            closeOnClickOutside={!saving && !duplicating && !refreshing}
            closeOnEscape={!saving && !duplicating && !refreshing}
            returnFocus
            shadow="md"
            width={320}
        >
            <Popover.Target>
                <Button
                    variant="subtle"
                    color="gray"
                    size="compact-sm"
                    aria-label={`Options for view ${view.name}`}
                    aria-expanded={draft !== null}
                    disabled={mutationStatus.pending}
                    onClick={() => draft ? closeEditor() : openEditor()}
                >
                    ⋯
                </Button>
            </Popover.Target>
            <Popover.Dropdown maw="calc(100vw - 2rem)">
                {draft && (
                    <Stack component="form" gap="sm" onSubmit={saveDetails}>
                        {(error || mutationStatus.error) && (
                            <Alert color="red" role="alert">{error ?? mutationStatus.error}</Alert>
                        )}
                        <TextInput
                            label="View name"
                            value={draft.name}
                            maxLength={255}
                            required
                            disabled={editingDisabled}
                            onChange={event => setDraft({...draft, name: event.currentTarget.value})}
                        />
                        <NativeSelect
                            label="Position"
                            value={String(draft.position)}
                            data={Array.from({length: viewCount}, (_, index) => ({
                                value: String(index + 1),
                                label: String(index + 1),
                            }))}
                            disabled={editingDisabled || viewCount < 2}
                            onChange={event => setDraft({...draft, position: Number(event.currentTarget.value)})}
                        />
                        <Divider/>
                        <Button
                            type="button"
                            variant="default"
                            loading={duplicating}
                            disabled={editingDisabled}
                            onClick={() => void duplicateView()}
                        >
                            Duplicate view
                        </Button>
                        <Group justify="flex-end">
                            {mutationStatus.needsRefresh && (
                                <Button
                                    type="button"
                                    variant="default"
                                    loading={refreshing}
                                    disabled={mutationStatus.pending || refreshing}
                                    onClick={() => void refreshList()}
                                >
                                    Refresh view list
                                </Button>
                            )}
                            <Button
                                type="button"
                                variant="default"
                                onClick={closeEditor}
                                disabled={saving || duplicating || refreshing}
                            >
                                Cancel
                            </Button>
                            <Button type="submit" loading={saving} disabled={editingDisabled || !changed}>
                                Save changes
                            </Button>
                        </Group>
                    </Stack>
                )}
            </Popover.Dropdown>
        </Popover>
    );
}