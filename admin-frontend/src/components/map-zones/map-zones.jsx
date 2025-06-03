import { Polygon, Popup } from "react-leaflet";

function MapZones({ zones, getZoneColor, handleDelete }) {
    const positions = [
        {
            latitude: 59.92654957090054,
            longitude: 30.291200208134555
        }, 
        {
            latitude: 59.94752675506172,
            longitude: 30.33378290039971
        }
    ]
    return (
        <>
            {
                zones.slice().reverse().map((zone) => (
                    <Polygon
                        key={zone.id}
                        positions={zone.area.map(point => [point.latitude, point.longitude])}
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