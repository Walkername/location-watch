import { useNavigate } from "react-router";

function NavigationBar({ title }) {
    const navigate = useNavigate()

    const handleLogout = (e) => {
        localStorage.removeItem("token");
        navigate("/login");
    }

    return (
        <nav className="nav-bar">
            <h1>{title}</h1>
            <button
                className="exit-button"
                onClick={handleLogout}
            >
                Log out
            </button>
        </nav>
    )
}

export default NavigationBar;