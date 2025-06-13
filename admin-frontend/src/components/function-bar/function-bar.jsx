import CreateZoneForm from "../create-zone-form/create-zone-form";
import ViolatorsList from "../violators-list/violators-list";
import ZoneList from "../zone-list/zone-list";

function FunctionBar({ positions, setPositions, setZones, typeName, setTypeName, zones, violations, setPopupZoneId }) {
    const handleZoneClick = (zoneId) => {
        setPopupZoneId(zoneId);
    };

    return (
        <aside className="sidebar">
            <CreateZoneForm
                positions={positions}
                setPositions={setPositions}
                setZones={setZones}
                typeName={typeName}
                setTypeName={setTypeName}
            />

            <ZoneList
                zones={zones}
                onZoneClick={handleZoneClick}
            />

            <ViolatorsList violations={violations} />
        </aside>
    )
}

export default FunctionBar;