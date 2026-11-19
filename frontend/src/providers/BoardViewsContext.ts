import {createContext} from 'react';
import type {BoardViewData, FilterRule, ViewSorting} from '../components/types/BoardViewData.ts';
import type {ViewSettingsStatus} from './boardViewSettings.ts';

export type NewBoardView = ViewSorting & {
    name: string;
    type: 'BOARD' | 'TABLE' | 'ROADMAP';
    groupBy: 'STATUS';
};

type BoardViewsContextValue = {
    boardViews: BoardViewData[];
    selectedView: BoardViewData | null;
    loading: boolean;
    error: string | null;
    createView: (input: NewBoardView) => Promise<void>;
    settingsStatus: ViewSettingsStatus;
    settingsRevision: number;
    saveFilterRules: (viewId: number, filterRules: FilterRule[]) => Promise<boolean>;
    saveSorting: (viewId: number, sorting: ViewSorting) => Promise<boolean>;
    reloadViewSettings: (viewId: number) => Promise<boolean>;
};

export const BoardViewsContext =
    createContext<BoardViewsContextValue | null>(null);
