# ⚡ Quick Start - Chạy trong 5 phút

Hướng dẫn siêu nhanh để chạy project EV SWAP trong IntelliJ IDEA.

---

## 🎯 TL;DR - 3 Lệnh là xong!

```bash
# 1. Cài đặt dependencies
npm install

# 2. Tạo file .env
cp .env.example .env

# 3. Chạy server
npm run dev
```

✅ **Mở browser**: `http://localhost:3000`  
🔐 **Đăng nhập Admin**: `admin@evswap.com` / `Admin@123456`

---

## 📋 Yêu cầu tối thiểu

- ✅ **Node.js 18+** - [Download](https://nodejs.org/)
- ✅ **IntelliJ IDEA** (Ultimate hoặc Community)

Kiểm tra Node.js:
```bash
node --version  # Phải >= v18.0.0
```

---

## 🚀 Các bước chi tiết

### 1️⃣ Mở Project

**IntelliJ IDEA** → **File** → **Open** → Chọn thư mục project

### 2️⃣ Mở Terminal trong IntelliJ

**View** → **Tool Windows** → **Terminal** (hoặc `Alt + F12`)

### 3️⃣ Cài đặt packages

```bash
npm install
```

⏱️ Mất ~1-2 phút

### 4️⃣ Tạo file .env

```bash
cp .env.example .env
```

File `.env` đã có sẵn config, không cần sửa gì!

### 5️⃣ Chạy development server

**Cách 1 - Dùng npm Tool Window**:
1. **View** → **Tool Windows** → **npm**
2. **Double-click** vào **"dev"** script

**Cách 2 - Dùng Terminal**:
```bash
npm run dev
```

### 6️⃣ Mở browser

Tự động mở tại: `http://localhost:3000`

---

## 🎮 Test các tính năng

### 1. Đăng nhập Admin
- **Email**: `admin@evswap.com`
- **Password**: `Admin@123456`
- ✅ Xem dashboard admin, quản lý users, tạo staff, tạo trạm

### 2. Tạo tài khoản Staff (từ Admin dashboard)
- Vào tab **"Người dùng"**
- Click **"Tạo Staff"**
- Điền thông tin
- ✅ Đăng xuất admin, login với staff account

### 3. Đăng ký Driver
- Đăng xuất
- Click **"Đăng ký"** trên trang chủ
- Điền thông tin
- Đăng ký xe (model, VIN, biển số)
- ✅ Vào dashboard driver

---

## 🛠️ Các lệnh hay dùng

```bash
# Chạy dev (hot reload)
npm run dev

# Build production
npm run build

# Preview production build
npm run preview

# Check TypeScript errors
npm run type-check
```

---

## 📁 File cần biết

```
evswap/
├── App.tsx              # ⭐ Main component - Tất cả logic ở đây
├── main.tsx             # Entry point
├── styles/
│   ├── globals.css      # Tailwind config
│   └── app.css          # ⭐ Custom CSS - Tất cả styles ở đây
└── supabase/functions/
    └── server/
        └── index.tsx    # Backend API routes
```

---

## 💡 Tips cho IntelliJ

### Tìm file nhanh
- `Ctrl + Shift + N` (Windows)
- `Cmd + Shift + O` (Mac)

### Find in files
- `Ctrl + Shift + F` (Windows)
- `Cmd + Shift + F` (Mac)

### Go to definition
- `Ctrl + B` (Windows)
- `Cmd + B` (Mac)

### Refactor/Rename
- `Shift + F6`

---

## 🐛 Gặp lỗi?

### "Cannot find module 'react'"

```bash
rm -rf node_modules package-lock.json
npm install
```

### "Port 3000 already in use"

```bash
# macOS/Linux
lsof -ti:3000 | xargs kill -9

# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

### TypeScript errors trong IntelliJ

1. **Settings** → **Languages & Frameworks** → **TypeScript**
2. Click **"Restart TypeScript Service"**

---

## 🎓 Muốn hiểu sâu hơn?

- 📖 **README.md** - Full documentation
- 🎯 **SETUP-INTELLIJ.md** - Hướng dẫn chi tiết IntelliJ
- 🚀 **DEPLOYMENT.md** - Hướng dẫn deploy production

---

## ✅ Hoàn thành!

Bây giờ bạn có thể:
- ✏️ Sửa code trong `App.tsx` và `app.css`
- 🔄 Auto reload khi save file
- 🎨 Thêm tính năng mới
- 🚀 Build và deploy

**Happy coding! 🎉**

---

## 📞 Cần trợ giúp?

1. Đọc lại **README.md**
2. Xem **Troubleshooting** section
3. Kiểm tra console logs
4. Google error message 😉
