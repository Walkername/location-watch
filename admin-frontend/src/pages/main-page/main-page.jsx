import { DivIcon, Icon } from "leaflet";
import { useEffect, useState } from "react";
import { MapContainer, Marker, Polygon, Polyline, Popup, TileLayer, useMapEvents } from "react-leaflet";
import { createZone, getZones } from "../../api/zones-api";


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

    const createNumberedIcon = (number) => {
        return new DivIcon({
            className: 'custom-marker',
            html: `
                    <div style="
                        background-color: red;
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
            iconSize: [24, 24] // size of the div element
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

    const [typeName, setTypeName] = useState("NO_SPEED")

    const handleTypeChange = (e) => {
        setTypeName(e.target.value.toUpperCase());
    };

    const handleCreateZone = (e) => {
        e.preventDefault(); // Prevent default form submission

        // Validate there are enough points to form a polygon
        if (positions.length < 3) {
            alert("You need at least 3 points to create a zone");
            return;
        }

        // Prepare the form data according to required structure
        const formData = {
            typeName: typeName,
            area: positions.map(position => ({
                x: position[0],  // Latitude
                y: position[1]   // Longitude
            }))
        };

        // Call API to create zone
        createZone(formData).then(() => {
            getZones().then((response) => {
                setZones(response);
            });
        })
            .catch((error) => {
                alert("Error to create zone!")
            });

        // Clear markers after successful creation
        setPositions([]);
    }

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
                                key={index}
                                position={position}
                                icon={createNumberedIcon(index + 1)}
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
                        {zones.map((zone, index) => (
                            <Polygon
                                key={index}
                                positions={zone.area.map(point => [point.x, point.y])}
                                color={getZoneColor(zone.typeName)}
                            >
                                <Popup>
                                    <div>
                                        <h3>{zone.typeName} Zone</h3>
                                        <p>Points: {zone.area.length}</p>
                                    </div>
                                </Popup>
                            </Polygon>
                        ))}
                    </MapContainer>

                    <div>
                        <div>You can use "Ctrl + Z" shortcut to undo last marker</div>

                        <button
                            onClick={() => setPositions([])}
                        >Clear</button>

                        <form method="POST" onSubmit={handleCreateZone}>
                            <label>Type name of zone:</label>
                            <br></br>
                            <input
                                type="radio"
                                id="no_speed"
                                value="NO_SPEED"
                                name="typeName"
                                checked={typeName === "NO_SPEED"}
                                onChange={handleTypeChange}
                            />
                            <label htmlFor="no_speed">NO SPEED</label>
                            <br></br>
                            <input
                                type="radio"
                                id="less_speed"
                                value="LESS_SPEED"
                                name="typeName"
                                onChange={handleTypeChange}
                            />
                            <label htmlFor="less_speed">LESS SPEED</label>
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
