
function LoginPage() {

    const handleSubmit = () => {

    }

    return (
        <>
            <h1>Login</h1>
            
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

                        <input type="submit" value="Login" />
                    </form>
                </div>
            </div>
        </>
    )
}

export default LoginPage;