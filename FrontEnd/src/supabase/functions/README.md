# 🦕 Supabase Edge Functions

Backend cho EV SWAP chạy trên Supabase Edge Functions (Deno runtime).

---

## 📁 Cấu trúc

```
supabase/functions/
└── server/
    ├── index.tsx       - API routes (Hono framework)
    └── kv_store.tsx    - Key-Value store utility (PROTECTED)
```

---

## ⚠️ File Protected

**`kv_store.tsx`** là file tự động generate bởi Figma Make.  
❌ **KHÔNG được chỉnh sửa hoặc xóa file này!**

---

## 🚀 Deploy Edge Functions (Nếu dùng Supabase riêng)

### Yêu cầu

- Supabase CLI: `npm install -g supabase`
- Supabase account & project

### Bước 1: Login

```bash
supabase login
```

### Bước 2: Link project

```bash
supabase link --project-ref YOUR_PROJECT_ID
```

### Bước 3: Deploy

```bash
supabase functions deploy server
```

### Bước 4: Set Environment Variables

```bash
supabase secrets set SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
supabase secrets set SUPABASE_DB_URL=your_database_url
```

---

## 🔍 Test Edge Function

```bash
# List functions
supabase functions list

# View logs
supabase functions logs server

# Test endpoint
curl https://YOUR_PROJECT_ID.supabase.co/functions/v1/make-server-c0c28b62/stations
```

---

## 📡 API Routes

Tất cả routes có prefix: `/make-server-c0c28b62/`

### Authentication
- `POST /signup` - Đăng ký driver
- `POST /login` - Đăng nhập
- `GET /me` - Lấy user hiện tại

### Vehicles
- `POST /vehicles` - Đăng ký xe
- `GET /vehicles` - Lấy danh sách xe

### Admin
- `POST /admin/staff` - Tạo staff
- `GET /admin/users` - Lấy all users
- `POST /admin/stations` - Tạo trạm

### Public
- `GET /stations` - Lấy trạm (public)

### Transactions
- `POST /transactions` - Tạo giao dịch (staff)
- `GET /transactions` - Lấy lịch sử

### Support
- `POST /tickets` - Tạo ticket
- `GET /tickets` - Lấy tickets
- `PATCH /tickets/:id` - Cập nhật ticket

---

## 🛠 Local Development (Supabase CLI)

### Start local server

```bash
supabase functions serve server
```

Function sẽ chạy tại: `http://localhost:54321/functions/v1/make-server-c0c28b62/`

### Test local

```bash
curl http://localhost:54321/functions/v1/make-server-c0c28b62/stations
```

---

## 🔐 Environment Variables

Backend cần các biến môi trường sau:

| Variable | Description | Required |
|----------|-------------|----------|
| `SUPABASE_URL` | Supabase project URL | ✅ Yes |
| `SUPABASE_SERVICE_ROLE_KEY` | Service role key | ✅ Yes |
| `SUPABASE_DB_URL` | PostgreSQL connection string | ❌ No (dùng KV store) |

---

## 🗄️ Database Schema

Backend sử dụng **Key-Value Store** thay vì SQL tables.

### KV Store Keys Pattern

```
user:{user_id}                 - User data
user:{email}                   - Email -> User ID mapping
vehicle:{vehicle_id}           - Vehicle data
driver_vehicle:{driver_id}     - Driver -> Vehicle mapping
station:{station_id}           - Station data
battery:{battery_id}           - Battery data
transaction:{transaction_id}   - Transaction data
ticket:{ticket_id}             - Support ticket data
```

### Tại sao dùng KV Store?

✅ Không cần migrations  
✅ Schema flexible  
✅ Phù hợp cho prototyping  
✅ Đơn giản hơn SQL cho use case này  

---

## 🔧 Modify Backend

### Thêm route mới

Edit `index.tsx`:

```typescript
app.post('/make-server-c0c28b62/new-route', async (c) => {
  try {
    // Your logic here
    return c.json({ success: true });
  } catch (error) {
    console.log(`Error: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});
```

### Sử dụng KV Store

```typescript
import * as kv from './kv_store.tsx';

// Set value
await kv.set('key', { data: 'value' });

// Get value
const data = await kv.get('key');

// Get by prefix
const users = await kv.getByPrefix('user:');

// Delete
await kv.del('key');
```

---

## ⚠️ Important Notes

### CORS

CORS đã được cấu hình mở:

```typescript
app.use('*', cors());
```

Nếu deploy production, nên restrict origins:

```typescript
app.use('*', cors({
  origin: ['https://your-domain.com'],
  credentials: true,
}));
```

### Authentication

Routes cần auth sẽ check access token:

```typescript
const accessToken = c.req.header('Authorization')?.split(' ')[1];
const { data: { user } } = await supabase.auth.getUser(accessToken);
if (!user) {
  return c.json({ error: 'Unauthorized' }, 401);
}
```

### Error Handling

Luôn log errors và return proper status codes:

```typescript
try {
  // Your code
} catch (error) {
  console.log(`Error context: ${error}`);
  return c.json({ error: 'Error message' }, 500);
}
```

---

## 🐛 Debugging

### View logs

```bash
supabase functions logs server --limit 100
```

### Invoke function manually

```bash
supabase functions invoke server \
  --data '{"email":"test@example.com"}' \
  --method POST
```

---

## 📚 Tech Stack

- **Runtime**: Deno 1.38+
- **Framework**: Hono (Express-like for Deno)
- **Database**: Supabase PostgreSQL (KV Store)
- **Auth**: Supabase Auth
- **CORS**: Hono CORS middleware

---

## 🔗 Resources

- [Supabase Edge Functions Docs](https://supabase.com/docs/guides/functions)
- [Hono Framework](https://hono.dev/)
- [Deno Documentation](https://deno.land/manual)

---

## 🆘 Troubleshooting

### Function không deploy được

```bash
# Check CLI version
supabase --version

# Update CLI
npm update -g supabase

# Force deploy
supabase functions deploy server --no-verify-jwt
```

### CORS errors

Đảm bảo `cors()` middleware được add:

```typescript
import { cors } from 'npm:hono/cors';
app.use('*', cors());
```

### Database connection errors

Kiểm tra `SUPABASE_SERVICE_ROLE_KEY` đã set chưa:

```bash
supabase secrets list
```

---

✅ **Lưu ý**: Nếu bạn đang dùng backend Figma Make (default), **KHÔNG CẦN** deploy gì cả!
