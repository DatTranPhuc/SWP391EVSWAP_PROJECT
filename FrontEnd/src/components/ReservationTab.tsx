import React from 'react';

interface ReservationTabProps {
  reservationStep: number;
  setReservationStep: (step: number) => void;
  selectedStation: any;
  setSelectedStation: (station: any) => void;
  selectedDate: string;
  setSelectedDate: (date: string) => void;
  availableTimeSlots: any[];
  selectedTimeSlot: any;
  setSelectedTimeSlot: (slot: any) => void;
  stations: any[];
  vehicles: any[];
  selectedVehicle: any;
  setSelectedVehicle: (vehicle: any) => void;
  isCreatingReservation: boolean;
  reservationResult: any;
  handleCreateReservation: () => void;
  loadAvailableTimeSlots: (stationId: string, date: string) => void;
  resetReservationFlow: () => void;
  setActiveMenu: (menu: string) => void;
}

export default function ReservationTab({
  reservationStep,
  setReservationStep,
  selectedStation,
  setSelectedStation,
  selectedDate,
  setSelectedDate,
  availableTimeSlots,
  selectedTimeSlot,
  setSelectedTimeSlot,
  stations,
  vehicles,
  selectedVehicle,
  setSelectedVehicle,
  isCreatingReservation,
  reservationResult,
  handleCreateReservation,
  loadAvailableTimeSlots,
  resetReservationFlow,
  setActiveMenu
}: ReservationTabProps) {
  return (
    <>
      <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
        <div>
          <h1>Đặt lịch đổi pin</h1>
          <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Đặt trước lịch đổi pin tiện lợi</p>
        </div>
      </div>

      {/* Progress Steps */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', position: 'relative' }}>
          {/* Progress line */}
          <div style={{ position: 'absolute', top: '20px', left: '40px', right: '40px', height: '2px', backgroundColor: '#e5e7eb', zIndex: 0 }}>
            <div style={{ height: '100%', width: `${((reservationStep - 1) / 2) * 100}%`, backgroundColor: '#10b981', transition: 'width 0.3s ease' }}></div>
          </div>

          {/* Steps */}
          {[
            { step: 1, label: 'Chọn trạm', icon: <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg> },
            { step: 2, label: 'Chọn thời gian', icon: <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg> },
            { step: 3, label: 'Xác nhận & QR', icon: <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg> }
          ].map((item) => (
            <div key={item.step} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative', zIndex: 1, flex: 1 }}>
              <div style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                backgroundColor: reservationStep >= item.step ? '#10b981' : '#ffffff',
                border: `2px solid ${reservationStep >= item.step ? '#10b981' : '#e5e7eb'}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: reservationStep >= item.step ? '#ffffff' : '#9ca3af',
                transition: 'all 0.3s ease',
                marginBottom: '0.5rem'
              }}>
                {item.icon}
              </div>
              <span style={{ fontSize: '0.875rem', color: reservationStep >= item.step ? '#10b981' : '#6b7280' }}>{item.label}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Step 1: Select Station */}
      {reservationStep === 1 && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              Chọn trạm đổi pin
            </h2>
            <p className="card-description">Chọn trạm đổi pin gần bạn nhất</p>
          </div>
          <div className="card-content">
            {stations.length > 0 ? (
              <div className="content-grid">
                {stations.map((station, index) => (
                  <div 
                    key={station.id} 
                    className="card" 
                    style={{ 
                      border: selectedStation?.id === station.id ? '2px solid #10b981' : '2px solid #e5e7eb',
                      cursor: 'pointer',
                      transition: 'all 0.3s ease'
                    }}
                    onClick={() => setSelectedStation(station)}
                  >
                    <div className="card-content">
                      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                        <div style={{ 
                          width: '48px', 
                          height: '48px', 
                          borderRadius: '12px', 
                          background: index % 3 === 0 ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : 
                                     index % 3 === 1 ? 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' :
                                     'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: 'white',
                          flexShrink: 0
                        }}>
                          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                            <circle cx="12" cy="10" r="3"></circle>
                          </svg>
                        </div>
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                            <h3>{station.name}</h3>
                            {selectedStation?.id === station.id && (
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                <polyline points="22 4 12 14.01 9 11.01"></polyline>
                              </svg>
                            )}
                          </div>
                          <p style={{ color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.5rem' }}>{station.address}</p>
                          <p style={{ color: '#3b82f6', fontSize: '0.875rem' }}>
                            Cách bạn {(Math.random() * 5 + 1).toFixed(1)} km
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="empty-state">Chưa có trạm đổi pin nào</p>
            )}

            {selectedStation && (
              <div style={{ marginTop: '2rem', textAlign: 'right' }}>
                <button 
                  className="btn btn-primary"
                  onClick={() => {
                    setReservationStep(2);
                    loadAvailableTimeSlots(selectedStation.id, selectedDate);
                  }}
                >
                  Tiếp tục
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginLeft: '0.5rem' }}>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                  </svg>
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Step 2: Select Time */}
      {reservationStep === 2 && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              Chọn thời gian đặt lịch
            </h2>
            <p className="card-description">Chọn khung giờ phù hợp tại {selectedStation?.name}</p>
          </div>
          <div className="card-content">
            {/* Date picker */}
            <div style={{ marginBottom: '1.5rem' }}>
              <label className="form-label" htmlFor="reservation-date">Chọn ngày</label>
              <input 
                id="reservation-date"
                type="date" 
                className="form-input"
                value={selectedDate}
                min={new Date().toISOString().split('T')[0]}
                onChange={(e) => {
                  setSelectedDate(e.target.value);
                  loadAvailableTimeSlots(selectedStation.id, e.target.value);
                  setSelectedTimeSlot(null);
                }}
              />
            </div>
            
            {/* Time slots grid */}
            {availableTimeSlots.length > 0 ? (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))', gap: '1rem' }}>
                {availableTimeSlots.map((slot) => (
                  <div 
                    key={slot.time}
                    onClick={() => slot.available > 0 && setSelectedTimeSlot(slot)}
                    style={{
                      border: selectedTimeSlot?.time === slot.time ? '2px solid #10b981' : '2px solid #e5e7eb',
                      padding: '1rem',
                      borderRadius: '8px',
                      cursor: slot.available > 0 ? 'pointer' : 'not-allowed',
                      opacity: slot.available > 0 ? 1 : 0.5,
                      transition: 'all 0.3s ease',
                      textAlign: 'center',
                      backgroundColor: selectedTimeSlot?.time === slot.time ? '#f0fdf4' : '#ffffff'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke={selectedTimeSlot?.time === slot.time ? '#10b981' : '#6b7280'} strokeWidth="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                      </svg>
                      <h4 style={{ margin: 0, color: selectedTimeSlot?.time === slot.time ? '#10b981' : '#1f2937' }}>{slot.time}</h4>
                    </div>
                    <p style={{ fontSize: '0.875rem', color: slot.available > 0 ? '#10b981' : '#ef4444', margin: 0 }}>
                      {slot.available > 0 ? `Còn ${slot.available}/${slot.total} chỗ` : 'Đã hết chỗ'}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="empty-state">Không có khung giờ nào khả dụng cho ngày này</p>
            )}
            
            <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem', justifyContent: 'flex-end' }}>
              <button 
                className="btn btn-secondary"
                onClick={() => {
                  setReservationStep(1);
                  setSelectedTimeSlot(null);
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                  <line x1="19" y1="12" x2="5" y2="12"></line>
                  <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
                Quay lại
              </button>
              {selectedTimeSlot && (
                <button 
                  className="btn btn-primary"
                  onClick={() => {
                    if (vehicles.length > 0) setSelectedVehicle(vehicles[0]);
                    handleCreateReservation();
                  }}
                  disabled={isCreatingReservation}
                >
                  {isCreatingReservation ? 'Đang xử lý...' : 'Xác nhận đặt lịch'}
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginLeft: '0.5rem' }}>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                  </svg>
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Step 3: Confirmation & QR Code */}
      {reservationStep === 3 && reservationResult && (
        <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem' }}>
          <div style={{ marginBottom: '2rem' }}>
            <div style={{ 
              width: '100px', 
              height: '100px', 
              margin: '0 auto',
              borderRadius: '50%', 
              background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '1.5rem'
            }}>
              <svg xmlns="http://www.w3.org/2000/svg" width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <h1 style={{ marginBottom: '1rem', color: '#10b981' }}>Đặt lịch thành công!</h1>
            <p style={{ color: '#6b7280', fontSize: '1.125rem' }}>
              Vui lòng xuất trình mã QR dưới đây cho nhân viên tại trạm
            </p>
          </div>

          {/* QR Code */}
          <div style={{ marginBottom: '2rem' }}>
            <div style={{ 
              width: '250px', 
              height: '250px',
              margin: '0 auto',
              border: '3px solid #10b981',
              borderRadius: '16px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: '#f0fdf4',
              padding: '1rem'
            }}>
              {/* QR Code placeholder - In real app, use qrcode.react */}
              <svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2">
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="14" y="14" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
                <line x1="11" y1="7" x2="11" y2="7.01"></line>
                <line x1="18" y1="11" x2="18" y2="11.01"></line>
                <line x1="7" y1="18" x2="7" y2="18.01"></line>
              </svg>
            </div>
            <p style={{ marginTop: '1rem', fontSize: '0.875rem', color: '#6b7280', fontFamily: 'monospace' }}>
              QR Token: {reservationResult.qr_token.substring(0, 16)}...
            </p>
          </div>

          {/* Reservation Details */}
          <div style={{ padding: '2rem', backgroundColor: '#f9fafb', borderRadius: '12px', marginBottom: '2rem', textAlign: 'left' }}>
            <h3 style={{ textAlign: 'center', marginBottom: '1.5rem' }}>Thông tin đặt lịch</h3>
            
            <div style={{ display: 'grid', gap: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                <span style={{ color: '#6b7280' }}>Mã đặt lịch</span>
                <span style={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>{reservationResult.id.substring(0, 8).toUpperCase()}</span>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                <span style={{ color: '#6b7280' }}>Xe</span>
                <span>{reservationResult.vehicle.model} - {reservationResult.vehicle.plate_number}</span>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                <span style={{ color: '#6b7280' }}>Trạm</span>
                <span>{reservationResult.station.name}</span>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                <span style={{ color: '#6b7280' }}>Thời gian đến</span>
                <span style={{ color: '#10b981' }}>{new Date(reservationResult.reserved_start).toLocaleString('vi-VN')}</span>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                <span style={{ color: '#6b7280' }}>Mã QR hết hạn lúc</span>
                <span style={{ color: '#ef4444' }}>{new Date(reservationResult.qr_expires_at).toLocaleString('vi-VN')}</span>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: '#6b7280' }}>Trạng thái</span>
                <span className="badge badge-primary">Chờ xác nhận</span>
              </div>
            </div>
          </div>

          {/* Instructions */}
          <div style={{ padding: '1.5rem', background: '#dbeafe', borderRadius: '12px', borderLeft: '4px solid #3b82f6', marginBottom: '2rem', textAlign: 'left' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" strokeWidth="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <h4 style={{ margin: 0, color: '#1e40af' }}>Hướng dẫn sử dụng</h4>
            </div>
            <ol style={{ paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#1e40af', margin: 0 }}>
              <li style={{ marginBottom: '0.5rem' }}>Đến trạm đổi pin đúng giờ đã đặt ({new Date(reservationResult.reserved_start).toLocaleTimeString('vi-VN')})</li>
              <li style={{ marginBottom: '0.5rem' }}>Xuất trình mã QR trên cho nhân viên quét</li>
              <li style={{ marginBottom: '0.5rem' }}>Nhân viên sẽ xác nhận và hỗ trợ đổi pin</li>
              <li>Mã QR có hiệu lực trong vòng 30 phút kể từ thời gian đặt</li>
            </ol>
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <button 
              className="btn btn-secondary"
              onClick={() => setActiveMenu('overview')}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="14" y="14" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
              </svg>
              Về Dashboard
            </button>
            <button 
              className="btn btn-primary"
              onClick={resetReservationFlow}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              Đặt lịch mới
            </button>
          </div>
        </div>
      )}
    </>
  );
}
