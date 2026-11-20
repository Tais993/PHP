import {Button, Divider, Group, ScrollArea} from "@mantine/core";
import {useContext} from "react";
import {BoardsContext} from "../../providers/BoardsContext.ts";
import {BoardViewsContext} from "../../providers/BoardViewsContext.ts";
import {TasksContext} from "../../providers/TasksContext.ts";
import {Link} from "react-router";
import AddViewButton from "../buttons/AddViewButton.tsx";
import ViewOptionsPopover from "./ViewOptionsPopover.tsx";

export default function BoardTopBar() {
    const boardsContext = useContext(BoardsContext);
    const boardViewsContext = useContext(BoardViewsContext);
    const tasksContext = useContext(TasksContext);

    if (!boardsContext) {
        throw new Error('TopBar must be inside BoardsProvider');
    }

    if (!tasksContext) {
        throw new Error('TopBar must be inside TasksProvider');
    }

    if (!boardViewsContext) {
        throw new Error('TopBar must be inside BoardViewsProvider');
    }

    const {boards, loading: boardsLoading, error: boardsError} = boardsContext;
    
    const {
        boardViews,
        loading: boardViewsLoading,
        error: boardViewsError,
        saveViewDetails,
        duplicateView,
        viewMutationStatus,
        refreshBoardViews,
    } = boardViewsContext;



    return <>

        <ScrollArea type="auto">
            <Group
                h={55}
                px="md"
                gap="xs"
                wrap="nowrap"
                style={{width: 'max-content'}}
            >
                <Group>
                    {boardsLoading && <span>Loading boards...</span>}
                    {boardsError && <span>{boardsError}</span>}

                    {boards.map(board => (
                        <Button
                            key={board.id}
                            variant="subtle"
                            color="gray"
                            component={Link}
                            to={`/boards/${board.id}`}
                        >
                            {board.title}
                        </Button>
                    ))}
                </Group>

                <Divider orientation="vertical" my="sm"/>

                <Group>
                    {boardViewsLoading && <span>Loading boards...</span>}
                    {boardViewsError && <span>{boardViewsError}</span>}
                    {viewMutationStatus.error && <span role="alert">{viewMutationStatus.error}</span>}
                    {viewMutationStatus.needsRefresh && (
                        <Button
                            variant="default"
                            loading={viewMutationStatus.pending}
                            disabled={viewMutationStatus.pending}
                            onClick={() => void refreshBoardViews()}
                        >
                            Refresh view list
                        </Button>
                    )}

                    {boardViews.map((boardView, index) => (
                        <Group key={boardView.id} gap={0} wrap="nowrap">
                            <Button
                                variant="subtle"
                                color="gray"
                                component={Link}
                                to={`/boards/${boardView.boardId}/views/${boardView.id}`}
                            >
                                {boardView.name}
                            </Button>
                            <ViewOptionsPopover
                                view={boardView}
                                position={index + 1}
                                viewCount={boardViews.length}
                                mutationStatus={viewMutationStatus}
                                onSave={details => saveViewDetails(boardView.id, details)}
                                onDuplicate={name => duplicateView(boardView.id, name)}
                                onRefresh={refreshBoardViews}
                            />
                        </Group>
                    ))}
                </Group>

                <Divider orientation="vertical" my="sm"/>

                <AddViewButton/>
            </Group>

            <Divider/>

        </ScrollArea>
    </>
}
