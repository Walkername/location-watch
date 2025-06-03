
import { useEffect, useState } from "react";
import validateDate from "../../utils/date-validation/date-validation";

function ViolatorsList({ violations }) {
    return (
        <>
            <h2>Active Violations</h2>
            <ul>
                {
                    Array.from(violations.entries()).map(([key, value], index) => (
                        <div key={index}>
                            <div>Client: {key}</div>
                            <div>
                                Zones: {
                                    value.crossedZones.map((zone, index) => {
                                        return <span key={index}> {zone.title} </span>
                                    })
                                }
                            </div>
                            <div>
                                Speed: {value.speed} km/h
                            </div>
                            <div>
                                Last time: {validateDate(value.timestamp)}
                            </div>
                        </div>
                    ))
                }
            </ul>
        </>
    );
}

export default ViolatorsList;