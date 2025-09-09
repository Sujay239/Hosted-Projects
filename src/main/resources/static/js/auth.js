// Wait for DOM to be fully loaded before accessing elements
document.addEventListener('DOMContentLoaded', function () {
    // Get all elements safely
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const switchText = document.getElementById('switch-text');
    const formTitle = document.getElementById('form-title');

    // Input fields - use optional chaining and null checks
    const registerUsername = document.getElementById('register-username');
    const registerEmail = document.getElementById('register-email');
    const registerPassword = document.getElementById('register-password');
    const registerConfirmPassword = document.getElementById('register-confirm-password');
    const registerError = document.getElementById('register-error');

    const loginUsername = document.getElementById('login-username');
    const loginPassword = document.getElementById('login-password');
    const loginError = document.getElementById('login-error');

    let isLogin = true;
    const BACKEND_URL = "https://hosted-projects-production.up.railway.app";

    // Check if essential elements exist
    if (!loginForm || !registerForm || !switchText || !formTitle) {
        console.error('Essential form elements not found!');
        return;
    }

    // Function to switch between login and register forms
    function switchForm() {
        isLogin = !isLogin;

        if (isLogin) {
            if (loginForm) loginForm.style.display = 'block';
            if (registerForm) registerForm.style.display = 'none';
            if (formTitle) formTitle.textContent = 'Login';
            if (switchText) switchText.innerHTML = `Don't have an account? <span class="switch-link" id="switch-link">Register</span>`;
        } else {
            if (loginForm) loginForm.style.display = 'none';
            if (registerForm) registerForm.style.display = 'block';
            if (formTitle) formTitle.textContent = 'Register';
            if (switchText) switchText.innerHTML = `Already have an account? <span class="switch-link" id="switch-link">Login</span>`;
        }

        // Clear errors when switching forms
        if (registerError) registerError.style.display = 'none';
        if (loginError) loginError.style.display = 'none';

        // Reattach event listener to the new switch link
        setTimeout(() => {
            const newSwitchLink = document.getElementById('switch-link');
            if (newSwitchLink) {
                newSwitchLink.addEventListener('click', switchForm);
            }
        }, 100);
    }

    // Initial event listener - check if switch link exists
    const initialSwitchLink = document.getElementById('switch-link');
    if (initialSwitchLink) {
        initialSwitchLink.addEventListener('click', switchForm);
    }

    // Registration form submission - check if form exists
    if (registerForm) {
        registerForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            // Check if all required fields exist
            if (!registerUsername || !registerEmail || !registerPassword || !registerConfirmPassword) {
                if (registerError) {
                    registerError.textContent = "Form elements missing. Please refresh the page.";
                    registerError.style.display = "block";
                }
                return;
            }

            if (registerPassword.value !== registerConfirmPassword.value) {
                if (registerError) {
                    registerError.textContent = "Passwords do not match.";
                    registerError.style.display = "block";
                    registerConfirmPassword.focus();
                }
                return;
            }

            try {
                const response = await fetch(`${BACKEND_URL}/api/auth/register`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        username: registerUsername.value,
                        email: registerEmail.value,
                        password: registerPassword.value
                    }),
                    credentials: 'include' // if your backend uses sessions/cookies
                });

                if (!response) {
                    if (registerError) {
                        registerError.textContent = "No response from server. Is the backend running?";
                        registerError.style.display = "block";
                    }
                    return;
                }

                const responseText = await response.text();
                let data;

                try {
                    data = JSON.parse(responseText);
                } catch (e) {
                    if (registerError) {
                        registerError.textContent = `Something went wrong.`;
                        registerError.style.display = "block";
                    }
                    console.error("Raw server response:", responseText);
                    return;
                }

                if (response.ok) {
                    // Save token and redirect
                    localStorage.setItem("token", data.token);
                    localStorage.setItem("username", data.username);
                    localStorage.setItem("email", data.email);
                    window.location.href = "chat.html";
                } else {
                    if (registerError) {
                        registerError.textContent = data.message || `Error: ${response.status},Unauthorized : Please check the credentials`;
                        registerError.style.display = "block";
                    }
                }

            } catch (err) {
                console.error("Full error details:", err);
                if (registerError) {
                    registerError.textContent = `Network error: ${err.message}. Check if server is running on port 8080.`;
                    registerError.style.display = "block";
                }
            }
        });
    }

    // Login form submission - check if form exists
    if (loginForm) {
        loginForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            // Check if all required fields exist
            if (!loginUsername || !loginPassword) {
                if (loginError) {
                    loginError.textContent = "Form elements missing. Please refresh the page.";
                    loginError.style.display = "block";
                }
                return;
            }

            try {
                const response = await fetch(`${BACKEND_URL}/api/auth/login`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        username: loginUsername.value,
                        password: loginPassword.value
                    }),
                    credentials: 'include'
                });

                const responseText = await response.text();
                let data;

                try {
                    data = JSON.parse(responseText);
                } catch (e) {
                    if (loginError) {
                        loginError.textContent = `Something went wrong.`;
                        loginError.style.display = "block";
                    }
                    console.error("Raw server response:", responseText);
                    return;
                }

                if (response.ok) {
                    localStorage.setItem("token", data.token);
                    localStorage.setItem("username", data.username);
                    localStorage.setItem("email", data.email);
                    window.location.href = "chat.html";
                } else {
                    if (loginError) {
                        loginError.textContent = data.message || `Error: ${response.status}, Unauthorized : Please check the credentials`;
                        loginError.style.display = "block";
                    }
                }
            } catch (err) {
                console.error("Login error:", err);
                if (loginError) {
                    loginError.textContent = "Network error. Please check console for details.";
                    loginError.style.display = "block";
                }
            }
        });
    }

    // Hide error messages when user starts typing - with null checks
    const registerInputs = [registerUsername, registerEmail, registerPassword, registerConfirmPassword];
    registerInputs.forEach(input => {
        if (input && registerError) {
            input.addEventListener('input', () => {
                registerError.style.display = 'none';
            });
        }
    });

    const loginInputs = [loginUsername, loginPassword];
    loginInputs.forEach(input => {
        if (input && loginError) {
            input.addEventListener('input', () => {
                loginError.style.display = 'none';
            });
        }
    });

    // Hide error when passwords match
    if (registerConfirmPassword && registerError) {
        registerConfirmPassword.addEventListener('input', function () {
            if (registerPassword && registerPassword.value === registerConfirmPassword.value) {
                registerError.style.display = "none";
            }
        });
    }

    console.log('Form initialization complete');
});