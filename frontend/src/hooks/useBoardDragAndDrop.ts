import type { DragEvent } from 'react';
import { useMemo, useState } from 'react';
import type { TaskData } from '../components/types/TaskData.ts';

type GroupData = {
    id: number | null;
    name: string;
    position: number;
    items: TaskData[];
};

type UseBoardDragAndDropOptions = {
    groupsRecord?: Record<string, GroupData>;
    moveTask: (taskId: number, statusId: number | null) => Promise<void>;
};

export function useBoardDragAndDrop({ groupsRecord, moveTask }: UseBoardDragAndDropOptions) {
    const [draggedTaskId, setDraggedTaskId] = useState<number | null>(null);
    const [hoveredStatusId, setHoveredStatusId] = useState<number | null | undefined>(undefined);

    const groups = useMemo(
        () => Object.values(groupsRecord ?? {}).sort((a, b) => a.position - b.position),
        [groupsRecord]
    );

    const displayedGroups = useMemo(() => {
        if (draggedTaskId === null || hoveredStatusId === undefined) {
            return groups;
        }

        let draggedTask: TaskData | null = null;
        for (const group of groups) {
            const found = group.items.find(t => t.id === draggedTaskId);
            if (found) {
                draggedTask = found;
                break;
            }
        }

        if (!draggedTask) {
            return groups;
        }

        return groups.map(group => {
            const isSourceGroup = group.items.some(t => t.id === draggedTaskId);
            const isTargetGroup = group.id === hoveredStatusId;

            if (isSourceGroup && isTargetGroup) {
                return group;
            }
            if (isSourceGroup) {
                return { ...group, items: group.items.filter(t => t.id !== draggedTaskId) };
            }
            if (isTargetGroup) {
                return { ...group, items: [...group.items, draggedTask] };
            }
            return group;
        });
    }, [groups, draggedTaskId, hoveredStatusId]);

    const handleDragEnter = (statusId: number | null) => {
        if (draggedTaskId !== null) {
            setHoveredStatusId(statusId);
        }
    };

    const handleDragOver = (event: DragEvent<HTMLElement>, statusId: number | null) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
        if (draggedTaskId !== null && hoveredStatusId !== statusId) {
            setHoveredStatusId(statusId);
        }
    };

    const handleDragLeave = (event: DragEvent<HTMLElement>) => {
        if (!event.currentTarget.contains(event.relatedTarget as Node)) {
            if (draggedTaskId !== null) {
                setHoveredStatusId(undefined);
            }
        }
    };

    const handleTaskDragStart = (taskId: number) => {
        setDraggedTaskId(taskId);
    };

    const handleTaskDragEnd = () => {
        setDraggedTaskId(null);
        setHoveredStatusId(undefined);
    };

    const handleDrop = async (event: DragEvent<HTMLElement>, statusId: number | null) => {
        event.preventDefault();
        setDraggedTaskId(null);
        setHoveredStatusId(undefined);

        const taskIdString = event.dataTransfer.getData('text/plain');
        if (!taskIdString) {
            return;
        }

        const taskId = Number(taskIdString);
        if (Number.isNaN(taskId)) {
            return;
        }

        try {
            await moveTask(taskId, statusId);
        } catch (error) {
            console.error('Failed to move task:', error);
        }
    };

    return {
        draggedTaskId,
        displayedGroups,
        handleDragEnter,
        handleDragOver,
        handleDragLeave,
        handleDrop,
        handleTaskDragStart,
        handleTaskDragEnd,
    };
}
