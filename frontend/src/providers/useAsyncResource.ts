import {useCallback, useEffect, useState} from 'react';

type Resource<T> = {
    key: string | null;
    load: (signal: AbortSignal) => Promise<T>;
    errorMessage: string;
    data: T | null;
    error: string | null;
    loading: boolean;
};

export function useAsyncResource<T>(
    key: string | null,
    load: (signal: AbortSignal) => Promise<T>,
    errorMessage: string,
) {
    const [resource, setResource] = useState<Resource<T>>({
        key, load, errorMessage, data: null, error: null, loading: key !== null,
    });

    if (resource.key !== key || resource.load !== load || resource.errorMessage !== errorMessage) {
        setResource({key, load, errorMessage, data: null, error: null, loading: key !== null});
    }

    useEffect(() => {
        if (key === null) return;

        const controller = new AbortController();
        const updateResource = (update: (current: Resource<T>) => Resource<T>) => {
            if (controller.signal.aborted) return;
            setResource(current => !controller.signal.aborted && current.key === key && current.load === load &&
                current.errorMessage === errorMessage ? update(current) : current);
        };

        async function run() {
            try {
                const data = await load(controller.signal);
                updateResource(current => ({...current, data, error: null}));
            } catch (exception) {
                if (!controller.signal.aborted) {
                    updateResource(current => ({...current, data: null, error: errorMessage}));
                    console.error(exception);
                }
            } finally {
                updateResource(current => ({...current, loading: false}));
            }
        }

        void run();
        return () => controller.abort();
    }, [key, load, errorMessage]);

    const updateData = useCallback((update: (data: T) => T) => {
        setResource(current => {
            if (current.key !== key || current.load !== load || current.errorMessage !== errorMessage ||
                current.data === null) return current;
            return {...current, data: update(current.data)};
        });
    }, [key, load, errorMessage]);

    return {data: resource.data, error: resource.error, loading: resource.loading, updateData};
}