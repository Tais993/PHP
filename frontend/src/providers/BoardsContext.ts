import {createContext} from 'react';
import type {BoardData} from '../components/types/BoardData.ts';

export type NewBoard = {
    title: string;
    description: string;
};

type BoardsContextValue = {
    boards: BoardData[];
    loading: boolean;
    error: string | null;
    createBoard: (input: NewBoard) => Promise<void>;
};

export const BoardsContext = createContext<BoardsContextValue | null>(null);
