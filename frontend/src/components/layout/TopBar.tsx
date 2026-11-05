import {Button, Divider, Group} from "@mantine/core";
import {Link} from "react-router";

export default function TopBar() {
    return (
        <>
            <Group
                h={64}
                px="md"
                justify="space-between"
                wrap="nowrap"
            >
                <Group gap="md" wrap="nowrap">
                    {/*It shouldnt look like a button, it should kinda be the "logo" at the top left that also links to the home page*/}
                    <Button variant={"subtle"} color="gray" fw={700} visibleFrom="sm" component="a" href="/">
                        My homepage
                    </Button>

                    <Divider
                        orientation="vertical"
                        visibleFrom="sm"
                    />

                    <Button variant="subtle" color="gray" component={Link} to="/boards">
                        All boards
                    </Button>

                    <Button variant="subtle" color="gray" component={Link} to="/prototype">
                        Prototype
                    </Button>
                </Group>

                <Button variant="default" size="sm">
                    New board
                </Button>
            </Group>

            {/*<Divider/>*/}
        </>
    )
};