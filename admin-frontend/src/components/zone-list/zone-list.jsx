
function ZoneList({ zones, onZoneClick }) {
    return (
        <div class="list-section">
            <h2>All Zones</h2>
            <div className="zones-container" id="zonesContainer">
                <div className="zones-list-scroll">
                    {zones.map(zone => (
                        <div
                            key={zone.id}
                            className="zone-card"
                            onClick={() => onZoneClick(zone.id)}
                        >
                            <div className="zone-header">
                                <div className="zone-title">{zone.title}</div>
                                <div className={`zone-badge ${zone.typeName === 'NO_SPEED' ? 'badge-no' :
                                    zone.typeName === 'LESS_SPEED' ? 'badge-less' : ''
                                    }`}>
                                    {zone.typeName}
                                </div>
                            </div>
                            <div className="zone-details">
                                {
                                    zone.typeName === "LESS_SPEED" ? (
                                        <span><strong>Max Speed:</strong> {zone.speed} km/h</span>
                                    )
                                        : <span>It is prohibited to be in this area</span>
                                }

                            </div>

                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}

export default ZoneList;