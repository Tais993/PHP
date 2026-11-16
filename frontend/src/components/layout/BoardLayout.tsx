import {useContext} from 'react'
import {Button, Divider, Group, ScrollArea} from "@mantine/core";
import {TaskList} from "../TaskList.tsx";
import {BoardsContext} from "../../providers/BoardsContext.ts";
import {Link} from "react-router";
import {TasksContext} from "../../providers/TasksContext.ts";
import AddTaskButton from "../buttons/AddTaskButton.tsx";
import {BoardViewsContext} from "../../providers/BoardViewsContext.ts";


export default function BoardLayout() {
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
    const {tasks} = tasksContext;
    const {
        boardViews,
        loading: boardViewsLoading, error: boardViewsError
    } = boardViewsContext;

    return (
        <>
            <header>
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

                            {boardViews.map(boardView => (
                                <Button
                                    key={boardView.id}
                                    variant="subtle"
                                    color="gray"
                                    component={Link}
                                    to={`/boards/${boardView.boardId}/views/${boardView.id}`}
                                >
                                    {boardView.name}
                                </Button>
                            ))}
                        </Group>

                        <Divider orientation="vertical" my="sm"/>

                        <Button
                            variant="subtle"
                            color="gray"
                            size="sm"
                        >
                            + Add view
                        </Button>
                    </Group>

                    <Divider/>

                </ScrollArea>
            </header>


            <main>
                <Group justify="space-between" mb="md">
                    <h1>Tasks</h1>
                    <AddTaskButton/>
                </Group>


                <Group align="flex-start" wrap="nowrap" gap="md"
                       style={{width: 'max-content'}}>
                    {
                        Object.values(tasks?.groups ?? {})
                            .sort((a, b) => a.position - b.position)
                            .map(group => (
                                <section key={group.id ?? "unassigned"}>
                                    <h2>{group.name}</h2>
                                    <TaskList tasks={group.items}/>
                                </section>
                            ))
                    }
                </Group>
            </main>

        </>
    )
}
