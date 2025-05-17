import { DivIcon, Icon } from "leaflet";
import { useEffect, useState } from "react";
import { MapContainer, Marker, Polygon, Polyline, Popup, TileLayer, useMapEvents } from "react-leaflet";
import { createZone, deleteZone, getZones } from "../../api/zones-api";
import FunctionBar from "../../components/function-bar/function-bar";
import MapZones from "../../components/map-zones/map-zones";


function MainPage() {

    const spbPosition = [59.937500, 30.308611];

    const [zones, setZones] = useState([])

    const [positions, setPositions] = useState([]);

    const handleMapClick = (latlng) => {
        setPositions([...positions, [latlng.lat, latlng.lng]]);
        console.log(positions);
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

    const createNumberedIcon = (number, type) => {
        return new DivIcon({
            className: 'custom-marker',
            html: `
                    <div style="
                        background-color: ${getZoneColor(type)};
                        border-radius: 50%;
                        width: 24px;
                        height: 24px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: white;
                        font-weight: bold;
                        border: 2px solid white;
                    ">
                        ${number}
                    </div>
                `,
            iconSize: [24, 24]
        });
    };

    // ZONES

    useEffect(() => {
        getZones().then((response) => {
            setZones(response);
        })
            .catch((error) => {
                alert("Connection error");
            })
    }, [])

    // Add this function to get color based on zone type
    const getZoneColor = (type) => {
        switch (type) {
            case 'NO_SPEED': return 'red';
            case 'LESS_SPEED': return 'blue';
            default: return 'green';
        }
    };

    // Listener to remove last marker

    useEffect(() => {
        const handleKeyPress = (e) => {
            if (e.ctrlKey && e.key === 'z') {
                e.preventDefault();
                setPositions(prev => prev.slice(0, -1));
            }
        };

        window.addEventListener('keydown', handleKeyPress);
        return () => window.removeEventListener('keydown', handleKeyPress);
    }, []);

    // Handle to delete zone

    const handleDelete = (id) => {
        deleteZone(id)
            .then(() => {
                getZones().then((response) => {
                    setZones(response);
                })
            })
            .catch((error) => {
                alert("Error to delete the zone!")
            });
    }

    const [typeName, setTypeName] = useState("NO_SPEED")

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
                            <Marker
                                key={`marker-${position[0]}-${position[1]}-${index}`}
                                position={position}
                                icon={createNumberedIcon(index + 1, typeName)}
                            >
                                <Popup>
                                    Point {index + 1}<br />
                                    Lat: {position[0].toFixed(6)}<br />
                                    Lng: {position[1].toFixed(6)}
                                </Popup>
                            </Marker>
                        ))}

                        {/* Render connecting lines */}
                        {positions.length >= 2 && (
                            <Polyline
                                key={`polyline-${typeName}`}
                                positions={polylinePositions}
                                color={getZoneColor(typeName)}
                            />
                        )}

                        {/* Render semi-transparent polygon when 3+ markers exist */}
                        {positions.length >= 3 && (
                            <Polygon
                                positions={polylinePositions}
                                pathOptions={{
                                    fillColor: getZoneColor(typeName),
                                    color: 'transparent'
                                }}
                            />
                        )}

                        {/* Display existing zones as polygons */}
                        <MapZones
                            zones={zones}
                            getZoneColor={getZoneColor}
                            handleDelete={handleDelete}
                        />
                    </MapContainer>

                    <FunctionBar
                        positions={positions}
                        setPositions={setPositions}
                        setZones={setZones}
                        typeName={typeName}
                        setTypeName={setTypeName}
                        zones={zones}
                    />
                </div>
            </div>
        </>
    );
}

export default MainPage;
