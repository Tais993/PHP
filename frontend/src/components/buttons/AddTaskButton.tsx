import {type ComponentProps, useContext} from 'react';
import type {ButtonProps} from '@mantine/core';
import {TasksContext} from '../../providers/TasksContext.ts';
import AddItemButton from './AddItemButton.tsx';

type AddTaskButtonProps = Pick<ButtonProps, 'fullWidth' | 'mt'> & {
    statusId?: number | null;
    label?: string;
    dataCy?: ComponentProps<typeof AddItemButton>['dataCy'];
};

export default function AddTaskButton({
    statusId = null,
    label,
    dataCy,
    fullWidth,
    mt,
}: Readonly<AddTaskButtonProps>) {
    const context = useContext(TasksContext);

    if (!context) {
        throw new Error('AddTaskButton must be inside TasksProvider');
    }

    const {createTask, loading, error: loadError} = context;

    return (
        <AddItemButton
            itemName="task"
            label={label}
            dataCy={dataCy}
            buttonProps={{variant: 'default', fullWidth, mt}}
            disabled={loading || Boolean(loadError)}
            onCreate={(input) => createTask({...input, statusId})}
        />
    );
}
