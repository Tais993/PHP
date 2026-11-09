import {
    AppShell,
    Badge,
    Box,
    Button,
    Divider,
    Group,
    Paper,
    ScrollArea,
    Select,
    Stack,
    Text,
    Title,
} from '@mantine/core'

type TaskData = {
    id: number
    title: string
    description: string
    status: string
    dueDate: string
}

const statuses = [
    'To do',
    'In progress',
    'Done',
    'Custom status',
]

const tasks: TaskData[] = [
    {
        id: 101,
        title: 'Task title',
        description: 'Short description preview…',
        status: 'To do',
        dueDate: 'DD MMM',
    },
    {
        id: 102,
        title: 'Task title',
        description: 'Short description preview…',
        status: 'To do',
        dueDate: 'DD MMM',
    },
    {
        id: 103,
        title: 'Task title',
        description: 'Short description preview…',
        status: 'To do',
        dueDate: 'DD MMM',
    },
    {
        id: 104,
        title: 'Task title',
        description: 'Short description preview…',
        status: 'In progress',
        dueDate: 'DD MMM',
    },
    {
        id: 105,
        title: 'Task title',
        description: 'Short description preview…',
        status: 'In progress',
        dueDate: 'DD MMM',
    },
    {
        id: 107,
        title: 'Task title',
        description: 'Short description preview…',
        status: 'Done',
        dueDate: 'DD MMM',
    },
]

