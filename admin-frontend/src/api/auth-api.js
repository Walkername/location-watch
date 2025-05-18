
export const login = async (formData) => {
    console.log(process.env.REACT_APP_USER_SERVICE_URL);
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    });
    if (!response.ok) {
        throw new Error('Failed to login');
    }
    return response.json();
}

export const register = async (formData) => {
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/auth/register`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Registration failed");
    }

    return data;
}