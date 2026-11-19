import {createContext} from 'react';
import type {BoardViewData} from '../components/types/BoardViewData.ts';

export type NewBoardView = {
    name: string;
    type: 'BOARD' | 'TABLE' | 'ROADMAP';
    groupBy: 'STATUS';
    sortBy: 'TITLE' | 'CREATED_AT';
    sortDirection: 'ASC' | 'DESC';
};

type BoardViewsContextValue = {
    boardViews: BoardViewData[];
    selectedView: BoardViewData | null;
    loading: boolean;
    error: string | null;
    createView: (input: NewBoardView) => Promise<void>;
};

export const BoardViewsContext =
    createContext<BoardViewsContextValue | null>(null);
