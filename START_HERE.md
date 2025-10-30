# 🚨 START HERE - CRITICAL INSTRUCTIONS

## ⚠️ CRITICAL ISSUE DETECTED

Error: `No qualifying bean of type 'org.springframework.web.client.RestTemplate' available`

## 🔧 ROOT CAUSE

IntelliJ IDEA đang sử dụng **compiled classes cũ** (stale compiled classes). Cần force rebuild toàn bộ project.

## ✅ SOLUTION (MUST DO IN ORDER!)

### Step 1: STOP Application

**Method A - In IntelliJ:**
```
1. Tìm nút đỏ vuông (Stop) ở toolbar
2. Click vào đó
3. Hoặc nhấn Ctrl + F2
```

**Method B - Kill Process:**
```
1. Mở Task Manager (Ctrl + Shift + Esc)
2. Tìm process "java.exe"
3. End Task
```

### Step 2: Invalidate Caches (IMPORTANT!)

```
1. File → Invalidate Caches...
2. Tick tất cả checkboxes
3. Click "Invalidate and Restart"
4. Chờ IntelliJ restart tự động (1-2 phút)
```

### Step 3: Delete Target Folder Manually

```
1. Trong IntelliJ: Right-click vào folder "target"
2. Delete → "Do Refactor"
3. Confirm
```

**Hoặc trong Windows Explorer:**
```
1. Mở folder: C:\Users\ADMIN\OneDrive\Desktop\Projects\GitHub\SWP391EVSWAP_PROJECT
2. Delete folder "target"
```

### Step 4: Rebuild Project

```
1. Build → Rebuild Project
2. Hoặc nhấn Ctrl + Shift + F9
3. Đợi đến khi status bar hiển thị "Build completed"
```

### Step 5: Run Application

```
1. Run → Run 'Swp391To4Application'
2. Hoặc nhấn Shift + F10
3. Đợi đến khi thấy "Started Swp391To4Application"
```

## ✅ SUCCESS INDICATORS

Sau khi chạy thành công, bạn sẽ thấy:

```
Started Swp391To4Application in X.XXX seconds
```

**KHÔNG** còn thấy lỗi:
- ❌ `No qualifying bean`
- ❌ `RestTemplate not found`
- ❌ `PasswordEncoder not found`

## 📞 If Still Fails

Try this **nuclear option**:

1. Close IntelliJ completely
2. Delete `target` folder manually
3. Delete `.idea` folder manually
4. Reopen IntelliJ
5. Open project again
6. Rebuild project
7. Run application

## 🎯 After Success

Once application starts successfully:

1. Open browser: http://localhost:8080
2. Login with test account:
   - Email: `driver1@test.com`
   - Password: `123456`
3. Test PayOS payment:
   - Go to Wallet
   - Top-up 10,000 VND
   - Select PayOS payment
   - Complete payment flow

## 📚 Next Steps

After successful start, read:
- [README.md](README.md) - Complete project documentation
- [SYSTEM_TEST_GUIDE.md](SYSTEM_TEST_GUIDE.md) - Testing guide
- [README_PAYMENT_INTEGRATION.md](README_PAYMENT_INTEGRATION.md) - PayOS integration details

---

**Status**: ⏸️ **WAITING FOR USER ACTION**  
**Next**: Follow steps 1-5 above

