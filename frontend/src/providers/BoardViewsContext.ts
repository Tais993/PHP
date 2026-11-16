import {createContext} from 'react';
import type {BoardViewData} from '../components/types/BoardViewData.ts';

type BoardViewsContextValue = {
    boardViews: BoardViewData[];
    selectedViewId: number | null;
    loading: boolean;
    error: string | null;
};

export const BoardViewsContext = createContext<BoardViewsContextValue | null>(null);
