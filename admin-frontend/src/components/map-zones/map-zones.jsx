import { forwardRef, useEffect, useImperativeHandle, useRef } from "react";
import { Polygon, Popup, useMap } from "react-leaflet";

const Zone = forwardRef(({ zone, getZoneColor, handleDelete, popupZoneId, setPopupZoneId }, ref) => {
    const map = useMap();
    const polygonRef = useRef();

    useImperativeHandle(ref, () => ({
        openPopup: () => {
            if (polygonRef.current) {
                polygonRef.current.openPopup()
            }
        }
    }))

    useEffect(() => {
        if (popupZoneId === zone.id && polygonRef.current) {
            polygonRef.current.openPopup();
            map.flyTo([
                zone.area[0].latitude,
                zone.area[0].longitude
            ], 11);
            setPopupZoneId(null);
        }
    }, [popupZoneId, zone.id, map]);

    return (
        <Polygon
            ref={polygonRef}
            positions={zone.area.map(point => [point.latitude, point.longitude])}
            color={getZoneColor(zone.typeName)}
        >
            <Popup>
                {/* <div>
                    <h3>{zone.title}</h3>
                    <p><b>Type:</b> {zone.typeName}</p>
                    <p><b>Max. Speed:</b> {zone.speed} km/h</p>
                    <p><b>Points:</b> {zone.area.length}</p>
                    <button
                        onClick={() => handleDelete(zone.id)}
                    >Delete</button>
                </div> */}
                <div className="zone-info-popup">
                    <div className="zone-info-header">
                        <h3>{zone.title}</h3>
                    </div>
                    <div className="zone-info-body">
                        <p><b>Type:</b> {zone.typeName}</p>
                        {zone.typeName === "LESS_SPEED" && (
                            <p><b>Max. Speed:</b> {zone.speed} km/h</p>
                        )}
                        <p><b>Points:</b> {zone.area.length}</p>
                    </div>
                    <button className="zone-delete-btn" onClick={() => handleDelete(zone.id)}>
                        Delete
                    </button>
                </div>
            </Popup>
        </Polygon>
    );

});

function MapZones({ zones, getZoneColor, handleDelete, popupZoneId, setPopupZoneId }) {
    const zoneRefs = useRef({});

    useEffect(() => {
        if (popupZoneId && zoneRefs.current[popupZoneId]) {
            zoneRefs.current[popupZoneId].openPopup();
        }
    }, [popupZoneId]);

    return (
        <>
            {zones.slice().reverse().map((zone) => (
                <Zone
                    key={zone.id}
                    ref={el => zoneRefs.current[zone.id] = el}
                    zone={zone}
                    getZoneColor={getZoneColor}
                    handleDelete={handleDelete}
                    popupZoneId={popupZoneId}
                    setPopupZoneId={setPopupZoneId}
                />
            ))}
        </>
    );
}

export default MapZones;