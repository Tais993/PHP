import {useContext} from 'react'
import {Button, Divider, Group, ScrollArea} from "@mantine/core";
import {TaskList} from "../TaskList.tsx";
import {BoardsContext} from "../../providers/BoardsProvider.tsx";
import {Link} from "react-router";
import {TasksContext} from "../../providers/TasksProvider.tsx";


export default function BoardLayout() {
    const boardsContext = useContext(BoardsContext);
    const tasksContext = useContext(TasksContext);

    if (!boardsContext) {
        throw new Error('TopBar must be inside BoardsProvider');
    }

    if (!tasksContext) {
        throw new Error('TopBar must be inside TasksProvider');
    }

    const {boards, loading: boardsLoading, error: boardsError} = boardsContext;
    const {tasks} = tasksContext;


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


                        <Button
                            variant="subtle"
                            color="gray"
                            size="sm"
                        >
                            All tasks (placeholder)
                        </Button>

                        <Button
                            variant="light"
                            size="sm"
                            aria-current="page"
                        >
                            Current sprint (placeholder)
                        </Button>

                        <Button
                            variant="subtle"
                            color="gray"
                            size="sm"
                        >
                            Upcoming deadlines (placeholder)
                        </Button>

                        <Button
                            variant="subtle"
                            color="gray"
                            size="sm"
                        >
                            Project planning (placeholder)
                        </Button>

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
                <h1>Tasks</h1>
                <TaskList tasks={tasks}/>
            </main>

        </>
    )
}