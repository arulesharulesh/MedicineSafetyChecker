const LOGIN_URL = "/api/auth/login";

const form = document.querySelector("#login-form");
const emailInput = document.querySelector("#email");
const passwordInput = document.querySelector("#password");
const passwordToggle = document.querySelector("#password-toggle");
const submitButton = document.querySelector("#submit-button");
const buttonLabel = submitButton.querySelector(".button-label");
const message = document.querySelector("#form-message");

function showMessage(text, type) {
  message.textContent = text;
  message.className = `message ${type}`;
  message.hidden = false;
}

passwordToggle.addEventListener("click", () => {
  const shouldShowPassword = passwordInput.type === "password";
  passwordInput.type = shouldShowPassword ? "text" : "password";
  passwordToggle.textContent = shouldShowPassword ? "Hide" : "Show";
  passwordToggle.setAttribute("aria-label", `${shouldShowPassword ? "Hide" : "Show"} password`);
  passwordToggle.setAttribute("aria-pressed", String(shouldShowPassword));
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  message.hidden = true;
  submitButton.disabled = true;
  submitButton.setAttribute("aria-busy", "true");
  buttonLabel.textContent = "Signing in";

  try {
    const response = await fetch(LOGIN_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email: emailInput.value.trim(),
        password: passwordInput.value,
      }),
    });

    let result;
    try {
      result = await response.json();
    } catch {
      throw new Error("The server returned an unreadable response. Please try again.");
    }

    if (!response.ok) {
      throw new Error(result.message || "Sign in failed. Please check your details.");
    }

    if (!result.token || !result.user) {
      throw new Error("The server response is missing login details. Please try again.");
    }

    sessionStorage.setItem("authToken", result.token);
    sessionStorage.setItem("authUser", JSON.stringify(result.user));
    showMessage(`Signed in successfully${result.user.name ? `, ${result.user.name}` : ""}.`, "success");
  } catch (error) {
    showMessage(
      error instanceof TypeError
        ? "Could not reach the login server. Make sure the backend is running on port 5000."
        : error.message,
      "error",
    );
  } finally {
    submitButton.disabled = false;
    submitButton.removeAttribute("aria-busy");
    buttonLabel.textContent = "Sign in";
  }
});
