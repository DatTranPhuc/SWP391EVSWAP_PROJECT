# 📝 Naming Conventions - Quick Reference

## 🎯 Quick Cheat Sheet

| Element | Format | Example |
|---------|--------|---------|
| **Class** | PascalCase | `PaymentService` |
| **Method** | camelCase | `createPayment()` |
| **Variable** | camelCase | `paymentId` |
| **Constant** | UPPER_SNAKE_CASE | `MAX_LENGTH` |
| **Package** | lowercase | `evswap.swp391to4` |
| **File** | PascalCase | `PaymentService.java` |

## 📋 Common Patterns

### Service Methods
```java
// Create operations
createPayment()
createPayOsTopUpRequest()

// Read operations
getPaymentById()
getAllPayments()
findPaymentByOrderCode()

// Update operations
updatePayment()
updatePaymentStatus()

// Delete operations
deletePayment()

// Business operations
simulateTopUp()
simulateRefund()
handlePayOsWebhook()
```

### Repository Methods
```java
// JPA query methods
findByDriverOrderByPaidAtDesc(Driver driver)
findByOrderCode(String orderCode)
findByPaymentIdAndDriver(Integer paymentId, Driver driver)
```

### Controller Methods
```java
// CRUD operations
@GetMapping("/{id}")
getPayment(@PathVariable Integer id)

@PostMapping
createPayment(@RequestBody PaymentRequest request)

@PutMapping("/{id}")
updatePayment(@PathVariable Integer id, ...)

@DeleteMapping("/{id}")
deletePayment(@PathVariable Integer id)
```

### Variables
```java
// Simple variables
String name;
BigDecimal amount;
Integer paymentId;

// Compound names
String payosEndpoint;
String payosApiKey;
String webhookUrl;
```

## ✅ DO's

- ✅ Use full words: `createPayment()` not `createPay()`
- ✅ Be descriptive: `getPaymentById()` not `getById()`
- ✅ Use domain terms: `Reservation`, `Driver`, `BatterySwap`
- ✅ Follow conventions: All services end with `Service`
- ✅ Use camelCase for variables and methods
- ✅ Use PascalCase for classes and interfaces

## ❌ DON'Ts

- ❌ Don't abbreviate unnecessarily: `getPayId()` → `getPaymentId()`
- ❌ Don't use underscores in Java names: `payment_id` → `paymentId`
- ❌ Don't start with numbers: `2ndPayment` → `secondPayment`
- ❌ Don't use magic numbers: `11` → `MAX_ORDER_CODE_LENGTH`
- ❌ Don't use vague names: `process()` → `processPayment()`

## 📚 See Also

For complete documentation, see [NAMING_CONVENTIONS.md](NAMING_CONVENTIONS.md)

---
**Last Updated**: 2025-10-30
