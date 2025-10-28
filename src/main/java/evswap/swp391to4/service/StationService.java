package evswap.swp391to4.service;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;

import java.math.BigDecimal;
import java.util.List;

public interface StationService {
    StationResponse createStation(StationCreateRequest req);
    List<StationResponse> getAllStations();
    List<StationResponse> searchByName(String name);
    List<StationResponse> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm);
    StationResponse findById(Integer stationId);
    StationResponse updateStation(Integer stationId, StationCreateRequest req);
    void deleteStation(Integer stationId);
}
