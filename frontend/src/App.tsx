import {useEffect, useState} from 'react'
import {Task} from './components/Task'

export type TaskData = {
    id: number
    title: string
    description: string
    status: string
    dueDate: string
}

export default function App() {
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
        <main>
            <h1>Tasks</h1>
            {tasks.map(task => (
                <Task key={task.id} task={task}/>
            ))}
        </main>
    )
}