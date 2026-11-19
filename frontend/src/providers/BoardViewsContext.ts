import {createContext} from 'react';
import type {BoardViewData} from '../components/types/BoardViewData.ts';

type BoardViewsContextValue = {
    boardViews: BoardViewData[];
    selectedView: BoardViewData | null;
    loading: boolean;
    error: string | null;
};

export const BoardViewsContext =
    createContext<BoardViewsContextValue | null>(null);