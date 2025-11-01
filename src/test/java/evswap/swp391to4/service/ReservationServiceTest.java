package evswap.swp391to4.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import evswap.swp391to4.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private WalletService walletService;
    @Mock
    private BatteryService batteryService;
    @Mock
    private SwapTransactionRepository swapTransactionRepository;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                driverRepository,
                stationRepository,
                vehicleRepository,
                paymentService,
                walletService,
                batteryService,
                swapTransactionRepository);
    }

    @Test
    void checkInReservation_withValidToken_updatesStatusAndQr() {
        Reservation reservation = Reservation.builder()
                .reservationId(1)
                .status("confirmed")
                .qrToken("TOKEN-123")
                .qrStatus("active")
                .qrExpiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        reservationService.checkInReservation(1, "TOKEN-123");

        assertEquals("checked_in", reservation.getStatus());
        assertEquals("used", reservation.getQrStatus());
        assertNotNull(reservation.getCheckedInAt());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void checkInReservation_withInvalidToken_throwsException() {
        Reservation reservation = Reservation.builder()
                .reservationId(2)
                .status("confirmed")
                .qrToken("TOKEN-ABC")
                .qrStatus("active")
                .qrExpiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(reservationRepository.findById(2)).thenReturn(Optional.of(reservation));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reservationService.checkInReservation(2, "TOKEN-WRONG"));

        assertEquals("Mã QR không chính xác", exception.getMessage());
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void checkInReservation_withExpiredToken_marksExpiredAndThrows() {
        Reservation reservation = Reservation.builder()
                .reservationId(3)
                .status("confirmed")
                .qrToken("TOKEN-XYZ")
                .qrStatus("active")
                .qrExpiresAt(Instant.now().minusSeconds(120))
                .build();

        when(reservationRepository.findById(3)).thenReturn(Optional.of(reservation));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reservationService.checkInReservation(3, "TOKEN-XYZ"));

        assertEquals("Mã QR đã hết hạn", exception.getMessage());
        assertEquals("expired", reservation.getQrStatus());
        verify(reservationRepository).save(reservation);
    }
}
