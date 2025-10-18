package evswap.swp391to4.dto;

import java.io.Serializable;

/**
 * Lightweight representation of the logged-in driver that is safe to store in the HTTP session.
 */
public record SessionDriver(Integer driverId, String fullName, String email) implements Serializable {
}

