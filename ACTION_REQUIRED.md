# ⚠️ ACTION REQUIRED - DỪNG ỨNG DỤNG VÀ REBUILD

## ❌ VẤN ĐỀ
Có Java processes đang chạy đang lock các files trong thư mục `target`, khiến không thể clean/rebuild project được.

## ✅ GIẢI PHÁP (Làm theo thứ tự):

### Step 1: Dừng tất cả Java processes

**Cách 1: Trong IntelliJ IDEA**
1. Tìm nút **Stop** (màu đỏ vuông) ở toolbar
2. Click vào nút đó để dừng ứng dụng
3. Hoặc nhấn `Ctrl + F2`
4. Chờ đến khi thấy "Process finished with exit code X"

**Cách 2: Trong Task Manager**
1. Nhấn `Ctrl + Shift + Esc` để mở Task Manager
2. Tìm tab "Details"
3. Tìm tất cả processes có tên `java.exe`
4. Right-click vào từng process → "End task"
5. Xác nhận "End process"

**Cách 3: CMD (Nếu cần thiết)**
Mở PowerShell/CMD với quyền Admin và chạy:
```powershell
taskkill /F /IM java.exe
```

### Step 2: Xóa thư mục target thủ công

1. Mở IntelliJ IDEA
2. Trong Project Explorer, tìm thư mục **target**
3. Right-click vào thư mục **target** → **Delete**
4. Chọn "Do Refactor" và xác nhận

### Step 3: Invalidate Caches và Rebuild

1. Trong IntelliJ: **File** → **Invalidate Caches...**
2. Tick tất cả các checkbox
3. Click **Invalidate and Restart**
4. Chờ IntelliJ restart (1-2 phút)

### Step 4: Rebuild Project

Sau khi IntelliJ restart:

1. **Build** → **Rebuild Project**
2. Hoặc nhấn `Ctrl + Shift + F9`
3. Chờ đến khi build hoàn tất (status bar ở dưới)

### Step 5: Run Application

1. **Run** → **Run 'Swp391To4Application'**
2. Hoặc nhấn `Shift + F10`

## ✅ KẾT QUẢ MONG ĐỢI

Nếu thành công, bạn sẽ thấy trong console:
```
Started Swp391To4Application in X.XXX seconds
```

## 🔍 KIỂM TRA

Sau khi ứng dụng khởi động:

1. Mở browser: http://localhost:8080
2. Login vào tài khoản
3. Test tính năng payment:
   - Nạp tiền ví qua PayOS
   - Tạo reservation
   - Kiểm tra wallet balance
   - Kiểm tra transaction history

## 📝 CẤU TRÚC HIỆN TẠI

### Files đã hoàn thành:
- ✅ `RestTemplateConfig.java` - Configuration cho RestTemplate
- ✅ `PaymentController.java` - Webhook endpoint
- ✅ `PaymentService.java` - Business logic
- ✅ `PayOsUtil.java` - HMAC verification
- ✅ Tất cả PayOS DTOs
- ✅ `SecurityConfig.java` - PasswordEncoder bean
- ✅ `application.properties` - PayOS configuration

### Chức năng đã hoàn thành:
- ✅ PayOS integration
- ✅ Webhook handling
- ✅ Signature verification
- ✅ Top-up flow
- ✅ Refund logic (60% cho no-show)
- ✅ Wallet management

---

**LƯU Ý:** Không skip các bước trên! Việc dừng processes và invalidate caches là **BẮT BUỘC** để Spring Boot nhận diện được tất cả beans.

