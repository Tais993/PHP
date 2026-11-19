import {apiUrl} from '../config.ts';
import type {BoardViewData, FilterRule, ViewSorting} from '../components/types/BoardViewData.ts';

export type ViewSettingsStatus = {
    pending: boolean;
    needsRefresh: boolean;
    error: string | null;
};

type ViewSettings = ViewSettingsStatus & {
    revision: number;
    savedView?: BoardViewData;
};

export const initialSettings: ViewSettings = {
    revision: 0,
    pending: false,
    needsRefresh: false,
    error: null,
};

// Saves and their outcomes outlive the board provider.
let settings = new Map<number, ViewSettings>();
let revision = 0;
const listeners = new Set<() => void>();

export const getViewSettings = () => settings;
export const getSettingsRevision = () => revision;

export function subscribeToViewSettings(listener: () => void) {
    listeners.add(listener);
    return () => {
        listeners.delete(listener);
    };
}

function updateSettings(viewId: number, next: ViewSettings) {
    settings = new Map(settings).set(viewId, next);
    listeners.forEach(listener => listener());
}

export async function updateViewSettings(
    viewId: number,
    ...change: [] | ['filter-rules', {filterRules: FilterRule[]}] | ['sorting', ViewSorting]
): Promise<boolean> {
    const current = settings.get(viewId) ?? initialSettings;
    const [kind, payload] = change;
    const reloading = kind === undefined;
    if (current.pending || reloading !== current.needsRefresh) {
        return false;
    }

    const path = `/views/${viewId}`;
    const signal = AbortSignal.timeout(reloading ? 5_000 : 15_000);
    const fallbackError = reloading
        ? 'Could not reload the saved settings. Try again.'
        : 'Could not save view settings. Try again.';
    updateSettings(viewId, {...current, pending: true, error: null});

    try {
        const response = reloading
            ? await fetch(apiUrl(path), {cache: 'no-store', signal})
            : await fetch(apiUrl(`${path}/${kind}`), {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload),
                signal,
            });
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(typeof problem?.detail === 'string' && problem.detail ? problem.detail : fallbackError);
        }

        const savedView: BoardViewData = await response.json();
        updateSettings(viewId, {...initialSettings, savedView, revision: ++revision});
        return true;

    } catch (exception) {
        const saveTimedOut = !reloading && signal.aborted;
        const errorMessage = exception instanceof Error ? exception.message : fallbackError;

        updateSettings(viewId, {
            ...current,
            needsRefresh: reloading || saveTimedOut,
            error: saveTimedOut
                ? 'The save timed out. Reload the saved settings before applying changes again.'
                : errorMessage,
        });
        return false;
    }
}