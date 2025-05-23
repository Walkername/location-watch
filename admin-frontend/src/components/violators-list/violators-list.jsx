import { Client } from "@stomp/stompjs";
import { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import validateDate from "../../utils/date-validation/date-validation";

function ViolatorsList() {
    const accessToken = localStorage.getItem("accessToken");

    const [violations, setViolations] = useState([]);
    const [client, setClient] = useState(null);

    useEffect(() => {
        const stompClient = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            reconnectDelay: 5000,
            debug: (str) => console.log(str),
        });

        stompClient.onConnect = (frame) => {
            stompClient.subscribe('/topic/violations',
                (message) => {
                    const newViolations = JSON.parse(message.body);
                    setViolations(newViolations);
                }
            );
        };

        stompClient.activate();
        setClient(stompClient);

        return () => {
            stompClient.deactivate();
        };
    }, []);

    return (
        <>
            <h2>Active Violations</h2>
            <ul>
                {
                    violations.map((violation, index) => {
                        return (
                            <div key={index}>{violation.clientId}, {violation.zoneTitle}, {validateDate(violation.timestamp)}</div>
                        );
                    })
                }
            </ul>
        </>
    );
}

export default ViolatorsList;