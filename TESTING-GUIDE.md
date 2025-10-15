# Hướng dẫn Testing cho EV Swap Project

## 📋 Tổng quan

Dự án EV Swap sử dụng Spring Boot với các loại test sau:
- **Unit Tests**: Test các component riêng lẻ
- **Integration Tests**: Test tương tác giữa các component
- **API Tests**: Test các REST endpoints
- **Database Tests**: Test với H2 in-memory database

## 🛠️ Cấu hình Test Environment

### 1. Dependencies cần thiết (đã có trong pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. Test Configuration

Tạo file `src/test/resources/application-test.properties`:

```properties
# H2 Database cho testing
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Disable security for testing
spring.security.user.name=test
spring.security.user.password=test

# Logging
logging.level.evswap.swp391to4=DEBUG
```

## 🧪 Các loại Test

### 1. Unit Tests

#### Test Service Layer

```java
@ExtendWith(MockitoExtension.class)
class DriverServiceTest {
    
    @Mock
    private DriverRepository driverRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private DriverService driverService;
    
    @Test
    void shouldCreateDriverSuccessfully() {
        // Given
        Driver driver = Driver.builder()
            .email("test@example.com")
            .fullName("Test User")
            .phone("0123456789")
            .build();
        
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        
        // When
        Driver result = driverService.createDriver(driver);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(driverRepository).save(driver);
    }
}
```

#### Test Controller Layer

```java
@WebMvcTest(DriverController.class)
class DriverControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private DriverService driverService;
    
    @Test
    void shouldGetDriverById() throws Exception {
        // Given
        Driver driver = Driver.builder()
            .driverId(1)
            .email("test@example.com")
            .fullName("Test User")
            .build();
        
        when(driverService.findById(1)).thenReturn(driver);
        
        // When & Then
        mockMvc.perform(get("/api/drivers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.fullName").value("Test User"));
    }
}
```

### 2. Integration Tests

#### Test Repository Layer

```java
@DataJpaTest
class DriverRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Test
    void shouldFindDriverByEmail() {
        // Given
        Driver driver = Driver.builder()
            .email("test@example.com")
            .fullName("Test User")
            .passwordHash("encoded_password")
            .emailVerified(true)
            .createdAt(Instant.now())
            .build();
        
        entityManager.persistAndFlush(driver);
        
        // When
        Optional<Driver> found = driverRepository.findByEmail("test@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test User");
    }
}
```

#### Test Full Application Context

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class EvSwapApplicationIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Test
    void shouldCreateAndRetrieveDriver() {
        // Given
        Driver driver = Driver.builder()
            .email("integration@test.com")
            .fullName("Integration Test User")
            .passwordHash("encoded_password")
            .emailVerified(true)
            .createdAt(Instant.now())
            .build();
        
        // When
        ResponseEntity<Driver> response = restTemplate.postForEntity(
            "/api/drivers", driver, Driver.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getEmail()).isEqualTo("integration@test.com");
        
        // Verify in database
        Optional<Driver> saved = driverRepository.findByEmail("integration@test.com");
        assertThat(saved).isPresent();
    }
}
```

### 3. API Tests với TestContainers

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApiIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("evswap_test")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldHandleCompleteReservationFlow() {
        // Test complete flow: create driver -> create reservation -> make payment -> complete swap
    }
}
```

## 🎯 Test Scenarios

### 1. Driver Management Tests

```java
@Test
void shouldValidateDriverEmail() {
    // Test email validation
}

@Test
void shouldHandleDuplicateEmail() {
    // Test duplicate email handling
}

@Test
void shouldEncryptPassword() {
    // Test password encryption
}
```

### 2. Reservation Flow Tests

```java
@Test
void shouldCreateReservation() {
    // Test reservation creation
}

@Test
void shouldValidateReservationTime() {
    // Test reservation time validation
}

@Test
void shouldGenerateQRCode() {
    // Test QR code generation
}

@Test
void shouldHandleReservationExpiry() {
    // Test reservation expiry logic
}
```

### 3. Payment Tests

```java
@Test
void shouldProcessPayment() {
    // Test payment processing
}

@Test
void shouldHandlePaymentFailure() {
    // Test payment failure scenarios
}

@Test
void shouldRefundPayment() {
    // Test refund functionality
}
```

### 4. Battery Swap Tests

```java
@Test
void shouldValidateBatteryCompatibility() {
    // Test battery-vehicle compatibility
}

@Test
void shouldUpdateBatteryStatus() {
    // Test battery status updates
}

@Test
void shouldRecordSwapTransaction() {
    // Test swap transaction recording
}
```

## 🔧 Test Utilities

### 1. Test Data Builder

```java
public class TestDataBuilder {
    
    public static Driver.DriverBuilder driverBuilder() {
        return Driver.builder()
            .email("test@example.com")
            .fullName("Test User")
            .phone("0123456789")
            .emailVerified(true)
            .createdAt(Instant.now());
    }
    
    public static Station.StationBuilder stationBuilder() {
        return Station.builder()
            .name("Test Station")
            .address("Test Address")
            .latitude(new BigDecimal("10.7769"))
            .longitude(new BigDecimal("106.7009"))
            .status("active");
    }
}
```

### 2. Test Configuration

```java
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    }
}
```

## 📊 Test Coverage

### 1. Coverage Goals
- **Unit Tests**: 80%+ coverage
- **Integration Tests**: 70%+ coverage
- **API Tests**: 90%+ coverage

### 2. Coverage Report
```bash
mvn test jacoco:report
```

## 🚀 Chạy Tests

### 1. Chạy tất cả tests
```bash
mvn test
```

### 2. Chạy tests với coverage
```bash
mvn test jacoco:report
```

### 3. Chạy tests cụ thể
```bash
mvn test -Dtest=DriverServiceTest
```

### 4. Chạy integration tests
```bash
mvn test -Dtest=*IntegrationTest
```

## 📝 Best Practices

### 1. Test Naming
- Use descriptive names: `shouldReturnDriverWhenValidIdProvided()`
- Follow Given-When-Then pattern

### 2. Test Data
- Use builders for complex objects
- Create reusable test data
- Use @DirtiesContext when needed

### 3. Assertions
- Use AssertJ for fluent assertions
- Test both positive and negative cases
- Verify side effects

### 4. Mocking
- Mock external dependencies
- Don't mock the class under test
- Use @MockBean for Spring context

## 🔍 Debugging Tests

### 1. Enable SQL Logging
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 2. Test Logging
```properties
logging.level.evswap.swp391to4=DEBUG
logging.level.org.springframework.test=DEBUG
```

### 3. Breakpoints
- Set breakpoints in test methods
- Use IDE debugger
- Check database state during tests

## 📋 Test Checklist

- [ ] Unit tests for all services
- [ ] Integration tests for repositories
- [ ] API tests for all endpoints
- [ ] Error handling tests
- [ ] Validation tests
- [ ] Security tests
- [ ] Performance tests (nếu cần)
- [ ] Test data cleanup
- [ ] Coverage reports
- [ ] CI/CD integration

## 🎯 Next Steps

1. Tạo test classes cho từng service
2. Implement integration tests
3. Setup CI/CD pipeline với tests
4. Monitor test coverage
5. Refactor based on test results
