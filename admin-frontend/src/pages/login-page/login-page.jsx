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
                navigate("/");
            })
            .catch((error) => {
                console.error("Error:", error);
                setErrorMessage("Wrong username or password");
            })
    }

    return (
        <>
            <h1>Login</h1>

            <div className="page-content-container">
                <div className="page-content">
                    <form method="POST" onSubmit={handleSubmit} >
                        <label>Username:</label>
                        <br></br>
                        <input name="username" type="text" min="5" max="20" value={formData.title} onChange={handleChange} required />
                        <br></br>

                        <label>Password:</label>
                        <br></br>
                        <input name="password" type="password" min="5" value={formData.title} onChange={handleChange} required />
                        <br></br>
                        {
                            errorMessage
                                ? <>
                                    <span style={{ color: "red" }}>{errorMessage}</span>
                                    <br></br>
                                </>
                                : <></>
                        }

                        <input type="submit" value="Login" />
                    </form>
                </div>
            </div>
        </>
    )
}

export default LoginPage;