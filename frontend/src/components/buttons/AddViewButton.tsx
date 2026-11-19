import {useContext} from 'react';
import {NativeSelect} from '@mantine/core';
import {BoardViewsContext, type NewBoardView} from '../../providers/BoardViewsContext.ts';
import AddItemButton from './AddItemButton.tsx';

export default function AddViewButton() {
    const context = useContext(BoardViewsContext);

    if (!context) {
        throw new Error('AddViewButton must be inside BoardViewsProvider');
    }

    const {createView, loading, error} = context;

    return (
        <AddItemButton
            itemName="view"
            buttonProps={{variant: 'subtle', color: 'gray', size: 'sm'}}
            titleLabel="Name"
            titlePlaceholder="e.g. Sprint board"
            showDescription={false}
            fields={(
                <>
                    <NativeSelect
                        label="View type"
                        name="type"
                        defaultValue="BOARD"
                        data={[
                            {value: 'BOARD', label: 'Board'},
                            {value: 'TABLE', label: 'Table', disabled: true},
                            {value: 'ROADMAP', label: 'Roadmap', disabled: true},
                        ]}
                    />
                    <NativeSelect
                        label="Group by"
                        name="groupBy"
                        defaultValue="STATUS"
                        data={[{value: 'STATUS', label: 'Status'}]}
                    />
                    <NativeSelect
                        label="Sort by"
                        name="sortBy"
                        defaultValue="CREATED_AT"
                        data={[
                            {value: 'TITLE', label: 'Title'},
                            {value: 'CREATED_AT', label: 'Date created'},
                        ]}
                    />
                    <NativeSelect
                        label="Sort direction"
                        name="sortDirection"
                        defaultValue="DESC"
                        data={[
                            {value: 'ASC', label: 'Ascending'},
                            {value: 'DESC', label: 'Descending'},
                        ]}
                    />
                </>
            )}
            disabled={loading || Boolean(error)}
            onCreate={({title}, fields) => createView({
                name: title,
                type: fields.get('type') as NewBoardView['type'],
                groupBy: fields.get('groupBy') as NewBoardView['groupBy'],
                sortBy: fields.get('sortBy') as NewBoardView['sortBy'],
                sortDirection: fields.get('sortDirection') as NewBoardView['sortDirection'],
            })}
        />
    );
}
