import assert from 'node:assert/strict';
import {describe, test} from 'node:test';
import {validateRule} from '../src/components/boards/filterRules.ts';

const statusIds = new Set(['1', '2']);

describe('status filters', () => {
    test('accepts a selection of available statuses', () => {
        assert.equal(validateRule({field: 'STATUS', operator: 'IN', values: ['1', '2']}, statusIds), null);
    });

    test('requires at least one status', () => {
        assert.equal(validateRule({field: 'STATUS', operator: 'IN', values: []}, statusIds),
            'Select at least one status.');
    });

    test('rejects selections that include an unavailable status', () => {
        assert.equal(validateRule({field: 'STATUS', operator: 'NOT_IN', values: ['1', '3']}, statusIds),
            'Remove unavailable statuses.');
    });
});

describe('title filters', () => {
    test('accepts one title value', () => {
        assert.equal(validateRule({field: 'TITLE', operator: 'CONTAINS', values: ['Fix login']}, statusIds), null);
    });

    test('requires a title value', () => {
        assert.equal(validateRule({field: 'TITLE', operator: 'EQUALS', values: []}, statusIds),
            'Enter a title value.');
    });

    test('rejects multiple title values', () => {
        assert.equal(validateRule({field: 'TITLE', operator: 'EQUALS', values: ['Fix login', 'Write docs']}, statusIds),
            'Enter a title value.');
    });
});

describe('due date filters', () => {
    for (const operator of ['IS_NULL', 'IS_NOT_NULL'] as const) {
        test(`${operator} accepts an empty value list`, () => {
            assert.equal(validateRule({field: 'DUE_DATE', operator, values: []}, statusIds), null);
        });
    }

    test('accepts a leap day with hours and minutes', () => {
        assert.equal(validateRule({
            field: 'DUE_DATE', operator: 'LESS_THAN_OR_EQUAL', values: ['2024-02-29T08:30'],
        }, statusIds), null);
    });

    test('accepts a local time with fractional seconds', () => {
        assert.equal(validateRule({
            field: 'DUE_DATE', operator: 'GREATER_THAN_OR_EQUAL', values: ['2025-01-01T23:59:59.123'],
        }, statusIds), null);
    });

    test('requires a value for date comparisons', () => {
        assert.equal(validateRule({field: 'DUE_DATE', operator: 'LESS_THAN_OR_EQUAL', values: []}, statusIds),
            'Enter a valid local date and time without a time zone.');
    });

    for (const value of [
        '2023-02-29T08:30',
        '2024-04-31T08:30',
        '2024-13-01T08:30',
        '2024-02-29T24:00',
        '2024-02-29T08:60',
        '2024-02-29T08:30:60',
        '2024-02-29T08:30Z',
        '2024-02-29T08:30+01:00',
    ]) {
        test(`rejects ${value}`, () => {
            assert.equal(validateRule({field: 'DUE_DATE', operator: 'LESS_THAN_OR_EQUAL', values: [value]}, statusIds),
                'Enter a valid local date and time without a time zone.');
        });
    }
});
