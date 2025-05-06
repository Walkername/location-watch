
export const getZones = async () => {
    const response = await fetch(`http://localhost:8080/zones`);
    if (!response.ok) {
        throw new Error('Failed to get zones');
    }
    return response.json();
}