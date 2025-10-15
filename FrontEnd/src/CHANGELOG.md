# Changelog

All notable changes to EV SWAP project will be documented in this file.

---

## [1.0.0] - 2025-10-01

### 🎉 Initial Release - Export từ Figma Make

### ✨ Features

#### Frontend
- ✅ Landing page với giới thiệu hệ thống
- ✅ Authentication system (Login/Register)
- ✅ Role-based dashboards (Admin/Staff/Driver)
- ✅ Vehicle registration cho Driver
- ✅ Support ticket system
- ✅ Station management
- ✅ Transaction history
- ✅ Toast notifications
- ✅ Modal dialogs
- ✅ Responsive design

#### Backend
- ✅ Supabase Edge Functions (Deno)
- ✅ RESTful API với Hono framework
- ✅ Key-Value store database
- ✅ JWT authentication
- ✅ Role-based access control
- ✅ CORS enabled
- ✅ Error logging

#### Tech Stack
- ✅ React 18
- ✅ TypeScript
- ✅ Vite build tool
- ✅ Tailwind CSS v4 (Pure CSS)
- ✅ Supabase (Auth + Database + Functions)

### 📦 Export Configuration

#### Added Files
- `package.json` - Node.js dependencies
- `vite.config.ts` - Vite configuration
- `tsconfig.json` - TypeScript config
- `index.html` - HTML entry point
- `main.tsx` - React entry point
- `.env` - Environment variables
- `.env.example` - Env template
- `.gitignore` - Git ignore rules

#### Documentation
- `README.md` - Full documentation
- `QUICKSTART.md` - Quick start guide
- `SETUP-INTELLIJ.md` - IntelliJ setup guide
- `EXPORT-GUIDE.md` - Export explanation
- `DEPLOYMENT.md` - Deployment guide
- `DOCS-INDEX.md` - Documentation index
- `CHANGELOG.md` - This file
- `LICENSE` - MIT license

#### Backend Docs
- `supabase/functions/README.md` - Backend documentation

#### Setup Scripts
- `setup.bat` - Windows auto setup
- `setup.sh` - macOS/Linux auto setup

#### IDE Configs
- `.vscode/settings.json` - VSCode settings
- `.vscode/extensions.json` - Recommended extensions
- `.vscode/launch.json` - Debug configuration

#### Assets
- `public/battery-icon.svg` - App favicon

### 🔧 Modified Files

- `App.tsx` - Import từ `utils/supabase/config.ts` thay vì `info.tsx`
- `utils/supabase/config.ts` - (NEW) Đọc env vars cho local dev

### 🎨 Styling

- Pure HTML + CSS (không dùng Tailwind classes)
- CSS được tổ chức trong `styles/app.css`
- Tailwind v4 tokens trong `styles/globals.css`

### 🔐 Default Accounts

#### Admin (Auto-created)
- Email: `admin@evswap.com`
- Password: `Admin@123456`

#### Staff
- Được tạo bởi Admin trong dashboard

#### Driver
- Đăng ký qua UI

### 🌐 API Endpoints

Tất cả có prefix: `/make-server-c0c28b62/`

#### Authentication
- `POST /signup` - Đăng ký
- `POST /login` - Đăng nhập
- `GET /me` - Get current user

#### Vehicles
- `POST /vehicles` - Đăng ký xe
- `GET /vehicles` - Lấy xe của driver

#### Admin
- `POST /admin/staff` - Tạo staff
- `GET /admin/users` - Lấy all users
- `POST /admin/stations` - Tạo trạm

#### Public
- `GET /stations` - Lấy trạm

#### Transactions
- `POST /transactions` - Tạo giao dịch
- `GET /transactions` - Lấy lịch sử

#### Support
- `POST /tickets` - Tạo ticket
- `GET /tickets` - Lấy tickets
- `PATCH /tickets/:id` - Cập nhật ticket

### 📊 Database Schema (KV Store)

Keys pattern:
- `user:{user_id}` - User data
- `user:{email}` - Email mapping
- `vehicle:{vehicle_id}` - Vehicle data
- `driver_vehicle:{driver_id}` - Driver mapping
- `station:{station_id}` - Station data
- `battery:{battery_id}` - Battery data
- `transaction:{transaction_id}` - Transaction data
- `ticket:{ticket_id}` - Ticket data

### 🚀 Deployment Options

- ✅ Vercel (Khuyến nghị)
- ✅ Netlify
- ✅ GitHub Pages
- ✅ Docker

### 📝 Known Limitations

- Backend chạy trên Figma Make (cần internet)
- Không có email notifications (chưa config SMTP)
- Không có real-time updates (chưa dùng Supabase Realtime)
- Không có file upload (chưa dùng Storage)

### 🎯 Future Enhancements

- [ ] Real-time notifications với Supabase Realtime
- [ ] Email notifications cho tickets
- [ ] File upload cho user avatars
- [ ] Battery swap simulation
- [ ] Map integration cho stations
- [ ] Analytics dashboard
- [ ] Mobile responsive improvements
- [ ] Dark mode
- [ ] Multi-language support
- [ ] Export data to Excel/PDF

---

## Development Notes

### Version Naming
- Version format: `MAJOR.MINOR.PATCH`
- Current: `1.0.0` (Initial release)

### Git Workflow (Nếu sử dụng Git)
```bash
git checkout -b feature/new-feature
# Make changes
git commit -m "feat: add new feature"
git push origin feature/new-feature
# Create pull request
```

### Commit Convention
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Adding tests
- `chore:` - Build/config changes

---

## Links

- **Repository**: (Add GitHub repo URL here)
- **Live Demo**: (Add deployed URL here)
- **Documentation**: See `/DOCS-INDEX.md`
- **Issues**: (Add issue tracker URL here)

---

**Maintained by**: EV SWAP Development Team  
**License**: MIT  
**Last Updated**: October 1, 2025
