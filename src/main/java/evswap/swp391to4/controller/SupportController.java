package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.TicketSupportRequest;
import evswap.swp391to4.dto.TicketSupportResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.TicketSupportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final TicketSupportService ticketService;

    /**
     * Hiển thị trang support (form + danh sách ticket của driver)
     */
    @GetMapping
    public String supportPage(HttpSession session, Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        // Lấy ticket của driver này
        List<TicketSupportResponse> ticketList = ticketService.getTicketsByDriver(driver.getDriverId());
        model.addAttribute("ticketList", ticketList);

        // Form object
        model.addAttribute("ticket", new TicketSupportRequest());

        return "support";
    }

    /**
     * Xử lý submit ticket mới
     */
    @PostMapping
    public String submitTicket(@Valid @ModelAttribute("ticket") TicketSupportRequest ticket,
                              BindingResult bindingResult,
                              @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                              HttpSession session,
                              Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            // Hiển thị lỗi validation
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            
            // Load lại dữ liệu cần thiết
            List<TicketSupportResponse> ticketList = ticketService.getTicketsByDriver(driver.getDriverId());
            model.addAttribute("ticketList", ticketList);
            model.addAttribute("ticket", ticket);
            return "support";
        }

        try {
            // Tạo ticket trước
            TicketSupportResponse createdTicket = ticketService.createTicket(ticket, driver);
            
            // Nếu có file đính kèm, upload file
            if (attachment != null && !attachment.isEmpty()) {
                try {
                    // Validate file size (5MB)
                    if (attachment.getSize() > 5 * 1024 * 1024) {
                        model.addAttribute("attachmentError", "File quá lớn! Tối đa 5MB.");
                        model.addAttribute("ticket", ticket);
                        List<TicketSupportResponse> ticketList = ticketService.getTicketsByDriver(driver.getDriverId());
                        model.addAttribute("ticketList", ticketList);
                        return "support";
                    }

                    // Validate file type
                    String contentType = attachment.getContentType();
                    if (contentType == null || (!contentType.startsWith("image/") && 
                        !contentType.equals("application/pdf") && 
                        !contentType.equals("application/msword") && 
                        !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                        model.addAttribute("attachmentError", "Chỉ chấp nhận file ảnh, PDF hoặc Word!");
                        model.addAttribute("ticket", ticket);
                        List<TicketSupportResponse> ticketList = ticketService.getTicketsByDriver(driver.getDriverId());
                        model.addAttribute("ticketList", ticketList);
                        return "support";
                    }

                    // Upload file
                    ticketService.saveAttachment(createdTicket.getTicketId(), attachment);
                } catch (Exception e) {
                    model.addAttribute("attachmentError", "Lỗi upload file: " + e.getMessage());
                    model.addAttribute("ticket", ticket);
                    List<TicketSupportResponse> ticketList = ticketService.getTicketsByDriver(driver.getDriverId());
                    model.addAttribute("ticketList", ticketList);
                    return "support";
                }
            }
            
            model.addAttribute("success", "Gửi ticket hỗ trợ thành công!");
            model.addAttribute("ticket", new TicketSupportRequest());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("ticket", ticket);
        }

        // Load lại dữ liệu
        List<TicketSupportResponse> ticketList = ticketService.getTicketsByDriver(driver.getDriverId());
        model.addAttribute("ticketList", ticketList);

        return "support";
    }

    /**
     * Xem chi tiết ticket
     */
    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Integer id,
                             HttpSession session,
                             Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        try {
            // Kiểm tra ID hợp lệ
            if (id == null || id <= 0) {
                model.addAttribute("error", "ID ticket không hợp lệ");
                return "redirect:/support";
            }

            TicketSupportResponse ticket = ticketService.getTicketById(id);
            
            // Kiểm tra quyền xem ticket (chỉ driver sở hữu)
            if (ticket.getDriverId() == null || !ticket.getDriverId().equals(driver.getDriverId())) {
                model.addAttribute("error", "Bạn không có quyền xem ticket này");
                return "redirect:/support";
            }
            
            model.addAttribute("ticket", ticket);
            
            // Load comments for display
            try {
                List<TicketSupportService.Comment> comments = ticketService.getCommentsByTicketId(id);
                model.addAttribute("comments", comments);
            } catch (Exception e) {
                System.err.println("Error loading comments for ticket " + id + ": " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("comments", new java.util.ArrayList<>());
            }
            
            return "support-detail";
            
        } catch (IllegalArgumentException e) {
            // Ticket không tồn tại
            model.addAttribute("error", "Ticket không tồn tại");
            return "redirect:/support";
        } catch (Exception e) {
            // Log lỗi chi tiết
            System.err.println("Error in viewTicket: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/support";
        }
    }

    /**
     * Thêm comment vào ticket
     */
    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Integer id,
                           @RequestParam String message,
                           HttpSession session,
                           RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        try {
            ticketService.addComment(id, "driver", driver.getFullName(), message);
            redirect.addFlashAttribute("success", "Đã thêm bình luận thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/support/" + id;
    }

    /**
     * Upload file attachment
     */
    @PostMapping("/{id}/upload")
    public String uploadFile(@PathVariable Integer id,
                           @RequestParam("file") MultipartFile file,
                           HttpSession session,
                           RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        try {
            // Kiểm tra kích thước file (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                redirect.addFlashAttribute("error", "File quá lớn! Tối đa 5MB.");
                return "redirect:/support/" + id;
            }

            // Kiểm tra loại file
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && 
                !contentType.equals("application/pdf") && 
                !contentType.equals("application/msword") && 
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                redirect.addFlashAttribute("error", "Chỉ chấp nhận file ảnh, PDF hoặc Word!");
                return "redirect:/support/" + id;
            }

            ticketService.saveAttachment(id, file);
            redirect.addFlashAttribute("success", "Tải lên file thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/support/" + id;
    }

    /**
     * Test endpoint để kiểm tra database
     */
    @GetMapping("/test")
    @ResponseBody
    public Map<String, Object> testEndpoint(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra session
            Driver driver = (Driver) session.getAttribute("loggedInDriver");
            if (driver == null) {
                response.put("error", "Chưa đăng nhập");
                return response;
            }
            
            // Kiểm tra database connection
            List<TicketSupportResponse> allTickets = ticketService.getTicketsByDriver(driver.getDriverId());
            response.put("driverId", driver.getDriverId());
            response.put("ticketCount", allTickets.size());
            response.put("tickets", allTickets);
            response.put("status", "success");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
        }
        
        return response;
    }

    /**
     * Test endpoint để tạo ticket mẫu
     */
    @GetMapping("/create-test-ticket")
    @ResponseBody
    public Map<String, Object> createTestTicket(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Driver driver = (Driver) session.getAttribute("loggedInDriver");
            if (driver == null) {
                response.put("error", "Chưa đăng nhập");
                return response;
            }
            
            // Tạo ticket test
            TicketSupportRequest testTicket = TicketSupportRequest.builder()
                    .category("technical")
                    .comment("Test ticket để kiểm tra hệ thống")
                    .build();
            
            TicketSupportResponse createdTicket = ticketService.createTicket(testTicket, driver);
            response.put("success", true);
            response.put("ticket", createdTicket);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
        }
        
        return response;
    }

    /**
     * Cập nhật ticket
     */
    @PostMapping("/update/{id}")
    public String updateTicket(@PathVariable Integer id,
                              @Valid @ModelAttribute("ticket") TicketSupportRequest ticket,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("error", "Dữ liệu không hợp lệ");
            return "redirect:/support";
        }

        try {
            ticketService.updateTicket(id, ticket, driver.getDriverId());
            redirect.addFlashAttribute("success", "Cập nhật ticket thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/support";
    }

    /**
     * Xóa ticket
     */
    @PostMapping("/delete/{id}")
    public String deleteTicket(@PathVariable Integer id,
                              HttpSession session,
                              RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        try {
            ticketService.deleteTicket(id, driver.getDriverId());
            redirect.addFlashAttribute("success", "Xóa ticket thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/support";
    }

    /**
     * API endpoint để lấy số lượng notification
     */
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public Map<String, Object> getNotificationCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            response.put("count", 0);
            return response;
        }

        try {
            // Lấy thời gian cuối cùng driver xem (từ session)
            Instant lastSeenAt = (Instant) session.getAttribute("lastSeenAt");
            if (lastSeenAt == null) {
                lastSeenAt = Instant.now().minusSeconds(86400); // 24h trước
            }

            long count = ticketService.getUnreadTicketCount(driver.getDriverId(), lastSeenAt);
            response.put("count", count);
            
            // Cập nhật lastSeenAt
            session.setAttribute("lastSeenAt", Instant.now());
            
        } catch (Exception e) {
            response.put("count", 0);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
