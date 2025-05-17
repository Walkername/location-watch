
function ZoneList({ zones }) {
    return (
        <>
            <h2>All zones:</h2>
            <ol>
                {
                    zones.map((zone, index) => (
                        <li key={index}>{zone.title}: {zone.typeName}</li>
                    ))
                }
            </ol>
        </>
    )
}

export default ZoneList;