document.addEventListener('DOMContentLoaded', function () {
    // --- STATE MANAGEMENT (Quản lý trạng thái dữ liệu của trang) ---
    const state = {
        staff: [],
        stations: [],
        filteredStaff: [],
        filteredStations: [],
        isStaffLoaded: false,
        isStationsLoaded: false,
    };

    // --- DOM ELEMENTS (Lưu trữ các thành phần trên giao diện) ---
    const ui = {
        menuItems: document.querySelectorAll('.sidebar-menu .menu-item'),
        contentSections: document.querySelectorAll('.main-content .content-section'),
        mainTitle: document.getElementById('main-title'),
        addStaffBtn: document.getElementById('addStaffBtn'),
        addStationBtn: document.getElementById('addStationBtn'),
        logoutButton: document.getElementById('logoutButton'),
        themeToggle: document.getElementById('theme-toggle'),
        menuToggle: document.getElementById('menu-toggle'),
        sidebar: document.querySelector('.sidebar'),
        staffTableBody: document.getElementById('staff-table-body'),
        stationTableBody: document.getElementById('station-table-body'),
        staffLoader: document.getElementById('staff-loader'),
        stationLoader: document.getElementById('station-loader'),
        staffSearch: document.getElementById('staff-search'),
        stationSearch: document.getElementById('station-search'),
    };

    // --- API HELPERS (Các hàm tiện ích để gọi API backend) ---
    const api = {
        get: (url) => fetch(url),
        post: (url, data) => fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json; charset=UTF-8' }, body: JSON.stringify(data) }),
        put: (url, data) => fetch(url, { method: 'PUT', headers: { 'Content-Type': 'application/json; charset=UTF-8' }, body: JSON.stringify(data) }),
        delete: (url) => fetch(url, { method: 'DELETE' }),
    };

    // --- RENDER FUNCTIONS (Các hàm để vẽ dữ liệu ra HTML) ---
    const render = {
        staffTable: () => {
            const dataToRender = state.filteredStaff;
            if (dataToRender.length === 0 && state.isStaffLoaded) {
                ui.staffTableBody.innerHTML = `<tr><td colspan="6" class="no-data">Không có dữ liệu nhân viên.</td></tr>`;
                return;
            }
            ui.staffTableBody.innerHTML = dataToRender.map(staff => `
                <tr>
                    <td>#${staff.staffId}</td>
                    <td>${staff.fullName}</td>
                    <td>${staff.email}</td>
                    <td>${staff.stationName || 'Chưa phân công'}</td>
                    <td><span class="status ${staff.status.toLowerCase()}">${staff.status === 'ACTIVE' ? 'Hoạt động' : 'Tạm ngưng'}</span></td>
                    <td>
                        <button class="action-btn edit" data-id="${staff.staffId}" title="Sửa"><i class="fa-solid fa-pencil"></i></button>
                        <button class="action-btn delete" data-id="${staff.staffId}" data-name="${staff.fullName}" title="Xóa"><i class="fa-solid fa-trash-can"></i></button>
                    </td>
                </tr>
            `).join('');
        },
        stationTable: () => {
            const dataToRender = state.filteredStations;
            if (dataToRender.length === 0 && state.isStationsLoaded) {
                ui.stationTableBody.innerHTML = `<tr><td colspan="5" class="no-data">Không có dữ liệu trạm pin.</td></tr>`;
                return;
            }
            ui.stationTableBody.innerHTML = dataToRender.map(station => `
                <tr>
                    <td>#${station.stationId}</td>
                    <td>${station.name}</td>
                    <td>${station.address}</td>
                    <td><span class="status ${station.status.toLowerCase()}">${station.status === 'OPERATIONAL' ? 'Hoạt động' : 'Bảo trì'}</span></td>
                    <td>
                        <button class="action-btn edit" data-id="${station.stationId}" title="Sửa"><i class="fa-solid fa-pencil"></i></button>
                        <button class="action-btn delete" data-id="${station.stationId}" data-name="${station.name}" title="Xóa"><i class="fa-solid fa-trash-can"></i></button>
                    </td>
                </tr>
            `).join('');
        },
        toggleLoader: (loader, show) => {
            loader.style.display = show ? 'flex' : 'none';
        }
    };

    // --- LOGIC FUNCTIONS (Các hàm xử lý nghiệp vụ chính) ---
    const logic = {
        navigateTo: (targetId) => {
            ui.menuItems.forEach(item => item.classList.toggle('active', item.dataset.target === targetId));
            ui.contentSections.forEach(section => section.classList.toggle('active', section.id === targetId));
            const activeMenuItem = document.querySelector(`.menu-item[data-target="${targetId}"]`);
            ui.mainTitle.textContent = activeMenuItem.querySelector('span').textContent;
            if (targetId === 'staff-section' && !state.isStaffLoaded) logic.loadStaff();
            if (targetId === 'station-section' && !state.isStationsLoaded) logic.loadStations();
        },
        loadStaff: async () => {
            render.toggleLoader(ui.staffLoader, true);
            try {
                const response = await api.get('/api/admin/staff');
                if (!response.ok) throw new Error('Không thể tải danh sách nhân viên.');
                state.staff = await response.json();
                state.filteredStaff = state.staff;
                render.staffTable();
            } catch (e) {
                ui.staffTableBody.innerHTML = `<tr><td colspan="6" class="no-data error">${e.message}</td></tr>`;
            } finally {
                state.isStaffLoaded = true;
                render.toggleLoader(ui.staffLoader, false);
            }
        },
        loadStations: async () => {
            render.toggleLoader(ui.stationLoader, true);
            try {
                const response = await api.get('/api/admin/stations');
                if (!response.ok) throw new Error('Không thể tải danh sách trạm.');
                state.stations = await response.json();
                state.filteredStations = state.stations;
                render.stationTable();
            } catch (e) {
                ui.stationTableBody.innerHTML = `<tr><td colspan="5" class="no-data error">${e.message}</td></tr>`;
            } finally {
                state.isStationsLoaded = true;
                render.toggleLoader(ui.stationLoader, false);
            }
        },

        // --- CRUD Staff ---
        handleAddStaff: async () => {
            const { value: formValues } = await Swal.fire({
                title: 'Tạo tài khoản nhân viên mới',
                html: `
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-fullName">Họ và Tên</label>
                        <input id="swal-fullName" class="swal2-custom-input" placeholder="Nguyễn Văn A">
                    </div>
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-email">Email</label>
                        <input id="swal-email" type="email" class="swal2-custom-input" placeholder="email@example.com">
                    </div>
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-password">Mật khẩu</label>
                        <input id="swal-password" type="password" class="swal2-custom-input" placeholder="Mật khẩu ban đầu">
                    </div>
                     <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-stationId">ID Trạm làm việc</label>
                        <input id="swal-stationId" type="number" class="swal2-custom-input" placeholder="Nhập ID của trạm">
                    </div>`,
                focusConfirm: false, showCancelButton: true,
                confirmButtonText: '<i class="fa-solid fa-save"></i> Lưu lại',
                cancelButtonText: 'Hủy', confirmButtonColor: 'var(--primary-color)',
                preConfirm: () => {
                    const fullName = document.getElementById('swal-fullName').value;
                    const email = document.getElementById('swal-email').value;
                    const password = document.getElementById('swal-password').value;
                    const stationId = document.getElementById('swal-stationId').value;
                    if (!fullName || !email || !password || !stationId) {
                        Swal.showValidationMessage(`Vui lòng điền đầy đủ thông tin`);
                        return false;
                    }
                    return { fullName, email, password, stationId: parseInt(stationId) };
                }
            });
            if (formValues) {
                try {
                    const response = await api.post('/api/admin/staff', formValues);
                    if (!response.ok) throw new Error(await response.text());
                    Swal.fire('Thành công!', 'Đã tạo nhân viên mới.', 'success');
                    await logic.loadStaff();
                } catch (error) { Swal.fire('Thất bại!', error.message, 'error'); }
            }
        },
        handleEditStaff: async (staffId) => {
            try {
                const res = await api.get(`/api/admin/staff/${staffId}`);
                if (!res.ok) throw new Error('Không tìm thấy thông tin nhân viên.');
                const staff = await res.json();

                const { value: formValues } = await Swal.fire({
                    title: 'Cập nhật thông tin nhân viên',
                    html: `
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-fullName">Họ và Tên</label>
                        <input id="swal-fullName" class="swal2-custom-input" value="${staff.fullName}">
                    </div>
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-email">Email (Không thể thay đổi)</label>
                        <input id="swal-email" type="email" class="swal2-custom-input" value="${staff.email}" disabled>
                    </div>
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-password">Mật khẩu mới (Bỏ trống nếu không đổi)</label>
                        <input id="swal-password" type="password" class="swal2-custom-input" placeholder="Nhập mật khẩu mới">
                    </div>
                     <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-stationId">ID Trạm làm việc</label>
                        <input id="swal-stationId" type="number" class="swal2-custom-input" value="${staff.stationId || ''}">
                    </div>`,
                    focusConfirm: false, showCancelButton: true,
                    confirmButtonText: '<i class="fa-solid fa-save"></i> Cập nhật',
                    cancelButtonText: 'Hủy', confirmButtonColor: 'var(--primary-color)',
                    preConfirm: () => {
                        const fullName = document.getElementById('swal-fullName').value;
                        const password = document.getElementById('swal-password').value;
                        const stationId = document.getElementById('swal-stationId').value;
                        if (!fullName || !stationId) {
                            Swal.showValidationMessage(`Vui lòng điền đủ Họ tên và ID Trạm`); return false;
                        }
                        return { fullName, password, stationId: parseInt(stationId) };
                    }
                });
                if (formValues) {
                    const response = await api.put(`/api/admin/staff/${staffId}`, formValues);
                    if (!response.ok) throw new Error(await response.text());
                    Swal.fire('Thành công!', 'Đã cập nhật thông tin nhân viên.', 'success');
                    await logic.loadStaff();
                }
            } catch (error) { Swal.fire('Thất bại!', error.message, 'error'); }
        },
        handleDeleteStaff: (staffId, staffName) => {
            Swal.fire({
                title: 'Bạn có chắc chắn?', text: `Bạn sẽ xóa vĩnh viễn nhân viên "${staffName}"!`,
                icon: 'warning', showCancelButton: true,
                confirmButtonColor: 'var(--danger)', cancelButtonColor: 'var(--text-secondary)',
                confirmButtonText: 'Vâng, xóa đi!', cancelButtonText: 'Hủy'
            }).then(async (result) => {
                if (result.isConfirmed) {
                    try {
                        const response = await api.delete(`/api/admin/staff/${staffId}`);
                        if (!response.ok) throw new Error(await response.text());
                        Swal.fire('Đã xóa!', `Nhân viên ${staffName} đã được xóa.`, 'success');
                        await logic.loadStaff();
                    } catch (error) { Swal.fire('Lỗi!', error.message, 'error'); }
                }
            });
        },

        // --- CRUD Stations ---
        handleAddStation: async () => {
            const { value: formValues } = await Swal.fire({
                title: 'Tạo trạm pin mới',
                html: `
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-name">Tên trạm</label>
                        <input id="swal-name" class="swal2-custom-input" placeholder="Ví dụ: EV SWAP Quận 1">
                    </div>
                    <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-address">Địa chỉ</label>
                        <input id="swal-address" class="swal2-custom-input" placeholder="123 Nguyễn Huệ, P. Bến Nghé, Q.1">
                    </div>
                     <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-latitude">Vĩ độ (Latitude)</label>
                        <input id="swal-latitude" type="number" step="any" class="swal2-custom-input" placeholder="10.776889">
                    </div>
                     <div class="swal2-form-group">
                        <label class="swal2-input-label" for="swal-longitude">Kinh độ (Longitude)</label>
                        <input id="swal-longitude" type="number" step="any" class="swal2-custom-input" placeholder="106.700818">
                    </div>`,
                focusConfirm: false, showCancelButton: true,
                confirmButtonText: '<i class="fa-solid fa-save"></i> Lưu lại',
                cancelButtonText: 'Hủy', confirmButtonColor: 'var(--primary-color)',
                preConfirm: () => {
                    const name = document.getElementById('swal-name').value;
                    const address = document.getElementById('swal-address').value;
                    const latitude = document.getElementById('swal-latitude').value;
                    const longitude = document.getElementById('swal-longitude').value;
                    if (!name || !address || !latitude || !longitude) {
                        Swal.showValidationMessage(`Vui lòng điền đầy đủ thông tin`);
                        return false;
                    }
                    return { name, address, latitude: parseFloat(latitude), longitude: parseFloat(longitude) };
                }
            });
            if (formValues) {
                try {
                    const response = await api.post('/api/admin/stations', formValues);
                    if (!response.ok) throw new Error(await response.text());
                    Swal.fire('Thành công!', 'Đã tạo trạm mới.', 'success');
                    await logic.loadStations();
                } catch (error) { Swal.fire('Thất bại!', error.message, 'error'); }
            }
        },
        handleEditStation: async (stationId) => {
            try {
                const res = await api.get(`/api/admin/stations/${stationId}`);
                if (!res.ok) throw new Error('Không tìm thấy thông tin trạm.');
                const station = await res.json();

                const { value: formValues } = await Swal.fire({
                    title: 'Cập nhật thông tin trạm',
                    html: `
                        <div class="swal2-form-group">
                            <label class="swal2-input-label" for="swal-name">Tên trạm</label>
                            <input id="swal-name" class="swal2-custom-input" value="${station.name}">
                        </div>
                        <div class="swal2-form-group">
                            <label class="swal2-input-label" for="swal-address">Địa chỉ</label>
                            <input id="swal-address" class="swal2-custom-input" value="${station.address}">
                        </div>
                        <div class="swal2-form-group">
                            <label class="swal2-input-label" for="swal-latitude">Vĩ độ</label>
                            <input id="swal-latitude" type="number" step="any" class="swal2-custom-input" value="${station.latitude}">
                        </div>
                        <div class="swal2-form-group">
                            <label class="swal2-input-label" for="swal-longitude">Kinh độ</label>
                            <input id="swal-longitude" type="number" step="any" class="swal2-custom-input" value="${station.longitude}">
                        </div>
                        <div class="swal2-form-group">
                            <label class="swal2-input-label" for="swal-status">Trạng thái</label>
                            <select id="swal-status" class="swal2-custom-input">
                                <option value="OPERATIONAL" ${station.status === 'OPERATIONAL' ? 'selected' : ''}>Hoạt động</option>
                                <option value="MAINTENANCE" ${station.status === 'MAINTENANCE' ? 'selected' : ''}>Bảo trì</option>
                            </select>
                        </div>`,
                    focusConfirm: false, showCancelButton: true,
                    confirmButtonText: '<i class="fa-solid fa-save"></i> Cập nhật',
                    cancelButtonText: 'Hủy', confirmButtonColor: 'var(--primary-color)',
                    preConfirm: () => {
                        const name = document.getElementById('swal-name').value;
                        const address = document.getElementById('swal-address').value;
                        const latitude = document.getElementById('swal-latitude').value;
                        const longitude = document.getElementById('swal-longitude').value;
                        const status = document.getElementById('swal-status').value;
                        if (!name || !address || !latitude || !longitude) {
                            Swal.showValidationMessage(`Vui lòng điền đầy đủ thông tin`); return false;
                        }
                        return { name, address, latitude: parseFloat(latitude), longitude: parseFloat(longitude), status };
                    }
                });
                if (formValues) {
                    const response = await api.put(`/api/admin/stations/${stationId}`, formValues);
                    if (!response.ok) throw new Error(await response.text());
                    Swal.fire('Thành công!', 'Đã cập nhật thông tin trạm.', 'success');
                    await logic.loadStations();
                }
            } catch (error) { Swal.fire('Thất bại!', error.message, 'error'); }
        },
        handleDeleteStation: (stationId, stationName) => {
            Swal.fire({
                title: 'Bạn có chắc chắn?', text: `Bạn sẽ xóa vĩnh viễn trạm "${stationName}"!`,
                icon: 'warning', showCancelButton: true,
                confirmButtonColor: 'var(--danger)', cancelButtonColor: 'var(--text-secondary)',
                confirmButtonText: 'Vâng, xóa đi!', cancelButtonText: 'Hủy'
            }).then(async (result) => {
                if (result.isConfirmed) {
                    try {
                        const response = await api.delete(`/api/admin/stations/${stationId}`);
                        if (!response.ok) throw new Error(await response.text());
                        Swal.fire('Đã xóa!', `Trạm ${stationName} đã được xóa.`, 'success');
                        await logic.loadStations();
                    } catch (error) { Swal.fire('Lỗi!', error.message, 'error'); }
                }
            });
        },

        // --- Others ---
        handleLogout: () => {
            Swal.fire({
                title: 'Bạn có muốn đăng xuất?', icon: 'question',
                showCancelButton: true, confirmButtonText: 'Đăng xuất', cancelButtonText: 'Hủy'
            }).then(async (result) => {
                if (result.isConfirmed) {
                    await api.post('/api/auth/logout');
                    window.location.href = '/login.html';
                }
            });
        },
        toggleTheme: () => {
            document.body.classList.toggle('dark-mode');
            localStorage.setItem('theme', document.body.classList.contains('dark-mode') ? 'dark' : 'light');
        },
        initTheme: () => {
            if (localStorage.getItem('theme') === 'dark') {
                document.body.classList.add('dark-mode');
            }
        },
    };

    // --- EVENT LISTENERS ---
    function attachEventListeners() {
        ui.menuItems.forEach(item => item.addEventListener('click', (e) => {
            e.preventDefault(); logic.navigateTo(item.dataset.target);
        }));

        ui.addStaffBtn.addEventListener('click', logic.handleAddStaff);
        ui.addStationBtn.addEventListener('click', logic.handleAddStation);

        document.body.addEventListener('click', (e) => {
            const button = e.target.closest('button.action-btn');
            if (!button) return;

            const id = button.dataset.id;
            const name = button.dataset.name;
            const tableBody = button.closest('tbody');

            if (tableBody === ui.staffTableBody) {
                if (button.classList.contains('edit')) logic.handleEditStaff(id);
                else if (button.classList.contains('delete')) logic.handleDeleteStaff(id, name);
            } else if (tableBody === ui.stationTableBody) {
                if (button.classList.contains('edit')) logic.handleEditStation(id);
                else if (button.classList.contains('delete')) logic.handleDeleteStation(id, name);
            }
        });

        ui.staffSearch.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            state.filteredStaff = state.staff.filter(s => s.fullName.toLowerCase().includes(query) || s.email.toLowerCase().includes(query));
            render.staffTable();
        });

        ui.stationSearch.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            state.filteredStations = state.stations.filter(s => s.name.toLowerCase().includes(query) || s.address.toLowerCase().includes(query));
            render.stationTable();
        });

        ui.logoutButton.addEventListener('click', logic.handleLogout);
        ui.themeToggle.addEventListener('click', logic.toggleTheme);
        ui.menuToggle.addEventListener('click', () => ui.sidebar.classList.toggle('open'));
    }

    // --- INITIALIZATION ---
    function init() {
        logic.initTheme();
        attachEventListeners();
        logic.navigateTo('overview-section');
    }

    init();
});