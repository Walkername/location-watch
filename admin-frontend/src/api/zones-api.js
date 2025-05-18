
export const getZones = async () => {
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/zones`);
    if (!response.ok) {
        throw new Error('Failed to get zones');
    }
    return response.json();
}

export const createZone = async (zoneData) => {
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/zones/add`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(zoneData)
    });
    if (!response.ok) {
        throw new Error('Failed to add new zone');
    }
    return response.json();
}

export const deleteZone = async (id) => {
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/zones/delete/${id}`, {
        method: "DELETE"
    });
    if (!response.ok) {
        throw new Error('Failed to delete the zone');
    }
    return response.json();
}
