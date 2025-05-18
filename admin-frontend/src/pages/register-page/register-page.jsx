import { useState } from "react";
import { useNavigate } from "react-router";
import { register } from "../../api/auth-api";

function RegisterPage() {
    const navigate = useNavigate();

    const [errorMessage, setErrorMessage] = useState("");

    const [clientForm, setClientForm] = useState({
        username: "",
        password: "",
        passwordConfirmation: ""
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setClientForm({ ...clientForm, [name]: value });
    }

    const handleSubmit = (e) => {
        e.preventDefault();

        if (clientForm.passwordConfirmation === clientForm.password) {
            const formData = {
                username: clientForm.username,
                password: clientForm.password
            }

            register(formData)
                .then(() => {
                    console.log("Register successfully");
                    setErrorMessage("");
                    navigate("/login");
                })
                .catch((error) => {
                    setErrorMessage(error.message.replace(';', " "));
                })
        } else {
            setErrorMessage("Password Confirmation failed!")
        }
    }

    return (
        <>
            <h1>Register</h1>

            <div className="page-content-container">
                <div className="page-content">
                    <form method="POST" onSubmit={handleSubmit}>
                        <label>Username:</label>
                        <br></br>
                        <input name="username" type="text" min="5" max="20" value={clientForm.username} onChange={handleChange} required />
                        <br></br>

                        <label>Password:</label>
                        <br></br>
                        <input name="password" type="password" min="5" value={clientForm.password} onChange={handleChange} required />
                        <br></br>

                        <label>Password confirmation:</label>
                        <br></br>
                        <input name="passwordConfirmation" type="password" value={clientForm.passwordConfirmation} onChange={handleChange} required />
                        <br></br>
                        {
                            errorMessage
                                ? <>
                                    <span style={{ color: "red" }}>{errorMessage}</span>
                                    <br></br>
                                </>
                                : <></>
                        }

                        <input type="submit" value="Register" />
                    </form>
                </div>
            </div>
        </>
    )
}

export default RegisterPage;
