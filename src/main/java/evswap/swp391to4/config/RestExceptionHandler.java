package evswap.swp391to4.config;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({ DataIntegrityViolationException.class })
    @ResponseBody
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Dữ liệu không hợp lệ";
        Throwable root = ex.getMostSpecificCause();
        String detail = root != null ? root.getMessage() : ex.getMessage();

        if (detail != null) {
            String lower = detail.toLowerCase();
            if (lower.contains("unique") || lower.contains("duplicate")) {
                if (lower.contains("email")) message = "Email đã được đăng ký";
                else if (lower.contains("phone") || lower.contains("số điện thoại")) message = "Số điện thoại đã được sử dụng";
                else if (lower.contains("vin")) message = "VIN đã tồn tại";
                else message = "Giá trị đã tồn tại, vui lòng kiểm tra lại";
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", message);
        body.put("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    @ResponseBody
    public ResponseEntity<?> handleConstraint(ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", "Dữ liệu không hợp lệ: " + ex.getMessage());
        body.put("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class, RuntimeException.class })
    @ResponseBody
    public ResponseEntity<?> handleCommon(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", ex.getMessage());
        body.put("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}


