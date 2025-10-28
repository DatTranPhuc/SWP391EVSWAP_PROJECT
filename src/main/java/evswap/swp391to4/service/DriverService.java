package evswap.swp391to4.service;

import evswap.swp391to4.entity.Driver;
import java.util.Optional;

public interface DriverService {
    Driver registerDriver(String email, String rawPassword, String fullName);
    Optional<Driver> login(String email, String rawPassword);
    Driver getDriverById(Integer id);
    boolean verifyOtp(String email, String otp);
}
