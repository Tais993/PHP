import { useEffect, useState } from 'react'
import { Task } from './components/Task'

export type TaskData = {
    id: number
    title: string
    description: string
    status: string
}

export default function App() {
    const [task, setTask] = useState<TaskData | null>(null)

    useEffect(() => {
        fetch('/task?id=2')
            .then(response => {
                if (!response.ok) throw new Error('Task not found')
                return response.json()
            })
            .then(setTask)
            .catch(console.error)
    }, [])

    if (!task) return <p>Loading task...</p>

    return <Task task={task} />
}