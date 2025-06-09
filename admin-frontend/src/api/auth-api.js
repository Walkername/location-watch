import request from "./fetch-client";

export const login = formData => request(
    `/auth/login`,
    {
        method: "POST",
        body: formData
    }
);

export const register = formData => request(
    `/auth/register`,
    {
        method: "POST",
        body: formData
    }
);

export const refreshTokens = formData => request(
    '/auth/refresh',
    {
        method: "POST",
        body: formData
    }
)
