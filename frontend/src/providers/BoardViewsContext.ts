import {createContext} from 'react';
import type {BoardViewData, FilterRule, ViewSorting} from '../components/types/BoardViewData.ts';
import type {ViewMutationStatus, ViewSettingsStatus} from './boardViewSettings.ts';

export type NewBoardView = ViewSorting & {
    name: string;
    type: 'BOARD' | 'TABLE' | 'ROADMAP';
    groupBy: 'STATUS';
};

export type ViewDetails = {
    name: string;
    position: number;
};

type BoardViewsContextValue = {
    boardViews: BoardViewData[];
    selectedView: BoardViewData | null;
    loading: boolean;
    error: string | null;
    createView: (input: NewBoardView) => Promise<void>;
    settingsStatus: ViewSettingsStatus;
    settingsRevision: number;
    viewMutationStatus: ViewMutationStatus;
    saveFilterRules: (viewId: number, filterRules: FilterRule[]) => Promise<boolean>;
    saveSorting: (viewId: number, sorting: ViewSorting) => Promise<boolean>;
    reloadViewSettings: (viewId: number) => Promise<boolean>;
    saveViewDetails: (viewId: number, details: ViewDetails) => Promise<boolean>;
    duplicateView: (viewId: number, name: string) => Promise<boolean>;
    refreshBoardViews: () => Promise<boolean>;
};

export const BoardViewsContext =
    createContext<BoardViewsContextValue | null>(null);
