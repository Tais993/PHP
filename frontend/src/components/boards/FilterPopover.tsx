import { type SubmitEvent, useRef, useState } from 'react';
import { Button, Group, Popover, Stack, Text } from '@mantine/core';
import type { ViewSettingsStatus } from '../../providers/boardViewSettings.ts';
import type { FilterRule } from '../types/BoardViewData.ts';
import type { TaskViewData } from '../types/TaskViewData.ts';
import FilterRuleEditor from './FilterRuleEditor.tsx';
import { validateRule } from './filterRules.ts';

type RuleDraft = { id: number; rule: FilterRule };

type FilterPopoverProps = {
    rules: FilterRule[];
    groups?: TaskViewData['groups'];
    settingsStatus: ViewSettingsStatus;
    onSave: (rules: FilterRule[]) => Promise<boolean>;
};

export default function FilterPopover({
    rules,
    groups,
    settingsStatus,
    onSave,
}: Readonly<FilterPopoverProps>) {
    const [draft, setDraft] = useState<RuleDraft[] | null>(null);
    const [showValidation, setShowValidation] = useState(false);
    const [statusDropdownOpen, setStatusDropdownOpen] = useState(false);
    const nextRuleId = useRef(0);
    const { pending, needsRefresh } = settingsStatus;
    const editingDisabled = !groups || pending || needsRefresh;
    const statusOptions = Object.values(groups ?? {})
        .filter(group => group.id !== null)
        .sort((a, b) => a.position - b.position)
        .map(group => ({ value: String(group.id), label: group.name }));
    const statusIds = new Set(statusOptions.map(option => option.value));
    const validationErrors = draft?.map(({ rule }) => validateRule(rule, statusIds));
    const actionsDisabled = editingDisabled || (!rules.length && !draft?.length);

    function toggleFilters() {
        if (draft) {
            setDraft(null);
            return;
        }
        if (editingDisabled) {
            return;
        }

        setShowValidation(false);
        setStatusDropdownOpen(false);
        setDraft(rules.map(rule => ({ id: nextRuleId.current++, rule })));
    }

    function updateRule(id: number, rule: FilterRule) {
        setDraft(current => current?.map(entry => entry.id === id ? { id, rule } : entry) ?? null);
    }

    function addRule() {
        const id = nextRuleId.current++;
        setDraft(current => current && [...current, { id, rule: { field: 'STATUS', operator: 'IN', values: [] } }]);
    }

    function removeRule(id: number) {
        setDraft(current => current?.filter(entry => entry.id !== id) ?? null);
    }

    async function save(nextRules: FilterRule[]) {
        if (await onSave(nextRules)) {
            setDraft(null);
        }
    }

    function applyFilters(event: SubmitEvent) {
        event.preventDefault();
        if (!draft || actionsDisabled) {
            return;
        }
        setShowValidation(true);
        if (!validationErrors?.some(Boolean)) {
            void save(draft.map(({ rule }) => rule));
        }
    }

    return (
        <Popover
            position="bottom-start"
            opened={draft !== null}
            onDismiss={() => setDraft(null)}
            closeOnEscape={!statusDropdownOpen}
            trapFocus={!needsRefresh}
            closeOnClickOutside={!needsRefresh}
            returnFocus
            shadow="md"
            width={520}
        >
            <Popover.Target>
                <Button
                    variant="default"
                    aria-disabled={editingDisabled && !draft}
                    data-disabled={editingDisabled && !draft || undefined}
                    onClick={toggleFilters}
                >
                    {rules.length ? `Filter (${rules.length})` : 'Filter'}
                </Button>
            </Popover.Target>
            <Popover.Dropdown aria-label="Filter tasks" maw="calc(100vw - 2rem)">
                <Stack
                    component="form"
                    gap="sm"
                    mah="min(65dvh, 36rem)"
                    style={{ overflowY: 'auto' }}
                    px={2}
                    py={2}
                    onSubmit={applyFilters}
                >
                    <Text size="sm" c="dimmed">Tasks must match every rule.</Text>
                    {draft?.map(({ id, rule }, index) => (
                        <FilterRuleEditor
                            key={id}
                            index={index}
                            rule={rule}
                            disabled={editingDisabled}
                            statusOptions={statusOptions}
                            error={showValidation ? validationErrors?.[index] : null}
                            onChange={nextRule => updateRule(id, nextRule)}
                            onRemove={() => removeRule(id)}
                            onStatusPickerChange={setStatusDropdownOpen}
                        />
                    ))}
                    <Group justify="space-between" gap="xs">
                        <Button
                            variant="default"
                            size="compact-sm"
                            disabled={editingDisabled}
                            onClick={addRule}
                        >
                            Add rule
                        </Button>
                        <Group gap="xs">
                            <Button
                                variant="subtle"
                                size="compact-sm"
                                disabled={actionsDisabled}
                                onClick={() => void save([])}
                            >
                                Clear filters
                            </Button>
                            <Button
                                type="submit"
                                size="compact-sm"
                                disabled={actionsDisabled}
                            >
                                Apply
                            </Button>
                        </Group>
                    </Group>
                </Stack>
            </Popover.Dropdown>
        </Popover>
    );
}
