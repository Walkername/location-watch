import { useState } from "react";
import { createZone, getZones } from "../../api/zones-api";

function CreateZoneForm({ positions, setPositions, setZones, typeName, setTypeName }) {
    const [zoneName, setZoneName] = useState("");
    const [speed, setSpeed] = useState();

    const handleTypeChange = (e) => {
        const newType = e.target.value.toUpperCase();
        setTypeName(newType);

        // Reset speed to 0 when NO_SPEED is selected
        if (newType === "NO_SPEED") {
            setSpeed(0);
        }
    };

    const handleZoneName = (e) => {
        setZoneName(e.target.value);
    };

    const handleSpeedChange = (e) => {
        const value = parseInt(e.target.value) || 0;
        // Ensure value stays within 0-20 range
        if (value >= 0 && value <= 20) {
            setSpeed(value);
        }
    };

    const handleCreateZone = (e) => {
        e.preventDefault();

        if (positions.length < 3) {
            alert("You need at least 3 points to create a zone");
            return;
        }

        const formData = {
            title: zoneName,
            typeName: typeName,
            area: positions.map(position => ({
                latitude: position[0],
                longitude: position[1]
            })),
            speed: typeName === "LESS_SPEED" ? speed : 0 // Add speed to formData
        };

        createZone(formData)
            .then(() => getZones())
            .then(response => setZones(response))
            .catch(() => alert("Error to create zone!"));

        setPositions([]);
    };

    return (
        <form method="POST" onSubmit={handleCreateZone} className="form-section" >
            <h2>Create Zone</h2>

            <div className="form-group">
                <label>Zone Name (unique)</label>
                <input
                    type="text"
                    placeholder="Enter name"
                    onChange={handleZoneName}
                />
            </div>

            <div className="form-group">
                <label>Zone Type</label>
                <select onChange={handleTypeChange}>
                    <option value="NO_SPEED">No speed</option>
                    <option value="LESS_SPEED">Less Speed</option>
                </select>
            </div>

            {
                typeName === "LESS_SPEED" && (
                    <div className="form-group" id="speedGroup">
                        <label for="maxSpeed">Max Speed (km/h): 0..20</label>
                        <input
                            type="text"
                            value={speed}
                            placeholder="Enter max speed"
                            onChange={handleSpeedChange}
                            min="0"
                            max="20"
                        />
                    </div>
                )
            }

            <div className="form-actions">
                <button onClick={
                    (e) => {
                        e.preventDefault();
                        setPositions([]);
                    }} className="btn btn-secondary">Clear</button>
                <button type="submit" className="btn btn-primary">Create</button>
            </div>

            {/* Conditionally render speed input */}
            {/* {typeName === "LESS_SPEED" && (
                <>
                    <label>Speed limit (0-20 km/h):</label>
                    <br />
                    <input
                        type="number"
                        value={speed}
                        onChange={handleSpeedChange}
                        placeholder="0"
                        min="0"
                        max="20"
                    />
                    <br /><br />
                </>
            )}

            <input type="submit" value="Create" /> */}
        </form>
    );
}

export default CreateZoneForm;