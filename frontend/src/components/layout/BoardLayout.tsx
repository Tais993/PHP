import {useEffect, useState} from 'react'
import {Button, Divider, Group, ScrollArea, Select} from "@mantine/core";
import type {TaskData} from "../types/TaskData.ts";
import {TaskList} from "../TaskList.tsx";


export default function BoardLayout() {
    const [tasks, setTasks] = useState<TaskData[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        fetch('/tasks')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Could not load tasks')
                }
                return response.json()
            })
            .then((data: TaskData[]) => setTasks(data))
            .catch(() => setError('Could not load tasks'))
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <p>Loading tasks...</p>
    if (error) return <p>{error}</p>
    if (tasks.length === 0) return <p>No tasks yet.</p>

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
                        <Select
                            aria-label="Current board"
                            data={[
                                'Personal tasks (placeholder)',
                                'School (placeholder)',
                                'Photography (placeholder)',
                            ]}
                            value="Personal tasks"
                            allowDeselect={false}
                            w={220}
                            size="sm"
                            mr="xs"
                        />

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

                    <Divider />

                </ScrollArea>
            </header>


            <main>
                <h1>Tasks</h1>
                <TaskList tasks={tasks} />
            </main>

        </>
    )
}