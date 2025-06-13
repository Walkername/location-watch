
import { useEffect, useState } from "react";
import validateDate from "../../utils/date-validation/date-validation";

function ViolatorsList({ violations }) {
    return (
        <div class="list-section">
            <h2>Active Violations</h2>
            <ul class="list">
                {
                    Array.from(violations.entries()).map(([key, value], index) => (
                        <div key={index} class="violation-card">
                            <div class="violation-header">
                                <div class="violation-id">User #{key}</div>
                                <div class="violation-time">{validateDate(value.timestamp)}</div>
                            </div>
                            <div class="violation-body">
                                <span><strong>Zones:</strong>
                                    {
                                        value.crossedZones.map((zone, index) => {
                                            return <span key={index}> {zone.title} </span>
                                        })
                                    }
                                </span>
                                <span><strong>Speed:</strong> {value.speed} km/h</span>
                            </div>
                        </div>
                    ))
                }

                <div class="violations-container" id="violationsContainer">
                    <div class="violation-card">
                        <div class="violation-header">
                            <div class="violation-id">Test User</div>
                            <div class="violation-time">01.02.2002, 14:36:03</div>
                        </div>
                        <div class="violation-body">
                            <span><strong>Zones:</strong> Zone A, Zone B</span>
                            <span><strong>Speed:</strong> 75 km/h</span>
                        </div>
                    </div>
                </div>
            </ul>
        </div>
    );
}

export default ViolatorsList;