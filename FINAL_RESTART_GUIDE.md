# 🚀 FINAL RESTART GUIDE - CRITICAL

## ⚠️ CRITICAL ISSUE
Spring không tìm thấy `RestTemplate` bean vì IntelliJ đang dùng compiled classes cũ.

## 🛑 REQUIRED ACTIONS (MUST DO IN ORDER)

### Step 1: STOP Application
```
Trong IntelliJ IDEA:
1. Click vào Stop button (đỏ vuông)
2. Hoặc nhấn Ctrl + F2
3. Đợi đến khi "Process finished with exit code X"
```

### Step 2: Invalidate Caches
```
1. File → Invalidate Caches...
2. Tick "Invalidate caches and restart"
3. Click "Invalidate and Restart"
4. IntelliJ sẽ restart tự động
```

### Step 3: Rebuild Project
```
Sau khi IntelliJ restart:
1. Build → Rebuild Project
2. Hoặc nhấn Ctrl + Shift + F9
3. Đợi đến khi rebuild hoàn tất (status bar)
```

### Step 4: Run Application
```
1. Run → Run 'Swp391To4Application'
2. Hoặc nhấn Shift + F10
```

## ✅ Expected Success Log
```
Started Swp391To4Application in X.XXX seconds
```

## 🔍 If Still Fails

Check for these issues:

### Issue 1: RestTemplate Bean Not Found
**Solution**: Invalidate caches and rebuild (done above)

### Issue 2: ComponentScan Not Working
**Current Fix**: 
```java
@ComponentScan(basePackages = "evswap.swp391to4")
```
Already added to main application class.

### Issue 3: Configuration Files Not Found
**Check**: Make sure `RestTemplateConfig.java` exists and is in:
```
src/main/java/evswap/swp391to4/config/RestTemplateConfig.java
```

### Issue 4: Missing Dependencies
**Check**: Make sure `pom.xml` has:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 📋 Complete Fix Checklist

- [x] Fixed application.properties encoding
- [x] Fixed SecurityConfig
- [x] Fixed Swp391To4Application
- [x] Fixed TicketSupportRepository imports
- [x] Created RestTemplateConfig
- [x] Created all PayOS DTOs
- [ ] ⚠️ **PENDING**: Invalidate caches and restart IntelliJ
- [ ] ⚠️ **PENDING**: Rebuild project
- [ ] ⚠️ **PENDING**: Run application

## 🎯 After Restart

Application should:
1. ✅ Start without errors
2. ✅ Connect to database
3. ✅ Load all beans correctly
4. ✅ Have RestTemplate available
5. ✅ Ready for PayOS testing

---
**Created**: 2025-10-30  
**Status**: ⏸️ Waiting for user to restart IntelliJ and rebuild

