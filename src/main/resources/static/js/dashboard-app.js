const appRoot = document.querySelector('[data-dashboard-app]');

if (appRoot) {
    const loader = appRoot.querySelector('[data-dashboard-loader]');
    const content = appRoot.querySelector('[data-dashboard-content]');
    const errorBox = appRoot.querySelector('[data-dashboard-error]');
    const refreshButton = appRoot.querySelector('[data-refresh-dashboard]');
    const reservationForm = appRoot.querySelector('[data-reservation-form]');
    const reservationFeedback = appRoot.querySelector('[data-reservation-feedback]');
    const supportForm = appRoot.querySelector('[data-support-form]');
    const supportFeedback = appRoot.querySelector('[data-support-feedback]');
    const stationSelect = appRoot.querySelector('[data-reservation-station]');
    const stationList = appRoot.querySelector('[data-station-list]');
    const reportList = appRoot.querySelector('[data-report-list]');
    const vehiclesBody = appRoot.querySelector('[data-vehicles-body]');
    const reservationsBody = appRoot.querySelector('[data-reservations-body]');
    const swapsBody = appRoot.querySelector('[data-swaps-body]');
    const paymentsBody = appRoot.querySelector('[data-payments-body]');
    const ticketsBody = appRoot.querySelector('[data-tickets-body]');
    const notificationList = appRoot.querySelector('[data-notification-list]');
    const markAllReadButton = appRoot.querySelector('[data-mark-all-read]');
    const metrics = {
        vehicles: appRoot.querySelector('[data-metric="vehicles"]'),
        reservations: appRoot.querySelector('[data-metric="reservations"]'),
        swaps: appRoot.querySelector('[data-metric="swaps"]'),
        totalPaid: appRoot.querySelector('[data-metric="totalPaid"]'),
        unread: appRoot.querySelector('[data-metric="unread"]')
    };
    const nextReservationCard = appRoot.querySelector('[data-next-reservation]');
    const lastPaymentCard = appRoot.querySelector('[data-last-payment]');

    const sections = Array.from(appRoot.querySelectorAll('.app-section'));
    const navLinks = Array.from(document.querySelectorAll('[data-nav-target]'));
    const observer = new IntersectionObserver(handleSectionObserve, {
        threshold: 0.35,
        rootMargin: '-80px 0px -40px 0px'
    });

    const state = {
        snapshot: null,
        loading: false
    };

    sections.forEach(section => observer.observe(section));

    navLinks.forEach(link => {
        link.addEventListener('click', event => {
            const targetId = link.dataset.navTarget;
            if (!targetId) {
                return;
            }
            const section = document.getElementById(targetId);
            if (section) {
                event.preventDefault();
                section.scrollIntoView({ behavior: 'smooth', block: 'start' });
                setActiveNav(targetId);
            }
        });
    });

    if (refreshButton) {
        refreshButton.addEventListener('click', () => loadDashboard({ showLoader: true }));
    }

    if (reservationForm) {
        reservationForm.addEventListener('submit', async event => {
            event.preventDefault();
            clearFeedback(reservationFeedback);
            const formData = new FormData(reservationForm);
            const payload = {
                driverId: Number(formData.get('driverId')),
                stationId: Number(formData.get('stationId')),
                reservedStart: toIsoString(formData.get('reservedStart'))
            };

            if (!payload.stationId || !payload.reservedStart) {
                showFeedback(reservationFeedback, 'Vui lòng chọn trạm và thời gian hợp lệ.', false);
                return;
            }

            try {
                await postJson('/api/reservations', payload);
                showFeedback(reservationFeedback, 'Đặt lịch đổi pin thành công! Hệ thống đã gửi thông báo cho bạn.', true);
                reservationForm.reset();
                await loadDashboard({ showLoader: false });
            } catch (error) {
                showFeedback(reservationFeedback, error.message || 'Không thể đặt lịch. Vui lòng thử lại.', false);
            }
        });

        const resetButton = reservationForm.querySelector('[data-reset-reservation]');
        if (resetButton) {
            resetButton.addEventListener('click', () => {
                reservationForm.reset();
                clearFeedback(reservationFeedback);
            });
        }
    }

    if (supportForm) {
        supportForm.addEventListener('submit', async event => {
            event.preventDefault();
            clearFeedback(supportFeedback);
            const formData = new FormData(supportForm);
            const payload = {
                driverId: Number(formData.get('driverId')),
                category: formData.get('category'),
                comment: (formData.get('comment') || '').trim()
            };

            if (!payload.comment) {
                showFeedback(supportFeedback, 'Vui lòng mô tả chi tiết vấn đề bạn gặp phải.', false);
                return;
            }

            try {
                await postJson('/api/support', payload);
                showFeedback(supportFeedback, 'Yêu cầu hỗ trợ đã được gửi. Đội ngũ EV SWAP sẽ phản hồi sớm nhất.', true);
                supportForm.reset();
                await loadDashboard({ showLoader: false });
            } catch (error) {
                showFeedback(supportFeedback, error.message || 'Không thể gửi yêu cầu hỗ trợ. Vui lòng thử lại.', false);
            }
        });

        const resetSupport = supportForm.querySelector('[data-reset-support]');
        if (resetSupport) {
            resetSupport.addEventListener('click', () => {
                supportForm.reset();
                clearFeedback(supportFeedback);
            });
        }
    }

    if (markAllReadButton) {
        markAllReadButton.addEventListener('click', async () => {
            if (!state.snapshot || !state.snapshot.notifications) {
                return;
            }
            const unreadIds = state.snapshot.notifications
                .filter(notification => !notification.read)
                .map(notification => notification.notificationId)
                .filter(Boolean);
            if (!unreadIds.length) {
                showTemporaryToast('Bạn đã đọc tất cả thông báo.');
                return;
            }
            try {
                for (const id of unreadIds) {
                    await markNotificationAsRead(id);
                }
                await loadDashboard({ showLoader: false });
                showTemporaryToast('Đã đánh dấu tất cả thông báo là đã đọc.');
            } catch (error) {
                showTemporaryToast(error.message || 'Không thể cập nhật thông báo.', false);
            }
        });
    }

    notificationList?.addEventListener('click', async event => {
        const button = event.target.closest('[data-mark-read]');
        if (!button) {
            return;
        }
        const notificationId = Number(button.dataset.markRead);
        button.disabled = true;
        try {
            await markNotificationAsRead(notificationId);
            await loadDashboard({ showLoader: false });
        } catch (error) {
            showTemporaryToast(error.message || 'Không thể đánh dấu thông báo.', false);
        } finally {
            button.disabled = false;
        }
    });

    loadDashboard({ showLoader: true });

    async function loadDashboard({ showLoader }) {
        if (state.loading) {
            return;
        }
        state.loading = true;
        hideError();
        if (showLoader && loader) {
            loader.hidden = false;
        }
        if (content) {
            content.hidden = true;
        }
        try {
            const response = await fetch('/api/dashboard', {
                headers: {
                    'Accept': 'application/json'
                }
            });
            if (response.status === 401) {
                throw new Error('Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.');
            }
            if (!response.ok) {
                throw new Error('Không thể tải dữ liệu bảng điều khiển.');
            }
            const snapshot = await response.json();
            state.snapshot = snapshot;
            updateDashboard(snapshot);
            if (content) {
                content.hidden = false;
            }
        } catch (error) {
            showError(error.message || 'Đã xảy ra lỗi không xác định.');
        } finally {
            state.loading = false;
            if (loader) {
                loader.hidden = true;
            }
        }
    }

    function updateDashboard(snapshot) {
        updateMetrics(snapshot.overview || {});
        updateNextReservation(snapshot.overview?.nextReservation);
        updateLastPayment(snapshot.overview?.lastPayment);
        renderVehicles(snapshot.vehicles || []);
        renderReservations(snapshot.reservations || []);
        renderSwaps(snapshot.swaps || []);
        renderPayments(snapshot.payments || []);
        renderTickets(snapshot.tickets || []);
        renderNotifications(snapshot.notifications || []);
        renderStations(snapshot.stations || []);
        populateStationOptions(snapshot.stations || []);
        renderReport(snapshot);
    }

    function updateMetrics(overview) {
        if (metrics.vehicles) metrics.vehicles.textContent = numberFormatter(overview.totalVehicles);
        if (metrics.reservations) metrics.reservations.textContent = numberFormatter(overview.upcomingReservations);
        if (metrics.swaps) metrics.swaps.textContent = numberFormatter(overview.completedSwaps);
        if (metrics.totalPaid) metrics.totalPaid.textContent = formatCurrency(overview.totalPaid);
        if (metrics.unread) metrics.unread.textContent = numberFormatter(overview.unreadNotifications);
    }

    function updateNextReservation(nextReservation) {
        if (!nextReservationCard) {
            return;
        }
        const empty = nextReservationCard.querySelector('.insight-empty');
        const details = nextReservationCard.querySelector('.insight-details');
        if (!nextReservation) {
            if (empty) empty.hidden = false;
            if (details) details.hidden = true;
            return;
        }
        if (empty) empty.hidden = true;
        if (details) {
            details.hidden = false;
            const station = details.querySelector('[data-field="station"]');
            const time = details.querySelector('[data-field="time"]');
            const status = details.querySelector('[data-field="status"]');
            if (station) station.textContent = nextReservation.stationName || 'Chưa rõ trạm';
            if (time) time.textContent = formatDateTime(nextReservation.reservedStart);
            if (status) status.textContent = reservationStatusLabel(nextReservation.status);
        }
    }

    function updateLastPayment(lastPayment) {
        if (!lastPaymentCard) {
            return;
        }
        const empty = lastPaymentCard.querySelector('.insight-empty');
        const details = lastPaymentCard.querySelector('.insight-details');
        if (!lastPayment) {
            if (empty) empty.hidden = false;
            if (details) details.hidden = true;
            return;
        }
        if (empty) empty.hidden = true;
        if (details) {
            details.hidden = false;
            const amount = details.querySelector('[data-field="amount"]');
            const method = details.querySelector('[data-field="method"]');
            const status = details.querySelector('[data-field="status"]');
            const time = details.querySelector('[data-field="time"]');
            if (amount) amount.textContent = formatCurrency(lastPayment.amount);
            if (method) method.textContent = normalizePaymentMethod(lastPayment.method);
            if (status) status.textContent = paymentStatusLabel(lastPayment.status);
            if (time) time.textContent = formatDateTime(lastPayment.paidAt);
        }
    }

    function renderVehicles(vehicles) {
        renderRows(vehiclesBody, vehicles, vehicle => {
            const tr = document.createElement('tr');
            tr.append(
                createCell(vehicle.vehicleId ?? '—'),
                createCell(vehicle.displayName || 'Chưa đặt tên'),
                createCell(vehicle.plateNumber || 'Chưa cập nhật'),
                createCell(vehicle.vin || '—'),
                createCell(formatDate(vehicle.createdAt))
            );
            return tr;
        });
    }

    function renderReservations(reservations) {
        const rows = reservations.slice(0, 6);
        renderRows(reservationsBody, rows, reservation => {
            const tr = document.createElement('tr');
            const statusInfo = reservationStatusInfo(reservation.status);
            tr.append(
                createCell(reservation.reservationId ?? '—'),
                createCell(reservation.stationName || 'Chưa xác định'),
                createCell(formatDateTime(reservation.reservedStart)),
                createStatusCell(statusInfo)
            );
            return tr;
        });
    }

    function renderSwaps(swaps) {
        const rows = swaps.slice(0, 6);
        renderRows(swapsBody, rows, swap => {
            const tr = document.createElement('tr');
            const statusInfo = swapResultInfo(swap.result);
            tr.append(
                createCell(swap.swapId ?? '—'),
                createCell(swap.stationName || 'Chưa xác định'),
                createCell(formatDateTime(swap.swappedAt)),
                createStatusCell(statusInfo)
            );
            return tr;
        });
    }

    function renderPayments(payments) {
        const rows = payments.slice(0, 6);
        renderRows(paymentsBody, rows, payment => {
            const tr = document.createElement('tr');
            const statusInfo = paymentStatusInfo(payment.status);
            tr.append(
                createCell(payment.paymentId ?? '—'),
                createCell(formatCurrency(payment.amount)),
                createCell(normalizePaymentMethod(payment.method)),
                createStatusCell(statusInfo),
                createCell(formatDateTime(payment.paidAt))
            );
            return tr;
        });
    }

    function renderTickets(tickets) {
        const rows = tickets.slice(0, 6);
        renderRows(ticketsBody, rows, ticket => {
            const tr = document.createElement('tr');
            const statusInfo = ticketStatusInfo(ticket.status);
            tr.append(
                createCell(ticket.ticketId ?? '—'),
                createCell(ticket.category || 'Khác'),
                createStatusCell(statusInfo),
                createCell(formatDateTime(ticket.createdAt))
            );
            return tr;
        });
    }

    function renderNotifications(notifications) {
        if (!notificationList) {
            return;
        }
        notificationList.innerHTML = '';
        if (!notifications.length) {
            notificationList.append(createEmptyItem('Bạn đã xem hết thông báo.'));
            return;
        }

        notifications.slice(0, 6).forEach(notification => {
            const li = document.createElement('li');
            if (!notification.read) {
                li.classList.add('unread');
            }
            const title = document.createElement('div');
            title.className = 'notification-title';
            title.textContent = notification.title || 'Thông báo hệ thống';

            const details = [];
            if (notification.type) {
                details.push(normalizeNotificationType(notification.type));
            }
            if (notification.sentAt) {
                details.push(formatDateTime(notification.sentAt));
            }
            const meta = document.createElement('div');
            meta.className = 'notification-meta';
            meta.textContent = details.join(' • ');

            li.append(title, meta);

            if (!notification.read) {
                const actions = document.createElement('div');
                actions.className = 'notification-actions';
                const markBtn = document.createElement('button');
                markBtn.type = 'button';
                markBtn.className = 'btn ghost btn--sm';
                markBtn.textContent = 'Đánh dấu đã đọc';
                markBtn.dataset.markRead = notification.notificationId;
                actions.append(markBtn);
                li.append(actions);
            }

            notificationList.append(li);
        });
    }

    function renderStations(stations) {
        if (!stationList) {
            return;
        }
        stationList.innerHTML = '';
        if (!stations.length) {
            stationList.append(createEmptyItem('Chưa có trạm nào khả dụng.'));
            return;
        }
        stations.forEach(station => {
            const li = document.createElement('li');
            const title = document.createElement('strong');
            title.textContent = station.name || `Trạm #${station.stationId}`;
            const address = document.createElement('span');
            address.textContent = station.address || 'Đang cập nhật địa chỉ';
            const status = document.createElement('span');
            status.textContent = `Trạng thái: ${stationStatusLabel(station.status)}`;
            li.append(title, address, status);
            stationList.append(li);
        });
    }

    function populateStationOptions(stations) {
        if (!stationSelect) {
            return;
        }
        const placeholder = stationSelect.querySelector('option[value=""]');
        stationSelect.innerHTML = '';
        if (placeholder) {
            stationSelect.append(placeholder);
        } else {
            const option = document.createElement('option');
            option.value = '';
            option.disabled = true;
            option.selected = true;
            option.textContent = 'Chọn trạm đổi pin';
            stationSelect.append(option);
        }
        stations.forEach(station => {
            const option = document.createElement('option');
            option.value = station.stationId;
            option.textContent = station.name ? `${station.name} • ${station.address || 'Chưa rõ địa chỉ'}` : `Trạm #${station.stationId}`;
            stationSelect.append(option);
        });
    }

    function renderReport(snapshot) {
        if (!reportList) {
            return;
        }
        reportList.innerHTML = '';
        const overview = snapshot.overview || {};
        const vehicles = overview.totalVehicles ?? 0;
        const reservations = overview.upcomingReservations ?? 0;
        const swaps = overview.completedSwaps ?? 0;
        const unread = overview.unreadNotifications ?? 0;
        const totalPaid = formatCurrency(overview.totalPaid);

        const items = [
            {
                title: `${vehicles} phương tiện đang liên kết`,
                detail: 'Mỗi phương tiện đã được xác thực để đổi pin trong hệ thống.'
            },
            {
                title: `${reservations} lịch đổi pin đang chờ`,
                detail: 'Luôn đến đúng giờ để giữ trạng thái ưu tiên tại trạm.'
            },
            {
                title: `${swaps} lần đổi pin đã hoàn tất gần đây`,
                detail: 'Giữ pin ở trạng thái tốt nhất bằng cách đổi định kỳ.'
            },
            {
                title: `${totalPaid} đã thanh toán trong 90 ngày`,
                detail: 'Theo dõi chi tiêu để quản lý chi phí nhiên liệu hiệu quả.'
            },
            {
                title: `${unread} thông báo chưa đọc`,
                detail: 'Đừng bỏ lỡ thông báo quan trọng về lịch hẹn và khuyến mãi.'
            }
        ];

        items.forEach(item => {
            const li = document.createElement('li');
            const title = document.createElement('strong');
            title.textContent = item.title;
            const detail = document.createElement('span');
            detail.textContent = item.detail;
            li.append(title, detail);
            reportList.append(li);
        });
    }

    function renderRows(tbody, items, buildRow) {
        if (!tbody) {
            return;
        }
        tbody.innerHTML = '';
        const data = Array.isArray(items) ? items : [];
        if (!data.length) {
            const tr = document.createElement('tr');
            tr.className = 'empty';
            const td = document.createElement('td');
            td.colSpan = columnCount(tbody);
            td.textContent = 'Chưa có dữ liệu.';
            tr.append(td);
            tbody.append(tr);
            return;
        }
        data.forEach(item => tbody.append(buildRow(item)));
    }

    function createCell(value) {
        const td = document.createElement('td');
        td.textContent = value ?? '—';
        return td;
    }

    function createStatusCell(info) {
        const td = document.createElement('td');
        const span = document.createElement('span');
        span.className = `status-badge status-badge--${info.tone}`;
        span.textContent = info.label;
        td.append(span);
        return td;
    }

    function createEmptyItem(message) {
        const li = document.createElement('li');
        li.textContent = message;
        return li;
    }

    function columnCount(tbody) {
        const table = tbody.closest('table');
        const headRow = table?.querySelector('thead tr');
        return headRow ? headRow.children.length : 1;
    }

    async function postJson(url, body) {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(body)
        });
        if (!response.ok) {
            const message = await extractErrorMessage(response);
            throw new Error(message);
        }
        return response.json().catch(() => null);
    }

    async function markNotificationAsRead(notificationId) {
        const response = await fetch(`/api/notifications/${notificationId}/read`, {
            method: 'PATCH',
            headers: {
                'Accept': 'application/json'
            }
        });
        if (!response.ok) {
            const message = await extractErrorMessage(response);
            throw new Error(message);
        }
        return response.json().catch(() => null);
    }

    async function extractErrorMessage(response) {
        try {
            const payload = await response.json();
            if (payload && typeof payload === 'object' && payload.message) {
                return payload.message;
            }
        } catch (ignored) {
            // response body không phải JSON
        }
        return response.status >= 400 && response.status < 500
            ? 'Yêu cầu không hợp lệ. Vui lòng kiểm tra lại dữ liệu.'
            : 'Máy chủ đang bận. Vui lòng thử lại sau.';
    }

    function showError(message) {
        if (!errorBox) {
            return;
        }
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function hideError() {
        if (errorBox) {
            errorBox.hidden = true;
            errorBox.textContent = '';
        }
    }

    function showFeedback(element, message, isSuccess) {
        if (!element) {
            return;
        }
        element.textContent = message;
        element.classList.toggle('success', Boolean(isSuccess));
        element.classList.toggle('error', !isSuccess);
        element.hidden = false;
    }

    function clearFeedback(element) {
        if (!element) {
            return;
        }
        element.textContent = '';
        element.classList.remove('success', 'error');
        element.hidden = true;
    }

    function showTemporaryToast(message, success = true) {
        const toast = document.createElement('div');
        toast.className = `dashboard-toast ${success ? 'dashboard-toast--success' : 'dashboard-toast--error'}`;
        toast.textContent = message;
        document.body.append(toast);
        requestAnimationFrame(() => toast.classList.add('is-visible'));
        setTimeout(() => {
            toast.classList.remove('is-visible');
            toast.addEventListener('transitionend', () => toast.remove(), { once: true });
        }, 2800);
    }

    function handleSectionObserve(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                setActiveNav(entry.target.id);
            }
        });
    }

    function setActiveNav(sectionId) {
        navLinks.forEach(link => {
            link.classList.toggle('active', link.dataset.navTarget === sectionId);
        });
    }

    function numberFormatter(value) {
        const num = Number(value ?? 0);
        if (Number.isNaN(num)) {
            return '0';
        }
        return num.toLocaleString('vi-VN');
    }

    function formatCurrency(value) {
        if (value == null) {
            return '0 ₫';
        }
        const amount = Number(value);
        if (Number.isNaN(amount)) {
            return '0 ₫';
        }
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            maximumFractionDigits: 0
        }).format(amount);
    }

    function formatDateTime(value) {
        if (!value) {
            return 'Chưa cập nhật';
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return date.toLocaleString('vi-VN', {
            dateStyle: 'short',
            timeStyle: 'short'
        });
    }

    function formatDate(value) {
        if (!value) {
            return '—';
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return date.toLocaleDateString('vi-VN');
    }

    function toIsoString(datetimeValue) {
        if (!datetimeValue) {
            return null;
        }
        const date = new Date(datetimeValue);
        if (Number.isNaN(date.getTime())) {
            return null;
        }
        return date.toISOString();
    }

    function reservationStatusInfo(status) {
        const map = {
            pending: { label: 'Chờ xác nhận', tone: 'pending' },
            confirmed: { label: 'Đã xác nhận', tone: 'confirmed' },
            completed: { label: 'Hoàn tất', tone: 'completed' },
            canceled: { label: 'Đã hủy', tone: 'canceled' },
            no_show: { label: 'Không đến', tone: 'dark' }
        };
        const key = (status || 'pending').toLowerCase();
        return map[key] || { label: status || 'Đang xử lý', tone: 'info' };
    }

    function reservationStatusLabel(status) {
        return reservationStatusInfo(status).label;
    }

    function swapResultInfo(result) {
        const map = {
            success: { label: 'Thành công', tone: 'completed' },
            completed: { label: 'Hoàn tất', tone: 'completed' },
            failed: { label: 'Thất bại', tone: 'canceled' }
        };
        const key = (result || '').toLowerCase();
        return map[key] || { label: result || 'Đang xử lý', tone: 'info' };
    }

    function paymentStatusInfo(status) {
        const map = {
            pending: { label: 'Chờ xử lý', tone: 'pending' },
            succeed: { label: 'Thành công', tone: 'completed' },
            failed: { label: 'Thất bại', tone: 'canceled' },
            refunded: { label: 'Đã hoàn tiền', tone: 'info' }
        };
        const key = (status || '').toLowerCase();
        return map[key] || { label: status || 'Không xác định', tone: 'dark' };
    }

    function paymentStatusLabel(status) {
        return paymentStatusInfo(status).label;
    }

    function normalizePaymentMethod(method) {
        if (!method) {
            return 'Chưa rõ phương thức';
        }
        const normalized = method.toLowerCase();
        if (normalized.includes('momo')) return 'MoMo';
        if (normalized.includes('zalo')) return 'ZaloPay';
        if (normalized.includes('cash')) return 'Tiền mặt';
        if (normalized.includes('card')) return 'Thẻ ngân hàng';
        return method;
    }

    function ticketStatusInfo(status) {
        const map = {
            open: { label: 'Đang mở', tone: 'pending' },
            in_progress: { label: 'Đang xử lý', tone: 'info' },
            resolved: { label: 'Đã giải quyết', tone: 'completed' },
            closed: { label: 'Đã đóng', tone: 'dark' }
        };
        const key = (status || '').toLowerCase();
        return map[key] || { label: status || 'Chưa rõ', tone: 'info' };
    }

    function stationStatusLabel(status) {
        if (!status) {
            return 'Hoạt động';
        }
        const normalized = status.toLowerCase();
        if (normalized === 'active') return 'Hoạt động';
        if (normalized === 'maintenance') return 'Bảo trì';
        if (normalized === 'inactive') return 'Tạm ngưng';
        return status;
    }

    function normalizeNotificationType(type) {
        const normalized = type.toLowerCase();
        if (normalized.includes('reservation')) return 'Lịch đổi pin';
        if (normalized.includes('payment')) return 'Thanh toán';
        if (normalized.includes('system')) return 'Hệ thống';
        return type;
    }
}
