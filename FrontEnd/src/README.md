# EV SWAP - Hệ thống quản lý đổi pin xe máy điện

Hệ thống quản lý đổi pin xe máy điện thông minh với 3 vai trò: Driver, Staff, và Admin.

## 🚀 Cài đặt và chạy project

### Yêu cầu hệ thống

- **Node.js** >= 18.0.0 (khuyến nghị dùng LTS)
- **npm** hoặc **yarn** hoặc **pnpm**
- **IntelliJ IDEA Ultimate** (hoặc VSCode nếu muốn)

### Bước 1: Cài đặt dependencies

```bash
# Sử dụng npm
npm install

# Hoặc yarn
yarn install

# Hoặc pnpm
pnpm install
```

### Bước 2: Cấu hình environment variables

1. Copy file `.env.example` thành `.env`:

```bash
cp .env.example .env
```

2. **QUAN TRỌNG**: Chọn một trong hai options:

#### Option 1: Sử dụng backend Figma Make (Khuyến nghị cho testing)

File `.env` đã có sẵn thông tin, không cần thay đổi gì:

```env
VITE_SUPABASE_PROJECT_ID=yktfgqpcmdgtycnyxpby
VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

✅ **Lợi ích**: Không cần setup gì thêm, chạy ngay được.  
⚠️ **Lưu ý**: Backend này chạy trên Figma Make, chỉ dùng để test/demo.

#### Option 2: Setup Supabase project riêng

1. Tạo project tại [supabase.com](https://supabase.com)
2. Lấy `Project ID` và `Anon Key` từ **Project Settings → API**
3. Cập nhật file `.env`:

```env
VITE_SUPABASE_PROJECT_ID=your_project_id
VITE_SUPABASE_ANON_KEY=your_anon_key
```

4. Deploy Edge Functions (xem mục **Backend Setup** bên dưới)

### Bước 3: Chạy development server

```bash
npm run dev
```

Ứng dụng sẽ mở tại: **http://localhost:3000**

### Bước 4: Build cho production

```bash
npm run build
```

File build sẽ được tạo trong thư mục `dist/`.

---

## 📁 Cấu trúc project

```
evswap/
├── App.tsx                 # Main React component
├── main.tsx                # Entry point
├── index.html              # HTML template
├── package.json            # Dependencies
├── vite.config.ts          # Vite configuration
├── tsconfig.json           # TypeScript config
├── components/             # React components
│   └── ui/                # shadcn/ui components
├── styles/
│   ├── globals.css        # Tailwind CSS v4
│   └── app.css            # Custom CSS
├── utils/
│   └── supabase/
│       ├── info.tsx       # Supabase info (Figma Make)
│       └── config.ts      # Supabase config (Local)
└── supabase/
    └── functions/
        └── server/         # Backend Edge Functions (Deno)
            ├── index.tsx
            └── kv_store.tsx
