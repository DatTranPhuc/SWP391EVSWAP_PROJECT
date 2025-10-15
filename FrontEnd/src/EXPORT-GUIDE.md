# 📦 Export Guide - Chạy code ngoài Figma Make

Hướng dẫn export và chạy project EV SWAP ngoài Figma Make environment.

---

## 🎯 Tại sao cần export?

Figma Make là môi trường **web-based** chỉ chạy trong browser. Nếu bạn muốn:
- ✅ Code trong **IntelliJ IDEA** hoặc **VSCode**
- ✅ Chạy **offline**
- ✅ Tùy chỉnh **build process**
- ✅ Deploy lên **hosting riêng**

→ Bạn cần export code ra môi trường local.

---

## 📋 Project này đã được export!

✅ Tất cả file cần thiết đã được tạo sẵn:

```
✅ package.json          - Dependencies & scripts
✅ vite.config.ts        - Vite configuration
✅ tsconfig.json         - TypeScript config
✅ index.html            - Entry HTML
✅ main.tsx              - React entry point
✅ .env                  - Environment variables
✅ README.md             - Hướng dẫn đầy đủ
✅ QUICKSTART.md         - Hướng dẫn nhanh
✅ SETUP-INTELLIJ.md     - Setup IntelliJ chi tiết
```

---

## 🚀 Cách sử dụng

### Option A: Quick Start (5 phút)

```bash
# 1. Cài dependencies
npm install

# 2. Chạy dev server
npm run dev
```

➡️ Xem **QUICKSTART.md** để biết thêm chi tiết

### Option B: Setup trong IntelliJ (10 phút)

➡️ Xem **SETUP-INTELLIJ.md** để có hướng dẫn từng bước

### Option C: Tự động setup (Windows/Mac/Linux)

**Windows**:
```bash
setup.bat
```

**macOS/Linux**:
```bash
chmod +x setup.sh
./setup.sh
```

---

## ⚙️ Sự khác biệt giữa Figma Make và Local

| Tính năng | Figma Make | Local (Exported) |
|-----------|------------|------------------|
| Môi trường | Web browser | IntelliJ/VSCode |
| Build tool | Vite (tự động) | Vite (cần setup) |
| Dependencies | Có sẵn | Phải `npm install` |
| Backend | Supabase Edge Functions | Giữ nguyên endpoint |
| Hot reload | ✅ Tự động | ✅ Tự động |
| TypeScript | ✅ Có | ✅ Có |
| Debug | Browser DevTools | IDE Debugger |

---

## 🔄 Backend Options

### Option 1: Tiếp tục dùng Figma Make Backend (Khuyến nghị)

✅ **Ưu điểm**:
- Không cần setup gì thêm
- Backend đã chạy sẵn
- Edge Functions đã deploy

⚠️ **Lưu ý**:
- Cần internet connection
- Backend có thể offline nếu Figma Make project đóng

**Config** (đã có sẵn trong `.env`):
```env
VITE_SUPABASE_PROJECT_ID=yktfgqpcmdgtycnyxpby
VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Option 2: Setup Supabase riêng

📖 Xem **README.md** section **Backend Setup** để biết chi tiết.

Tóm tắt:
1. Tạo Supabase project mới
2. Deploy Edge Functions từ `/supabase/functions/`
3. Cập nhật `.env` với credentials mới

---

## 📝 Files được giữ nguyên vs Modified

### Giữ nguyên (không thay đổi):
- ✅ **App.tsx** - Logic của ứng dụng
- ✅ **styles/app.css** - Custom styles
- ✅ **styles/globals.css** - Tailwind v4 config
- ✅ **supabase/functions/** - Backend code

### Mới thêm (để chạy local):
- ✨ **package.json** - Node.js dependencies
- ✨ **vite.config.ts** - Vite build config
- ✨ **tsconfig.json** - TypeScript settings
- ✨ **index.html** - HTML template
- ✨ **main.tsx** - React mount point
- ✨ **.env** - Environment variables
- ✨ **utils/supabase/config.ts** - Đọc env vars

### Modified (nhỏ):
- 🔄 **App.tsx** - Import từ `config.ts` thay vì `info.tsx`

---

## 🎓 Workflow khuyến nghị

### 1. Development (Code mới)

Code trong **IntelliJ/VSCode local**:
- ✅ Full IDE features (autocomplete, refactor, debug)
- ✅ Offline coding
- ✅ Git integration tốt hơn

### 2. Testing

Test trong **browser local**: `http://localhost:3000`
- ✅ Hot reload nhanh
- ✅ DevTools debugging

### 3. Production

Deploy lên **Vercel/Netlify**:
- ✅ Tự động build & deploy
- ✅ CDN global
- ✅ HTTPS miễn phí

➡️ Xem **DEPLOYMENT.md** để biết cách deploy

---

## ✅ Checklist sau khi export

Đảm bảo bạn đã có:

- [ ] Node.js 18+ đã cài đặt
- [ ] npm/yarn/pnpm đã cài đặt
- [ ] File `.env` đã được tạo
- [ ] `npm install` chạy thành công
- [ ] `npm run dev` khởi động server
- [ ] Browser mở `http://localhost:3000`
- [ ] Đăng nhập admin thành công
- [ ] IntelliJ/VSCode nhận diện TypeScript

---

## 🆘 Troubleshooting Export

### "Module not found"

```bash
npm install
```

### "Cannot find config file"

Đảm bảo các file này tồn tại:
- `vite.config.ts`
- `tsconfig.json`
- `package.json`

### TypeScript errors

```bash
npm run type-check
```

### Build errors

```bash
rm -rf node_modules package-lock.json dist
npm install
npm run build
```

---

## 📚 Tài liệu liên quan

1. **QUICKSTART.md** - Chạy nhanh trong 5 phút
2. **README.md** - Full documentation
3. **SETUP-INTELLIJ.md** - Setup IntelliJ chi tiết
4. **DEPLOYMENT.md** - Deploy production

---

## 🎉 Hoàn thành!

Bạn đã có project **hoàn toàn độc lập** có thể chạy ngoài Figma Make!

**Next steps**:
1. Chạy `npm run dev` và test
2. Code thêm features mới
3. Deploy lên production

**Happy coding! 🚀**
