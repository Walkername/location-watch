import { Polygon, Popup } from "react-leaflet";

function MapZones({ zones, getZoneColor, handleDelete }) {
    return (
        <>
            {
                zones.map((zone) => (
                    <Polygon
                        key={zone.id}
                        positions={zone.area.map(point => [point.x, point.y])}
                        color={getZoneColor(zone.typeName)}
                    >
                        <Popup>
                            <div>
                                <h3>{zone.title}</h3>
                                <p>Type: {zone.typeName}</p>
                                <p>Points: {zone.area.length}</p>
                                <button
                                    onClick={() => handleDelete(zone.id)}
                                >Delete</button>
                            </div>
                        </Popup>
                    </Polygon>
                ))
            }
        </>
    )
}

export default MapZones;