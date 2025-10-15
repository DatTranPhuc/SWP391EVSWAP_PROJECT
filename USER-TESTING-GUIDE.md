# Hướng dẫn Test từ Góc độ Người dùng - EV Swap Project

## 📋 Tổng quan

Tài liệu này hướng dẫn cách test các chức năng của hệ thống EV Swap từ góc độ người dùng thực tế. Các test được thiết kế để mô phỏng hành vi và trải nghiệm của người dùng cuối.

## 🎯 Các loại Test từ Góc độ Người dùng

### 1. User Acceptance Tests (UAT)
**File**: `UserAcceptanceTest.java`

Test các chức năng cơ bản mà người dùng cần:
- ✅ Đăng ký tài khoản mới
- ✅ Xác thực email
- ✅ Đăng nhập hệ thống
- ✅ Đăng ký xe
- ✅ Tìm trạm đổi pin gần nhất
- ✅ Đặt lịch đổi pin
- ✅ Thanh toán dịch vụ
- ✅ Thực hiện đổi pin
- ✅ Đánh giá dịch vụ
- ✅ Hủy lịch đặt
- ✅ Xem lịch sử giao dịch
- ✅ Gửi yêu cầu hỗ trợ

### 2. End-to-End User Journey Tests
**File**: `EndToEndUserJourneyTest.java`

Test toàn bộ hành trình của người dùng:

#### 🚀 Complete User Journey
```
Đăng ký → Xác thực Email → Đăng nhập → Đăng ký Xe → 
Tìm Trạm → Đặt Lịch → Thanh toán → Check-in → 
Đổi Pin → Đánh giá Dịch vụ
```

#### ⚠️ Error Handling Journey
- Xử lý email trùng lặp
- Xử lý email chưa xác thực
- Xử lý trạm không hoạt động
- Xử lý thanh toán không hợp lệ

#### ⚡ Performance Journey
- Test với nhiều request đồng thời
- Kiểm tra hiệu suất hệ thống

### 3. User Scenario Tests
**File**: `UserScenarioTest.java`

Test các tình huống sử dụng thực tế:

#### 🆕 Scenario 1: Tài xế mới sử dụng dịch vụ lần đầu
- Đăng ký và tìm hiểu dịch vụ
- Đặt lịch thử nghiệm

#### 🔄 Scenario 2: Tài xế thường xuyên sử dụng dịch vụ
- Đăng nhập nhanh
- Xem lịch sử giao dịch
- Đặt lịch thường xuyên
- Thanh toán tự động

#### 🆘 Scenario 3: Tài xế gặp sự cố và cần hỗ trợ
- Gặp sự cố tại trạm
- Gửi yêu cầu hỗ trợ
- Nhận hỗ trợ từ staff
- Hoàn thành giao dịch

#### ❌ Scenario 4: Tài xế hủy lịch và yêu cầu hoàn tiền
- Hủy lịch vì lý do cá nhân
- Yêu cầu hoàn tiền
- Nhận hoàn tiền

#### ⏰ Scenario 5: Tài xế sử dụng dịch vụ trong giờ cao điểm
- Tìm trạm có sẵn pin
- Chờ đợi và kiên nhẫn
- Hoàn thành giao dịch

### 4. API Workflow Tests
**File**: `ApiWorkflowTest.java`

Test các luồng xử lý API chính:

#### 🔐 Authentication Flow
```
Register → Verify Email → Login → Get Profile
```

#### 🚗 Vehicle Management Flow
```
Register Vehicle → Update Vehicle → Get Vehicle List → Delete Vehicle
```

#### 📍 Station Discovery Flow
```
Get All Stations → Get Station Details → Get Nearby Stations → Get Station Batteries
```

#### 📅 Reservation Management Flow
```
Create Reservation → Update Reservation → Check-in → Complete → Cancel
```

#### 💳 Payment Processing Flow
```
Create Payment → Process Payment → Verify Payment → Refund Payment
```

#### 🔋 Battery Swap Flow
```
Check Compatibility → Perform Swap → Record Transaction → Update Status
```