export default function Prototype() {
    return (
        <AppShell
            header={{ height: 120 }}
            footer={{ height: 44 }}
            padding="lg"
        >
            <AppShell.Header>
                <Group
                    h={64}
                    px="md"
                    justify="space-between"
                    wrap="nowrap"
                >
                    <Group gap="md" wrap="nowrap">
                        <Text fw={700} visibleFrom="sm">
                            My homepage
                        </Text>

                        <Divider
                            orientation="vertical"
                            visibleFrom="sm"
                        />

                        <Button variant="subtle" color="gray">
                            All boards
                        </Button>
                    </Group>

                    <Button variant="default" size="sm">
                        New board
                    </Button>
                </Group>

                <Divider />

                <ScrollArea type="auto">
                    <Group
                        h={55}
                        px="md"
                        gap="xs"
                        wrap="nowrap"
                        style={{ width: 'max-content' }}
                    >
                        <Select
                            aria-label="Current board"
                            data={[
                                'Personal tasks',
                                'School',
                                'Photography',
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
                            All tasks
                        </Button>

                        <Button
                            variant="light"
                            size="sm"
                            aria-current="page"
                        >
                            Current sprint
                        </Button>

                        <Button
                            variant="subtle"
                            color="gray"
                            size="sm"
                        >
                            Upcoming deadlines
                        </Button>

                        <Button
                            variant="subtle"
                            color="gray"
                            size="sm"
                        >
                            Project planning
                        </Button>

                        <Divider orientation="vertical" my="sm" />

                        <Button
                            variant="subtle"
                            color="gray"
                            size="sm"
                        >
                            + Add view
                        </Button>
                    </Group>
                </ScrollArea>
            </AppShell.Header>

            <AppShell.Main>
                <Group justify="space-between" mb="lg">
                    <div>
                        <Title order={1} size="h2">
                            Current sprint
                        </Title>

                        <Text size="sm" c="dimmed" mt={4}>
                            {tasks.length} tasks in Personal tasks
                        </Text>
                    </div>

                    <Button variant="default" size="sm">
                        New task
                    </Button>
                </Group>

                <Group gap="xs" mb="lg">
                    <Button variant="default" size="sm">
                        Filter
                    </Button>

                    <Button variant="default" size="sm">
                        Sort
                    </Button>

                    <Button variant="default" size="sm">
                        Group by: Status
                    </Button>
                </Group>

                <ScrollArea type="auto" offsetScrollbars>
                    <Box
                        style={{
                            display: 'flex',
                            alignItems: 'stretch',
                            gap: 16,
                            minWidth: 1260,
                        }}
                    >
                        {statuses.map(status => {
                            const columnTasks = tasks.filter(
                                task => task.status === status,
                            )

                            return (
                                <Paper
                                    key={status}
                                    withBorder
                                    radius="sm"
                                    bg="gray.0"
                                    style={{
                                        flex: '1 0 300px',
                                        minWidth: 300,
                                        minHeight: 610,
                                        display: 'flex',
                                        flexDirection: 'column',
                                    }}
                                >
                                    <Group
                                        justify="space-between"
                                        p="sm"
                                    >
                                        <Text fw={600}>
                                            {status}
                                        </Text>

                                        <Text size="sm" c="dimmed">
                                            {columnTasks.length}
                                        </Text>
                                    </Group>

                                    <Divider />

                                    <Stack
                                        gap="sm"
                                        p="xs"
                                        style={{ flex: 1 }}
                                    >
                                        {columnTasks.length === 0 ? (
                                            <Paper
                                                withBorder
                                                radius="sm"
                                                p="lg"
                                            >
                                                <Text
                                                    size="sm"
                                                    c="dimmed"
                                                    ta="center"
                                                >
                                                    No tasks with this status
                                                </Text>
                                            </Paper>
                                        ) : (
                                            columnTasks.map(task => (
                                                <Paper
                                                    key={task.id}
                                                    withBorder
                                                    radius="sm"
                                                    p="sm"
                                                >
                                                    <Stack gap="sm">
                                                        <Group
                                                            justify="space-between"
                                                            align="flex-start"
                                                            wrap="nowrap"
                                                        >
                                                            <Group
                                                                gap="xs"
                                                                wrap="nowrap"
                                                                style={{
                                                                    minWidth: 0,
                                                                }}
                                                            >
                                                                <Text
                                                                    size="xs"
                                                                    c="dimmed"
                                                                    style={{
                                                                        flexShrink: 0,
                                                                    }}
                                                                >
                                                                    #{task.id}
                                                                </Text>

                                                                <Text
                                                                    size="sm"
                                                                    fw={600}
                                                                    style={{
                                                                        minWidth: 0,
                                                                        overflowWrap:
                                                                            'anywhere',
                                                                    }}
                                                                >
                                                                    {task.title}
                                                                </Text>
                                                            </Group>

                                                            <Badge
                                                                variant="light"
                                                                color="gray"
                                                                radius="sm"
                                                                size="sm"
                                                                style={{
                                                                    flexShrink: 0,
                                                                }}
                                                            >
                                                                {task.status}
                                                            </Badge>
                                                        </Group>

                                                        <Text
                                                            size="sm"
                                                            c="dimmed"
                                                            lineClamp={2}
                                                            style={{
                                                                overflowWrap:
                                                                    'anywhere',
                                                            }}
                                                        >
                                                            {task.description}
                                                        </Text>

                                                        <Group
                                                            justify="space-between"
                                                            mt="xs"
                                                        >
                                                            <Text
                                                                size="xs"
                                                                fw={600}
                                                            >
                                                                Due: {task.dueDate}
                                                            </Text>

                                                            <Button
                                                                variant="default"
                                                                size="xs"
                                                            >
                                                                Open task
                                                            </Button>
                                                        </Group>
                                                    </Stack>
                                                </Paper>
                                            ))
                                        )}

                                        <Button
                                            variant="default"
                                            fullWidth
                                            mt="auto"
                                        >
                                            + Add task
                                        </Button>
                                    </Stack>
                                </Paper>
                            )
                        })}
                    </Box>
                </ScrollArea>
            </AppShell.Main>

            <AppShell.Footer>
                <Group
                    h="100%"
                    px="md"
                    justify="space-between"
                >
                    <Text size="xs" c="dimmed">
                        Personal homepage
                    </Text>

                    <Text size="xs" c="dimmed">
                        Built by Tijs
                    </Text>
                </Group>
            </AppShell.Footer>
        </AppShell>
    )
}