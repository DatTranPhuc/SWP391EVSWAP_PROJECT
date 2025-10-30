# 📝 Java Naming Conventions Guide

## 🎯 Overview

This guide outlines the naming conventions used in the EVSWAP project, following Java best practices and Spring Boot conventions.

## 📚 Basic Rules

### 1. **Classes & Interfaces** (PascalCase)
- ✅ **Correct**: `PaymentService`, `PayOsWebhookRequest`, `Driver`, `RestTemplateConfig`
- ❌ **Wrong**: `payment_service`, `payos_webhook_request`, `driver`

**Pattern**: `[Noun]Service`, `[Noun]Controller`, `[Noun]Repository`, `[Noun]Entity`, `[Noun]Dto`

### 2. **Methods** (camelCase)
- ✅ **Correct**: `createPayOsTopUpRequest()`, `simulateTopUp()`, `handlePayOsWebhook()`, `getPaymentById()`
- ❌ **Wrong**: `create_payos_topup_request()`, `CreatePayOsTopUpRequest()`

**Pattern**: `[verb][Object][OptionalModifier]`
- `create` + `Payment` → `createPayment()`
- `simulate` + `TopUp` → `simulateTopUp()`
- `get` + `Payment` + `By` + `Id` → `getPaymentById()`
- `handle` + `PayOs` + `Webhook` → `handlePayOsWebhook()`

### 3. **Variables** (camelCase)
- ✅ **Correct**: `payosEndpoint`, `payosApiKey`, `orderCode`, `amount`, `driver`, `payment`
- ❌ **Wrong**: `payos_endpoint`, `PayosEndpoint`, `order_code`

**Pattern**: 
- `[adjective][noun]` → `payosEndpoint`, `webhookUrl`
- `[noun]` → `amount`, `driver`, `status`

### 4. **Constants** (UPPER_SNAKE_CASE)
- ✅ **Correct**: `MAX_ORDER_CODE_LENGTH`, `HMAC_SHA256`, `DEFAULT_CURRENCY`
- ❌ **Wrong**: `maxOrderCodeLength`, `MAXOrderCodeLength`

### 5. **Packages** (lowercase, dot-separated)
- ✅ **Correct**: `evswap.swp391to4.service`, `evswap.swp391to4.dto`
- ❌ **Wrong**: `Evswap.Swp391To4.Service`, `evswap_swp391to4_service`

## 🏗️ Domain-Specific Naming

### Service Layer
```java
@Service
public class PaymentService {
    
    // Service methods should be verbs
    public Payment createPayment(...) { }
    public Payment updatePayment(...) { }
    public void deletePayment(...) { }
    public Payment getPaymentById(...) { }
    public List<Payment> getAllPayments() { }
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    // Controller methods map to HTTP verbs
    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Integer id) { }
    
    @PostMapping
    public Payment createPayment(@RequestBody PaymentRequest request) { }
    
    @PutMapping("/{id}")
    public Payment updatePayment(@PathVariable Integer id, ...) { }
    
    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Integer id) { }
}
```

### Repository Layer
```java
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Repository methods should describe queries
    List<Payment> findByDriverOrderByPaidAtDesc(Driver driver);
    Optional<Payment> findByOrderCode(String orderCode);
    Optional<Payment> findByPaymentIdAndDriver(Integer paymentId, Driver driver);
    
    // Custom queries use @Query
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findPaymentsByStatus(@Param("status") String status);
}
```

### DTOs (Data Transfer Objects)
```java
@Data
public class PayOsCreatePaymentRequest {
    private Long orderCode;
    private Long amount;
    private String description;
    private String webhookUrl;
    private String cancelUrl;
    private String successUrl;
}

@Data
public class PayOsWebhookResponse {
    private Integer code;
    private String desc;
    private PayOsData data;
    
    @Data
    public static class PayOsData {
        private Long orderCode;
        private String providerTransactionCode;
    }
}
```

### Entity Classes
```java
@Entity
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;
    
    @ManyToOne
    private Driver driver;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @Column(name = "status")
    private String status;
    
    // Getters and setters
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
}
```

