import { useState } from "react";
import { createZone, getZones } from "../../api/zones-api";

function CreateZoneForm({ positions, setPositions, setZones, typeName, setTypeName }) {

    const handleTypeChange = (e) => {
        setTypeName(e.target.value.toUpperCase());
    };

    const [zoneName, setZoneName] = useState("")

    const handleZoneName = (e) => {
        setZoneName(e.target.value)
    }

    const handleCreateZone = (e) => {
        e.preventDefault(); // Prevent default form submission

        // Validate there are enough points to form a polygon
        if (positions.length < 3) {
            alert("You need at least 3 points to create a zone");
            return;
        }

        // Prepare the form data according to required structure
        const formData = {
            title: zoneName,
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

    return (
        <>
            <form method="POST" onSubmit={handleCreateZone}>
                <label>Type zone name (unique):</label>
                <br></br>
                <input
                    type="text"
                    name="zoneName"
                    onChange={handleZoneName}
                />
                <br></br>

                <label>Choose zone type:</label>
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
                <br></br>
                <input type="submit" value="Create" />
            </form>
        </>
    )
}

export default CreateZoneForm;