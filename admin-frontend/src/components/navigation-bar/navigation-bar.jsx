import { useNavigate } from "react-router";

function NavigationBar({ title }) {
    const navigate = useNavigate()

    const handleLogout = (e) => {
        localStorage.removeItem("token");
        navigate("/login");
    }

    return (
        <header>
            <h1>{title}</h1>
            <button
                className="exit-button"
                onClick={handleLogout}
            >
                Log out
            </button>
        </header>
    )
}

export default NavigationBar;