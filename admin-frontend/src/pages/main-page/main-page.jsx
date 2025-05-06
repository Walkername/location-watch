import { Icon } from "leaflet";
import { useEffect, useState } from "react";
import { MapContainer, Marker, Polyline, Popup, TileLayer, useMapEvents } from "react-leaflet";
import { getZones } from "../../api/zones-api";


function MainPage() {

    const spbPosition = [59.937500, 30.308611];

    const [zones, setZones] = useState([])

    const [positions, setPositions] = useState([]);

    const handleMapClick = (latlng) => {
        setPositions([...positions, [latlng.lat, latlng.lng]]);
        console.log(latlng.lat, latlng.lng);
    };

    // Component to handle map click events
    const MapClickHandler = () => {
        useMapEvents({
            click(e) {
                handleMapClick(e.latlng);
            },
        });
        return null;
    };

    // Calculate positions for polyline (close the polygon if there are 3+ points)
    const polylinePositions = positions.length >= 3
        ? [...positions, positions[0]]
        : positions;

    const customIcon = new Icon({
        // iconUrl: "https://cdn-icons-png.flaticon.com/512/447/447031.png",
        iconUrl: require("../../icons/pngegg.png"),
        iconSize: [72, 60] // size of the icon
    });

    // ZONES

    useEffect(() => {
        getZones().then((response) => {
            setZones(response)
        })
    }, [])

    return (
        <>
            <h1>Admin Interface</h1>

            <div className="page-content-container">
                <div className="page-content">
                    <MapContainer center={spbPosition} zoom={11}>
                        <TileLayer
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />
                        <MapClickHandler />

                        {/* Render markers */}
                        {positions.map((position, index) => (
                            <Marker key={index} position={position} icon={customIcon}>
                                <Popup />
                            </Marker>
                        ))}

                        {/* Render connecting lines */}
                        {positions.length >= 2 && (
                            <Polyline positions={polylinePositions} color="blue" />
                        )}
                    </MapContainer>

                    <div>
                        <form method="POST">
                            <label>Type name of zone:</label>
                            <br></br>
                            <input type="text" />
                            <input type="submit" value="Create" />
                        </form>


                        <h2>All zones:</h2>
                        <ul>
                            {
                                zones.map((zone, index) => (
                                    <li key={index}>{zone.typeName}</li>
                                ))
                            }
                        </ul>
                    </div>
                </div>
            </div>
        </>
    );
}

export default MainPage;
