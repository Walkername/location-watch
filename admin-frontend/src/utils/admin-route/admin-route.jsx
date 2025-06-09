import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";
import ForbiddenPage from "../../pages/forbidden-page/forbidden-page";
import getClaimFromToken from "../token-validation/token-validation";

export default function AdminRoute() {
    const [authChecked, setAuthChecked] = useState(false);
    const [isAllowed, setIsAllowed] = useState(false);

    useEffect(() => {
        const checkAuth = async () => {
            let accessToken = localStorage.getItem("accessToken");
            let refreshToken = localStorage.getItem("refreshToken");

            const exp = getClaimFromToken(accessToken, "exp");
            const isExpired = !exp || (Date.now() / 1000 > exp);

            if (!accessToken || !refreshToken) {
                // If localStorate doesn't have a pair of tokens
                setIsAllowed(false);
                setAuthChecked(true);
                return;
            }

            if (isExpired) {
                // Trying to refresh
                try {
                    const res = await fetch(`${process.env.REACT_APP_BACKEND_URL}/auth/refresh`, {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ refreshToken }),
                    });

                    if (res.ok) {
                        const data = await res.json();
                        localStorage.setItem("accessToken", data.accessToken);
                        localStorage.setItem("refreshToken", data.refreshToken);
                        accessToken = data.accessToken;
                    } else {
                        throw new Error("Refresh failed");
                    }
                } catch (e) {
                    console.warn("Token refresh failed:", e);
                    setIsAllowed(false);
                    setAuthChecked(true);
                    return;
                }
            }

            // Check Admin Role
            const role = getClaimFromToken(accessToken, "role");
            const isAdmin = role === "ROLE_ADMIN";
            setIsAllowed(isAdmin);
            setAuthChecked(true);

            console.log("Here");
        };

        checkAuth();
    }, []);

    if (!authChecked) return <div>Checking access...</div>;

    return isAllowed ? <Outlet /> : <ForbiddenPage />;
}