# 👋 START HERE - Chào mừng đến với EV SWAP!

Đây là **điểm bắt đầu** cho project EV SWAP Battery Management System.

---

## 🎯 Bạn đang ở đâu?

✅ **Project này đã được EXPORT từ Figma Make**  
✅ Hoàn toàn có thể chạy trong **IntelliJ IDEA** hoặc **VSCode**  
✅ Không cần Figma Make nữa (nhưng backend vẫn dùng endpoint của Figma Make)

---

## ⚡ Chạy ngay trong 30 giây

### Windows
```bash
setup.bat
```

### macOS/Linux
```bash
chmod +x setup.sh
./setup.sh
```

Sau đó:
```bash
npm run dev
```

Mở browser: **http://localhost:3000**

---

## 📚 Đọc tài liệu gì tiếp?

### 🚀 Nếu bạn muốn chạy thử NGAY

➡️ Đọc: **[QUICKSTART.md](./QUICKSTART.md)**

Bạn sẽ học:
- Cài đặt dependencies
- Chạy dev server
- Test app với tài khoản demo
- **Thời gian: 5 phút**

---

### 🔧 Nếu bạn muốn setup IntelliJ IDEA

➡️ Đọc: **[SETUP-INTELLIJ.md](./SETUP-INTELLIJ.md)**

Bạn sẽ học:
- Cấu hình IntelliJ chi tiết
- Setup TypeScript & Node.js
- Chạy dev server trong IDE
- Debug application
- **Thời gian: 10-15 phút**

---

### 📖 Nếu bạn muốn hiểu TOÀN BỘ project

➡️ Đọc: **[README.md](./README.md)**

Bạn sẽ học:
- Cấu trúc project
- Tech stack
- Backend architecture
- API endpoints
- Database schema
- **Thời gian: 30 phút**

---

### 📦 Nếu bạn muốn hiểu về EXPORT process

➡️ Đọc: **[EXPORT-GUIDE.md](./EXPORT-GUIDE.md)**

Bạn sẽ học:
- Tại sao cần export?
- Files đã được export
- Sự khác biệt Figma Make vs Local
- Backend options
- **Thời gian: 10 phút**

---

### 🚀 Nếu bạn muốn DEPLOY lên production

➡️ Đọc: **[DEPLOYMENT.md](./DEPLOYMENT.md)**

Bạn sẽ học:
- Deploy lên Vercel/Netlify
- Setup environment variables
- Deploy backend (nếu cần)
- Custom domain
- **Thời gian: 20 phút**

---

### 📚 Nếu bạn muốn xem TẤT CẢ tài liệu

➡️ Đọc: **[DOCS-INDEX.md](./DOCS-INDEX.md)**

Bạn sẽ thấy:
- Danh sách tất cả docs
- Hướng dẫn đọc docs theo thứ tự
- Quick reference
- Roadmap for reading

---

## 🎮 Demo Accounts (Test ngay!)

### Admin
- **Email**: `admin@evswap.com`
- **Password**: `Admin@123456`
- **Features**: Quản lý users, tạo staff, tạo trạm

### Staff
- Được tạo bởi Admin trong dashboard
- **Features**: Xử lý tickets, quản lý giao dịch

### Driver
- Đăng ký qua nút "Đăng ký" trên landing page
- **Features**: Đăng ký xe, xem lịch sử, gửi ticket

---

## 💡 Quick Tips

### Nếu gặp lỗi khi chạy:

```bash
# Xóa và cài lại dependencies
rm -rf node_modules package-lock.json
npm install

# Chạy lại
npm run dev
```

### Nếu port 3000 bị chiếm:

Edit `vite.config.ts`:
```ts
server: {
  port: 3001, // Đổi sang port khác
}
```

### Nếu IntelliJ không nhận TypeScript:

**Settings → Languages & Frameworks → TypeScript → Restart Service**

---

## 🗂 File Structure Overview

```
evswap/
├── 📄 START-HERE.md          ← BẠN ĐANG Ở ĐÂY
├── 📄 QUICKSTART.md          ← Đọc tiếp đây
├── 📄 README.md              ← Full docs
├── 📄 SETUP-INTELLIJ.md      ← Setup IDE
│
├── ⚛️ App.tsx                ← Main React component
├── 🎨 styles/app.css         ← Custom CSS
├── 🔧 vite.config.ts         ← Build config
├── 📦 package.json           ← Dependencies
│
├── supabase/functions/       ← Backend (Deno)
│   └── server/
│       └── index.tsx         ← API routes
│
└── components/               ← UI components
    └── ui/                   ← shadcn components
```

---

## 🎯 Workflow Khuyến Nghị

### Day 1: Setup & Run (Hôm nay!)
1. ✅ Chạy `setup.bat` hoặc `setup.sh`
2. ✅ Chạy `npm run dev`
3. ✅ Test app với tài khoản admin
4. ✅ Explore UI

### Day 2: Understanding
1. 📖 Đọc README.md
2. 👀 Xem code trong App.tsx
3. 🎨 Xem CSS trong app.css
4. 🔍 Test các tính năng

### Day 3: Development
1. ✏️ Sửa code
2. 🔄 Test hot reload
3. 🐛 Debug nếu có lỗi
4. 💾 Commit changes

### Day 4: Production
1. 🏗 Build: `npm run build`
2. 🚀 Deploy lên Vercel
3. 🌐 Test production app
4. 🎉 Share với team!

---

## 🆘 Cần giúp đỡ?

### Checklist Debugging:

- [ ] Node.js >= 18.0.0 đã cài?
- [ ] `npm install` chạy thành công?
- [ ] File `.env` đã tồn tại?
- [ ] Dev server đang chạy?
- [ ] Browser đã mở `localhost:3000`?
- [ ] Console có lỗi gì không?

### Nếu vẫn lỗi:

1. 📖 Đọc **QUICKSTART.md** → Troubleshooting
2. 📖 Đọc **SETUP-INTELLIJ.md** → Troubleshooting
3. 🔍 Google error message
4. 💬 Hỏi team hoặc create issue

---

## ✨ What's Next?

Sau khi chạy được app:

1. 🎨 **Customize**: Sửa CSS, thêm features
2. 🔧 **Backend**: Đọc backend docs
3. 🚀 **Deploy**: Đưa lên production
4. 📱 **Mobile**: Improve responsive
5. 🌙 **Dark Mode**: Thêm theme switcher

---

## 📞 Support

- 📚 **Documentation**: Xem DOCS-INDEX.md
- 🐛 **Issues**: (GitHub issues URL)
- 💬 **Discussion**: (Discord/Slack URL)
- 📧 **Email**: (Support email)

---

## 🎉 Ready?

**Bắt đầu ngay:**

```bash
npm install
npm run dev
```

**Hoặc đọc tiếp:**
- ⚡ [QUICKSTART.md](./QUICKSTART.md) - Nếu vội
- 🔧 [SETUP-INTELLIJ.md](./SETUP-INTELLIJ.md) - Nếu dùng IntelliJ
- 📖 [README.md](./README.md) - Nếu muốn hiểu sâu

---

**Welcome to EV SWAP! 🔋⚡**

Let's build something amazing together! 🚀
