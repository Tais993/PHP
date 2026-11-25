import {useContext} from 'react';
import {BoardsContext} from '../../providers/BoardsContext.ts';
import AddItemButton from './AddItemButton.tsx';

export default function AddBoardButton() {
    const context = useContext(BoardsContext);

    if (!context) {
        throw new Error('AddBoardButton must be inside BoardsProvider');
    }

    const {createBoard, loading, error: loadError} = context;

    return (
        <AddItemButton
            itemName="board"
            dataCy={{trigger: 'add-board', title: 'board-title', submit: 'create-board'}}
            disabled={loading || Boolean(loadError)}
            onCreate={createBoard}
        />
    );
}
