import type { FilterField, FilterOperator, FilterRule } from '../types/BoardViewData.ts';

export const fieldOptions: { value: FilterField; label: string }[] = [
    { value: 'STATUS', label: 'Status' },
    { value: 'TITLE', label: 'Title' },
    { value: 'DUE_DATE', label: 'Due date' },
];

export const operators: Record<FilterField, { value: FilterOperator; label: string }[]> = {
    STATUS: [
        { value: 'IN', label: 'Is any of' },
        { value: 'NOT_IN', label: 'Is none of' },
    ],
    TITLE: [
        { value: 'CONTAINS', label: 'Contains' },
        { value: 'EQUALS', label: 'Equals' },
    ],
    DUE_DATE: [
        { value: 'LESS_THAN_OR_EQUAL', label: 'On or before' },
        { value: 'GREATER_THAN_OR_EQUAL', label: 'On or after' },
        { value: 'IS_NULL', label: 'Is empty' },
        { value: 'IS_NOT_NULL', label: 'Is set' },
    ],
};

export function isNullCheck(operator: FilterOperator) {
    return operator === 'IS_NULL' || operator === 'IS_NOT_NULL';
}

function isLocalDateTime(value: string) {
    const parts = /^([+-]\d{4,9}|\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(?:\.\d{0,9})?)?$/i.exec(value);
    if (parts?.[0] !== value) {
        return false;
    }

    const [, yearText, monthText, dayText, hourText, minuteText, secondText = '0'] = parts;
    const [year, month, day, hour, minute, second] = [yearText, monthText, dayText, hourText, minuteText, secondText].map(Number);
    const validYear = (!yearText.startsWith('+') || yearText.length > 5)
        && (!yearText.startsWith('-') || year !== 0);
    const leapYear = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0);
    const days = [31, leapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

    return validYear && month >= 1 && month <= 12 && day >= 1 && day <= days[month - 1]
        && hour <= 23 && minute <= 59 && second <= 59;
}

export function validateRule(rule: FilterRule, statusIds: Set<string>) {
    if (rule.field === 'STATUS') {
        if (rule.values.length === 0) {
            return 'Select at least one status.';
        }
        return rule.values.every(value => statusIds.has(value)) ? null : 'Remove unavailable statuses.';
    }
    if (rule.field === 'TITLE') {
        return rule.values.length === 1 ? null : 'Enter a title value.';
    }
    if (isNullCheck(rule.operator)) {
        return null;
    }
    return isLocalDateTime(rule.values[0] ?? '') ? null : 'Enter a valid local date and time without a time zone.';
}
