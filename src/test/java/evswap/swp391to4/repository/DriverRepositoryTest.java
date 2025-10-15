package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DriverRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DriverRepository driverRepository;

    private Driver testDriver;

    @BeforeEach
    void setUp() {
        testDriver = Driver.builder()
                .email("test@example.com")
                .passwordHash("encoded_password")
                .fullName("Test User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldSaveAndFindDriver() {
        // Given
        Driver savedDriver = entityManager.persistAndFlush(testDriver);

        // When
        Optional<Driver> found = driverRepository.findById(savedDriver.getDriverId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFullName()).isEqualTo("Test User");
    }

    @Test
    void shouldFindDriverByEmail() {
        // Given
        entityManager.persistAndFlush(testDriver);

        // When
        Optional<Driver> found = driverRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<Driver> found = driverRepository.findByEmail("notfound@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindDriverByPhone() {
        // Given
        entityManager.persistAndFlush(testDriver);

        // When
        Optional<Driver> found = driverRepository.findByPhone("0123456789");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPhone()).isEqualTo("0123456789");
    }

    @Test
    void shouldFindVerifiedDrivers() {
        // Given
        Driver verifiedDriver = Driver.builder()
                .email("verified@example.com")
                .passwordHash("encoded_password")
                .fullName("Verified User")
                .phone("0987654321")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        Driver unverifiedDriver = Driver.builder()
                .email("unverified@example.com")
                .passwordHash("encoded_password")
                .fullName("Unverified User")
                .phone("0912345678")
                .emailVerified(false)
                .createdAt(Instant.now())
                .build();

        entityManager.persistAndFlush(verifiedDriver);
        entityManager.persistAndFlush(unverifiedDriver);

        // When
        List<Driver> verifiedDrivers = driverRepository.findByEmailVerifiedTrue();

        // Then
        assertThat(verifiedDrivers).hasSize(1);
        assertThat(verifiedDrivers.get(0).getEmail()).isEqualTo("verified@example.com");
    }

    @Test
    void shouldFindDriversByEmailContaining() {
        // Given
        Driver driver1 = Driver.builder()
                .email("john.doe@example.com")
                .passwordHash("encoded_password")
                .fullName("John Doe")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        Driver driver2 = Driver.builder()
                .email("jane.doe@example.com")
                .passwordHash("encoded_password")
                .fullName("Jane Doe")
                .phone("0987654321")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        Driver driver3 = Driver.builder()
                .email("smith@test.com")
                .passwordHash("encoded_password")
                .fullName("John Smith")
                .phone("0912345678")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        entityManager.persistAndFlush(driver1);
        entityManager.persistAndFlush(driver2);
        entityManager.persistAndFlush(driver3);

        // When
        List<Driver> doeDrivers = driverRepository.findByEmailContainingIgnoreCase("doe");

        // Then
        assertThat(doeDrivers).hasSize(2);
        assertThat(doeDrivers).extracting(Driver::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.doe@example.com");
    }

    @Test
    void shouldCountDriversByEmailVerified() {
        // Given
        Driver verifiedDriver = Driver.builder()
                .email("verified@example.com")
                .passwordHash("encoded_password")
                .fullName("Verified User")
                .phone("0987654321")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        Driver unverifiedDriver = Driver.builder()
                .email("unverified@example.com")
                .passwordHash("encoded_password")
                .fullName("Unverified User")
                .phone("0912345678")
                .emailVerified(false)
                .createdAt(Instant.now())
                .build();

        entityManager.persistAndFlush(verifiedDriver);
        entityManager.persistAndFlush(unverifiedDriver);

        // When
        long verifiedCount = driverRepository.countByEmailVerifiedTrue();
        long unverifiedCount = driverRepository.countByEmailVerifiedFalse();

        // Then
        assertThat(verifiedCount).isEqualTo(1);
        assertThat(unverifiedCount).isEqualTo(1);
    }

    @Test
    void shouldDeleteDriver() {
        // Given
        Driver savedDriver = entityManager.persistAndFlush(testDriver);
        int driverId = savedDriver.getDriverId();

        // When
        driverRepository.deleteById(driverId);
        entityManager.flush();

        // Then
        Optional<Driver> found = driverRepository.findById(driverId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateDriver() {
        // Given
        Driver savedDriver = entityManager.persistAndFlush(testDriver);
        savedDriver.setFullName("Updated Name");
        savedDriver.setPhone("0999999999");

        // When
        Driver updatedDriver = driverRepository.save(savedDriver);
        entityManager.flush();

        // Then
        Optional<Driver> found = driverRepository.findById(updatedDriver.getDriverId());
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Updated Name");
        assertThat(found.get().getPhone()).isEqualTo("0999999999");
    }

    @Test
    void shouldEnforceUniqueEmail() {
        // Given
        Driver driver1 = Driver.builder()
                .email("duplicate@example.com")
                .passwordHash("encoded_password")
                .fullName("Driver 1")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        Driver driver2 = Driver.builder()
                .email("duplicate@example.com") // Same email
                .passwordHash("encoded_password")
                .fullName("Driver 2")
                .phone("0987654321")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        // When
        entityManager.persistAndFlush(driver1);

        // Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(driver2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldEnforceUniquePhone() {
        // Given
        Driver driver1 = Driver.builder()
                .email("driver1@example.com")
                .passwordHash("encoded_password")
                .fullName("Driver 1")
                .phone("0123456789") // Same phone
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        Driver driver2 = Driver.builder()
                .email("driver2@example.com")
                .passwordHash("encoded_password")
                .fullName("Driver 2")
                .phone("0123456789") // Same phone
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        // When
        entityManager.persistAndFlush(driver1);

        // Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(driver2))
                .isInstanceOf(Exception.class);
    }
}
