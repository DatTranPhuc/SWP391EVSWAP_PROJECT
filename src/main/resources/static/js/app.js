(() => {
    const API = {
        login: '/api/auth/login',
        logout: '/api/auth/logout',
        register: '/api/auth/register',
        verifyOtp: '/api/auth/verify-otp',
        dashboard: '/api/dashboard',
        dashboardAction: '/api/dashboard/action',
        vehicleOverview: (driverId) => `/api/drivers/${driverId}/vehicles/overview`,
        addVehicle: (driverId) => `/api/drivers/${driverId}/vehicles`
    };

    const STORAGE_KEYS = {
        driverId: 'driverId',
        driverName: 'driverName',
        loginSuccess: 'loginSuccess',
        logoutMessage: 'logoutMessage',
        loginRequired: 'loginRequired',
        registerSuccess: 'registerSuccess',
        verifyEmail: 'verifyEmail',
        pendingFullName: 'pendingFullName',
        vehicleVerifyMessage: 'vehicleVerifyMessage',
        vehicleManageSuccess: 'vehicleManageSuccess',
        vehicleManageError: 'vehicleManageError'
    };

    const removeClasses = ['alert-success', 'alert-error', 'alert-warning', 'success', 'error', 'warning'];

    function safeBaseClass(element) {
        if (!element) {
            return '';
        }
        if (element.dataset.baseClass) {
            return element.dataset.baseClass;
        }
        const className = element.getAttribute('data-base-class') || element.className || '';
        element.dataset.baseClass = className;
        return className;
    }

    function setMessage(element, message, type = 'info') {
        if (!element) {
            return;
        }
        const baseClass = safeBaseClass(element);
        const classes = baseClass.split(/\s+/).filter(Boolean).filter((cls) => !removeClasses.includes(cls));
        if (!message) {
            element.className = classes.join(' ');
            element.textContent = '';
            element.hidden = true;
            return;
        }

        const hasAlertBase = classes.includes('alert') || classes.includes('alert-message');
        if (type === 'success') {
            classes.push(hasAlertBase ? 'alert-success' : 'success');
        } else if (type === 'error') {
            classes.push(hasAlertBase ? 'alert-error' : 'error');
        } else if (type === 'warning') {
            classes.push(hasAlertBase ? 'alert-warning' : 'warning');
        }

        element.className = classes.join(' ');
        element.textContent = message;
        element.hidden = false;
    }

    async function requestJson(url, options = {}) {
        const config = {
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            },
            ...options
        };
        if (config.body && typeof config.body !== 'string') {
            config.body = JSON.stringify(config.body);
        }
        try {
            const response = await fetch(url, config);
            const data = await parseJson(response);
            return { response, data };
        } catch (error) {
            return { response: { ok: false, status: 0 }, data: null, error };
        }
    }

    async function parseJson(response) {
        try {
            const text = await response.text();
            if (!text) {
                return {};
            }
            return JSON.parse(text);
        } catch (error) {
            return {};
        }
    }

    function setStoredDriver(driverId, fullName) {
        if (driverId !== undefined && driverId !== null) {
            sessionStorage.setItem(STORAGE_KEYS.driverId, String(driverId));
        }
        if (fullName) {
            sessionStorage.setItem(STORAGE_KEYS.driverName, fullName);
        }
    }

    function clearStoredDriver() {
        sessionStorage.removeItem(STORAGE_KEYS.driverId);
        sessionStorage.removeItem(STORAGE_KEYS.driverName);
    }

    function getStoredDriverId() {
        return sessionStorage.getItem(STORAGE_KEYS.driverId);
    }

    function getStoredDriverName() {
        return sessionStorage.getItem(STORAGE_KEYS.driverName) || sessionStorage.getItem(STORAGE_KEYS.pendingFullName) || '';
    }

    async function handleLogout(redirectTo = '/login.html') {
        await requestJson(API.logout, { method: 'POST' });
        clearStoredDriver();
        sessionStorage.setItem(STORAGE_KEYS.logoutMessage, 'Bạn đã đăng xuất thành công.');
        window.location.href = redirectTo;
    }

    function toggleLoading(button, isLoading, loadingText) {
        if (!button) {
            return;
        }
        if (isLoading) {
            button.dataset.originalText = button.textContent;
            button.disabled = true;
            if (loadingText) {
                button.textContent = loadingText;
            }
        } else {
            button.disabled = false;
            if (button.dataset.originalText) {
                button.textContent = button.dataset.originalText;
                delete button.dataset.originalText;
            }
        }
    }

    function formatDateTime(value) {
        if (!value) {
            return 'Chưa cập nhật';
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return 'Chưa cập nhật';
        }
        return new Intl.DateTimeFormat('vi-VN', {
            dateStyle: 'short',
            timeStyle: 'short'
        }).format(date);
    }

    function updateAuthVisibility(root, loggedIn, driverName) {
        if (!root) {
            root = document;
        }
        const authBlocks = root.querySelectorAll('[data-auth-state]');
        authBlocks.forEach((block) => {
            const state = block.dataset.authState;
            if (state === 'logged-in') {
                block.hidden = !loggedIn;
            } else if (state === 'logged-out') {
                block.hidden = loggedIn;
            }
        });
        const nameTarget = root.querySelector('#dashboardDriverName');
        if (nameTarget && driverName) {
            nameTarget.textContent = driverName;
        }
    }

    function attachLogoutHandler(button, redirectTo) {
        if (button) {
            button.addEventListener('click', (event) => {
                event.preventDefault();
                handleLogout(redirectTo);
            });
        }
    }

    function consumeSessionMessage(key, target, type = 'info') {
        const message = sessionStorage.getItem(key);
        if (message) {
            setMessage(target, message, type);
            sessionStorage.removeItem(key);
        }
    }

    function initLoginPage() {
        const form = document.getElementById('loginForm');
        const messageEl = document.getElementById('loginMessage');

        consumeSessionMessage(STORAGE_KEYS.logoutMessage, messageEl, 'success');
        consumeSessionMessage(STORAGE_KEYS.loginRequired, messageEl, 'warning');
        consumeSessionMessage(STORAGE_KEYS.registerSuccess, messageEl, 'success');

        if (!form) {
            return;
        }

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            setMessage(messageEl, '');

            const email = form.email.value.trim();
            const password = form.password.value;
            if (!email || !password) {
                setMessage(messageEl, 'Vui lòng nhập email và mật khẩu.', 'error');
                return;
            }

            const submitButton = form.querySelector('button[type="submit"]');
            toggleLoading(submitButton, true, 'Đang đăng nhập...');
            const { response, data } = await requestJson(API.login, {
                method: 'POST',
                body: { email, password }
            });

            if (!response.ok) {
                const errorMessage = data?.error || 'Đăng nhập thất bại. Vui lòng thử lại.';
                setMessage(messageEl, errorMessage, 'error');
            } else {
                setStoredDriver(data.driverId, data.fullName);
                sessionStorage.setItem(STORAGE_KEYS.loginSuccess, data.message || 'Đăng nhập thành công!');
                window.location.href = '/dashboard.html';
            }
            toggleLoading(submitButton, false);
        });
    }

    function initRegisterPage() {
        const form = document.getElementById('registerForm');
        const messageEl = document.getElementById('registerMessage');
        if (!form) {
            return;
        }

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            setMessage(messageEl, '');

            const fullName = form.fullName.value.trim();
            const email = form.email.value.trim();
            const password = form.password.value;
            const phoneRaw = form.phone.value.trim();
            const phone = phoneRaw.length === 0 ? null : phoneRaw;

            if (!fullName || !email || !password) {
                setMessage(messageEl, 'Vui lòng nhập đầy đủ thông tin bắt buộc.', 'error');
                return;
            }

            const submitButton = form.querySelector('button[type="submit"]');
            toggleLoading(submitButton, true, 'Đang đăng ký...');
            const { response, data } = await requestJson(API.register, {
                method: 'POST',
                body: { fullName, email, password, phone }
            });

            if (!response.ok) {
                const errorMessage = data?.error || 'Đăng ký thất bại. Vui lòng thử lại.';
                setMessage(messageEl, errorMessage, 'error');
            } else {
                sessionStorage.setItem(STORAGE_KEYS.registerSuccess, data.message || 'Đăng ký thành công!');
                sessionStorage.setItem(STORAGE_KEYS.verifyEmail, data.email || email);
                sessionStorage.setItem(STORAGE_KEYS.pendingFullName, data.fullName || fullName);
                window.location.href = '/verify.html';
            }
            toggleLoading(submitButton, false);
        });
    }

    function initVerifyPage() {
        const form = document.getElementById('verifyForm');
        const messageEl = document.getElementById('verifyMessage');
        if (!form) {
            return;
        }

        const emailInput = form.email;
        const storedEmail = sessionStorage.getItem(STORAGE_KEYS.verifyEmail);
        if (storedEmail) {
            emailInput.value = storedEmail;
        }
        consumeSessionMessage(STORAGE_KEYS.registerSuccess, messageEl, 'success');

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            setMessage(messageEl, '');

            const email = emailInput.value.trim();
            const otp = form.otp.value.trim();
            if (!email || otp.length !== 6) {
                setMessage(messageEl, 'Vui lòng nhập email và mã OTP gồm 6 chữ số.', 'error');
                return;
            }

            const submitButton = form.querySelector('button[type="submit"]');
            toggleLoading(submitButton, true, 'Đang xác minh...');
            const { response, data } = await requestJson(API.verifyOtp, {
                method: 'POST',
                body: { email, otp }
            });

            if (!response.ok) {
                const errorMessage = data?.error || 'Xác minh thất bại. Vui lòng thử lại.';
                setMessage(messageEl, errorMessage, 'error');
                if (data?.email) {
                    emailInput.value = data.email;
                }
            } else {
                setStoredDriver(data.driverId, data.fullName || getStoredDriverName());
                sessionStorage.setItem(STORAGE_KEYS.vehicleVerifyMessage, data.message || 'Xác minh thành công.');
                sessionStorage.removeItem(STORAGE_KEYS.verifyEmail);
                sessionStorage.removeItem(STORAGE_KEYS.pendingFullName);
                window.location.href = '/vehicle-register.html';
            }
            toggleLoading(submitButton, false);
        });
    }

    function initDashboardPage() {
        const root = document.body;
        const messageEl = document.getElementById('dashboardMessage');
        const logoutButton = document.getElementById('logoutButton');
        attachLogoutHandler(logoutButton, '/login.html');

        consumeSessionMessage(STORAGE_KEYS.loginSuccess, messageEl, 'success');

        const navButtons = Array.from(document.querySelectorAll('#dashboardNav [data-feature]'));
        let isLoggedIn = false;
        let driverName = getStoredDriverName();

        requestJson(API.dashboard).then(({ response, data }) => {
            if (response.ok && data) {
                isLoggedIn = Boolean(data.loggedIn);
                driverName = data.driverName || driverName;
                updateAuthVisibility(root, isLoggedIn, driverName);
                if (isLoggedIn && driverName) {
                    setStoredDriver(getStoredDriverId(), driverName);
                }
            } else {
                updateAuthVisibility(root, false, driverName);
            }
        });

        navButtons.forEach((button) => {
            button.addEventListener('click', async () => {
                const requiresAuth = button.dataset.requiresAuth === 'true';
                const feature = button.dataset.feature;
                if (requiresAuth && !isLoggedIn) {
                    setMessage(messageEl, 'Vui lòng đăng nhập để sử dụng chức năng này.', 'warning');
                    return;
                }
                const { response, data } = await requestJson(API.dashboardAction, {
                    method: 'POST',
                    body: { feature }
                });
                if (!response.ok) {
                    const errorMessage = data?.message || 'Không thể xử lý yêu cầu. Vui lòng thử lại sau.';
                    setMessage(messageEl, errorMessage, 'error');
                    return;
                }
                if (data?.redirect) {
                    if (data.redirect.includes('/vehicles/overview')) {
                        window.location.href = '/vehicle-manage.html';
                        return;
                    }
                    if (data.redirect.includes('/dashboard')) {
                        window.location.href = '/dashboard.html';
                        return;
                    }
                }
                const infoMessage = data?.message || `Bạn đã chọn chức năng: ${feature}.`;
                setMessage(messageEl, infoMessage, 'success');
            });
        });
    }

    function initVehicleRegisterPage() {
        const form = document.getElementById('vehicleRegisterForm');
        const messageEl = document.getElementById('vehicleRegisterMessage');
        const greetingEl = document.getElementById('vehicleRegisterGreeting');
        const logoutPrimary = document.getElementById('vehicleRegisterLogout');
        const logoutSecondary = document.getElementById('vehicleRegisterLogoutSecondary');

        attachLogoutHandler(logoutPrimary, '/login.html');
        attachLogoutHandler(logoutSecondary, '/login.html');

        if (!form) {
            return;
        }

        consumeSessionMessage(STORAGE_KEYS.vehicleVerifyMessage, messageEl, 'success');
        const driverId = getStoredDriverId();
        const driverName = getStoredDriverName();
        if (greetingEl && driverName) {
            greetingEl.textContent = `Xin chào ${driverName}, hãy cung cấp thông tin xe để đồng bộ với hệ thống EV SWAP.`;
        }
        if (!driverId) {
            sessionStorage.setItem(STORAGE_KEYS.loginRequired, 'Vui lòng đăng nhập trước khi đăng ký phương tiện.');
            window.location.href = '/login.html';
            return;
        }

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            setMessage(messageEl, '');

            const payload = {
                model: form.model.value.trim(),
                vin: form.vin.value.trim(),
                plateNumber: form.plateNumber.value.trim()
            };
            if (!payload.model || !payload.vin) {
                setMessage(messageEl, 'Model xe và số VIN là bắt buộc.', 'error');
                return;
            }
            if (!payload.plateNumber) {
                delete payload.plateNumber;
            }

            const submitButton = form.querySelector('button[type="submit"]');
            toggleLoading(submitButton, true, 'Đang lưu...');
            const { response, data } = await requestJson(API.addVehicle(driverId), {
                method: 'POST',
                body: payload
            });
            if (!response.ok) {
                const errorMessage = data?.error || 'Không thể đăng ký phương tiện. Vui lòng thử lại.';
                setMessage(messageEl, errorMessage, 'error');
            } else {
                sessionStorage.setItem(STORAGE_KEYS.vehicleManageSuccess, 'Thêm phương tiện thành công!');
                window.location.href = '/vehicle-manage.html';
            }
            toggleLoading(submitButton, false);
        });
    }

    function initVehicleManagePage() {
        const driverId = getStoredDriverId();
        if (!driverId) {
            sessionStorage.setItem(STORAGE_KEYS.loginRequired, 'Vui lòng đăng nhập để quản lý phương tiện.');
            window.location.href = '/login.html';
            return;
        }

        const successEl = document.getElementById('vehicleManageSuccess');
        const errorEl = document.getElementById('vehicleManageError');
        const emptyState = document.getElementById('vehicleEmptyState');
        const cardsContainer = document.getElementById('vehicleCardsContainer');
        const template = document.getElementById('vehicleCardTemplate');
        const nameEl = document.getElementById('manageDriverName');
        const initialEl = document.getElementById('manageDriverInitial');
        const totalEl = document.getElementById('totalVehicles');
        const lastUpdatedEl = document.getElementById('lastUpdatedAt');
        const openButtons = [
            document.getElementById('openVehicleModalButton'),
            document.getElementById('emptyStateAddButton')
        ];
        const modal = document.getElementById('vehicleModal');
        const modalBackdrop = document.getElementById('modalBackdrop');
        const closeModalBtn = document.getElementById('closeVehicleModal');
        const cancelModalBtn = document.getElementById('cancelVehicleModal');
        const form = document.getElementById('vehicleManageForm');

        consumeSessionMessage(STORAGE_KEYS.vehicleManageSuccess, successEl, 'success');
        consumeSessionMessage(STORAGE_KEYS.vehicleManageError, errorEl, 'error');

        const driverName = getStoredDriverName();
        if (nameEl && driverName) {
            nameEl.textContent = driverName;
        }
        if (initialEl && driverName) {
            initialEl.textContent = driverName.trim().charAt(0).toUpperCase();
        }

        openButtons.forEach((btn) => {
            if (btn) {
                btn.addEventListener('click', () => openVehicleModal());
            }
        });
        if (modalBackdrop) {
            modalBackdrop.addEventListener('click', () => closeVehicleModal());
        }
        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', () => closeVehicleModal());
        }
        if (cancelModalBtn) {
            cancelModalBtn.addEventListener('click', () => closeVehicleModal());
        }
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && modal?.classList.contains('is-open')) {
                closeVehicleModal();
            }
        });

        if (form) {
            form.addEventListener('submit', async (event) => {
                event.preventDefault();
                setMessage(successEl, '');
                setMessage(errorEl, '');

                const formData = new FormData(form);
                const payload = {
                    model: formData.get('model')?.toString().trim() || '',
                    vin: formData.get('vin')?.toString().trim() || '',
                    plateNumber: formData.get('plateNumber')?.toString().trim() || ''
                };
                if (!payload.model || !payload.vin) {
                    setMessage(errorEl, 'Model xe và số VIN là bắt buộc.', 'error');
                    return;
                }
                if (!payload.plateNumber) {
                    delete payload.plateNumber;
                }

                const submitButton = form.querySelector('button[type="submit"]');
                toggleLoading(submitButton, true, 'Đang lưu...');
                const { response, data } = await requestJson(API.addVehicle(driverId), {
                    method: 'POST',
                    body: payload
                });
                toggleLoading(submitButton, false);

                if (!response.ok) {
                    const errorMessage = data?.error || 'Không thể thêm phương tiện. Vui lòng thử lại.';
                    setMessage(errorEl, errorMessage, 'error');
                    return;
                }

                setMessage(successEl, 'Thêm phương tiện thành công!', 'success');
                form.reset();
                closeVehicleModal();
                await loadOverview();
            });
        }

        async function loadOverview() {
            const { response, data } = await requestJson(API.vehicleOverview(driverId));
            if (!response.ok || !data) {
                setMessage(errorEl, data?.error || 'Không thể tải dữ liệu phương tiện.', 'error');
                return;
            }

            const driverNameResp = data.driverName || driverName;
            if (nameEl && driverNameResp) {
                nameEl.textContent = driverNameResp;
            }
            if (initialEl && driverNameResp) {
                initialEl.textContent = driverNameResp.trim().charAt(0).toUpperCase();
            }
            if (driverNameResp) {
                setStoredDriver(driverId, driverNameResp);
            }

            if (totalEl) {
                totalEl.textContent = data.totalVehicles ?? 0;
            }
            if (lastUpdatedEl) {
                lastUpdatedEl.textContent = formatDateTime(data.lastUpdatedAt);
            }

            const cards = Array.isArray(data.vehicleCards) ? data.vehicleCards : [];
            renderVehicleCards(cards);
        }

        function renderVehicleCards(cards) {
            if (!template || !cardsContainer || !emptyState) {
                return;
            }
            cardsContainer.innerHTML = '';
            if (cards.length === 0) {
                cardsContainer.hidden = true;
                emptyState.hidden = false;
                return;
            }

            cards.forEach((card) => {
                const fragment = template.content.cloneNode(true);
                const nameField = fragment.querySelector('.vehicle-name');
                const statusBadge = fragment.querySelector('.status-badge');
                const plateValue = fragment.querySelector('.plate-value');
                const batteryModel = fragment.querySelector('.battery-meta .meta-value');
                const batteryStatus = fragment.querySelectorAll('.battery-meta .meta-value')[1];
                const progressFill = fragment.querySelector('.progress-fill');
                const progressValue = fragment.querySelector('.progress-value');
                const progressStatus = fragment.querySelector('.progress-status');
                const progressCaption = fragment.querySelector('.progress-caption');
                const detailValues = fragment.querySelectorAll('.vehicle-details .detail-value');

                if (nameField) {
                    nameField.textContent = card.vehicleName || 'Phương tiện';
                }
                if (statusBadge) {
                    statusBadge.textContent = card.statusLabel || 'Đang hoạt động';
                    statusBadge.className = `status-badge ${card.statusBadge || ''}`.trim();
                }
                if (plateValue) {
                    plateValue.textContent = card.plateNumber || 'Chưa cập nhật';
                }
                if (batteryModel) {
                    batteryModel.textContent = card.batteryModel || 'EVS Pack 48V';
                }
                if (batteryStatus) {
                    batteryStatus.textContent = card.batteryStatus || 'Đang sử dụng';
                }
                if (progressFill) {
                    const percent = Number.isFinite(card.batteryPercent) ? card.batteryPercent : 0;
                    progressFill.style.width = `${Math.min(Math.max(percent, 0), 100)}%`;
                }
                if (progressValue) {
                    const percent = Number.isFinite(card.batteryPercent) ? card.batteryPercent : 0;
                    progressValue.textContent = `${percent}%`;
                }
                if (progressStatus) {
                    progressStatus.textContent = card.healthLabel || 'Tình trạng tốt';
                }
                if (progressCaption) {
                    progressCaption.textContent = card.healthDescription || '';
                }
                if (detailValues.length >= 3) {
                    detailValues[0].textContent = card.model || 'Chưa cập nhật';
                    detailValues[1].textContent = card.vin || 'Chưa cập nhật';
                    detailValues[2].textContent = formatDateTime(card.createdAt);
                }
                cardsContainer.appendChild(fragment);
            });
            cardsContainer.hidden = false;
            emptyState.hidden = true;
        }

        function openVehicleModal() {
            if (!modal) {
                return;
            }
            modal.setAttribute('aria-hidden', 'false');
            modal.classList.add('is-open');
            document.body.classList.add('modal-open');
            const firstInput = modal.querySelector('input');
            if (firstInput) {
                setTimeout(() => firstInput.focus(), 100);
            }
        }

        function closeVehicleModal() {
            if (!modal) {
                return;
            }
            modal.setAttribute('aria-hidden', 'true');
            modal.classList.remove('is-open');
            document.body.classList.remove('modal-open');
        }

        loadOverview();
    }

    const api = {
        initLoginPage,
        initRegisterPage,
        initVerifyPage,
        initDashboardPage,
        initVehicleRegisterPage,
        initVehicleManagePage,
        handleLogout
    };

    window.evswap = Object.assign(window.evswap || {}, api);
})();
