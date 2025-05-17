
function RegisterPage() {
    const handleSubmit = () => {

    }

    return (
        <>
            <h1>Register</h1>
            
            <div className="page-content-container">
                <div className="page-content">
                    <form method="POST" onSubmit={handleSubmit} >
                        <label>Username:</label>
                        <br></br>
                        <input
                            type="text"
                        />
                        <br></br>

                        <label>Password:</label>
                        <br></br>
                        <input
                            type="password"
                        />
                        <br></br>

                        <label>Password confirmation:</label>
                        <br></br>
                        <input
                            type="password"
                        />
                        <br></br>

                        <input type="submit" value="Register" />
                    </form>
                </div>
            </div>
        </>
    )
}

export default RegisterPage;
