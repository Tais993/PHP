import {useContext, useEffect, useState} from 'react'
import {Button, Divider, Group, ScrollArea} from "@mantine/core";
import type {TaskData} from "../types/TaskData.ts";
import {TaskList} from "../TaskList.tsx";
import {BoardsContext} from "../../BoardsProvider.tsx";
import {Link, useParams} from "react-router";


export default function BoardLayout() {
    const [tasks, setTasks] = useState<TaskData[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        fetch('/api/tasks')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Could not load tasks')
                }
                return response.json()
            })
            .then((data: TaskData[]) => setTasks(data))
            .catch((exception) => {
                setError('Could not load tasks')
                console.log(exception)
            })
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <p>Loading tasks...</p>
    if (error) return <p>{error}</p>
    if (tasks.length === 0) return <p>No tasks yet.</p>


    const {boardId} = useParams();


    const context = useContext(BoardsContext);

    if (!context) {
        throw new Error('TopBar must be inside BoardsProvider');
    }

    const {boards, loading: boardsLoading, error: boardsError} = context;


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