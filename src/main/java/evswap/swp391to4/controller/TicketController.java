package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.TicketSupport;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.TicketSupportRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketSupportRepository ticketSupportRepository;
    private final DriverRepository driverRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTickets(@RequestHeader("Authorization") String authHeader) {
        try {
            List<TicketSupport> tickets = ticketSupportRepository.findAll();
            List<Map<String, Object>> ticketList = tickets.stream()
                .map(ticket -> {
                    Map<String, Object> ticketData = new HashMap<>();
                    ticketData.put("ticketId", ticket.getTicketId());
                    ticketData.put("category", ticket.getCategory());
                    ticketData.put("comment", ticket.getComment());
                    ticketData.put("status", ticket.getStatus());
                    ticketData.put("createdAt", ticket.getCreatedAt());
                    ticketData.put("resolvedAt", ticket.getResolvedAt());
                    ticketData.put("note", ticket.getNote());
                    
                    if (ticket.getDriver() != null) {
                        ticketData.put("driverId", ticket.getDriver().getDriverId());
                        ticketData.put("driverName", ticket.getDriver().getFullName());
                    }
                    
                    if (ticket.getStaff() != null) {
                        ticketData.put("staffId", ticket.getStaff().getStaffId());
                        ticketData.put("staffName", ticket.getStaff().getFullName());
                    }
                    
                    return ticketData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("tickets", ticketList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTicket(@RequestHeader("Authorization") String authHeader,
                                                           @RequestBody Map<String, Object> request) {
        try {
            String category = (String) request.get("category");
            String comment = (String) request.get("comment");
            Integer driverId = (Integer) request.get("driverId");

            if (driverId == null) {
                driverId = 1; // Default driver for demo
            }

            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            TicketSupport ticket = TicketSupport.builder()
                .category(category)
                .comment(comment)
                .status("open")
                .driver(driver)
                .createdAt(Instant.now())
                .build();

            TicketSupport savedTicket = ticketSupportRepository.save(ticket);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ticket", Map.of(
                "ticketId", savedTicket.getTicketId(),
                "category", savedTicket.getCategory(),
                "status", savedTicket.getStatus()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> getTicket(@PathVariable Integer ticketId,
                                                         @RequestHeader("Authorization") String authHeader) {
        try {
            TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("ticketId", ticket.getTicketId());
            ticketData.put("category", ticket.getCategory());
            ticketData.put("comment", ticket.getComment());
            ticketData.put("status", ticket.getStatus());
            ticketData.put("createdAt", ticket.getCreatedAt());
            ticketData.put("resolvedAt", ticket.getResolvedAt());
            ticketData.put("note", ticket.getNote());
            
            if (ticket.getDriver() != null) {
                ticketData.put("driverId", ticket.getDriver().getDriverId());
                ticketData.put("driverName", ticket.getDriver().getFullName());
            }
            
            if (ticket.getStaff() != null) {
                ticketData.put("staffId", ticket.getStaff().getStaffId());
                ticketData.put("staffName", ticket.getStaff().getFullName());
            }

            return ResponseEntity.ok(ticketData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> updateTicket(@PathVariable Integer ticketId,
                                                           @RequestHeader("Authorization") String authHeader,
                                                           @RequestBody Map<String, Object> request) {
        try {
            TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

            String status = (String) request.get("status");
            if (status != null) {
                ticket.setStatus(status);
                if ("resolved".equals(status) || "closed".equals(status)) {
                    ticket.setResolvedAt(Instant.now());
                }
            }

            String note = (String) request.get("note");
            if (note != null) {
                ticket.setNote(note);
            }

            ticketSupportRepository.save(ticket);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ticket updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> deleteTicket(@PathVariable Integer ticketId,
                                                           @RequestHeader("Authorization") String authHeader) {
        try {
            TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

            ticketSupportRepository.delete(ticket);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ticket deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
