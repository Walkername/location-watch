import { DivIcon } from "leaflet";
import { Client } from "@stomp/stompjs";
import { useEffect, useState } from "react";
import { MapContainer, Marker, Polygon, Polyline, Popup, TileLayer, useMapEvents } from "react-leaflet";
import { deleteZone, getZones } from "../../api/zones-api";
import FunctionBar from "../../components/function-bar/function-bar";
import MapZones from "../../components/map-zones/map-zones";
import NavigationBar from "../../components/navigation-bar/navigation-bar";


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

    // Violators list

    const accessToken = localStorage.getItem("accessToken");

    const [violations, setViolations] = useState(() => new Map());
    const [client, setClient] = useState(null);

    useEffect(() => {
        const stompClient = new Client({
            brokerURL: 'ws://localhost:8080/ws',
            connectHeaders: {
                Authorization: `Bearer ${accessToken}`
            },
            reconnectDelay: 5000,
            debug: (str) => console.log(str),
        });

        stompClient.onConnect = (frame) => {
            stompClient.subscribe('/topic/violations',
                (message) => {
                    const newViolation = JSON.parse(message.body);
                    const newMap = new Map(violations)
                    newMap.set(newViolation.clientId, newViolation)
                    setViolations(newMap);
                }
            );
        };

        stompClient.activate();
        setClient(stompClient);

        return () => {
            stompClient.deactivate();
        };
    }, []);

    return (
        <>
            <NavigationBar title="Admin Interface" />

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


                        {
                            Array.from(violations.entries()).map(([key, value], index) => (
                                <Marker
                                    key={`marker-${key}`}
                                    position={[value.latitude, value.longitude]}
                                    icon={createNumberedIcon(`U${key}`)}
                                ></Marker>
                            ))
                        }

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
                        violations={violations}
                    />
                </div>
            </div>
        </>
    );
}

export default MainPage;
