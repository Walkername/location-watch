import { useContext } from "react";
import { useNavigate } from "react-router";

function ForbiddenPage() {
    const navigate = useNavigate();
    // const { logout } = useContext(AuthContext);

    const handleGoBack = () => {
        navigate(-1); // Go back to previous page
    };

    return (
        <div className="forbidden-container">
            <div className="forbidden-content">
                <h1>403 Forbidden</h1>
                <p className="forbidden-text">
                    ⚠️ Access Denied: You don't have permission to view this page
                </p>
                <div className="forbidden-actions">
                    <button onClick={handleGoBack} className="forbidden-button">
                        Go Back
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ForbiddenPage;