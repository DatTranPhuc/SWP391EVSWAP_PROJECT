(() => {
    const root = document.querySelector('[data-admin-console]');
    if (!root) {
        return;
    }

    const yearEl = document.querySelector('[data-year]');
    if (yearEl) {
        yearEl.textContent = new Date().getFullYear();
    }

    const api = {
        async request(url, options = {}) {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...(options.headers || {})
                },
                ...options
            });

            if (!response.ok) {
                let message = `Yêu cầu thất bại (${response.status})`;
                try {
                    const payload = await response.json();
                    if (payload && payload.message) {
                        message = payload.message;
                    }
                } catch (err) {
                    // ignore json parse errors
                }
                throw new Error(message);
            }

            if (response.status === 204) {
                return null;
            }

            try {
                return await response.json();
            } catch (err) {
                return null;
            }
        }
    };

    const stationEls = {
        form: root.querySelector('[data-station-form]'),
        submit: root.querySelector('[data-station-submit]'),
        reset: root.querySelector('[data-station-reset]'),
        title: root.querySelector('[data-station-form-title]'),
        table: root.querySelector('[data-station-table]'),
        feedback: root.querySelector('[data-station-feedback]'),
        search: root.querySelector('[data-station-search]'),
        reload: root.querySelector('[data-reload-stations]')
    };

    const batteryEls = {
        form: root.querySelector('[data-battery-form]'),
        submit: root.querySelector('[data-battery-submit]'),
        reset: root.querySelector('[data-battery-reset]'),
        title: root.querySelector('[data-battery-form-title]'),
        table: root.querySelector('[data-battery-table]'),
        feedback: root.querySelector('[data-battery-feedback]'),
        filter: root.querySelector('[data-battery-filter]'),
        reload: root.querySelector('[data-reload-batteries]')
    };

    const stationState = {
        editingId: null,
        data: []
    };

    const batteryState = {
        editingId: null,
        data: []
    };

    function setFeedback(el, message, state = 'info') {
        if (!el) return;
        if (!message) {
            el.hidden = true;
            el.textContent = '';
            el.removeAttribute('data-state');
            return;
        }
        el.hidden = false;
        el.dataset.state = state;
        el.textContent = message;
    }

    function parseNumber(value) {
        if (value === '' || value === null || value === undefined) {
            return null;
        }
        const number = Number(value);
        if (Number.isNaN(number)) {
            throw new Error('Giá trị số không hợp lệ.');
        }
        return number;
    }

    function parseInteger(value) {
        if (value === '' || value === null || value === undefined) {
            return null;
        }
        const number = parseInt(value, 10);
        if (Number.isNaN(number)) {
            throw new Error('Giá trị nguyên không hợp lệ.');
        }
        return number;
    }

    function resetStationForm() {
        stationState.editingId = null;
        stationEls.form.reset();
        stationEls.submit.textContent = 'Thêm trạm';
        stationEls.title.textContent = 'Thêm trạm mới';
        stationEls.reset.hidden = true;
    }

    function resetBatteryForm() {
        batteryState.editingId = null;
        batteryEls.form.reset();
        batteryEls.submit.textContent = 'Thêm pin';
        batteryEls.title.textContent = 'Thêm pin mới';
        batteryEls.reset.hidden = true;
    }

    function drawStationTable(rows) {
        const tbody = stationEls.table;
        if (!tbody) return;
        tbody.innerHTML = '';
        if (!rows || rows.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.className = 'empty-row';
            emptyRow.innerHTML = '<td colspan="5">Chưa có trạm nào trong hệ thống.</td>';
            tbody.appendChild(emptyRow);
            return;
        }

        rows.forEach((station) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${station.stationId}</td>
                <td>${station.name || '-'}</td>
                <td>${station.address || '-'}</td>
                <td><span class="badge ${station.status === 'active' ? 'success' : station.status === 'closed' ? 'danger' : ''}">${station.status || '-'}</span></td>
                <td>
                    <div class="action-group">
                        <button type="button" class="table-btn edit" data-action="edit-station" data-id="${station.stationId}">Sửa</button>
                        <button type="button" class="table-btn delete" data-action="delete-station" data-id="${station.stationId}">Xóa</button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    function updateStationTable(data) {
        stationState.data = Array.isArray(data) ? data : [];
        if (!stationEls.search || !stationEls.search.value.trim()) {
            drawStationTable(stationState.data);
        } else {
            applyStationSearch();
        }
    }

    function drawBatteryTable(rows) {
        const tbody = batteryEls.table;
        if (!tbody) return;
        tbody.innerHTML = '';
        if (!rows || rows.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.className = 'empty-row';
            emptyRow.innerHTML = '<td colspan="6">Chưa có pin nào trong kho.</td>';
            tbody.appendChild(emptyRow);
            return;
        }

        rows.forEach((battery) => {
            const soh = battery.sohPercent ?? '-';
            const soc = battery.socPercent ?? '-';
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${battery.batteryId}</td>
                <td>${battery.stationName ? `${battery.stationName} (#${battery.stationId ?? '-'})` : `#${battery.stationId ?? '-'}`}</td>
                <td>${battery.model || '-'}</td>
                <td>${battery.state || '-'}</td>
                <td>${soh}/${soc}</td>
                <td>
                    <div class="action-group">
                        <button type="button" class="table-btn edit" data-action="edit-battery" data-id="${battery.batteryId}">Sửa</button>
                        <button type="button" class="table-btn delete" data-action="delete-battery" data-id="${battery.batteryId}">Xóa</button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    function updateBatteryTable(data) {
        batteryState.data = Array.isArray(data) ? data : [];
        applyBatteryFilter();
    }

    async function loadStations() {
        setFeedback(stationEls.feedback, 'Đang tải danh sách trạm...');
        try {
            const data = await api.request('/api/stations');
            const list = Array.isArray(data) ? data : [];
            updateStationTable(list);
            setFeedback(stationEls.feedback, `Đã tải ${list.length} trạm.`, 'success');
        } catch (error) {
            setFeedback(stationEls.feedback, error.message, 'error');
        }
    }

    async function loadBatteries() {
        setFeedback(batteryEls.feedback, 'Đang tải danh sách pin...');
        try {
            const data = await api.request('/api/batteries');
            const list = Array.isArray(data) ? data : [];
            updateBatteryTable(list);
            setFeedback(batteryEls.feedback, `Đã tải ${list.length} pin.`, 'success');
        } catch (error) {
            setFeedback(batteryEls.feedback, error.message, 'error');
        }
    }

    function applyStationSearch() {
        const term = (stationEls.search?.value || '').trim().toLowerCase();
        if (!term) {
            drawStationTable(stationState.data);
            return;
        }
        const filtered = stationState.data.filter((station) => {
            return station.name?.toLowerCase().includes(term) || station.address?.toLowerCase().includes(term);
        });
        drawStationTable(filtered);
    }

    function applyBatteryFilter() {
        const filterValue = batteryEls.filter?.value;
        if (!filterValue) {
            drawBatteryTable(batteryState.data);
            return;
        }
        const stationId = parseInt(filterValue, 10);
        if (Number.isNaN(stationId)) {
            drawBatteryTable(batteryState.data);
            return;
        }
        const filtered = batteryState.data.filter((battery) => battery.stationId === stationId);
        drawBatteryTable(filtered);
    }

    stationEls.form?.addEventListener('submit', async (event) => {
        event.preventDefault();
        const formData = new FormData(stationEls.form);
        const payload = {};
        try {
            const name = (formData.get('name') || '').toString().trim();
            if (!name) {
                throw new Error('Tên trạm là bắt buộc.');
            }
            payload.name = name;
            const address = (formData.get('address') || '').toString().trim();
            if (address) {
                payload.address = address;
            }
            const lat = parseNumber(formData.get('latitude'));
            const lng = parseNumber(formData.get('longitude'));
            if (lat !== null) payload.latitude = lat;
            if (lng !== null) payload.longitude = lng;
            const status = (formData.get('status') || '').toString();
            if (status) {
                payload.status = status;
            }
        } catch (error) {
            setFeedback(stationEls.feedback, error.message, 'error');
            return;
        }

        try {
            if (stationState.editingId) {
                await api.request(`/api/stations/${stationState.editingId}`, {
                    method: 'PUT',
                    body: JSON.stringify(payload)
                });
                setFeedback(stationEls.feedback, 'Đã cập nhật trạm thành công.', 'success');
            } else {
                await api.request('/api/stations', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                setFeedback(stationEls.feedback, 'Đã tạo trạm mới.', 'success');
            }
            resetStationForm();
            await loadStations();
        } catch (error) {
            setFeedback(stationEls.feedback, error.message, 'error');
        }
    });

    stationEls.reset?.addEventListener('click', () => {
        resetStationForm();
        setFeedback(stationEls.feedback, 'Đã hủy chỉnh sửa.', 'info');
    });

    stationEls.reload?.addEventListener('click', loadStations);
    stationEls.search?.addEventListener('input', applyStationSearch);

    stationEls.table?.addEventListener('click', async (event) => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) return;
        const action = target.dataset.action;
        const id = target.dataset.id ? parseInt(target.dataset.id, 10) : null;
        if (!action || !id) return;

        if (action === 'edit-station') {
            const station = stationState.data.find((item) => item.stationId === id);
            if (!station) return;
            stationState.editingId = id;
            stationEls.form.querySelector('#station-name').value = station.name || '';
            stationEls.form.querySelector('#station-address').value = station.address || '';
            stationEls.form.querySelector('#station-lat').value = station.latitude ?? '';
            stationEls.form.querySelector('#station-lng').value = station.longitude ?? '';
            stationEls.form.querySelector('#station-status').value = station.status || 'active';
            stationEls.submit.textContent = 'Cập nhật trạm';
            stationEls.title.textContent = `Chỉnh sửa: ${station.name || 'Trạm #' + id}`;
            stationEls.reset.hidden = false;
            stationEls.form.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        if (action === 'delete-station') {
            const confirmed = window.confirm('Bạn có chắc chắn muốn xóa trạm này?');
            if (!confirmed) return;
            try {
                await api.request(`/api/stations/${id}`, { method: 'DELETE' });
                setFeedback(stationEls.feedback, 'Đã xóa trạm.', 'success');
                if (stationState.editingId === id) {
                    resetStationForm();
                }
                await loadStations();
            } catch (error) {
                setFeedback(stationEls.feedback, error.message, 'error');
            }
        }
    });

    batteryEls.form?.addEventListener('submit', async (event) => {
        event.preventDefault();
        const formData = new FormData(batteryEls.form);
        const payload = {};
        try {
            const stationId = parseInteger(formData.get('stationId'));
            if (!stationId) {
                throw new Error('ID trạm là bắt buộc.');
            }
            payload.stationId = stationId;
            const model = (formData.get('model') || '').toString().trim();
            if (!model) {
                throw new Error('Model pin là bắt buộc.');
            }
            payload.model = model;
            const state = (formData.get('state') || '').toString();
            if (state) payload.state = state;
            const soh = parseInteger(formData.get('sohPercent'));
            const soc = parseInteger(formData.get('socPercent'));
            if (soh !== null) payload.sohPercent = soh;
            if (soc !== null) payload.socPercent = soc;
        } catch (error) {
            setFeedback(batteryEls.feedback, error.message, 'error');
            return;
        }

        try {
            if (batteryState.editingId) {
                await api.request(`/api/batteries/${batteryState.editingId}`, {
                    method: 'PATCH',
                    body: JSON.stringify(payload)
                });
                setFeedback(batteryEls.feedback, 'Đã cập nhật thông tin pin.', 'success');
            } else {
                await api.request('/api/batteries', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                setFeedback(batteryEls.feedback, 'Đã thêm pin mới.', 'success');
            }
            resetBatteryForm();
            await loadBatteries();
        } catch (error) {
            setFeedback(batteryEls.feedback, error.message, 'error');
        }
    });

    batteryEls.reset?.addEventListener('click', () => {
        resetBatteryForm();
        setFeedback(batteryEls.feedback, 'Đã hủy chỉnh sửa.', 'info');
    });

    batteryEls.reload?.addEventListener('click', loadBatteries);
    batteryEls.filter?.addEventListener('input', applyBatteryFilter);

    batteryEls.table?.addEventListener('click', async (event) => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) return;
        const action = target.dataset.action;
        const id = target.dataset.id ? parseInt(target.dataset.id, 10) : null;
        if (!action || !id) return;

        if (action === 'edit-battery') {
            const battery = batteryState.data.find((item) => item.batteryId === id);
            if (!battery) return;
            batteryState.editingId = id;
            batteryEls.form.querySelector('#battery-station').value = battery.stationId ?? '';
            batteryEls.form.querySelector('#battery-model').value = battery.model || '';
            batteryEls.form.querySelector('#battery-state').value = battery.state || 'full';
            batteryEls.form.querySelector('#battery-soh').value = battery.sohPercent ?? '';
            batteryEls.form.querySelector('#battery-soc').value = battery.socPercent ?? '';
            batteryEls.submit.textContent = 'Cập nhật pin';
            batteryEls.title.textContent = `Chỉnh sửa pin #${id}`;
            batteryEls.reset.hidden = false;
            batteryEls.form.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        if (action === 'delete-battery') {
            const confirmed = window.confirm('Xóa pin này khỏi kho?');
            if (!confirmed) return;
            try {
                await api.request(`/api/batteries/${id}`, { method: 'DELETE' });
                setFeedback(batteryEls.feedback, 'Đã xóa pin.', 'success');
                if (batteryState.editingId === id) {
                    resetBatteryForm();
                }
                await loadBatteries();
            } catch (error) {
                setFeedback(batteryEls.feedback, error.message, 'error');
            }
        }
    });

    // Initial load
    loadStations();
    loadBatteries();
})();