#### ⭐ Feedback and Rating Flow
```
Submit Feedback → Get Feedback → Update Feedback → Get Station Rating
```

## 🚀 Cách chạy Tests

### 1. Chạy tất cả User Tests
```bash
# Windows
run-tests.bat

# Linux/Mac
chmod +x run-tests.sh
./run-tests.sh
```

### 2. Chạy từng loại test riêng lẻ
```bash
# User Acceptance Tests
mvn test -Dtest=UserAcceptanceTest

# End-to-End Journey Tests
mvn test -Dtest=EndToEndUserJourneyTest

# User Scenario Tests
mvn test -Dtest=UserScenarioTest

# API Workflow Tests
mvn test -Dtest=ApiWorkflowTest
```

### 3. Chạy test cụ thể
```bash
# Test đăng ký người dùng mới
mvn test -Dtest=UserAcceptanceTest#userStory_RegisterNewDriver

# Test hành trình hoàn chỉnh
mvn test -Dtest=EndToEndUserJourneyTest#completeUserJourney_FromRegistrationToBatterySwap

# Test scenario tài xế mới
mvn test -Dtest=UserScenarioTest#scenario_NewDriverFirstTimeUsage
```

## 📊 Kết quả Test

### 1. Test Reports
- **Coverage Report**: `target/site/jacoco/index.html`
- **Test Results**: `target/surefire-reports/`
- **Console Output**: Hiển thị trực tiếp trong terminal

### 2. Metrics quan trọng
- **Test Success Rate**: Tỷ lệ test thành công
- **Code Coverage**: Độ bao phủ code
- **Response Time**: Thời gian phản hồi API
- **Error Rate**: Tỷ lệ lỗi

## 🎭 Test Data và Scenarios

### 1. Test Users
```java
// Tài xế mới
email: "newbie@example.com"
password: "password123"

// Tài xế thường xuyên
email: "regular@example.com"
password: "password123"

// Tài xế gặp sự cố
email: "support@example.com"
password: "password123"
```

### 2. Test Stations
```java
// Trạm hoạt động bình thường
name: "Test Station"
status: "active"

// Trạm bảo trì
name: "Maintenance Station"
status: "maintenance"
```

### 3. Test Vehicles
```java
// Xe VinFast VF8
vin: "VIN_TEST_001"
model: "VinFast VF8"
plateNumber: "30A-TEST"
```

## 🔍 Debugging và Troubleshooting

### 1. Enable Debug Logging
```properties
# application-test.properties
logging.level.evswap.swp391to4=DEBUG
logging.level.org.springframework.web=DEBUG
```

### 2. Common Issues
- **Database Connection**: Kiểm tra H2 database configuration
- **Mock Data**: Đảm bảo test data được tạo đúng
- **API Endpoints**: Kiểm tra controller mappings
- **Authentication**: Verify security configuration

### 3. Test Isolation
- Mỗi test method chạy độc lập
- Sử dụng `@Transactional` để rollback
- Clean up data sau mỗi test

## 📈 Best Practices

### 1. Test Design
- **Given-When-Then** pattern
- **Descriptive test names**
- **Realistic test data**
- **Error scenarios coverage**

### 2. Test Maintenance
- **Regular test updates**
- **Refactor when needed**
- **Monitor test performance**
- **Keep tests simple**

### 3. User Experience Focus
- **Test from user perspective**
- **Real-world scenarios**
- **Edge cases handling**
- **Performance considerations**

## 🎯 Kết luận

Các test từ góc độ người dùng giúp đảm bảo:
- ✅ Hệ thống hoạt động đúng như mong đợi của người dùng
- ✅ Tất cả các chức năng chính được test kỹ lưỡng
- ✅ Trải nghiệm người dùng được tối ưu
- ✅ Lỗi được phát hiện sớm và xử lý kịp thời
- ✅ Hệ thống sẵn sàng cho production

Chạy các test này thường xuyên để đảm bảo chất lượng hệ thống và trải nghiệm người dùng tốt nhất.
