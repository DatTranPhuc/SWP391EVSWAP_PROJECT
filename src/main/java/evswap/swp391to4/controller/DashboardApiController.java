package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.AdminRepository;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

	private final DriverRepository driverRepository;
	private final VehicleRepository vehicleRepository;
	private final StationRepository stationRepository;
	private final StaffRepository staffRepository;
	private final AdminRepository adminRepository;

	@GetMapping("/driver/{driverId}")
	public ResponseEntity<Map<String, Object>> driverDashboard(@PathVariable Integer driverId) {
		Driver driver = driverRepository.findById(driverId)
				.orElseThrow(() -> new IllegalStateException("Tài khoản tài xế không tồn tại"));

		long totalVehicles = vehicleRepository.count(); // fallback if repository lacks byDriver
		// Attempt to narrow by driver if relation is mapped and derived query exists in future

		Map<String, Object> body = new HashMap<>();
		body.put("driverId", driver.getDriverId());
		body.put("driverName", driver.getFullName());
		body.put("emailVerified", driver.getEmailVerified());
		body.put("vehiclesCount", totalVehicles);
		body.put("stationsCount", stationRepository.count());
		return ResponseEntity.ok(body);
	}

	@GetMapping("/staff/{staffId}")
	public ResponseEntity<Map<String, Object>> staffDashboard(@PathVariable Integer staffId) {
		Staff staff = staffRepository.findById(staffId)
				.orElseThrow(() -> new IllegalStateException("Nhân viên không tồn tại"));

		Station station = staff.getStation();

		Map<String, Object> body = new HashMap<>();
		body.put("staffId", staff.getStaffId());
		body.put("fullName", staff.getFullName());
		body.put("isActive", staff.getIsActive());
		if (station != null) {
			Map<String, Object> stationInfo = new HashMap<>();
			stationInfo.put("stationId", station.getStationId());
			stationInfo.put("name", station.getName());
			stationInfo.put("status", station.getStatus());
			stationInfo.put("address", station.getAddress());
			body.put("station", stationInfo);
		} else {
			body.put("station", null);
		}
		return ResponseEntity.ok(body);
	}

	@GetMapping("/admin")
	public ResponseEntity<Map<String, Object>> adminDashboard() {
		Map<String, Object> stats = new HashMap<>();
		stats.put("drivers", driverRepository.count());
		stats.put("vehicles", vehicleRepository.count());
		stats.put("stations", stationRepository.count());
		stats.put("staff", staffRepository.count());
		stats.put("admins", adminRepository.count());
		return ResponseEntity.ok(stats);
	}
}
