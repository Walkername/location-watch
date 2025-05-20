
export const getZones = async () => {
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/zones`);
    if (!response.ok) {
        throw new Error('Failed to get zones');
    }
    return response.json();
}

export const createZone = async (zoneData) => {
    const token = localStorage.getItem("accessToken");
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/zones/add`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(zoneData)
    });
    if (!response.ok) {
        throw new Error('Failed to add new zone');
    }
    return response.json();
}

export const deleteZone = async (id) => {
    const token = localStorage.getItem("accessToken");
    const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/zones/delete/${id}`, {
        method: "DELETE",
        headers: {
            "Authorization": "Bearer " + token
        }
    });
    if (!response.ok) {
        throw new Error('Failed to delete the zone');
    }
    return response.json();
}
