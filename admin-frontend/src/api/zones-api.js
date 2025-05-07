
export const getZones = async () => {
    const response = await fetch(`http://localhost:8080/zones`);
    if (!response.ok) {
        throw new Error('Failed to get zones');
    }
    return response.json();
}

export const createZone = async (zoneData) => {
    const response = await fetch(`http://localhost:8080/zones/add`, {
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
    const response = await fetch(`http://localhost:8080/zones/delete/${id}`, {
        method: "DELETE"
    });
    if (!response.ok) {
        throw new Error('Failed to delete the zone');
    }
    return response.json();
}