## 🔑 Configuration Properties

### application.properties
```properties
# Use lowercase with dots for hierarchical structure
payos.api.endpoint=https://api-merchant.payos.vn
payos.api.key=your-api-key
payos.api.client-id=your-client-id
payos.webhook.url=http://localhost:8080/api/payment/webhook
payos.webhook.key=your-webhook-key

# Use hyphens for multi-word keys
spring.datasource.url=jdbc:sqlserver://localhost:1433
spring.jpa.hibernate.ddl-auto=create-drop
spring.mail.properties.mail.smtp.auth=true
```

### Using @Value
```java
@Value("${payos.api.endpoint}")
private String payosEndpoint;

@Value("${payos.api.key}")
private String payosApiKey;

@Value("${spring.datasource.username}")
private String dbUsername;
```

## 🌐 HTTP Endpoints

### RESTful API Naming
```java
// ✅ Good RESTful design
GET    /api/payments           // Get all payments
GET    /api/payments/{id}      // Get payment by ID
POST   /api/payments           // Create payment
PUT    /api/payments/{id}      // Update payment
DELETE /api/payments/{id}      // Delete payment

// ❌ Avoid non-RESTful names
GET /api/getPayments           // Don't use "get" in URL
POST /api/createPayment        // Don't use "create" in URL
```

### Spring Boot @RequestMapping
```java
@RestController
@RequestMapping("/api/payment")  // Singular for controller-level path
public class PaymentController {
    
    @GetMapping
    public List<Payment> getAllPayments() { }
    
    @GetMapping("/{paymentId}")  // Use {variableName} for path variables
    public Payment getPayment(@PathVariable Integer paymentId) { }
    
    @PostMapping("/webhook")     // Use descriptive paths
    public ResponseEntity<PayOsWebhookResponse> handleWebhook() { }
}
```

## 📦 File Organization

### Directory Structure
```
evswap/swp391to4/
├── config/          # Configuration classes
│   ├── RestTemplateConfig.java
│   └── SecurityConfig.java
├── controller/      # REST controllers
│   ├── PaymentController.java
│   └── WalletController.java
├── dto/            # Data Transfer Objects
│   ├── PayOsCreatePaymentRequest.java
│   └── PayOsWebhookResponse.java
├── entity/         # JPA entities
│   └── Payment.java
├── repository/     # JPA repositories
│   └── PaymentRepository.java
├── service/        # Business logic
│   └── PaymentService.java
└── util/           # Utility classes
    └── PayOsUtil.java
```

## ✅ Summary

| Element | Convention | Example |
|---------|------------|---------|
| Class | PascalCase | `PaymentService` |
| Method | camelCase | `createPayment()` |
| Variable | camelCase | `paymentId` |
| Constant | UPPER_SNAKE_CASE | `MAX_LENGTH` |
| Package | lowercase | `evswap.swp391to4` |
| Property Key | lowercase.dots | `payos.api.key` |
| HTTP Path | lowercase/kebab-case | `/api/payment/webhook` |

## 🎯 Best Practices

1. ✅ **Be Descriptive**: Use full words, not abbreviations
   - ✅ `getPaymentById()` not `getPayId()`
   - ✅ `payosEndpoint` not `endpt`

2. ✅ **Use Intent-Revealing Names**: Name should explain purpose
   - ✅ `simulateTopUp()` clearly indicates it's simulated
   - ✅ `createPayOsTopUpRequest()` clearly indicates it creates PayOS request

3. ✅ **Avoid Magic Numbers**: Use named constants
   - ✅ `private static final int MAX_ORDER_CODE_LENGTH = 11;`
   - ❌ `if (orderCode.length() > 11)`

4. ✅ **Follow Domain Language**: Use terms from the business domain
   - ✅ `Reservation`, `Driver`, `BatterySwap`
   - ❌ `Data1`, `User2`, `Item3`

5. ✅ **Be Consistent**: Use same conventions throughout project
   - ✅ All services end with `Service`
   - ✅ All controllers end with `Controller`
   - ✅ All repositories end with `Repository`

---
**Last Updated**: 2025-10-30
