# 🧪 System Test Guide - EVSWAP Payment Integration

## ✅ Completed Fixes

### 1. Fixed `application.properties` Encoding
- **Issue**: File có emoji khiến Spring không đọc được
- **Solution**: Xóa tất cả emoji, chỉ giữ text thuần túy
- **Status**: ✅ Fixed

### 2. Fixed `SecurityConfig.java`
- **Issue**: `@RequiredArgsConstructor` không cần thiết và có thể gây conflict
- **Solution**: Xóa `@RequiredArgsConstructor` annotation
- **Status**: ✅ Fixed

### 3. Fixed `Swp391To4Application.java`
- **Issue**: Thiếu explicit package scan
- **Solution**: Thêm `@ComponentScan(basePackages = "evswap.swp391to4")`
- **Status**: ✅ Fixed

### 4. Fixed Unused Imports
- **Issue**: Unused imports trong `TicketSupportRepository.java`
- **Solution**: Xóa các imports không sử dụng
- **Status**: ✅ Fixed

## 🚀 Start Application

### Step 1: Stop Running Application
```
Trong IntelliJ IDEA: Click Stop button hoặc Ctrl + F2
```

### Step 2: Rebuild Project
```
Build → Rebuild Project hoặc Ctrl + Shift + F9
```

### Step 3: Run Application
```
Run → Run 'Swp391To4Application' hoặc Shift + F10
```

### Expected Output
```
Started Swp391To4Application in X.XXX seconds
```

## 🧪 Test Cases

### Test 1: Application Startup
- **Action**: Start application
- **Expected**: Application starts without errors
- **Status**: ⏳ Pending

### Test 2: Database Connection
- **Action**: Check logs for database connection
- **Expected**: "HikariPool-1 - Start completed"
- **Status**: ⏳ Pending

### Test 3: PayOS Configuration
- **Action**: Check logs for PayOS config loading
- **Expected**: No errors related to PayOS configuration
- **Status**: ⏳ Pending

### Test 4: User Registration
- **Action**: Register a new user
- **Expected**: User created successfully
- **Status**: ⏳ Pending

### Test 5: Wallet Top-up (Simulated)
- **Action**: Top-up wallet with simulated payment
- **Expected**: Balance updated correctly
- **Status**: ⏳ Pending

### Test 6: Wallet Top-up (PayOS)
- **Action**: Initiate PayOS top-up
- **Expected**: Payment request created, redirected to PayOS
- **Status**: ⏳ Pending

### Test 7: Webhook Handling
- **Action**: Receive PayOS webhook callback
- **Expected**: Payment status updated, signature verified
- **Status**: ⏳ Pending

### Test 8: Reservation Booking
- **Action**: Create a reservation
- **Expected**: Amount deducted from wallet
- **Status**: ⏳ Pending

### Test 9: No-Show Refund
- **Action**: Wait 30 minutes for auto-cancel
- **Expected**: 60% refund processed
- **Status**: ⏳ Pending

## 📋 Checklist

- [ ] Application starts successfully
- [ ] Database connection established
- [ ] User can register
- [ ] User can login
- [ ] User can top-up wallet (simulated)
- [ ] User can top-up wallet (PayOS)
- [ ] PayOS webhook received and processed
- [ ] User can book reservation
- [ ] Amount deducted from wallet
- [ ] No-show refund works correctly

## 🐛 Known Issues
None at this time.

## 📝 Notes
- All PayOS credentials are configured in `application.properties`
- Webhook URL: `http://localhost:8080/api/payment/webhook`
- Minimum top-up amount: 10,000 VND
- No-show refund rate: 60%

---
**Created**: 2025-10-30  
**Status**: Ready for testing

