# 🚀 Hướng dẫn Deploy Production

Hướng dẫn deploy EV SWAP lên các nền tảng hosting phổ biến.

---

## 📦 Build Production

Trước khi deploy, build project:

```bash
npm run build
```

Folder `dist/` sẽ chứa static files để deploy.

---

## 🌐 Option 1: Deploy lên Vercel (Khuyến nghị)

### Bước 1: Cài đặt Vercel CLI

```bash
npm install -g vercel
```

### Bước 2: Login

```bash
vercel login
```

### Bước 3: Deploy

```bash
vercel --prod
```

### Bước 4: Cấu hình Environment Variables

1. Vào Vercel Dashboard → Project Settings → Environment Variables
2. Thêm:
   - `VITE_SUPABASE_PROJECT_ID`
   - `VITE_SUPABASE_ANON_KEY`

### Bước 5: Redeploy

```bash
vercel --prod
```

**✅ Done!** App sẽ có URL: `https://your-app.vercel.app`

---

## 🔥 Option 2: Deploy lên Netlify

### Bước 1: Cài đặt Netlify CLI

```bash
npm install -g netlify-cli
```

### Bước 2: Login

```bash
netlify login
```

### Bước 3: Deploy

```bash
netlify deploy --prod
```

Chọn:
- **Publish directory**: `dist`

### Bước 4: Environment Variables

1. Netlify Dashboard → Site Settings → Environment Variables
2. Thêm các biến môi trường như Vercel

---

## 📂 Option 3: Deploy lên GitHub Pages

### Bước 1: Cài đặt gh-pages

```bash
npm install --save-dev gh-pages
```

### Bước 2: Thêm scripts vào package.json

```json
{
  "scripts": {
    "predeploy": "npm run build",
    "deploy": "gh-pages -d dist"
  }
}
```

### Bước 3: Cấu hình base trong vite.config.ts

```ts
export default defineConfig({
  base: '/repository-name/',
  // ... other config
})
```

### Bước 4: Deploy

```bash
npm run deploy
```

**⚠️ Lưu ý**: GitHub Pages không hỗ trợ environment variables, cần hardcode hoặc dùng khác.

---

## 🐳 Option 4: Deploy với Docker

### Dockerfile

```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### Build & Run

```bash
# Build image
docker build -t evswap .

# Run container
docker run -p 8080:80 evswap
```

---

## ☁️ Backend Deployment

### Option A: Tiếp tục dùng Figma Make Backend

✅ Không cần deploy gì thêm, backend đã chạy sẵn.

### Option B: Deploy Supabase Edge Functions

#### Bước 1: Login Supabase CLI

```bash
supabase login
```

#### Bước 2: Link Project

```bash
supabase link --project-ref YOUR_PROJECT_ID
```

#### Bước 3: Deploy Functions

```bash
supabase functions deploy server
```

#### Bước 4: Set Environment Variables

```bash
supabase secrets set SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
supabase secrets set SUPABASE_DB_URL=your_db_url
```

#### Bước 5: Test

```bash
curl https://YOUR_PROJECT_ID.supabase.co/functions/v1/make-server-c0c28b62/stations
```

---

## 🔐 Environment Variables cho Production

### Frontend (.env.production)

```env
VITE_SUPABASE_PROJECT_ID=your_production_project_id
VITE_SUPABASE_ANON_KEY=your_production_anon_key
```

### Backend (Supabase Secrets)

```bash
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_SERVICE_ROLE_KEY=eyJhbG...
SUPABASE_DB_URL=postgresql://...
```

---

## 📊 Monitoring & Logging

### Vercel Analytics

Thêm vào `App.tsx`:

```tsx
import { Analytics } from '@vercel/analytics/react';

export default function App() {
  return (
    <>
      {/* Your app */}
      <Analytics />
    </>
  );
}
```

### Supabase Logs

Xem logs Edge Functions:

```bash
supabase functions logs server
```

---

## ✅ Checklist Deploy

- [ ] Build thành công: `npm run build`
- [ ] Test local build: `npm run preview`
- [ ] Environment variables đã cấu hình đúng
- [ ] Backend đang hoạt động (test với curl/Postman)
- [ ] Admin account đã được tạo
- [ ] Database đã có bảng `kv_store_c0c28b62`
- [ ] CORS đã được cấu hình đúng

---

## 🆘 Troubleshooting Production

### 404 khi reload page

**Nguyên nhân**: SPA routing không được cấu hình

**Giải pháp (Vercel/Netlify)**: Tạo file `vercel.json` hoặc `netlify.toml`:

**vercel.json**:
```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

**netlify.toml**:
```toml
[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

### API CORS Error

Đảm bảo backend có CORS headers:

```ts
app.use('*', cors()); // Trong Hono server
```

---

## 📞 Support

Nếu gặp vấn đề khi deploy, kiểm tra:
1. Build logs
2. Runtime logs
3. Network tab trong DevTools

**Good luck! 🚀**
