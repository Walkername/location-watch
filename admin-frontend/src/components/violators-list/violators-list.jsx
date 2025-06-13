
import { useEffect, useState } from "react";
import validateDate from "../../utils/date-validation/date-validation";

function ViolatorsList({ violations }) {
    return (
        <div className="list-section">
            <h2>Active Violations</h2>
            <ul className="list">
                <div className="violations-container" id="violationsContainer">
                    <div className="violations-list-scroll">
                        {
                            Array.from(violations.entries()).map(([key, value], index) => (
                                <div key={index} className="violation-card">
                                    <div className="violation-header">
                                        <div className="violation-id">User #{key}</div>
                                        <div className="violation-time">{validateDate(value.timestamp)}</div>
                                    </div>
                                    <div className="violation-body">
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

                        <div className="violation-card">
                            <div className="violation-header">
                                <div className="violation-id">Test User</div>
                                <div className="violation-time">01.02.2002, 14:36:03</div>
                            </div>
                            <div className="violation-body">
                                <span><strong>Zones:</strong> Zone A, Zone B</span>
                                <span><strong>Speed:</strong> 75 km/h</span>
                            </div>
                        </div>

                        <div className="violation-card">
                            <div className="violation-header">
                                <div className="violation-id">Test User</div>
                                <div className="violation-time">01.02.2002, 14:36:03</div>
                            </div>
                            <div className="violation-body">
                                <span><strong>Zones:</strong> Zone A, Zone B</span>
                                <span><strong>Speed:</strong> 75 km/h</span>
                            </div>
                        </div>

                        <div className="violation-card">
                            <div className="violation-header">
                                <div className="violation-id">Test User</div>
                                <div className="violation-time">01.02.2002, 14:36:03</div>
                            </div>
                            <div className="violation-body">
                                <span><strong>Zones:</strong> Zone A, Zone B</span>
                                <span><strong>Speed:</strong> 75 km/h</span>
                            </div>
                        </div>

                        <div className="violation-card">
                            <div className="violation-header">
                                <div className="violation-id">Test User</div>
                                <div className="violation-time">01.02.2002, 14:36:03</div>
                            </div>
                            <div className="violation-body">
                                <span><strong>Zones:</strong> Zone A, Zone B</span>
                                <span><strong>Speed:</strong> 75 km/h</span>
                            </div>
                        </div>

                        <div className="violation-card">
                            <div className="violation-header">
                                <div className="violation-id">Test User</div>
                                <div className="violation-time">01.02.2002, 14:36:03</div>
                            </div>
                            <div className="violation-body">
                                <span><strong>Zones:</strong> Zone A, Zone B</span>
                                <span><strong>Speed:</strong> 75 km/h</span>
                            </div>
                        </div>
                    </div>
                </div>
            </ul>
        </div>
    );
}

export default ViolatorsList;