```

---

## 🔐 Tài khoản Demo

### Admin
- **Email**: `admin@evswap.com`
- **Password**: `Admin@123456`
- **Quyền hạn**: Quản lý toàn bộ hệ thống, tạo Staff, tạo trạm đổi pin

### Staff
- Được tạo bởi Admin trong dashboard
- **Quyền hạn**: Xử lý yêu cầu hỗ trợ, quản lý giao dịch đổi pin

### Driver
- Đăng ký qua nút "Đăng ký" trên trang chủ
- **Quyền hạn**: Đăng ký xe, xem lịch sử đổi pin, gửi yêu cầu hỗ trợ

---

## 🛠 Backend Setup (Nếu dùng Option 2)

Backend hiện tại chạy trên **Supabase Edge Functions** (Deno runtime).

### Cài đặt Supabase CLI

```bash
npm install -g supabase
```

### Login vào Supabase

```bash
supabase login
```

### Link project

```bash
supabase link --project-ref YOUR_PROJECT_ID
```

### Deploy Edge Functions

```bash
supabase functions deploy server
```

### Cấu hình Environment Variables cho Edge Functions

```bash
supabase secrets set SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
```

---

## 🎨 Công nghệ sử dụng

### Frontend
- ⚛️ **React 18** - UI Framework
- 🎨 **Tailwind CSS v4** - Styling (Pure CSS, không dùng Tailwind classes)
- ⚡ **Vite** - Build tool
- 📘 **TypeScript** - Type safety

### Backend
- 🦕 **Deno** - Runtime cho Edge Functions
- 🔥 **Hono** - Web framework
- 🗄️ **Supabase** - Database, Auth, Storage
- 🔐 **Supabase Auth** - Authentication & Authorization

### Database
- **PostgreSQL** (via Supabase)
- **Key-Value Store** - Lưu trữ dữ liệu trong bảng `kv_store_c0c28b62`

---

## 📝 Hướng dẫn sử dụng trong IntelliJ IDEA

### 1. Mở project

1. **File → Open** → Chọn thư mục project
2. IntelliJ sẽ tự động detect là React/TypeScript project

### 2. Cấu hình Node.js interpreter

1. **Settings → Languages & Frameworks → Node.js**
2. Chọn Node.js version (>= 18.0.0)
3. Enable **Coding assistance for Node.js**

### 3. Chạy npm scripts

**Cách 1: Dùng npm tool window**
1. View → Tool Windows → npm
2. Double-click vào script `dev`

**Cách 2: Dùng Terminal**
1. View → Tool Windows → Terminal
2. Chạy: `npm run dev`

**Cách 3: Tạo Run Configuration**
1. Run → Edit Configurations
2. Add → npm
3. Package.json: `package.json`
4. Command: `run`
5. Scripts: `dev`

### 4. Debug JavaScript/TypeScript

IntelliJ IDEA Ultimate hỗ trợ debug React app:

1. Tạo **JavaScript Debug** configuration
2. URL: `http://localhost:3000`
3. Đặt breakpoint trong code TypeScript
4. Click **Debug** button

---

## 🌐 API Endpoints

Tất cả API routes có prefix: `/make-server-c0c28b62/`

### Authentication
- `POST /signup` - Đăng ký Driver
- `POST /login` - Đăng nhập
- `GET /me` - Lấy thông tin user hiện tại

### Vehicles (Driver)
- `POST /vehicles` - Đăng ký xe
- `GET /vehicles` - Lấy danh sách xe của driver

### Admin
- `POST /admin/staff` - Tạo tài khoản Staff
- `GET /admin/users` - Lấy danh sách tất cả users
- `POST /admin/stations` - Tạo trạm đổi pin

### Stations
- `GET /stations` - Lấy danh sách trạm (public)

### Transactions
- `POST /transactions` - Tạo giao dịch đổi pin (Staff)
- `GET /transactions` - Lấy lịch sử giao dịch

### Support Tickets
- `POST /tickets` - Tạo yêu cầu hỗ trợ
- `GET /tickets` - Lấy danh sách tickets
- `PATCH /tickets/:id` - Cập nhật trạng thái ticket (Staff/Admin)

---

## 🔧 Troubleshooting

### Lỗi: `Cannot find module 'react'`

```bash
rm -rf node_modules package-lock.json
npm install
```

### Lỗi: `VITE_SUPABASE_PROJECT_ID is not defined`

1. Kiểm tra file `.env` đã tồn tại chưa
2. Restart dev server: `npm run dev`

### Lỗi: Backend không hoạt động

**Nếu dùng Option 1 (Figma Make backend)**:
- Backend này chỉ hoạt động khi Figma Make project đang chạy
- Nếu Figma Make đóng, backend sẽ không hoạt động

**Nếu dùng Option 2 (Supabase riêng)**:
- Kiểm tra Edge Functions đã deploy chưa: `supabase functions list`
- Xem logs: `supabase functions logs server`

### IntelliJ không nhận diện TypeScript

1. **Settings → Languages & Frameworks → TypeScript**
2. Enable **TypeScript Language Service**
3. Restart IntelliJ

---

## 📚 Tài liệu tham khảo

- [React Documentation](https://react.dev)
- [Vite Documentation](https://vitejs.dev)
- [Tailwind CSS v4](https://tailwindcss.com/docs/v4-beta)
- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Edge Functions](https://supabase.com/docs/guides/functions)
- [IntelliJ IDEA JavaScript Support](https://www.jetbrains.com/help/idea/javascript-specific-guidelines.html)

---

## 🤝 Hỗ trợ

Nếu gặp vấn đề, vui lòng tạo issue hoặc liên hệ team phát triển.

---

## 📄 License

MIT License - Xem file LICENSE để biết thêm chi tiết.
