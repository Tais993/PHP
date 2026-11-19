import type { ComponentProps } from 'react';
import { Alert, Button, FocusTrap, Group, Popover, Stack, Text } from '@mantine/core';
import type { ViewSettingsStatus } from '../../providers/boardViewSettings.ts';
import FilterPopover from './FilterPopover.tsx';
import SortPopover from './SortPopover.tsx';

type BoardViewControlsProps = {
    filter: Omit<ComponentProps<typeof FilterPopover>, 'settingsStatus'>;
    sort: Omit<ComponentProps<typeof SortPopover>, 'settingsStatus'>;
    settingsStatus: ViewSettingsStatus;
    onReload: () => Promise<boolean>;
};

export default function BoardViewControls({ filter, sort, settingsStatus, onReload }: Readonly<BoardViewControlsProps>) {
    const { pending, needsRefresh, error } = settingsStatus;

    return (
        <Stack gap="xs" mb="md">
            {pending && (
                <Text size="sm" role="status">
                    {needsRefresh ? 'Reloading view settings…' : 'Saving view settings…'}
                </Text>
            )}
            {error && <Alert color="red" role="alert">{error}</Alert>}
            {needsRefresh && (
                <Group>
                    <Button variant="default" disabled={pending} onClick={() => void onReload()}>
                        Reload settings
                    </Button>
                </Group>
            )}
            <Group gap="xs" role="group" aria-label="View controls">
                <FilterPopover {...filter} settingsStatus={settingsStatus} />
                <SortPopover {...sort} settingsStatus={settingsStatus} />
                <Popover position="bottom-start" shadow="md" trapFocus returnFocus>
                    <Popover.Target>
                        <Button variant="default">Group by: Status</Button>
                    </Popover.Target>
                    <Popover.Dropdown aria-label="Grouping">
                        <FocusTrap.InitialFocus />
                        <Text size="sm">Status is the only available grouping.</Text>
                    </Popover.Dropdown>
                </Popover>
            </Group>
        </Stack>
    );
}
