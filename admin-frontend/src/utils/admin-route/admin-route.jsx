import { Navigate, Outlet } from "react-router";
import getClaimFromToken from "../token-validation/token-validation";

export default function AdminRoute() {
    const token = localStorage.getItem("accessToken");
    const adminAccess = getClaimFromToken(token, "role");

    const exp = getClaimFromToken(token, "exp");
    const authStatus = Date.now() / 1000 <= exp && adminAccess === "ROLE_ADMIN";

    return (
        authStatus ? <Outlet /> : <Navigate to="/login" />
    )
}