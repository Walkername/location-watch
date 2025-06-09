
import request from "./fetch-client";

export const getZones = () => request(`/zones`);

export const createZone = zoneData => request(
    `/zones/add`,
    {
        method: 'POST',
        body: zoneData
    }
);

export const deleteZone = id => request(
    `/zones/delete/${id}`,
    {
        method: "DELETE",
    }
);
