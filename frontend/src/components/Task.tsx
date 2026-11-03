import { Badge, Button, Group, Paper, Stack, Text } from '@mantine/core'
import type { TaskData } from '../App'

type TaskProps = {
    task: TaskData
}

export function Task({ task }: TaskProps) {
    return (
        <Paper
            withBorder
            radius="sm"
            p="md"
            w="100%"
            maw={260}
        >
            <Stack gap="sm">
                <Group justify="space-between" align="flex-start" wrap="nowrap">
                    <Group
                        gap="xs"
                        wrap="nowrap"
                        style={{ minWidth: 0 }}
                    >
                        <Text size="xs" c="dimmed">
                            #{task.id}
                        </Text>

                        <Text
                            fw={600}
                            style={{ overflowWrap: 'anywhere', minWidth: 0 }}
                        >
                            {task.title}
                        </Text>
                    </Group>

                    <Badge
                        variant="light"
                        radius="sm"
                        style={{ flexShrink: 0 }}
                    >
                        {task.status}
                    </Badge>
                </Group>

                <Text
                    size="sm"
                    c="dimmed"
                    lineClamp={2}
                    style={{ overflowWrap: 'anywhere' }}
                >
                    {task.description || 'No description provided.'}
                </Text>

                <Group justify="space-between" mt="xs">
                    <Text size="xs" fw={600}>
                        Due: {task.dueDate || 'Not set'}
                    </Text>

                    <Button variant="default" size="xs">
                        Open task
                    </Button>
                </Group>
            </Stack>
        </Paper>
    )
}