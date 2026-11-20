import {apiUrl} from '../config.ts';

export type RequestOptions = {
    fallbackMessage: string;
    parseProblemDetail?: boolean;
    notFoundMessage?: string;
};

export class ApiResponseError extends Error {
    readonly status: number;

    constructor(message: string, status: number) {
        super(message);
        this.name = 'ApiResponseError';
        this.status = status;
    }
}

export async function request(
    path: string,
    init: RequestInit,
    options: RequestOptions,
): Promise<Response> {
    const response = await fetch(apiUrl(path), init);

    if (response.ok) {
        return response;
    }

    let message = response.status === 404 && options.notFoundMessage
        ? options.notFoundMessage
        : options.fallbackMessage;

    if (options.parseProblemDetail && !(response.status === 404 && options.notFoundMessage)) {
        const problem: unknown = await response.json().catch(() => null);
        if (
            typeof problem === 'object' && problem !== null &&
            'detail' in problem && typeof problem.detail === 'string' && problem.detail
        ) {
            message = problem.detail;
        }
    }

    throw new ApiResponseError(message, response.status);
}