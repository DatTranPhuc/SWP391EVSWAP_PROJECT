package evswap.swp391to4.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.dto.TicketSupportRequest;
import evswap.swp391to4.dto.TicketSupportResponse;
import evswap.swp391to4.dto.TicketUpdateRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.TicketSupport;
import evswap.swp391to4.repository.TicketSupportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketSupportService {

    private final TicketSupportRepository ticketRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/tickets/";

    /**
     * Tạo ticket mới từ driver
     */
    @Transactional
    public TicketSupportResponse createTicket(TicketSupportRequest request, Driver driver) {
        // Kiểm tra category hợp lệ
        if (!isValidCategory(request.getCategory())) {
            throw new IllegalArgumentException("Danh mục không hợp lệ");
        }

        // Tạo ticket entity
        TicketSupport ticket = TicketSupport.builder()
                .driver(driver)
                .category(request.getCategory())
                .comment(request.getComment())
                .status("open")
                .createdAt(Instant.now())
                .build();

        TicketSupport saved = ticketRepo.save(ticket);
        return mapToResponse(saved);
    }

    /**
     * Lấy tất cả ticket (cho admin/staff)
     */
    @Transactional(readOnly = true)
    public List<TicketSupportResponse> getAllTickets() {
        List<TicketSupport> tickets = ticketRepo.findAllByOrderByCreatedAtDesc();
        return mapToResponseList(tickets);
    }

    /**
     * Lấy ticket của một driver cụ thể
     */
    @Transactional(readOnly = true)
    public List<TicketSupportResponse> getTicketsByDriver(Integer driverId) {
        List<TicketSupport> tickets = ticketRepo.findByDriverDriverIdOrderByCreatedAtDesc(driverId);
        return mapToResponseList(tickets);
    }

    /**
     * Lấy ticket theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<TicketSupportResponse> getTicketsByStatus(String status) {
        List<TicketSupport> tickets = ticketRepo.findByStatusOrderByCreatedAtDesc(status);
        return mapToResponseList(tickets);
    }

    /**
     * Lấy ticket theo danh mục
     */
    @Transactional(readOnly = true)
    public List<TicketSupportResponse> getTicketsByCategory(String category) {
        List<TicketSupport> tickets = ticketRepo.findByCategoryOrderByCreatedAtDesc(category);
        return mapToResponseList(tickets);
    }

    /**
     * Lấy ticket được giao cho staff
     */
    @Transactional(readOnly = true)
    public List<TicketSupportResponse> getTicketsByStaff(Integer staffId) {
        List<TicketSupport> tickets = ticketRepo.findByStaffStaffIdOrderByCreatedAtDesc(staffId);
        return mapToResponseList(tickets);
    }

    /**
     * Lấy ticket theo ID
     */
    @Transactional(readOnly = true)
    public TicketSupportResponse getTicketById(Integer ticketId) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));
        return mapToResponse(ticket);
    }

    /**
     * Cập nhật ticket (staff/admin)
     */
    @Transactional
    public TicketSupportResponse updateTicket(Integer ticketId, TicketUpdateRequest request, Staff staff) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        // Cập nhật thông tin
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            ticket.setStatus(request.getStatus());
        }
        
        if (request.getNote() != null && !request.getNote().isEmpty()) {
            ticket.setNote(request.getNote());
        }
        
        if (request.getStaffId() != null) {
            // Gán staff cho ticket
            Staff assignedStaff = new Staff();
            assignedStaff.setStaffId(request.getStaffId());
            ticket.setStaff(assignedStaff);
        }

        // Nếu chuyển sang resolved, set thời gian resolved
        if ("resolved".equals(request.getStatus())) {
            ticket.setResolvedAt(Instant.now());
        }

        TicketSupport saved = ticketRepo.save(ticket);
        return mapToResponse(saved);
    }

    /**
     * Đánh dấu ticket là resolved
     */
    @Transactional
    public TicketSupportResponse resolveTicket(Integer ticketId, String note, Staff staff) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        ticket.setStatus("resolved");
        ticket.setNote(note);
        ticket.setResolvedAt(Instant.now());
        ticket.setStaff(staff);

        TicketSupport saved = ticketRepo.save(ticket);
        return mapToResponse(saved);
    }

    /**
     * Đóng ticket
     */
    @Transactional
    public TicketSupportResponse closeTicket(Integer ticketId) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        ticket.setStatus("closed");
        if (ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(Instant.now());
        }

        TicketSupport saved = ticketRepo.save(ticket);
        return mapToResponse(saved);
    }

    /**
     * Kiểm tra category hợp lệ
     */
    private boolean isValidCategory(String category) {
        return category != null && (
                "station".equals(category) ||
                "battery".equals(category) ||
                "payment".equals(category) ||
                "account".equals(category) ||
                "technical".equals(category) ||
                "other".equals(category)
        );
    }

    /**
     * Helper method để map Entity sang DTO
     */
    private TicketSupportResponse mapToResponse(TicketSupport ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket không được null");
        }
        
        if (ticket.getDriver() == null) {
            throw new IllegalArgumentException("Driver không được null trong ticket");
        }
        
        return TicketSupportResponse.builder()
                .ticketId(ticket.getTicketId())
                .driverId(ticket.getDriver().getDriverId())
                .driverName(ticket.getDriver().getFullName())
                .staffId(ticket.getStaff() != null ? ticket.getStaff().getStaffId() : null)
                .staffName(ticket.getStaff() != null ? ticket.getStaff().getFullName() : null)
                .category(ticket.getCategory())
                .comment(ticket.getComment())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .note(ticket.getNote())
                .commentHistory(ticket.getCommentHistory())
                .attachments(ticket.getAttachments())
                .build();
    }

    /**
     * Helper method để map list Entity sang list DTO
     */
    private List<TicketSupportResponse> mapToResponseList(List<TicketSupport> tickets) {
        List<TicketSupportResponse> responseList = new ArrayList<>();
        for (TicketSupport ticket : tickets) {
            responseList.add(mapToResponse(ticket));
        }
        return responseList;
    }

    /**
     * Thêm comment vào ticket
     */
    @Transactional
    public void addComment(Integer ticketId, String author, String authorName, String message) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        // Validate input
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author không được để trống");
        }
        if (authorName == null || authorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name không được để trống");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message không được để trống");
        }

        List<Comment> comments = getComments(ticket);
        
        Comment newComment = new Comment();
        newComment.setAuthor(author.trim());
        newComment.setName(authorName.trim());
        newComment.setMessage(message.trim());
        
        // Format timestamp to Vietnamese format
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        java.time.ZoneId vietnamZone = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        String formattedTimestamp = Instant.now().atZone(vietnamZone).format(formatter);
        newComment.setTimestamp(formattedTimestamp);
        
        comments.add(newComment);
        
        try {
            String commentHistoryJson = objectMapper.writeValueAsString(comments);
            ticket.setCommentHistory(commentHistoryJson);
            ticketRepo.save(ticket);
            
            // Log successful comment addition
            System.out.println("Comment added successfully to ticket " + ticketId + " by " + authorName);
        } catch (Exception e) {
            System.err.println("Error saving comment to ticket " + ticketId + ": " + e.getMessage());
            throw new RuntimeException("Lỗi khi lưu comment: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách comments từ ticket
     */
    public List<Comment> getComments(TicketSupport ticket) {
        if (ticket == null || ticket.getCommentHistory() == null || ticket.getCommentHistory().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<Comment> comments = objectMapper.readValue(ticket.getCommentHistory(), new TypeReference<List<Comment>>() {});
            if (comments == null) {
                return new ArrayList<>();
            }
            return comments;
        } catch (Exception e) {
            System.err.println("Error parsing comment history for ticket " + ticket.getTicketId() + ": " + e.getMessage());
            // Return empty list instead of null to prevent further errors
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách comments từ ticket ID
     */
    public List<Comment> getCommentsByTicketId(Integer ticketId) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));
        return getComments(ticket);
    }

    /**
     * Upload file attachment
     */
    @Transactional
    public String saveAttachment(Integer ticketId, MultipartFile file) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        try {
            // Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("Tên file không hợp lệ");
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Lưu file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Cập nhật attachments trong ticket
            String currentAttachments = ticket.getAttachments();
            if (currentAttachments == null || currentAttachments.isEmpty()) {
                ticket.setAttachments(uniqueFilename);
            } else {
                ticket.setAttachments(currentAttachments + "," + uniqueFilename);
            }
            
            ticketRepo.save(ticket);
            return uniqueFilename;
            
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Lỗi khi upload file: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách attachments
     */
    public List<String> getAttachments(TicketSupport ticket) {
        if (ticket.getAttachments() == null || ticket.getAttachments().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(ticket.getAttachments().split(","));
    }

    /**
     * Cập nhật ticket (chỉ driver sở hữu mới được cập nhật)
     */
    @Transactional
    public TicketSupportResponse updateTicket(Integer ticketId, TicketSupportRequest request, Integer driverId) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        // Kiểm tra quyền sở hữu
        if (!ticket.getDriver().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Bạn không có quyền cập nhật ticket này");
        }

        // Chỉ cho phép cập nhật nếu ticket chưa được xử lý
        if (!"open".equals(ticket.getStatus())) {
            throw new IllegalStateException("Không thể cập nhật ticket đã được xử lý");
        }

        // Kiểm tra category hợp lệ
        if (!isValidCategory(request.getCategory())) {
            throw new IllegalArgumentException("Danh mục không hợp lệ");
        }

        // Cập nhật thông tin
        ticket.setCategory(request.getCategory());
        ticket.setComment(request.getComment());

        TicketSupport saved = ticketRepo.save(ticket);
        return mapToResponse(saved);
    }

    /**
     * Xóa ticket (chỉ driver sở hữu mới được xóa)
     */
    @Transactional
    public void deleteTicket(Integer ticketId, Integer driverId) {
        TicketSupport ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket"));

        // Kiểm tra quyền sở hữu
        if (!ticket.getDriver().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Bạn không có quyền xóa ticket này");
        }

        // Chỉ cho phép xóa nếu ticket chưa được xử lý
        if (!"open".equals(ticket.getStatus())) {
            throw new IllegalStateException("Không thể xóa ticket đã được xử lý");
        }

        // Xóa attachments nếu có
        if (ticket.getAttachments() != null && !ticket.getAttachments().isEmpty()) {
            try {
                String[] attachmentFiles = ticket.getAttachments().split(",");
                for (String fileName : attachmentFiles) {
                    Path filePath = Paths.get(UPLOAD_DIR + fileName.trim());
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                }
            } catch (IOException e) {
                // Log error nhưng vẫn xóa ticket
                System.err.println("Lỗi khi xóa attachments: " + e.getMessage());
            }
        }

        ticketRepo.delete(ticket);
    }

    /**
     * Lấy số lượng ticket có cập nhật mới (cho notification)
     */
    @Transactional(readOnly = true)
    public long getUnreadTicketCount(Integer driverId, Instant lastSeenAt) {
        return ticketRepo.countByDriverDriverIdAndCreatedAtAfter(driverId, lastSeenAt);
    }

    /**
     * Inner class cho Comment
     */
    public static class Comment {
        private String author;
        private String name;
        private String message;
        private String timestamp;

        // Getters and Setters
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
