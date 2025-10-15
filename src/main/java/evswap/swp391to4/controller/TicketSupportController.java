package evswap.swp391to4.controller;

import evswap.swp391to4.dto.TicketCreateRequest;
import evswap.swp391to4.dto.TicketResponse;
import evswap.swp391to4.dto.TicketUpdateRequest;
import evswap.swp391to4.service.TicketSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class TicketSupportController {

    private final TicketSupportService ticketSupportService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody TicketCreateRequest request) {
        return ResponseEntity.status(201).body(ticketSupportService.createTicket(request));
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> update(@PathVariable Integer ticketId,
                                                 @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(ticketSupportService.updateTicket(ticketId, request));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TicketResponse>> listForDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(ticketSupportService.getTicketsForDriver(driverId));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<TicketResponse>> listForStaff(@PathVariable Integer staffId) {
        return ResponseEntity.ok(ticketSupportService.getTicketsForStaff(staffId));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listByStatus(@RequestParam("status") String status) {
        return ResponseEntity.ok(ticketSupportService.getTicketsByStatus(status));
    }
}
