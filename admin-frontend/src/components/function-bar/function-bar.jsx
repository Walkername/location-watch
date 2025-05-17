import CreateZoneForm from "../create-zone-form/create-zone-form";
import ZoneList from "../zone-list/zone-list";

function FunctionBar({ positions, setPositions, setZones, typeName, setTypeName, zones }) {
    return (
        <div>
            <div>You can use "Ctrl + Z" shortcut to undo last marker</div>

            <button
                onClick={() => setPositions([])}
            >Clear</button>

            <br></br>
            <br></br>

            <CreateZoneForm
                positions={positions}
                setPositions={setPositions}
                setZones={setZones}
                typeName={typeName}
                setTypeName={setTypeName}
            />

            <ZoneList zones={zones} />
        </div>
    )
}

export default FunctionBar;