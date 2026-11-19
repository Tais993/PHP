import { Button, Fieldset, Group, MultiSelect, NativeSelect, Stack, TextInput } from '@mantine/core';
import type { FilterField, FilterOperator, FilterRule } from '../types/BoardViewData.ts';
import { fieldOptions, isNullCheck, operators } from './filterRules.ts';

type FilterRuleEditorProps = {
    index: number;
    rule: FilterRule;
    disabled: boolean;
    statusOptions: { value: string; label: string }[];
    error?: string | null;
    onChange: (rule: FilterRule) => void;
    onRemove: () => void;
    onStatusPickerChange: (opened: boolean) => void;
};

export default function FilterRuleEditor({
    index,
    rule,
    disabled,
    statusOptions,
    error,
    onChange,
    onRemove,
    onStatusPickerChange,
}: Readonly<FilterRuleEditorProps>) {
    return (
        <Fieldset legend={`Rule ${index + 1}`} disabled={disabled} p="xs">
            <Stack gap="xs">
                <Group grow align="flex-start" gap="xs">
                    <NativeSelect
                        label="Field"
                        data={fieldOptions}
                        value={rule.field}
                        onChange={event => {
                            const field = event.currentTarget.value as FilterField;
                            onChange({ field, operator: operators[field][0].value, values: [] });
                        }}
                    />
                    <NativeSelect
                        label="Operator"
                        data={operators[rule.field]}
                        value={rule.operator}
                        onChange={event => {
                            const operator = event.currentTarget.value as FilterOperator;
                            onChange({
                                ...rule,
                                operator,
                                values: isNullCheck(operator) ? [] : rule.values,
                            });
                        }}
                    />
                </Group>
                {rule.field === 'STATUS' && (
                    <MultiSelect
                        label="Statuses"
                        placeholder="Select statuses"
                        searchable
                        nothingFoundMessage="No statuses found"
                        comboboxProps={{ withinPortal: false, floatingStrategy: 'fixed' }}
                        disabled={disabled}
                        data={statusOptions}
                        value={rule.values}
                        onDropdownOpen={() => onStatusPickerChange(true)}
                        onDropdownClose={() => onStatusPickerChange(false)}
                        onChange={values => onChange({ ...rule, values })}
                        error={error}
                    />
                )}
                {rule.field !== 'STATUS' && !isNullCheck(rule.operator) && (
                    <TextInput
                        label={rule.field === 'DUE_DATE' ? 'Local date and time' : 'Title value'}
                        placeholder={rule.field === 'DUE_DATE' ? '2026-11-19T17:00' : undefined}
                        description={rule.field === 'DUE_DATE' ? 'Use YYYY-MM-DDTHH:mm' : undefined}
                        value={rule.values[0] ?? ''}
                        onChange={event => {
                            const value = event.currentTarget.value;
                            onChange({ ...rule, values: value ? [value] : [] });
                        }}
                        error={error}
                    />
                )}
                <Button
                    variant="subtle"
                    color="red"
                    size="compact-sm"
                    aria-label={`Remove rule ${index + 1}`}
                    style={{ alignSelf: 'flex-end' }}
                    onClick={onRemove}
                >
                    Remove
                </Button>
            </Stack>
        </Fieldset>
    );
}
