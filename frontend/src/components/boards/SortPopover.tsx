import { type SubmitEvent, useState } from 'react';
import { Button, Group, NativeSelect, Popover, Stack } from '@mantine/core';
import type { ViewSettingsStatus } from '../../providers/boardViewSettings.ts';
import type { ViewSorting } from '../types/BoardViewData.ts';

const sortFields = { TITLE: 'Title', CREATED_AT: 'Date created' } satisfies Record<ViewSorting['sortBy'], string>;
const sortDirections = [
    { value: 'ASC', label: 'Ascending' },
    { value: 'DESC', label: 'Descending' },
];

type SortPopoverProps = {
    sorting: ViewSorting;
    settingsStatus: ViewSettingsStatus;
    onSave: (sorting: ViewSorting) => Promise<boolean>;
};

export default function SortPopover({
    sorting,
    settingsStatus,
    onSave,
}: Readonly<SortPopoverProps>) {
    const [draft, setDraft] = useState<ViewSorting | null>(null);
    const { pending, needsRefresh } = settingsStatus;
    const editingDisabled = pending || needsRefresh;
    const ascending = sorting.sortDirection === 'ASC';
    const field = sortFields[sorting.sortBy];
    const changed = draft && (draft.sortBy !== sorting.sortBy || draft.sortDirection !== sorting.sortDirection);

    async function applySorting(event: SubmitEvent) {
        event.preventDefault();
        if (draft && changed && !editingDisabled && await onSave(draft)) {
            setDraft(null);
        }
    }

    return (
        <Popover
            position="bottom-start"
            opened={draft !== null}
            onDismiss={() => setDraft(null)}
            trapFocus={!needsRefresh}
            closeOnClickOutside={!needsRefresh}
            returnFocus
            shadow="md"
            width={280}
        >
            <Popover.Target>
                <Button
                    variant="default"
                    aria-label={`Sort by: ${field}, ${ascending ? 'ascending' : 'descending'}`}
                    aria-disabled={editingDisabled && !draft}
                    data-disabled={editingDisabled && !draft || undefined}
                    onClick={() => {
                        if (draft) {
                            setDraft(null);
                        } else if (!editingDisabled) {
                            setDraft({ sortBy: sorting.sortBy, sortDirection: sorting.sortDirection });
                        }
                    }}
                >
                    Sort by: {field} {ascending ? '↑' : '↓'}
                </Button>
            </Popover.Target>
            <Popover.Dropdown maw="calc(100vw - 2rem)">
                {draft && (
                    <Stack component="form" gap="sm" onSubmit={applySorting}>
                        <NativeSelect
                            label="Sort field"
                            data={Object.entries(sortFields).map(([value, label]) => ({ value, label }))}
                            value={draft.sortBy}
                            disabled={editingDisabled}
                            onChange={event => setDraft({
                                ...draft,
                                sortBy: event.currentTarget.value as ViewSorting['sortBy'],
                            })}
                        />
                        <NativeSelect
                            label="Direction"
                            data={sortDirections}
                            value={draft.sortDirection}
                            disabled={editingDisabled}
                            onChange={event => setDraft({
                                ...draft,
                                sortDirection: event.currentTarget.value as ViewSorting['sortDirection'],
                            })}
                        />
                        <Group justify="flex-end">
                            <Button type="submit" size="compact-sm" disabled={editingDisabled || !changed}>
                                Apply
                            </Button>
                        </Group>
                    </Stack>
                )}
            </Popover.Dropdown>
        </Popover>
    );
}
