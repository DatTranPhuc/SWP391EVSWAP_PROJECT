# EVSWAP - Electric Vehicle Battery Swap Platform

## 🚀 Project Overview

EVSWAP is a comprehensive platform for electric vehicle battery swapping, featuring user management, station management, reservations, payments, and support ticketing.

## ✨ Features

### Core Features
- 👤 User management (Drivers, Staff, Admin)
- 🏢 Station management
- 🔋 Battery management
- 📅 Reservation system with QR code
- 💳 Payment integration (PayOS)
- 💰 Wallet system
- 📧 Email notifications
- 🎫 Support ticket system
- 📊 Analytics dashboard

### Payment Features
- ✅ PayOS integration
- ✅ Webhook handling with HMAC verification
- ✅ Wallet top-up
- ✅ Reservation payment
- ✅ Auto-refund (60% for no-show)

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.6
- **Frontend**: Thymeleaf
- **Database**: SQL Server
- **Security**: Spring Security with BCrypt
- **Payment**: PayOS API
- **Email**: Gmail SMTP
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 11+
- Maven 3.6+
- SQL Server (localhost:1433)
- IntelliJ IDEA (recommended)

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd SWP391EVSWAP_PROJECT
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE SWP391EVSWAP;
```

### 3. Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=SWP391EVSWAP
spring.datasource.username=sa
spring.datasource.password=your_password
```

### 4. Run Application

**IMPORTANT: In IntelliJ IDEA**

1. **Stop** any running application (Ctrl + F2)
2. **Invalidate Caches**: File → Invalidate Caches → Invalidate and Restart
3. **Rebuild Project**: Build → Rebuild Project (Ctrl + Shift + F9)
4. **Run**: Run → Run 'Swp391To4Application' (Shift + F10)

### 5. Access Application
```
http://localhost:8080
```

## 📁 Project Structure

```
SWP391EVSWAP_PROJECT/
├── src/main/java/evswap/swp391to4/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA entities
│   ├── repository/     # JPA repositories
│   ├── scheduler/      # Scheduled tasks
│   ├── service/        # Business logic
│   └── util/           # Utility classes
├── src/main/resources/
│   ├── application.properties
│   ├── static/         # CSS, images
│   └── templates/      # Thymeleaf templates
└── pom.xml
```

## 🔐 Default Accounts

### Admin
- Email: `admin@evswap.com`
- Password: Set in database

### Driver
- Register at: `http://localhost:8080/register`

## 📚 Documentation

- [README_QUICKSTART.md](README_QUICKSTART.md) - Quick start guide
- [SYSTEM_TEST_GUIDE.md](SYSTEM_TEST_GUIDE.md) - Testing guide
- [SYSTEM_STATUS.md](SYSTEM_STATUS.md) - Current system status
- [ACTION_REQUIRED.md](ACTION_REQUIRED.md) - User actions required
- [CHANGELOG.md](CHANGELOG.md) - Version history
- [ALL_FIXES_SUMMARY.md](ALL_FIXES_SUMMARY.md) - All fixes summary

## 🐛 Troubleshooting

### Application Won't Start

1. **RestTemplate Bean Not Found**
   - Fix: Invalidate caches and rebuild (see Quick Start)

2. **PasswordEncoder Bean Not Found**
   - Fix: Invalidate caches and rebuild

3. **Database Connection Failed**
   - Check: SQL Server is running on port 1433
   - Check: Database exists and credentials are correct

4. **Compiled Classes Locked**
   - Fix: Stop application, invalidate caches, rebuild

For more details, see [FINAL_RESTART_GUIDE.md](FINAL_RESTART_GUIDE.md)

## 🔧 Development

### Run Tests
```bash
mvn test
```

### Build Project
```bash
mvn clean install
```

### Run with Maven
```bash
mvn spring-boot:run
```

## 📝 License

Copyright © 2025 EVSWAP Team

## 👥 Team

- SWP391 Group

---

**Version**: 1.0.0  
**Last Updated**: 2025-10-30

