(() => {
  const projectId = "yktfgqpcmdgtycnyxpby";
  const publicAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlrdGZncXBjbWRndHljbnl4cGJ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkzMTAyMTIsImV4cCI6MjA3NDg4NjIxMn0.SVPPUbJXcyOuuc7_uXkKEyCoXIMpF9TaN-xS5Hyr170";
  const base = `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62`;
  const backendBase = "http://localhost:8080";

  function qs(sel) { return document.querySelector(sel); }
  function setError(msg) {
    const box = qs("[data-error]");
    if (!box) return;
    box.textContent = msg || "";
    box.style.display = msg ? "block" : "none";
  }
  function saveSession(accessToken, user) {
    try {
      localStorage.setItem("access_token", accessToken || "");
      localStorage.setItem("user", JSON.stringify(user || null));
    } catch {}
  }
  function getUser() {
    try {
      const txt = localStorage.getItem("user");
      return txt ? JSON.parse(txt) : null;
    } catch { return null; }
  }
  function getToken() {
    try { return localStorage.getItem("access_token") || ""; } catch { return ""; }
  }
  function clearSession() {
    try {
      localStorage.removeItem("access_token");
      localStorage.removeItem("user");
    } catch {}
  }

  async function fetchJSON(url, opts) {
    const res = await fetch(url, opts);
    let data = null;
    try { data = await res.json(); } catch {}
    return { ok: res.ok, status: res.status, data };
  }

  async function handlePostLoginFlow(accessToken, user) {
    saveSession(accessToken, user);
    if (!user || !user.role) {
      window.location.href = "dashboard.html";
      return;
    }
    if (user.role === "driver") {
      const r = await fetchJSON(`${base}/vehicles`, {
        headers: { "Authorization": `Bearer ${accessToken}` }
      });
      const vehicles = r?.data?.vehicles || [];
      if (!vehicles || vehicles.length === 0) {
        window.location.href = "vehicle-registration.html";
        return;
      }
    }
    window.location.href = "dashboard.html";
  }

  // Login page
  const loginForm = qs("form[data-login]");
  if (loginForm) {
    // Prefill email from query string if present
    try {
      const url = new URL(window.location.href);
      const preEmail = url.searchParams.get("email");
      if (preEmail) {
        const emailInput = qs("#email");
        if (emailInput) emailInput.value = preEmail;
      }
    } catch {}
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      setError("");
      const email = qs("#email")?.value?.trim();
      const password = qs("#password")?.value || "";
      if (!email || !password) { setError("Vui lòng nhập đầy đủ thông tin"); return; }
      const r = await fetchJSON(`${backendBase}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });
      if (r.ok && r.data?.success) {
        // Lưu phiên cục bộ để dashboard nhận ra trạng thái đăng nhập
        const user = { email, role: "user" };
        saveSession("local", user);
        window.location.href = "dashboard.html";
      } else {
        setError(r.data?.error || "Đăng nhập thất bại");
      }
    });
  }

  // Register page
  const registerForm = qs("form[data-register]");
  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      setError("");
      const full_name = qs("#fullName")?.value?.trim();
      const email = qs("#email")?.value?.trim();
      const phone = qs("#phone")?.value?.trim();
      const password = qs("#password")?.value || "";
      if (!full_name || !email || !phone || !password) { setError("Vui lòng nhập đầy đủ thông tin"); return; }
      const r = await fetchJSON(`${backendBase}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, fullName: full_name, phone })
      });
      if (r.ok && r.data?.success) {
        // chuyển đến trang verify của FE, prefill email qua query string
        window.location.href = `verify.html?email=${encodeURIComponent(email)}`;
      } else {
        const errMsg = r.data?.error || "Đăng ký thất bại";
        setError(errMsg);
        if (/exist|đã tồn tại/i.test(errMsg)) {
          setTimeout(() => {
            window.location.href = `login.html?email=${encodeURIComponent(email)}`;
          }, 1200);
        }
      }
    });
  }

  // Verify page
  const verifyForm = qs("form[data-verify]");
  if (verifyForm) {
    // Prefill email from query string if present
    try {
      const url = new URL(window.location.href);
      const preEmail = url.searchParams.get("email");
      if (preEmail) {
        const emailInput = qs("#email");
        if (emailInput) emailInput.value = preEmail;
      }
    } catch {}
    verifyForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      setError("");
      const email = qs("#email")?.value?.trim();
      const otp = qs("#otp")?.value?.trim();
      if (!email || !otp) { setError("Vui lòng nhập đầy đủ thông tin"); return; }
      const r = await fetchJSON(`${backendBase}/api/auth/verify`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, otp })
      });
      if (r.ok && r.data?.success) {
        // Xác minh thành công, chuyển đến trang login với email đã điền sẵn
        window.location.href = `login.html?email=${encodeURIComponent(email)}`;
      } else {
        setError(r.data?.error || "Xác minh thất bại");
      }
    });
  }

  // Vehicle registration page
  const vehicleForm = qs("form[data-vehicle]");
  if (vehicleForm) {
    vehicleForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      setError("");
      const email = qs("#email")?.value?.trim();
      const model = qs("#model")?.value?.trim();
      const vin = qs("#vin")?.value?.trim();
      const license_plate = qs("#licensePlate")?.value?.trim();
      if (!email || !model || !vin || !license_plate) { setError("Vui lòng nhập đầy đủ thông tin xe"); return; }
      const r = await fetchJSON(`${backendBase}/api/vehicles`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, model, vin, licensePlate: license_plate })
      });
      if (r.ok && r.data?.success) {
        window.location.href = "dashboard.html";
      } else {
        setError(r.data?.error || "Đăng ký xe thất bại");
      }
    });
  }

  // Dashboard page
  const logoutBtn = qs("[data-logout]");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      clearSession();
      window.location.href = "landing.html";
    });
  }
  const meBox = qs("[data-me]");
  if (meBox) {
    (async () => {
      const user = getUser();
      const token = getToken();
      if (user) {
        meBox.textContent = `${user.fullName || user.email} (${user.role || "user"})`;
        return;
      }
      if (!token) { window.location.href = "login.html"; return; }
      const r = await fetchJSON(`${base}/me`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (r.ok && r.data?.user) {
        meBox.textContent = `${r.data.user.name || r.data.user.email} (${r.data.user.role || "user"})`;
      } else {
        clearSession();
        window.location.href = "login.html";
      }
    })();
  }
})();


