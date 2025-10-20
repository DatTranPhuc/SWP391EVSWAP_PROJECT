package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountViewResponse {
    private String driverName;
    private String email;
    private String formattedBalance;
}