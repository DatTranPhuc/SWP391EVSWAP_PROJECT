# 🎯 Hướng dẫn Setup Project trong IntelliJ IDEA

Hướng dẫn chi tiết từng bước để chạy project EV SWAP trong IntelliJ IDEA.

---

## 📋 Yêu cầu

### 1. Cài đặt phần mềm

- ✅ **IntelliJ IDEA Ultimate 2023.3+** (hoặc Community Edition + JavaScript plugin)
- ✅ **Node.js 18.0.0+** ([Download tại đây](https://nodejs.org/))
- ✅ **Git** (optional, để clone project)

### 2. Kiểm tra Node.js đã cài đặt

Mở Terminal/Command Prompt và chạy:

```bash
node --version
# Phải hiển thị: v18.x.x hoặc cao hơn

npm --version
# Phải hiển thị: 9.x.x hoặc cao hơn
```

---

## 🚀 Bước 1: Mở Project trong IntelliJ

### Cách 1: Từ Welcome Screen

1. Mở IntelliJ IDEA
2. Click **"Open"** trên màn hình chào mừng
3. Chọn thư mục chứa project (thư mục có file `package.json`)
4. Click **"OK"**

### Cách 2: Từ Menu Bar

1. **File → Open...**
2. Chọn thư mục project
3. Click **"OK"**

⏱️ IntelliJ sẽ mất vài giây để index project.

---

## 🔧 Bước 2: Cấu hình Node.js trong IntelliJ

### 2.1. Mở Settings

- **Windows/Linux**: `Ctrl + Alt + S`
- **macOS**: `Cmd + ,`

### 2.2. Cấu hình Node.js Interpreter

1. Navigate: **Settings → Languages & Frameworks → Node.js**
2. **Node interpreter**: 
   - Click vào dropdown
   - Chọn Node.js version đã cài (ví dụ: `/usr/local/bin/node`)
   - Nếu không có, click **"..."** để browse và chọn
3. ✅ Check **"Coding assistance for Node.js"**
4. Click **"Apply"**

### 2.3. Enable TypeScript Support

1. **Settings → Languages & Frameworks → TypeScript**
2. ✅ Check **"TypeScript Language Service"**
3. **TypeScript**: Chọn **"Project TypeScript"** (sẽ tự động detect sau khi npm install)
4. Click **"Apply"**

### 2.4. Enable Auto Import

1. **Settings → Editor → General → Auto Import**
2. ✅ Check **"Add unambiguous imports on the fly"**
3. ✅ Check **"Optimize imports on the fly"**
4. Click **"OK"**

---

## 📦 Bước 3: Cài đặt Dependencies

### Cách 1: Dùng npm Tool Window (Khuyến nghị)

1. **View → Tool Windows → npm**
2. Trong npm tool window, tìm **"install"** script
3. **Right-click** → **Run 'install'**

### Cách 2: Dùng Terminal trong IntelliJ

1. **View → Tool Windows → Terminal** (hoặc `Alt + F12`)
2. Chạy lệnh:

```bash
npm install
```

⏱️ Quá trình cài đặt sẽ mất 1-2 phút.

---

## 🔐 Bước 4: Cấu hình Environment Variables

### 4.1. Tạo file .env

1. Trong **Project Explorer**, tìm file `.env.example`
2. **Right-click** → **Copy** (`Ctrl + C`)
3. **Right-click** vào thư mục root → **Paste** (`Ctrl + V`)
4. Rename thành `.env`

### 4.2. Hoặc dùng Terminal

```bash
cp .env.example .env
```

### 4.3. Kiểm tra nội dung .env

File `.env` đã có sẵn config cho Figma Make backend:

```env
VITE_SUPABASE_PROJECT_ID=yktfgqpcmdgtycnyxpby
VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

✅ **Không cần thay đổi** nếu bạn dùng backend Figma Make.

---

## ▶️ Bước 5: Chạy Development Server

### Cách 1: Dùng npm Scripts (Dễ nhất)

1. **View → Tool Windows → npm**
2. Expand **"Scripts"**
3. **Double-click** vào **"dev"**
4. Server sẽ chạy tại `http://localhost:3000`

### Cách 2: Tạo Run Configuration

1. **Run → Edit Configurations...**
2. Click **"+"** → **npm**
3. Đặt tên: **"Dev Server"**
4. **package.json**: Browse và chọn `package.json` trong project
5. **Command**: `run`
6. **Scripts**: `dev`
7. Click **"OK"**
8. Click nút **"Run"** (▶️) hoặc `Shift + F10`

### Cách 3: Dùng Terminal

1. **View → Tool Windows → Terminal**
2. Chạy:

```bash
npm run dev
```

### 🎉 Kết quả

Bạn sẽ thấy output:

```
VITE v5.4.2  ready in 500 ms

➜  Local:   http://localhost:3000/
➜  Network: use --host to expose
➜  press h + enter to show help
```

Browser sẽ tự động mở tại `http://localhost:3000`.

---

## 🐛 Bước 6: Debug Application

### 6.1. Tạo JavaScript Debug Configuration

1. **Run → Edit Configurations...**
2. Click **"+"** → **JavaScript Debug**
3. Đặt tên: **"Debug App"**
4. **URL**: `http://localhost:3000`
5. Click **"OK"**

### 6.2. Chạy Debug

1. **Đảm bảo dev server đang chạy** (`npm run dev`)
2. Đặt breakpoint trong code (click vào gutter bên trái số dòng)
3. Click nút **"Debug"** (🐞) hoặc `Shift + F9`
4. IntelliJ sẽ mở Chrome và dừng tại breakpoint

---

## 📝 Bước 7: Các Lệnh Thường Dùng

### Trong npm Tool Window

- **dev** - Chạy development server
- **build** - Build production
- **preview** - Preview production build
- **type-check** - Kiểm tra TypeScript errors

### Trong Terminal

```bash
# Chạy dev server
npm run dev

# Build production
npm run build

# Preview production build
npm run preview

# Type checking
npm run type-check
```

---

## 🎨 Bước 8: Tùy chỉnh IntelliJ (Optional)

### 8.1. Enable Prettier (Auto format on save)

1. **Settings → Languages & Frameworks → JavaScript → Prettier**
2. ✅ **"On save"**
3. ✅ **"On Reformat Code action"**

### 8.2. Configure Code Style

1. **Settings → Editor → Code Style → TypeScript**
2. Tab **"Tabs and Indents"**:
   - **Tab size**: 2
   - **Indent**: 2
   - **Continuation indent**: 2

### 8.3. Enable Auto Save

1. **Settings → Appearance & Behavior → System Settings**
2. ✅ **"Save files automatically if application is idle for X sec"**
3. Set: **2 seconds**

---

## ✅ Kiểm tra Setup thành công

### Checklist

- ✅ `npm run dev` chạy không lỗi
- ✅ Browser mở `http://localhost:3000`
- ✅ Trang landing hiển thị đúng
- ✅ Đăng nhập admin (`admin@evswap.com` / `Admin@123456`) thành công
- ✅ IntelliJ hiển thị TypeScript autocomplete
- ✅ Không có TypeScript errors trong code

---

## 🆘 Troubleshooting

### Lỗi: "Cannot find module 'react'"

**Nguyên nhân**: Dependencies chưa cài đặt

**Giải pháp**:

```bash
rm -rf node_modules package-lock.json
npm install
```

---

### Lỗi: "VITE_SUPABASE_PROJECT_ID is not defined"

**Nguyên nhân**: File `.env` chưa được tạo hoặc dev server chưa restart

**Giải pháp**:

1. Đảm bảo file `.env` tồn tại trong root folder
2. Stop dev server (`Ctrl + C` trong Terminal)
3. Chạy lại: `npm run dev`

---

### Lỗi: "Port 3000 is already in use"

**Nguyên nhân**: Có process khác đang dùng port 3000

**Giải pháp**:

**Windows**:
```bash
netstat -ano | findstr :3000
taskkill /PID <PID_NUMBER> /F
```

**macOS/Linux**:
```bash
lsof -ti:3000 | xargs kill -9
```

Hoặc đổi port trong `vite.config.ts`:

```ts
server: {
  port: 3001, // Đổi sang port khác
}
```

---

### IntelliJ không nhận TypeScript

**Giải pháp**:

1. **File → Invalidate Caches...**
2. Check **"Clear file system cache and Local History"**
3. Click **"Invalidate and Restart"**

---

### Autocomplete không hoạt động

**Giải pháp**:

1. **Settings → Languages & Frameworks → TypeScript**
2. Click **"Restart TypeScript service"**
3. Hoặc: **View → Tool Windows → TypeScript → Restart Service**

---

## 🎓 Tips & Tricks

### 1. Quick Actions

- **Find File**: `Ctrl + Shift + N` (Windows) / `Cmd + Shift + O` (Mac)
- **Find in Files**: `Ctrl + Shift + F` (Windows) / `Cmd + Shift + F` (Mac)
- **Go to Definition**: `Ctrl + B` (Windows) / `Cmd + B` (Mac)
- **Refactor/Rename**: `Shift + F6`

### 2. Tối ưu hiệu suất IntelliJ

**Settings → Appearance & Behavior → System Settings**:
- **Memory Heap**: Tăng lên 2048 MB (nếu máy có >= 8GB RAM)

### 3. Extensions hữu ích (IntelliJ Plugins)

1. **Settings → Plugins**
2. Tìm và cài đặt:
   - **Prettier**
   - **GitToolBox**
   - **Rainbow Brackets**

---

## 🎯 Bước tiếp theo

Sau khi setup thành công:

1. 📖 Đọc **README.md** để hiểu cấu trúc project
2. 🔐 Test đăng nhập với tài khoản admin
3. 💻 Bắt đầu code và thêm tính năng mới
4. 🚀 Build production: `npm run build`

---

## 📞 Liên hệ hỗ trợ

Nếu vẫn gặp vấn đề, vui lòng:
- Kiểm tra lại từng bước trong hướng dẫn
- Xem phần Troubleshooting
- Tạo issue với mô tả chi tiết lỗi

**Chúc bạn code vui vẻ! 🎉**
