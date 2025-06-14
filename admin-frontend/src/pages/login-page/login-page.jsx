import { useState } from "react";
import { useNavigate } from "react-router";
import { login } from "../../api/auth-api";

function LoginPage() {
    const navigate = useNavigate();

    const [errorMessage, setErrorMessage] = useState("");

    const [formData, setFormData] = useState({
        username: "",
        password: ""
    })

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    }

    const handleSubmit = (e) => {
        e.preventDefault();

        login(formData)
            .then((data) => {
                setErrorMessage("");
                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);
                navigate("/");
            })
            .catch((error) => {
                setErrorMessage("Wrong username or password");
            })
    }

    return (
        <div className="login-container">
            <h1 className="login-title">Login</h1>
            <form className="login-form" method="POST" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="username">Username</label>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        minLength="5"
                        maxLength="20"
                        value={formData.username}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="password">Password</label>
                    <input
                        id="password"
                        name="password"
                        type="password"
                        minLength="5"
                        value={formData.password}
                        onChange={handleChange}
                        required
                    />
                </div>
                {errorMessage && <div className="error-message">{errorMessage}</div>}
                <button type="submit" className="btn btn-primary login-btn">
                    Login
                </button>
            </form>
        </div>
    );
}

export default LoginPage;