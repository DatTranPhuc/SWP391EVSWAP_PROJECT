# 📚 Documentation Index

Tổng hợp tất cả tài liệu hướng dẫn cho EV SWAP project.

---

## 🚀 Getting Started

### 1️⃣ Quick Start (Khuyến nghị đọc đầu tiên)

**📄 [QUICKSTART.md](./QUICKSTART.md)**
- ⚡ Chạy project trong 5 phút
- 💻 3 lệnh là xong: install → setup → run
- 🎮 Hướng dẫn test các tính năng chính
- **Đọc trước**: Cho người mới

---

### 2️⃣ Setup IntelliJ IDEA

**📄 [SETUP-INTELLIJ.md](./SETUP-INTELLIJ.md)**
- 🔧 Cấu hình IntelliJ từng bước chi tiết
- ⚙️ Setup Node.js, TypeScript, auto-import
- ▶️ Cách chạy dev server trong IntelliJ
- 🐛 Debug application
- 🆘 Troubleshooting thường gặp
- **Đọc khi**: Muốn setup IntelliJ lần đầu

---

### 3️⃣ Full Documentation

**📄 [README.md](./README.md)**
- 📖 Tài liệu đầy đủ nhất
- 🏗 Cấu trúc project
- 🔐 Tài khoản demo (Admin/Staff/Driver)
- 🛠 Backend setup cho Supabase riêng
- 🎨 Tech stack chi tiết
- 🌐 API endpoints đầy đủ
- **Đọc khi**: Cần hiểu sâu về project

---

## 📦 Export & Deployment

### 4️⃣ Export Guide

**📄 [EXPORT-GUIDE.md](./EXPORT-GUIDE.md)**
- 📤 Giải thích tại sao cần export
- ✅ Checklist các file đã export
- 🔄 Sự khác biệt Figma Make vs Local
- 🎓 Workflow khuyến nghị
- **Đọc khi**: Muốn hiểu về export process

---

### 5️⃣ Deployment Guide

**📄 [DEPLOYMENT.md](./DEPLOYMENT.md)**
- 🚀 Deploy lên Vercel (khuyến nghị)
- 🔥 Deploy lên Netlify
- 📂 Deploy lên GitHub Pages
- 🐳 Deploy với Docker
- ☁️ Deploy Supabase Edge Functions
- 🔐 Environment variables cho production
- **Đọc khi**: Sẵn sàng deploy lên production

---

## 🛠 Technical Documentation

### 6️⃣ Backend Documentation

**📄 [supabase/functions/README.md](./supabase/functions/README.md)**
- 🦕 Supabase Edge Functions (Deno)
- 📡 API routes chi tiết
- 🗄️ Database schema (KV Store)
- 🔍 Testing & debugging
- 🔧 Modify backend
- **Đọc khi**: Muốn hiểu/sửa backend

---

### 7️⃣ Code Guidelines

**📄 [guidelines/Guidelines.md](./guidelines/Guidelines.md)** *(nếu có)*
- 📝 Coding standards
- 🎨 CSS conventions
- 📐 Component structure
- **Đọc khi**: Đóng góp code mới

---

## 🔧 Setup Scripts

### Auto Setup Scripts

**Windows**:
```bash
setup.bat
```

**macOS/Linux**:
```bash
chmod +x setup.sh
./setup.sh
```

Tự động:
- ✅ Check Node.js
- ✅ Install dependencies
- ✅ Create .env file
- ✅ Show next steps

---

## 📊 Document Hierarchy

```
Beginner → Advanced → Expert
   ↓           ↓         ↓
QUICKSTART → README → Backend Docs
   ↓           ↓         ↓
SETUP-      EXPORT    DEPLOYMENT
INTELLIJ    GUIDE
```

---

## 🎯 Roadmap for Reading

### Day 1: Setup & Run
1. **QUICKSTART.md** - Chạy project
2. **SETUP-INTELLIJ.md** - Setup IDE
3. Test app với tài khoản demo

### Day 2: Understanding
1. **README.md** - Đọc full docs
2. **EXPORT-GUIDE.md** - Hiểu cấu trúc
3. Explore code trong `App.tsx` và `app.css`

### Day 3: Advanced
1. **supabase/functions/README.md** - Hiểu backend
2. Sửa code, thêm features
3. Test thoroughly

### Day 4: Production
1. **DEPLOYMENT.md** - Deploy lên Vercel/Netlify
2. Setup custom domain
3. Monitor & optimize

---

## 📝 Quick Reference

### Common Commands

```bash
# Install
npm install

# Development
npm run dev

# Build
npm run build

# Preview
npm run preview

# Type check
npm run type-check
```

### Important Files

| File | Purpose |
|------|---------|
| `App.tsx` | Main application logic |
| `styles/app.css` | Custom CSS styles |
| `vite.config.ts` | Build configuration |
| `.env` | Environment variables |
| `supabase/functions/server/index.tsx` | Backend API |

### Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@evswap.com` | `Admin@123456` |
| Staff | Created by Admin | - |
| Driver | Register via UI | - |

---

## 🆘 Need Help?

### Reading Order for Common Issues

**"Tôi không biết bắt đầu từ đâu"**
→ Đọc **QUICKSTART.md**

**"IntelliJ không nhận diện TypeScript"**
→ Đọc **SETUP-INTELLIJ.md** → Troubleshooting section

**"Backend không hoạt động"**
→ Đọc **README.md** → Backend Setup section

**"Muốn deploy lên Vercel"**
→ Đọc **DEPLOYMENT.md** → Option 1: Vercel

**"Làm sao thêm API route mới?"**
→ Đọc **supabase/functions/README.md** → Modify Backend

**"Build bị lỗi"**
→ Đọc **QUICKSTART.md** → Troubleshooting

---

## 📞 Support Channels

1. 📖 Đọc documentation (file này)
2. 🔍 Search trong docs với `Ctrl + F`
3. 🐛 Check console logs
4. 💻 Google error messages
5. 🤝 Create GitHub issue (nếu có repo)

---

## 📅 Document Status

| Document | Status | Last Updated |
|----------|--------|--------------|
| QUICKSTART.md | ✅ Complete | 2025-10-01 |
| README.md | ✅ Complete | 2025-10-01 |
| SETUP-INTELLIJ.md | ✅ Complete | 2025-10-01 |
| EXPORT-GUIDE.md | ✅ Complete | 2025-10-01 |
| DEPLOYMENT.md | ✅ Complete | 2025-10-01 |
| supabase/functions/README.md | ✅ Complete | 2025-10-01 |

---

## 🎓 Recommended Learning Path

```
1. QUICKSTART.md (5 min)
   ↓
2. Run app & test features (10 min)
   ↓
3. SETUP-INTELLIJ.md (15 min)
   ↓
4. Read code in App.tsx (30 min)
   ↓
5. README.md full (30 min)
   ↓
6. Backend docs (30 min)
   ↓
7. Make changes & test (∞)
   ↓
8. DEPLOYMENT.md when ready
```

---

## ✨ Contributing

Nếu bạn tìm thấy lỗi trong docs hoặc muốn cải thiện:

1. Edit file markdown tương ứng
2. Test thay đổi
3. Submit pull request (nếu có repo)

---

## 📄 License

All documentation is under MIT License - see [LICENSE](./LICENSE)

---

**Happy learning! 📚✨**
