package evswap.swp391to4.service;

import evswap.swp391to4.dto.TicketCreateRequest;
import evswap.swp391to4.dto.TicketResponse;
import evswap.swp391to4.dto.TicketUpdateRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.TicketSupport;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.TicketSupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSupportService {

    private final TicketSupportRepository ticketSupportRepository;
    private final DriverRepository driverRepository;
    private final StaffRepository staffRepository;

    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản tài xế không tồn tại"));

        Staff staff = null;
        if (request.getStaffId() != null) {
            staff = staffRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        }

        TicketSupport ticket = TicketSupport.builder()
                .driver(driver)
                .staff(staff)
                .category(request.getCategory())
                .comment(request.getComment())
                .status("open")
                .createdAt(Instant.now())
                .build();

        TicketSupport saved = ticketSupportRepository.save(ticket);
        return toResponse(saved);
    }

    @Transactional
    public TicketResponse updateTicket(Integer ticketId, TicketUpdateRequest request) {
        TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket không tồn tại"));

        if (request.getStaffId() != null) {
            Staff staff = staffRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
            ticket.setStaff(staff);
        }

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            if ("resolved".equalsIgnoreCase(request.getStatus()) || "closed".equalsIgnoreCase(request.getStatus())) {
                ticket.setResolvedAt(Instant.now());
            }
        }

        if (request.getNote() != null) {
            ticket.setNote(request.getNote());
        }

        TicketSupport saved = ticketSupportRepository.save(ticket);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForDriver(Integer driverId) {
        return ticketSupportRepository.findByDriverDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForStaff(Integer staffId) {
        return ticketSupportRepository.findByStaffStaffIdOrderByCreatedAtDesc(staffId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByStatus(String status) {
        return ticketSupportRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketResponse toResponse(TicketSupport ticket) {
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .driverId(ticket.getDriver() != null ? ticket.getDriver().getDriverId() : null)
                .driverName(ticket.getDriver() != null ? ticket.getDriver().getFullName() : null)
                .staffId(ticket.getStaff() != null ? ticket.getStaff().getStaffId() : null)
                .staffName(ticket.getStaff() != null ? ticket.getStaff().getFullName() : null)
                .category(ticket.getCategory())
                .comment(ticket.getComment())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .note(ticket.getNote())
                .build();
    }
}
