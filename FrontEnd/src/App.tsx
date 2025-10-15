import { useState, useEffect } from "react";
// Import từ config.ts để hỗ trợ chạy local với .env
import { projectId, publicAnonKey } from "./utils/supabase/config";
import "./styles/app.css";

type Page = 'landing' | 'login' | 'register' | 'verify-otp' | 'vehicle-registration' | 'dashboard';

export default function App() {
  const [currentPage, setCurrentPage] = useState<Page>('landing');
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [accessToken, setAccessToken] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Form states
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [model, setModel] = useState('');
  const [vin, setVin] = useState('');
  const [plateNumber, setPlateNumber] = useState('');
  const [loading, setLoading] = useState(false);

  // Dashboard states
  const [vehicles, setVehicles] = useState<any[]>([]);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [stations, setStations] = useState<any[]>([]);
  const [tickets, setTickets] = useState<any[]>([]);
  const [users, setUsers] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState('users');
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState<'staff' | 'station' | 'ticket' | 'feedback' | 'vehicle' | null>(null);
  const [isLoadingStations, setIsLoadingStations] = useState(false);
  const [isAddingVehicle, setIsAddingVehicle] = useState(false);

  // Reservation states
  const [selectedStation, setSelectedStation] = useState<any>(null);
  const [selectedVehicle, setSelectedVehicle] = useState<any>(null);
  const [availableTimeSlots, setAvailableTimeSlots] = useState<any[]>([]);
  const [selectedTimeSlot, setSelectedTimeSlot] = useState<any>(null);
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [reservationStep, setReservationStep] = useState(1); // 1: Select Station, 2: Select Time, 3: Confirm & QR
  const [isCreatingReservation, setIsCreatingReservation] = useState(false);
  const [reservationResult, setReservationResult] = useState<any>(null);
  const [reservations, setReservations] = useState<any[]>([]);

  // Modal form states
  const [staffEmail, setStaffEmail] = useState('');
  const [staffPassword, setStaffPassword] = useState('');
  const [staffFullName, setStaffFullName] = useState('');
  const [staffPhone, setStaffPhone] = useState('');
  const [stationName, setStationName] = useState('');
  const [stationAddress, setStationAddress] = useState('');
  const [stationLat, setStationLat] = useState('');
  const [stationLng, setStationLng] = useState('');
  const [ticketSubject, setTicketSubject] = useState('');
  const [ticketDescription, setTicketDescription] = useState('');
  
  // Add vehicle form states
  const [newVehicleModel, setNewVehicleModel] = useState('');
  const [newVehicleVin, setNewVehicleVin] = useState('');
  const [newVehiclePlateNumber, setNewVehiclePlateNumber] = useState('');

  // Payment & Feedback states
  const [feedbacks, setFeedbacks] = useState<any[]>([]);
  const [payments, setPayments] = useState<any[]>([]);
  const [selectedTransactionForFeedback, setSelectedTransactionForFeedback] = useState<any>(null);
  const [feedbackRating, setFeedbackRating] = useState(5);
  const [feedbackComment, setFeedbackComment] = useState('');
  const [topUpAmount, setTopUpAmount] = useState('');
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [paymentTransactionId, setPaymentTransactionId] = useState('');
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState('momo');

  // Dashboard statistics states
  const [thisMonthCost, setThisMonthCost] = useState<number>(0);
  const [averageCostPerSwap, setAverageCostPerSwap] = useState<number>(0);

  // Vehicle detail states
  const [vehicleDetail, setVehicleDetail] = useState<any>(null);

  // System config states
  const [systemConfig, setSystemConfig] = useState<any>(null);

  // Settings Modals
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showSecurityModal, setShowSecurityModal] = useState(false);

  // Profile Edit States
  const [editFullName, setEditFullName] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [editPhone, setEditPhone] = useState('');
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);

  // Security States
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  // Settings States
  const [language, setLanguage] = useState('vi');
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [emailNotifications, setEmailNotifications] = useState(true);

  // Email Verification States
  const [showEmailVerificationModal, setShowEmailVerificationModal] = useState(false);
  const [verificationEmail, setVerificationEmail] = useState('');
  const [isEmailVerified, setIsEmailVerified] = useState(false);

  // Dropdown states
  const [showMenuDropdown, setShowMenuDropdown] = useState(false);
  const [showUserDropdown, setShowUserDropdown] = useState(false);
  
  // Sidebar states
  const [showRightSidebar, setShowRightSidebar] = useState(false);
  const [showLeftDrawer, setShowLeftDrawer] = useState(false);
  const [activeMenu, setActiveMenu] = useState('overview');

  // Swap states
  const [swapStep, setSwapStep] = useState(1);
  const [availableBatteries, setAvailableBatteries] = useState<any[]>([]);
  const [selectedBattery, setSelectedBattery] = useState<any>(null);
  const [isSwapping, setIsSwapping] = useState(false);
  const [swapResult, setSwapResult] = useState<any>(null);

  useEffect(() => {
    checkSession();
  }, []);

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('.menu-dropdown-container') && !target.closest('.user-dropdown-container')) {
        setShowMenuDropdown(false);
        setShowUserDropdown(false);
      }
    };

    if (showMenuDropdown || showUserDropdown) {
      document.addEventListener('click', handleClickOutside);
      return () => document.removeEventListener('click', handleClickOutside);
    }
  }, [showMenuDropdown, showUserDropdown]);

  useEffect(() => {
    if (currentPage === 'dashboard' && currentUser) {
      loadDashboardData();
    }
  }, [currentPage, currentUser]);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
  };

  const checkSession = async () => {
    const storedToken = localStorage.getItem('access_token');
    if (storedToken) {
      try {
        const response = await fetch(
          'http://localhost:8080/api/v1/me',
          { headers: { 'Authorization': `Bearer ${storedToken}` } }
        );
        
        if (response.ok) {
          const data = await response.json();
          setCurrentUser(data.user);
          setAccessToken(storedToken);
          
          if (data.user.role === 'driver') {
            const vehicleResponse = await fetch(
              'http://localhost:8080/api/v1/vehicles',
              { headers: { 'Authorization': `Bearer ${storedToken}` } }
            );
            const vehicleData = await vehicleResponse.json();
            if (!vehicleData.vehicles || vehicleData.vehicles.length === 0) {
              setCurrentPage('vehicle-registration');
            } else {
              setCurrentPage('dashboard');
            }
          } else {
            setCurrentPage('dashboard');
          }
        } else {
          localStorage.removeItem('access_token');
        }
      } catch (error) {
        localStorage.removeItem('access_token');
      }
    }
  };

  // Load wallet data
  const loadWalletData = async () => {
    if (!accessToken) return;
    
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/wallet/dashboard',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      
      if (response.ok) {
        const data = await response.json();
        if (data.success && data.dashboard) {
          // Update wallet balance in dashboard cards
          const balanceElement = document.querySelector('[data-wallet-balance]');
          if (balanceElement && data.dashboard.currentBalance) {
            const balance = new Intl.NumberFormat('vi-VN').format(data.dashboard.currentBalance);
            balanceElement.textContent = `${balance}đ`;
          }
          
          // Update sidebar balance
          const sidebarBalanceElement = document.querySelector('.sidebar-balance');
          if (sidebarBalanceElement && data.dashboard.currentBalance) {
            const balance = new Intl.NumberFormat('vi-VN').format(data.dashboard.currentBalance);
            sidebarBalanceElement.textContent = `${balance} VND`;
          }
          
          // Update swap count
          const swapCountElement = document.querySelector('[data-swap-count]');
          if (swapCountElement && data.dashboard.totalSwaps) {
            swapCountElement.textContent = data.dashboard.totalSwaps.toString();
          }
          
          // Update favorite station
          const favoriteStationElement = document.querySelector('[data-favorite-station]');
          if (favoriteStationElement && data.dashboard.favoriteStation) {
            favoriteStationElement.textContent = data.dashboard.favoriteStation;
          }
          
          // Update average cost
          const averageCostElement = document.querySelector('[data-average-cost]');
          if (averageCostElement && data.dashboard.averageCost) {
            const cost = new Intl.NumberFormat('vi-VN').format(data.dashboard.averageCost);
            averageCostElement.textContent = `${cost}đ`;
          }

          // Update this month cost
          if (data.dashboard.thisMonthCost !== undefined) {
            setThisMonthCost(Number(data.dashboard.thisMonthCost));
          }

          // Update average cost per swap
          if (data.dashboard.averageCostPerSwap !== undefined) {
            setAverageCostPerSwap(Number(data.dashboard.averageCostPerSwap));
          }
        }
      }
    } catch (error) {
      console.error('Error loading wallet data:', error);
    }
  };

  const loadDashboardData = async () => {
    // Load common data for all users
    loadSystemConfig();
    
    if (currentUser.role === 'driver') {
      loadVehicles();
      loadVehicleDetail();
      loadTransactions();
      loadStations();
      loadTickets();
      loadReservations();
      loadFeedbacks();
      loadWalletData();
    } else if (currentUser.role === 'admin') {
      loadUsers();
      loadStations();
    } else if (currentUser.role === 'staff') {
      loadStations();
      loadTickets();
      loadTransactions();
    }
  };

  const loadVehicles = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/vehicles',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.vehicles) setVehicles(data.vehicles);
    } catch (error) {
      console.error('Error loading vehicles:', error);
    }
  };

  const loadVehicleDetail = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/vehicles/detail',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.success && data.vehicleDetail) {
        setVehicleDetail(data.vehicleDetail);
      }
    } catch (error) {
      console.error('Error loading vehicle detail:', error);
    }
  };


  const loadSystemConfig = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/system-config',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.success && data.config) {
        setSystemConfig(data.config);
      }
    } catch (error) {
      console.error('Error loading system config:', error);
    }
  };

  const loadTransactions = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/transactions',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.transactions) setTransactions(data.transactions);
    } catch (error) {
      console.error('Error loading transactions:', error);
    }
  };

  const loadStations = async (showNotification = false) => {
    try {
      setIsLoadingStations(true);
      const response = await fetch(
        'http://localhost:8080/api/v1/stations',
        { headers: { 'Authorization': `Bearer ${accessToken || publicAnonKey}` } }
      );
      const data = await response.json();
      if (data.stations) {
        setStations(data.stations);
        if (showNotification) {
          showToast(`Tải thành công ${data.stations.length} trạm đổi pin`, 'success');
        }
      }
    } catch (error) {
      console.error('Error loading stations:', error);
      if (showNotification) {
        showToast('Có lỗi khi tải danh sách trạm', 'error');
      }
    } finally {
      setIsLoadingStations(false);
    }
  };

  const loadTickets = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/tickets',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.tickets) setTickets(data.tickets);
    } catch (error) {
      console.error('Error loading tickets:', error);
    }
  };

  const loadUsers = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/admin/users',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.users) setUsers(data.users);
    } catch (error) {
      console.error('Error loading users:', error);
    }
  };

  const loadAvailableTimeSlots = async (stationId: string, date: string) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/stations/${stationId}/timeslots?date=${date}`,
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.slots) setAvailableTimeSlots(data.slots);
    } catch (error) {
      console.error('Error loading time slots:', error);
    }
  };

  const loadAvailableBatteries = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/batteries',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.batteries) setAvailableBatteries(data.batteries);
    } catch (error) {
      console.error('Error loading batteries:', error);
    }
  };

  const handleSwapBattery = async () => {
    if (!selectedBattery) return;
    
    setIsSwapping(true);
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/transactions/swap',
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ batteryId: selectedBattery.batteryId }),
        }
      );
      
      const data = await response.json();
      if (response.ok) {
        setSwapResult(data);
        setSwapStep(3);
        showToast('Đổi pin thành công!', 'success');
      } else {
        showToast('Có lỗi xảy ra khi đổi pin', 'error');
      }
    } catch (error) {
      showToast('Có lỗi xảy ra khi đổi pin', 'error');
    } finally {
      setIsSwapping(false);
    }
  };

  const resetSwapFlow = () => {
    setSwapStep(1);
    setSelectedBattery(null);
    setAvailableBatteries([]);
    setSwapResult(null);
  };

  const loadReservations = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/reservations',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.reservations) setReservations(data.reservations);
    } catch (error) {
      console.error('Error loading reservations:', error);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/v1/auth/verify-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: (verificationEmail || email), otp }),
      });

      if (response.ok) {
        setIsEmailVerified(true);
        setCurrentPage('vehicle-registration');
        showToast('Xác minh email thành công!', 'success');
        setOtp('');
      } else {
        const data = await response.json().catch(() => null);
        setError((data && data.error) || 'OTP không hợp lệ hoặc đã hết hạn');
      }
    } catch (err) {
      setError('Có lỗi xảy ra khi xác minh OTP');
    } finally {
      setLoading(false);
    }
  };

  const loadFeedbacks = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/feedbacks',
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
      );
      const data = await response.json();
      if (data.feedbacks) setFeedbacks(data.feedbacks);
    } catch (error) {
      console.error('Error loading feedbacks:', error);
    }
  };

  const handleCreateReservation = async () => {
    if (!selectedVehicle || !selectedStation || !selectedTimeSlot) {
      showToast('Vui lòng chọn đầy đủ thông tin', 'error');
      return;
    }

    setIsCreatingReservation(true);
    
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/reservations',
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            vehicle_id: selectedVehicle.id,
            station_id: selectedStation.id,
            reserved_start: selectedTimeSlot.datetime
          }),
        }
      );

      const data = await response.json();
      
      if (response.ok && data.success) {
        setReservationResult(data.reservation);
        setReservationStep(3);
        showToast('Đặt lịch thành công!', 'success');
        
        // Reload reservations
        loadReservations();
      } else {
        showToast(data.error || 'Có lỗi xảy ra', 'error');
      }
    } catch (error) {
      showToast('Có lỗi xảy ra khi đặt lịch', 'error');
    } finally {
      setIsCreatingReservation(false);
    }
  };

  const resetReservationFlow = () => {
    setReservationStep(1);
    setSelectedStation(null);
    setSelectedVehicle(null);
    setSelectedTimeSlot(null);
    setSelectedDate(new Date().toISOString().split('T')[0]);
    setAvailableTimeSlots([]);
    setReservationResult(null);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();
      
      if (response.ok && data.success) {
        const user = {
          ...data.user,
          role: (data.user && data.user.role) ? data.user.role : 'driver',
        };
        setCurrentUser(user);
        setAccessToken(data.token || '');
        localStorage.setItem('access_token', data.token || '');
        setCurrentPage('dashboard');
        showToast('Đăng nhập thành công!', 'success');
      } else {
        setError(data.error || 'Đăng nhập thất bại');
      }
    } catch (error) {
      setError('Có lỗi xảy ra khi đăng nhập');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, fullName, phone }),
      });

      const data = await response.json();
      
      if (response.ok && data.success) {
        // Điều hướng sang trang xác thực OTP (FE)
        setVerificationEmail(email);
        setCurrentPage('verify-otp');
        showToast('Đăng ký thành công! Vui lòng nhập OTP gửi tới email.', 'success');
        
        // Clear form
        setEmail('');
        setPassword('');
        setFullName('');
        setPhone('');
      } else {
        setError(data.error || 'Đăng ký thất bại');
      }
    } catch (error) {
      setError('Có lỗi xảy ra khi đăng ký');
    } finally {
      setLoading(false);
    }
  };

  const handleAddVehicle = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/vehicles',
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ model, vin, plate_number: plateNumber }),
        }
      );

      const data = await response.json();
      
      if (response.ok && data.success) {
        showToast('Đăng ký xe thành công!', 'success');
        setCurrentPage('dashboard');
      } else {
        setError(data.error || 'Đăng ký xe thất bại');
      }
    } catch (error) {
      setError('Có lỗi xảy ra khi đăng ký xe');
    } finally {
      setLoading(false);
    }
  };

  const handleAddNewVehicle = async () => {
    if (!newVehicleModel || !newVehicleVin || !newVehiclePlateNumber) {
      showToast('Vui lòng điền đầy đủ thông tin xe', 'error');
      return;
    }

    // Check if user is authenticated
    if (!accessToken) {
      showToast('Vui lòng đăng nhập để thêm xe', 'error');
      return;
    }

    setIsAddingVehicle(true);
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/vehicles',
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ 
            model: newVehicleModel, 
            vin: newVehicleVin, 
            plate_number: newVehiclePlateNumber 
          }),
        }
      );

      const data = await response.json();
      
      if (response.ok && data.success) {
        showToast('Thêm xe thành công!', 'success');
        setShowModal(false);
        loadVehicles(); // Reload danh sách xe
        // Reset form
        setNewVehicleModel('');
        setNewVehicleVin('');
        setNewVehiclePlateNumber('');
      } else {
        showToast(data.error || 'Có lỗi xảy ra khi thêm xe', 'error');
      }
    } catch (error) {
      showToast('Có lỗi xảy ra khi thêm xe', 'error');
    } finally {
      setIsAddingVehicle(false);
    }
  };

  const handleCreateStaff = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/admin/staff',
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: staffEmail,
            password: staffPassword,
            full_name: staffFullName,
            phone: staffPhone,
          }),
        }
      );

      const data = await response.json();
      if (data.success) {
        showToast('Đã tạo tài khoản Staff thành công', 'success');
        setShowModal(false);
        loadUsers();
        setStaffEmail('');
        setStaffPassword('');
        setStaffFullName('');
        setStaffPhone('');
      } else {
        showToast(data.error || 'Có lỗi xảy ra', 'error');
      }
    } catch (error) {
      showToast('Có lỗi xảy ra', 'error');
    }
  };
  const handleCreateStation = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/admin/stations',
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: stationName,
            address: stationAddress,
            latitude: parseFloat(stationLat),
            longitude: parseFloat(stationLng),
          }),
        }
      );

      const data = await response.json();
      if (data.success) {
        showToast('Đã tạo trạm đổi pin thành công', 'success');
        setShowModal(false);
        loadStations();
        setStationName('');
        setStationAddress('');
        setStationLat('');
        setStationLng('');
      } else {
        showToast(data.error || 'Có lỗi xảy ra', 'error');
      }
    } catch (error) {
      showToast('Có lỗi xảy ra', 'error');
    }
  };
  const handleCreateTicket = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/v1/tickets',
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            subject: ticketSubject,
            description: ticketDescription,
          }),
        }
      );

      const data = await response.json();
      if (data.success) {
        showToast('Đã gửi yêu cầu hỗ trợ', 'success');
        setShowModal(false);
        loadTickets();
        setTicketSubject('');
        setTicketDescription('');
      } else {
        showToast(data.error || 'Có lỗi xảy ra', 'error');
      }
    } catch (error) {
      showToast('Có lỗi xảy ra', 'error');
    }
  };

  const handleUpdateTicketStatus = async (ticketId: string, status: string) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/tickets/${ticketId}`,
        {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ status }),
        }
      );

      const data = await response.json();
      if (data.success) {
        showToast('Đã cập nhật trạng thái ticket', 'success');
        loadTickets();
      } else {
        showToast(data.error || 'Có lỗi xảy ra', 'error');
      }
    } catch (error) {
      showToast('Có lỗi xảy ra', 'error');
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setAccessToken('');
    localStorage.removeItem('access_token');
    setCurrentPage('landing');
    showToast('Đã đăng xuất', 'success');
  };

  const openModal = (type: 'staff' | 'station' | 'ticket') => {
    setModalType(type);
    setShowModal(true);
  };

  return (
    <div>
      {/* Toast Notifications */}
      {toast && (
        <div className="toast-container">
          <div className={`toast ${toast.type === 'success' ? 'toast-success' : 'toast-error'}`}>
            {toast.message}
          </div>
        </div>
      )}

    {/* Verify OTP Page */}
    {currentPage === 'verify-otp' && (
      <div className="auth-page">
        <div className="auth-right" style={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
          <div className="auth-card" style={{ maxWidth: 480, width: '100%' }}>
            <button
              onClick={() => setCurrentPage('login')}
              style={{
                position: 'absolute', top: '1.5rem', left: '1.5rem', background: 'transparent', border: 'none', cursor: 'pointer', padding: '0.5rem', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#6b7280', fontSize: '0.9375rem', transition: 'all 0.2s ease', fontWeight: '500'
              }}
              onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = '#f3f4f6'; e.currentTarget.style.color = '#1f2937'; }}
              onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = '#6b7280'; }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>
              Quay lại
            </button>

            <div className="auth-header">
              <div className="auth-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
              </div>
              <h1 className="auth-title">Xác minh OTP</h1>
              <p className="auth-description">Nhập mã OTP đã gửi đến email {verificationEmail || email}</p>
            </div>

            <form onSubmit={handleVerifyOtp}>
              {error && (<div className="alert alert-error">{error}</div>)}

              <div className="form-group">
                <label className="form-label" htmlFor="otp">Mã OTP</label>
                <input
                  id="otp"
                  type="text"
                  inputMode="numeric"
                  className="form-input"
                  placeholder="Nhập mã 6 chữ số"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                {loading ? 'Đang xác minh...' : 'Xác minh'}
              </button>
            </form>
          </div>
        </div>
      </div>
    )}

      {/* Header */}
      <header className="header">
        <div className="header-content">
          {/* Left side - Hamburger Menu + Logo */}
          <div className="header-left">
            {currentUser && currentPage === 'dashboard' && (
              <button 
                className={`hamburger-menu ${showLeftDrawer ? 'active' : ''}`}
                onClick={() => setShowLeftDrawer(!showLeftDrawer)}
                aria-label="Menu"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="3" y1="12" x2="21" y2="12"></line>
                  <line x1="3" y1="6" x2="21" y2="6"></line>
                  <line x1="3" y1="18" x2="21" y2="18"></line>
                </svg>
              </button>
            )}
            <div className="logo">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                <polyline points="17 2 12 7 7 2"></polyline>
              </svg>
              <span>EV SWAP</span>
            </div>
          </div>

          {/* Center - Navigation Menu */}
          {!currentUser && currentPage === 'landing' && (
            <div className="dashboard-nav-container">
              <nav className="header-center-nav">
                <button 
                  className="header-nav-item"
                  onClick={() => {
                    showToast('Vui lòng đăng nhập để sử dụng tính năng này', 'error');
                    setTimeout(() => setCurrentPage('login'), 1000);
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="3" width="7" height="7"></rect>
                    <rect x="14" y="3" width="7" height="7"></rect>
                    <rect x="14" y="14" width="7" height="7"></rect>
                    <rect x="3" y="14" width="7" height="7"></rect>
                  </svg>
                  <span className="nav-text">Tổng quan</span>
                </button>
                
                <button 
                  className="header-nav-item"
                  onClick={() => {
                    showToast('Vui lòng đăng nhập để sử dụng tính năng này', 'error');
                    setTimeout(() => setCurrentPage('login'), 1000);
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                    <circle cx="7" cy="17" r="2"></circle>
                    <circle cx="17" cy="17" r="2"></circle>
                  </svg>
                  <span className="nav-text">Phương tiện</span>
                </button>

                <button 
                  className="header-nav-item"
                  onClick={() => {
                    showToast('Vui lòng đăng nhập để sử dụng tính năng này', 'error');
                    setTimeout(() => setCurrentPage('login'), 1000);
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                  <span className="nav-text">Tìm trạm</span>
                </button>

                <button 
                  className="header-nav-item"
                  onClick={() => {
                    showToast('Vui lòng đăng nhập để sử dụng tính năng này', 'error');
                    setTimeout(() => setCurrentPage('login'), 1000);
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6"></line>
                    <line x1="8" y1="2" x2="8" y2="6"></line>
                    <line x1="3" y1="10" x2="21" y2="10"></line>
                  </svg>
                  <span className="nav-text">Đặt lịch</span>
                </button>

                <button 
                  className="header-nav-item"
                  onClick={() => {
                    showToast('Vui lòng đăng nhập để sử dụng tính năng này', 'error');
                    setTimeout(() => setCurrentPage('login'), 1000);
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                    <line x1="1" y1="10" x2="23" y2="10"></line>
                  </svg>
                  <span className="nav-text">Thanh toán</span>
                </button>

                <button 
                  className="header-nav-item"
                  onClick={() => {
                    showToast('Vui lòng đăng nhập để sử dụng tính năng này', 'error');
                    setTimeout(() => setCurrentPage('login'), 1000);
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                    <line x1="12" y1="17" x2="12.01" y2="17"></line>
                  </svg>
                  <span className="nav-text">Hỗ trợ</span>
                </button>
              </nav>
            </div>
          )}

          {currentUser && currentPage === 'dashboard' && (
            <div className="dashboard-nav-container">
              <nav className="header-center-nav">
                {currentUser.role === 'driver' && (
                  <>
                    <button 
                      className={`header-nav-item ${activeMenu === 'overview' ? 'active' : ''}`}
                      onClick={() => setActiveMenu('overview')}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="3" y="3" width="7" height="7"></rect>
                        <rect x="14" y="3" width="7" height="7"></rect>
                        <rect x="14" y="14" width="7" height="7"></rect>
                        <rect x="3" y="14" width="7" height="7"></rect>
                      </svg>
                      <span className="nav-text">Tổng quan</span>
                    </button>
                    
                    <button 
                      className={`header-nav-item ${activeMenu === 'vehicles' ? 'active' : ''}`}
                      onClick={() => setActiveMenu('vehicles')}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                        <circle cx="7" cy="17" r="2"></circle>
                        <circle cx="17" cy="17" r="2"></circle>
                      </svg>
                      <span className="nav-text">Phương tiện</span>
                    </button>

                    <button 
                      className={`header-nav-item ${activeMenu === 'stations' ? 'active' : ''}`}
                      onClick={() => {
                        setActiveMenu('stations');
                        if (stations.length === 0) {
                          loadStations(true);
                        }
                      }}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                        <circle cx="12" cy="10" r="3"></circle>
                      </svg>
                      <span className="nav-text">Tìm trạm</span>
                    </button>

                    <button 
                      className={`header-nav-item ${activeMenu === 'reservation' ? 'active' : ''}`}
                      onClick={() => {
                        setActiveMenu('reservation');
                        if (stations.length === 0) {
                          loadStations();
                        }
                        loadReservations();
                      }}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                      </svg>
                      <span className="nav-text">Đặt lịch</span>
                    </button>

                    <button 
                      className={`header-nav-item ${activeMenu === 'payment' ? 'active' : ''}`}
                      onClick={() => setActiveMenu('payment')}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                        <line x1="1" y1="10" x2="23" y2="10"></line>
                      </svg>
                      <span className="nav-text">Thanh toán</span>
                    </button>

                    <button 
                      className={`header-nav-item ${activeMenu === 'support' ? 'active' : ''}`}
                      onClick={() => setActiveMenu('support')}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                      </svg>
                      <span className="nav-text">Hỗ trợ</span>
                      {tickets.filter(t => t.status === 'open').length > 0 && (
                        <span className="nav-badge">{tickets.filter(t => t.status === 'open').length}</span>
                      )}
                    </button>
                  </>
                )}

                {currentUser.role === 'admin' && (
                  <>
                    <button 
                      className={`header-nav-item ${activeMenu === 'overview' ? 'active' : ''}`}
                      onClick={() => setActiveMenu('overview')}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="3" y="3" width="7" height="7"></rect>
                        <rect x="14" y="3" width="7" height="7"></rect>
                        <rect x="14" y="14" width="7" height="7"></rect>
                        <rect x="3" y="14" width="7" height="7"></rect>
                      </svg>
                      <span className="nav-text">Tổng quan</span>
                    </button>

                    <button 
                      className={`header-nav-item ${activeMenu === 'users' ? 'active' : ''}`}
                      onClick={() => setActiveMenu('users')}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                      </svg>
                      <span className="nav-text">Người dùng</span>
                    </button>
                    
                    <button 
                      className={`header-nav-item ${activeMenu === 'stations' ? 'active' : ''}`}
                      onClick={() => {
                        setActiveMenu('stations');
                        if (stations.length === 0) {
                          loadStations();
                        }
                      }}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                        <circle cx="12" cy="10" r="3"></circle>
                      </svg>
                      <span className="nav-text">Trạm đổi pin</span>
                    </button>
                  </>
                )}
              </nav>
            </div>
          )}

          {/* Right side - Auth buttons or User dropdown */}
          <nav className="nav">
            {!currentUser ? (
              <>
                <button 
                  className="btn btn-secondary"
                  onClick={() => {
                    setError(null);
                    setCurrentPage('login');
                  }}
                >
                  Đăng nhập
                </button>
                <button 
                  className="btn btn-primary"
                  onClick={() => {
                    setError(null);
                    setCurrentPage('register');
                  }}
                >
                  Đăng ký
                </button>
              </>
            ) : currentUser && currentPage === 'dashboard' ? (
              <div className="user-dropdown-container">
                <button 
                  className="user-avatar-button"
                  onClick={() => {
                    if (currentPage === 'dashboard') {
                      setShowRightSidebar(!showRightSidebar);
                    } else {
                      setShowUserDropdown(!showUserDropdown);
                      setShowMenuDropdown(false);
                    }
                  }}
                >
                  <div className="user-avatar">
                    {currentUser.full_name?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  <div className="user-name-mobile">
                    <span className="user-greeting">Xin chào</span>
                    <span className="user-name">{currentUser.full_name}</span>
                  </div>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="chevron-icon">
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </button>
              </div>
            ) : null}
          </nav>
        </div>
      </header>


                {showUserDropdown && currentPage !== 'dashboard' && (
                  <div className="dropdown-menu dropdown-menu-right">
                    <div className="user-dropdown-header">
                      <div className="user-avatar-large">
                        {currentUser.full_name?.charAt(0)?.toUpperCase() || 'U'}
                      </div>
                      <div className="user-info-detail">
                        <p className="user-full-name">{currentUser.full_name}</p>
                        <p className="user-email">{currentUser.email}</p>
                        <span className={`role-badge role-${currentUser.role}`}>
                          {currentUser.role === 'admin' ? (
                            <>
                              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.25rem' }}>
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                              </svg>
                              Admin
                            </>
                          ) : currentUser.role === 'staff' ? (
                            <>
                              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.25rem' }}>
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                              </svg>
                              Staff
                            </>
                          ) : (
                            <>
                              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.25rem' }}>
                                <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                                <circle cx="7" cy="17" r="2"></circle>
                                <circle cx="17" cy="17" r="2"></circle>
                              </svg>
                              Driver
                            </>
                          )}
                        </span>
                      </div>
                    </div>
                    <div className="dropdown-divider"></div>
                    <div className="user-additional-info">
                      <div className="info-row">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                        </svg>
                        <span>{currentUser.phone}</span>
                      </div>
                      {currentUser.role === 'driver' && vehicles.length > 0 && (
                        <div className="info-row">
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                            <circle cx="7" cy="17" r="2"></circle>
                            <circle cx="17" cy="17" r="2"></circle>
                          </svg>
                          <span>{vehicles[0].plate_number}</span>
                        </div>
                      )}
                    </div>
                    <div className="dropdown-divider"></div>
                    <button className="dropdown-item" onClick={() => {
                      setShowUserDropdown(false);
                      setCurrentPage('dashboard');
                    }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="3" y="3" width="7" height="7"></rect>
                        <rect x="14" y="3" width="7" height="7"></rect>
                        <rect x="14" y="14" width="7" height="7"></rect>
                        <rect x="3" y="14" width="7" height="7"></rect>
                      </svg>
                      <span>Dashboard</span>
                    </button>
                    <button className="dropdown-item" onClick={() => {
                      setShowUserDropdown(false);
                      showToast('Chức năng đang phát triển', 'success');
                    }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                      <span>Thông tin cá nhân</span>
                    </button>
                    <div className="dropdown-divider"></div>
                    <button className="dropdown-item logout-item" onClick={() => {
                      setShowUserDropdown(false);
                      handleLogout();
                    }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                      </svg>
                      <span>Đăng xuất</span>
                    </button>
                  </div>
                )}
      {/* Landing Page */}
      {currentPage === 'landing' && (
        <div className="landing-page-modern">
          {/* Hero Section với Animated Background */}
          <section className="hero-modern">
            <div className="hero-bg-animated"></div>
            <div className="hero-container">
              <div className="hero-content-modern">
                <div className="hero-badge">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
                  </svg>
                  <span>Tương lai xanh cho Việt Nam</span>
                </div>
                
                <h1 className="hero-title-modern">
                  Đổi pin xe điện
                  <span className="hero-title-gradient">chỉ trong 3 phút</span>
                </h1>
                
                <p className="hero-description-modern">
                  Hệ thống trạm đổi pin thông minh với công nghệ tiên tiến, 
                  mạng lưới rộng khắp và dịch vụ nhanh chóng. Trải nghiệm tương lai 
                  của giao thông xanh ngay hôm nay.
                </p>

                <div className="hero-actions">
                  <button 
                    className="btn-hero-primary"
                    onClick={() => setCurrentPage('register')}
                  >
                    <span>Bắt đầu ngay</span>
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="5" y1="12" x2="19" y2="12"></line>
                      <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                  </button>
                  <button 
                    className="btn-hero-secondary"
                    onClick={() => setCurrentPage('login')}
                  >
                    <span>Đăng nhập</span>
                  </button>
                </div>

                <div className="hero-stats">
                  <div className="hero-stat-item">
                    <div className="hero-stat-number">50+</div>
                    <div className="hero-stat-label">Trạm đổi pin</div>
                  </div>
                  <div className="hero-stat-divider"></div>
                  <div className="hero-stat-item">
                    <div className="hero-stat-number">3 phút</div>
                    <div className="hero-stat-label">Thời gian đổi</div>
                  </div>
                  <div className="hero-stat-divider"></div>
                  <div className="hero-stat-item">
                    <div className="hero-stat-number">24/7</div>
                    <div className="hero-stat-label">Phục vụ</div>
                  </div>
                </div>
              </div>

              <div className="hero-visual">
                <div className="floating-card floating-card-1">
                  <div className="floating-card-icon green">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                      <polyline points="17 2 12 7 7 2"></polyline>
                    </svg>
                  </div>
                  <div className="floating-card-content">
                    <div className="floating-card-label">Pin sẵn có</div>
                    <div className="floating-card-value">156 pin</div>
                  </div>
                </div>

                <div className="floating-card floating-card-2">
                  <div className="floating-card-icon blue">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                      <circle cx="12" cy="10" r="3"></circle>
                    </svg>
                  </div>
                  <div className="floating-card-content">
                    <div className="floating-card-label">Tr��m gần nhất</div>
                    <div className="floating-card-value">2.3 km</div>
                  </div>
                </div>

                <div className="floating-card floating-card-3">
                  <div className="floating-card-icon purple">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                    </svg>
                  </div>
                  <div className="floating-card-content">
                    <div className="floating-card-label">Đánh giá</div>
                    <div className="floating-card-value">4.9/5.0</div>
                  </div>
                </div>

                <div className="hero-main-visual">
                  <div className="visual-glow"></div>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="visual-icon">
                    <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                    <polyline points="17 2 12 7 7 2"></polyline>
                  </svg>
                </div>
              </div>
            </div>
          </section>

          {/* Stats Section */}
          <section className="stats-modern">
            <div className="stats-container">
              <div className="stats-grid-modern">
                <div className="stat-card-landing">
                  <div className="stat-icon-wrapper green">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                  </div>
                  <div className="stat-number-landing">10,000+</div>
                  <div className="stat-label-landing">Người dùng đăng ký</div>
                </div>

                <div className="stat-card-landing">
                  <div className="stat-icon-wrapper blue">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                      <polyline points="17 2 12 7 7 2"></polyline>
                    </svg>
                  </div>
                  <div className="stat-number-landing">50,000+</div>
                  <div className="stat-label-landing">Lượt đổi pin</div>
                </div>

                <div className="stat-card-landing">
                  <div className="stat-icon-wrapper purple">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                      <circle cx="12" cy="10" r="3"></circle>
                    </svg>
                  </div>
                  <div className="stat-number-landing">50+</div>
                  <div className="stat-label-landing">Trạm toàn quốc</div>
                </div>

                <div className="stat-card-landing">
                  <div className="stat-icon-wrapper orange">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="12" r="10"></circle>
                      <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                  </div>
                  <div className="stat-number-landing">3 phút</div>
                  <div className="stat-label-landing">Thời gian trung bình</div>
                </div>
              </div>
            </div>
          </section>

          {/* Features Section Modern */}
          <section className="features-modern">
            <div className="features-container">
              <div className="section-header-modern">
                <span className="section-badge">Tính năng</span>
                <h2 className="section-title-modern">Tại sao chọn EV SWAP?</h2>
                <p className="section-description-modern">
                  Giải pháp đổi pin thông minh với công nghệ tiên tiến và dịch vụ tốt nhất
                </p>
              </div>

              <div className="features-grid-modern">
                <div className="feature-card-modern">
                  <div className="feature-card-glow green"></div>
                  <div className="feature-icon-modern green">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
                    </svg>
                  </div>
                  <h3 className="feature-title-modern">Siêu nhanh 3 phút</h3>
                  <p className="feature-description-modern">
                    Đổi pin trong 3 phút, nhanh hơn sạc điện gấp 20 lần. Không cần chờ đợi, tiếp tục hành trình ngay.
                  </p>
                </div>

                <div className="feature-card-modern">
                  <div className="feature-card-glow blue"></div>
                  <div className="feature-icon-modern blue">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                      <circle cx="12" cy="10" r="3"></circle>
                    </svg>
                  </div>
                  <h3 className="feature-title-modern">Mạng lưới rộng khắp</h3>
                  <p className="feature-description-modern">
                    Hơn 50 trạm phủ sóng các thành phố lớn. Luôn có trạm gần bạn dưới 5km.
                  </p>
                </div>

                <div className="feature-card-modern">
                  <div className="feature-card-glow purple"></div>
                  <div className="feature-icon-modern purple">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    </svg>
                  </div>
                  <h3 className="feature-title-modern">An toàn tuyệt đối</h3>
                  <p className="feature-description-modern">
                    Pin được kiểm tra nghiêm ngặt, đạt chuẩn quốc tế. Bảo hiểm toàn diện cho mọi giao dịch.
                  </p>
                </div>

                <div className="feature-card-modern">
                  <div className="feature-card-glow orange"></div>
                  <div className="feature-icon-modern orange">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="12" y1="1" x2="12" y2="23"></line>
                      <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                    </svg>
                  </div>
                  <h3 className="feature-title-modern">Tiết kiệm chi phí</h3>
                  <p className="feature-description-modern">
                    Chỉ 25.000đ/lần đổi. Tiết kiệm 60% so với xe xăng truyền thống.
                  </p>
                </div>

                <div className="feature-card-modern">
                  <div className="feature-card-glow teal"></div>
                  <div className="feature-icon-modern teal">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="12" r="10"></circle>
                      <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                  </div>
                  <h3 className="feature-title-modern">Hoạt động 24/7</h3>
                  <p className="feature-description-modern">
                    Trạm đổi pin phục vụ không ngừng nghỉ. Hỗ trợ khẩn cấp mọi lúc mọi nơi.
                  </p>
                </div>

                <div className="feature-card-modern">
                  <div className="feature-card-glow pink"></div>
                  <div className="feature-icon-modern pink">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M3 3h18v18H3z"></path>
                      <path d="M12 8v8"></path>
                      <path d="M8 12h8"></path>
                    </svg>
                  </div>
                  <h3 className="feature-title-modern">Thân thiện môi trường</h3>
                  <p className="feature-description-modern">
                    100% năng lượng sạch. Góp phần giảm 2 tấn CO₂ mỗi năm cho mỗi xe.
                  </p>
                </div>
              </div>
            </div>
          </section>

          {/* CTA Section Modern */}
          <section className="cta-modern">
            <div className="cta-container">
              <div className="cta-content-modern">
                <h2 className="cta-title-modern">Sẵn sàng chuyển đổi sang tương lai xanh?</h2>
                <p className="cta-description-modern">
                  Tham gia cộng đồng hơn 10,000 tài xế đang sử dụng EV SWAP. 
                  Trải nghiệm công nghệ đổi pin thông minh ngay hôm nay.
                </p>
                <div className="cta-actions-modern">
                  <button 
                    className="cta-btn-primary"
                    onClick={() => setCurrentPage('register')}
                  >
                    <span>Đăng ký ngay</span>
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="5" y1="12" x2="19" y2="12"></line>
                      <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                  </button>
                  <button 
                    className="cta-btn-secondary"
                    onClick={() => setCurrentPage('login')}
                  >
                    Đã có tài khoản
                  </button>
                </div>
              </div>
              <div className="cta-visual">
                <div className="cta-glow"></div>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                  <polyline points="17 2 12 7 7 2"></polyline>
                </svg>
              </div>
            </div>
          </section>
        </div>
      )}

      {/* Login Page */}
      {currentPage === 'login' && (
        <div className="auth-page">
          {/* Left Side - Visual Content */}
          <div className="auth-left">
            <div className="auth-left-content">
              <div className="auth-left-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                  <polyline points="17 2 12 7 7 2"></polyline>
                </svg>
              </div>
              
              <h1>EV SWAP</h1>
              <p>Hệ thống đổi pin xe máy điện thông minh nhất Việt Nam</p>
              
              <div className="auth-features">
                <div className="auth-feature-item">
                  <div className="auth-feature-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
                    </svg>
                  </div>
                  <div className="auth-feature-content">
                    <h3>Đổi pin siêu nhanh</h3>
                    <p>Chỉ mất 3 phút để hoàn tất</p>
                  </div>
                </div>

                <div className="auth-feature-item">
                  <div className="auth-feature-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                      <circle cx="12" cy="10" r="3"></circle>
                    </svg>
                  </div>
                  <div className="auth-feature-content">
                    <h3>Mạng lưới rộng khắp</h3>
                    <p>Hàng trăm trạm to��n quốc</p>
                  </div>
                </div>

                <div className="auth-feature-item">
                  <div className="auth-feature-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    </svg>
                  </div>
                  <div className="auth-feature-content">
                    <h3>An toàn tuyệt đối</h3>
                    <p>Pin được kiểm tra nghiêm ngặt</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Right Side - Login Form */}
          <div className="auth-right">
            <div className="auth-card">
              {/* Back Button */}
              <button
                onClick={() => setCurrentPage('landing')}
                style={{
                  position: 'absolute',
                  top: '1.5rem',
                  left: '1.5rem',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '0.5rem',
                  borderRadius: '8px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  color: '#6b7280',
                  fontSize: '0.9375rem',
                  transition: 'all 0.2s ease',
                  fontWeight: '500'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = '#f3f4f6';
                  e.currentTarget.style.color = '#1f2937';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                  e.currentTarget.style.color = '#6b7280';
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="19" y1="12" x2="5" y2="12"></line>
                  <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
                Quay lại
              </button>

              <div className="auth-header" style={{ flexShrink: 0 }}>
                <div className="auth-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                    <polyline points="17 2 12 7 7 2"></polyline>
                  </svg>
                </div>
                <h1 className="auth-title">Đăng nhập</h1>
                <p className="auth-description">Đăng nhập vào hệ thống EV SWAP</p>
              </div>

              <div style={{ flex: 1, overflowY: 'auto', padding: '0 1rem' }}>
                <form onSubmit={handleLogin}>
                  {error && (
                    <div className="alert alert-error">{error}</div>
                  )}

                  <div className="form-group">
                    <label className="form-label" htmlFor="email">Email</label>
                    <input
                      id="email"
                      type="email"
                      className="form-input"
                      placeholder="example@email.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="password">Mật khẩu</label>
                    <input
                      id="password"
                      type="password"
                      className="form-input"
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                    />
                  </div>

                  <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                    {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
                  </button>
                </form>
              </div>

              <div className="auth-footer">
                <p>
                  Chưa có tài khoản?{' '}
                  <span
                    className="auth-link"
                    onClick={() => {
                      setError(null);
                      setCurrentPage('register');
                    }}
                  >
                    Đăng ký ngay
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
      {/* Register Page */}
      {currentPage === 'register' && (
        <div className="auth-page">
          {/* Left Side - Visual Content */}
          <div className="auth-left">
            <div className="auth-left-content">
              <div className="auth-left-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                  <polyline points="17 2 12 7 7 2"></polyline>
                </svg>
              </div>
              
              <h1>Bắt đầu ngay!</h1>
              <p>Tham gia cộng đồng xe máy điện thông minh</p>
              
              <div className="auth-features">
                <div className="auth-feature-item">
                  <div className="auth-feature-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="12" r="10"></circle>
                      <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                  </div>
                  <div className="auth-feature-content">
                    <h3>Đăng ký miễn phí</h3>
                    <p>Không mất phí đăng ký tài khoản</p>
                  </div>
                </div>

                <div className="auth-feature-item">
                  <div className="auth-feature-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="12" y1="1" x2="12" y2="23"></line>
                      <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                    </svg>
                  </div>
                  <div className="auth-feature-content">
                    <h3>Ưu đãi hấp dẫn</h3>
                    <p>Nhận ngay 50.000đ khi đăng ký</p>
                  </div>
                </div>

                <div className="auth-feature-item">
                  <div className="auth-feature-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                  </div>
                  <div className="auth-feature-content">
                    <h3>Hỗ trợ 24/7</h3>
                    <p>Đội ngũ hỗ trợ luôn sẵn sàng</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Right Side - Register Form */}
          <div className="auth-right">
            <div className="auth-card">
              {/* Back Button */}
              <button
                onClick={() => setCurrentPage('landing')}
                style={{
                  position: 'absolute',
                  top: '1.5rem',
                  left: '1.5rem',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '0.5rem',
                  borderRadius: '8px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  color: '#6b7280',
                  fontSize: '0.9375rem',
                  transition: 'all 0.2s ease',
                  fontWeight: '500'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = '#f3f4f6';
                  e.currentTarget.style.color = '#1f2937';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                  e.currentTarget.style.color = '#6b7280';
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="19" y1="12" x2="5" y2="12"></line>
                  <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
                Quay lại
              </button>

              <div className="auth-header" style={{ flexShrink: 0 }}>
                <div className="auth-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                    <polyline points="17 2 12 7 7 2"></polyline>
                  </svg>
                </div>
                <h1 className="auth-title">Đăng ký tài khoản</h1>
                <p className="auth-description">Tạo tài khoản Driver để sử dụng dịch vụ đổi pin</p>
              </div>

              <div style={{ flex: 1, overflowY: 'auto', padding: '0 1rem' }}>
                <form onSubmit={handleRegister}>
                  {error && (
                    <div className="alert alert-error">{error}</div>
                  )}

                  <div className="form-group">
                    <label className="form-label" htmlFor="fullName">Họ và tên</label>
                    <input
                      id="fullName"
                      type="text"
                      className="form-input"
                      placeholder="Nguyễn Văn A"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="email">Email</label>
                    <input
                      id="email"
                      type="email"
                      className="form-input"
                      placeholder="example@email.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="phone">Số điện thoại</label>
                    <input
                      id="phone"
                      type="tel"
                      className="form-input"
                      placeholder="0123456789"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="password">Mật khẩu</label>
                    <input
                      id="password"
                      type="password"
                      className="form-input"
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      minLength={6}
                    />
                    <p className="form-hint">Mật khẩu phải có ít nhất 6 ký tự</p>
                  </div>

                  <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                    {loading ? 'Đang đăng ký...' : 'Đăng ký'}
                  </button>
                </form>
              </div>

              <div className="auth-footer">
                <p>
                  Đã có tài khoản?{' '}
                  <span
                    className="auth-link"
                    onClick={() => {
                      setError(null);
                      setCurrentPage('login');
                    }}
                  >
                    Đăng nhập ngay
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Vehicle Registration Page */}
      {currentPage === 'vehicle-registration' && (
        <div className="auth-page">
          <div className="auth-card" style={{ maxHeight: '90vh', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
            <div className="auth-header" style={{ flexShrink: 0 }}>
              <div className="auth-icon blue">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                  <circle cx="7" cy="17" r="2"></circle>
                  <circle cx="17" cy="17" r="2"></circle>
                </svg>
              </div>
              <h1 className="auth-title">Đăng ký xe điện</h1>
              <p className="auth-description">Vui lòng đăng ký thông tin xe máy điện của bạn để sử dụng dịch vụ</p>
            </div>

            <div style={{ flex: 1, overflowY: 'auto', padding: '0 2rem 2rem 2rem' }}>
              <form onSubmit={handleAddVehicle}>
                {error && (
                  <div className="alert alert-error">{error}</div>
                )}

                <div className="form-group">
                  <label className="form-label" htmlFor="model">Model xe</label>
                  <input
                    id="model"
                    type="text"
                    className="form-input"
                    placeholder={systemConfig?.vehicleModels?.models?.join(', ') || "VinFast Klara, Yadea..."}
                    value={model}
                    onChange={(e) => setModel(e.target.value)}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="vin">Số VIN (Số khung)</label>
                  <input
                    id="vin"
                    type="text"
                    className="form-input"
                    placeholder="VF1XXXXXXXXXXXXXXX"
                    value={vin}
                    onChange={(e) => setVin(e.target.value)}
                    required
                  />
                  <p className="form-hint">Số khung xe (Vehicle Identification Number)</p>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="plateNumber">Biển số xe</label>
                  <input
                    id="plateNumber"
                    type="text"
                    className="form-input"
                    placeholder="29A1-12345"
                    value={plateNumber}
                    onChange={(e) => setPlateNumber(e.target.value)}
                    required
                  />
                </div>

                <div className="info-box">
                  <p>💡 Thông tin xe của bạn sẽ được sử dụng để xác nhận khi đổi pin tại trạm</p>
                </div>

                <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                  {loading ? 'Đang đăng ký...' : 'Hoàn tất đăng ký'}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Dashboard - will continue in next part */}
      {currentPage === 'dashboard' && currentUser && (
        <div>
          {/* Left Drawer Overlay */}
          {showLeftDrawer && (
            <div 
              className="drawer-overlay" 
              onClick={() => setShowLeftDrawer(false)}
            />
          )}

          {/* Right Sidebar Overlay */}
          {showRightSidebar && (
            <div 
              className="drawer-overlay" 
              onClick={() => setShowRightSidebar(false)}
            />
          )}

          {/* Left Drawer Sidebar */}
          <div className={`left-drawer ${showLeftDrawer ? 'open' : ''}`}>
            <div className="left-drawer-header">
              <div className="drawer-brand">
                <div className="drawer-brand-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                    <polyline points="17 2 12 7 7 2"></polyline>
                  </svg>
                </div>
                <div className="drawer-brand-text">
                  <h3>EV SWAP</h3>
                  <p>Quản lý đổi pin thông minh</p>
                </div>
              </div>
              <button 
                className="close-drawer-btn"
                onClick={() => setShowLeftDrawer(false)}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>

            <div className="left-drawer-menu">
              <div className="drawer-menu-label">Chức năng chính</div>
              
              <button 
                className={`drawer-menu-item ${activeMenu === 'overview' ? 'active' : ''}`}
                onClick={() => {
                  setActiveMenu('overview');
                  setShowLeftDrawer(false);
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="3" width="7" height="7"></rect>
                  <rect x="14" y="3" width="7" height="7"></rect>
                  <rect x="14" y="14" width="7" height="7"></rect>
                  <rect x="3" y="14" width="7" height="7"></rect>
                </svg>
                <span>Tổng quan</span>
              </button>
              
              <button 
                className={`drawer-menu-item ${activeMenu === 'vehicles' ? 'active' : ''}`}
                onClick={() => {
                  setActiveMenu('vehicles');
                  setShowLeftDrawer(false);
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                  <circle cx="7" cy="17" r="2"></circle>
                  <circle cx="17" cy="17" r="2"></circle>
                </svg>
                <span>Phương tiện</span>
              </button>

              <button 
                className={`drawer-menu-item ${activeMenu === 'stations' ? 'active' : ''}`}
                onClick={() => {
                  setActiveMenu('stations');
                  setShowLeftDrawer(false);
                  if (stations.length === 0) {
                    loadStations(true);
                  }
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                <span>Tìm trạm</span>
              </button>

              <button 
                className={`drawer-menu-item ${activeMenu === 'reservation' ? 'active' : ''}`}
                onClick={() => {
                  setActiveMenu('reservation');
                  setShowLeftDrawer(false);
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span>Đặt lịch</span>
              </button>

              <button 
                className={`drawer-menu-item ${activeMenu === 'payment' ? 'active' : ''}`}
                onClick={() => {
                  setActiveMenu('payment');
                  setShowLeftDrawer(false);
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                  <line x1="1" y1="10" x2="23" y2="10"></line>
                </svg>
                <span>Thanh toán</span>
              </button>

              <button 
                className={`drawer-menu-item ${activeMenu === 'support' ? 'active' : ''}`}
                onClick={() => {
                  setActiveMenu('support');
                  setShowLeftDrawer(false);
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                  <line x1="12" y1="17" x2="12.01" y2="17"></line>
                </svg>
                <span>Hỗ trợ</span>
                {tickets.filter(t => t.status === 'open').length > 0 && (
                  <span className="drawer-badge">{tickets.filter(t => t.status === 'open').length}</span>
                )}
              </button>
            </div>
          </div>
          <div className="dashboard-layout">
            {/* Main Content */}
            <div className={`main-content ${showRightSidebar ? 'with-right-sidebar' : ''}`}>
            {/* Driver Dashboard */}
            {currentUser.role === 'driver' && (
              <>
                {/* Overview Tab */}
                {activeMenu === 'overview' && (
                  <>
                    {/* Stats Cards */}
                    <div className="stats-cards-row">
                  <div className="stat-card-modern">
                    <div className="stat-card-header-modern">
                      <div>
                        <div className="stat-label-modern">Số dư hiện tại</div>
                        <div className="stat-value-modern" data-wallet-balance>0đ</div>
                      </div>
                      <div className="stat-card-icon green">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <line x1="12" y1="1" x2="12" y2="23"></line>
                          <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                        </svg>
                      </div>
                    </div>
                  </div>

                  <div className="stat-card-modern">
                    <div className="stat-card-header-modern">
                      <div>
                        <div className="stat-label-modern">Lượt đổi pin</div>
                        <div className="stat-value-modern" data-swap-count>{transactions.length}</div>
                      </div>
                      <div className="stat-card-icon blue">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                          <polyline points="17 2 12 7 7 2"></polyline>
                        </svg>
                      </div>
                    </div>
                  </div>

                  <div className="stat-card-modern">
                    <div className="stat-card-header-modern">
                      <div>
                        <div className="stat-label-modern">Trạm yêu thích</div>
                        <div className="stat-value-modern" style={{ fontSize: '1.125rem' }} data-favorite-station>
                          {stations.length > 0 ? stations[0].name.substring(0, 15) + '...' : 'Chưa có'}
                        </div>
                      </div>
                      <div className="stat-card-icon purple">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                        </svg>
                      </div>
                    </div>
                  </div>

                  <div className="stat-card-modern">
                    <div className="stat-card-header-modern">
                      <div>
                        <div className="stat-label-modern">Chi phí trung bình</div>
                        <div className="stat-value-modern" style={{ fontSize: '1.25rem' }} data-average-cost>0đ</div>
                      </div>
                      <div className="stat-card-icon orange">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <line x1="18" y1="20" x2="18" y2="10"></line>
                          <line x1="12" y1="20" x2="12" y2="4"></line>
                          <line x1="6" y1="20" x2="6" y2="14"></line>
                        </svg>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="content-grid" style={{ marginTop: '2rem' }}>
                  <div className="card">
                    <div className="card-header">
                      <h2 className="card-title">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                          <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                          <circle cx="7" cy="17" r="2"></circle>
                          <circle cx="17" cy="17" r="2"></circle>
                        </svg>
                        Thông tin xe
                      </h2>
                    </div>
                    <div className="card-content">
                      {vehicles.length > 0 ? (
                        vehicles.map((vehicle) => (
                          <div key={vehicle.id} className="list-item">
                            <div className="item-grid">
                              <div>
                                <p className="item-label">Model</p>
                                <p className="item-value">{vehicle.model}</p>
                              </div>
                              <div>
                                <p className="item-label">Biển số</p>
                                <p className="item-value">{vehicle.plate_number}</p>
                              </div>
                              <div style={{ gridColumn: '1 / -1' }}>
                                <p className="item-label">Số VIN</p>
                                <p className="item-value" style={{ fontSize: '0.875rem' }}>{vehicle.vin}</p>
                              </div>
                            </div>
                          </div>
                        ))
                      ) : (
                        <p className="empty-state">Chưa có xe nào được đăng ký</p>
                      )}
                    </div>
                  </div>

                  <div className="card">
                    <div className="card-header">
                      <h2 className="card-title">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                          <circle cx="12" cy="12" r="10"></circle>
                          <polyline points="12 6 12 12 16 14"></polyline>
                        </svg>
                        Lịch sử đổi pin
                      </h2>
                    </div>
                    <div className="card-content">
                      {transactions.length > 0 ? (
                        transactions.slice(0, 5).map((transaction) => (
                          <div key={transaction.id} className="list-item">
                            <div className="flex justify-between items-center">
                              <div>
                                <p style={{ marginBottom: '0.25rem' }}>Đổi pin tại trạm</p>
                                <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                  {new Date(transaction.timestamp).toLocaleString('vi-VN')}
                                </p>
                              </div>
                              <span className="badge badge-success">Hoàn thành</span>
                            </div>
                          </div>
                        ))
                      ) : (
                        <p className="empty-state">Chưa có giao dịch nào</p>
                      )}
                    </div>
                  </div>

                  <div className="card">
                    <div className="card-header">
                      <h2 className="card-title">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                          <circle cx="12" cy="10" r="3"></circle>
                        </svg>
                        Trạm đổi pin
                      </h2>
                      <p className="card-description">Các trạm đổi pin có sẵn</p>
                    </div>
                    <div className="card-content">
                      {stations.length > 0 ? (
                        stations.slice(0, 5).map((station) => (
                          <div key={station.id} className="list-item">
                            <div className="flex justify-between items-center">
                              <div>
                                <p style={{ marginBottom: '0.25rem' }}>{station.name}</p>
                                <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>{station.address}</p>
                              </div>
                              <span className="badge badge-primary">Hoạt động</span>
                            </div>
                          </div>
                        ))
                      ) : (
                        <p className="empty-state">Chưa có trạm nào</p>
                      )}
                    </div>
                  </div>

                  <div className="card">
                    <div className="card-header">
                      <div className="card-actions">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <circle cx="12" cy="12" r="10"></circle>
                            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                            <line x1="12" y1="17" x2="12.01" y2="17"></line>
                          </svg>
                          Hỗ trợ kỹ thuật
                        </h2>
                        <button className="btn btn-primary" style={{ fontSize: '0.875rem', padding: '0.375rem 1rem' }} onClick={() => openModal('ticket')}>
                          Tạo yêu cầu
                        </button>
                      </div>
                    </div>
                    <div className="card-content">
                      {tickets.length > 0 ? (
                        tickets.slice(0, 5).map((ticket) => (
                          <div key={ticket.id} className="list-item">
                            <div className="flex justify-between items-center mb-2">
                              <p>{ticket.subject}</p>
                              <span className={`badge ${ticket.status === 'open' ? 'badge-primary' : 'badge-secondary'}`}>
                                {ticket.status === 'open' ? 'Đang xử lý' : 
                                 ticket.status === 'resolved' ? 'Đã giải quyết' : 'Đóng'}
                              </span>
                            </div>
                            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>{ticket.description}</p>
                            <p style={{ fontSize: '0.75rem', color: '#9ca3af' }}>
                              {new Date(ticket.created_at).toLocaleString('vi-VN')}
                            </p>
                          </div>
                        ))
                      ) : (
                        <p className="empty-state">Chưa có yêu cầu hỗ trợ nào</p>
                      )}
                    </div>
          </div>
          </div>
      </>
      )}

                {/* Vehicles Tab */}
                {activeMenu === 'vehicles' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <h1>Quản lý phương tiện</h1>
                          <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Thông tin xe và pin của bạn</p>
                        </div>
                        <button 
                          className="btn btn-primary"
                          onClick={() => {
                            setModalType('vehicle');
                            setShowModal(true);
                          }}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                            <line x1="12" y1="5" x2="12" y2="19"></line>
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                          </svg>
                          Thêm xe mới
                        </button>
                      </div>
                    </div>

                    {vehicles.length > 0 ? (
                      <>
                        {/* Vehicle Info Cards */}
                        <div className="content-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))' }}>
                          {vehicles.map((vehicle) => (
                            <div key={vehicle.id} className="card" style={{ border: '2px solid rgba(255, 255, 255, 0.15)' }}>
                              <div className="card-header" style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.1)', paddingBottom: '1rem' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                  <div style={{ 
                                    width: '60px', 
                                    height: '60px', 
                                    borderRadius: '12px', 
                                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    color: 'white'
                                  }}>
                                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                      <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                                      <circle cx="7" cy="17" r="2"></circle>
                                      <circle cx="17" cy="17" r="2"></circle>
                                    </svg>
                                  </div>
                                  <div style={{ flex: 1 }}>
                                    <h3 style={{ marginBottom: '0.25rem' }}>
                                      {vehicleDetail?.model || vehicle.model || 'Chưa cập nhật'}
                                    </h3>
                                    <p style={{ color: '#6b7280', fontSize: '0.875rem' }}>
                                      Biển số: {vehicleDetail?.plateNumber || vehicle.plate_number || 'Chưa cập nhật'}
                                    </p>
                                  </div>
                                  <span className="badge badge-success">
                                    {vehicleDetail?.status || 'Hoạt động'}
                                  </span>
                                </div>
                              </div>

                              <div className="card-content">
                                {/* Battery Information */}
                                <div style={{ marginBottom: '1.5rem' }}>
                                  <h4 style={{ marginBottom: '1rem', fontSize: '0.875rem', color: '#6b7280', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                      <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                                      <polyline points="17 2 12 7 7 2"></polyline>
                                    </svg>
                                    Thông tin pin
                                  </h4>

                                  <div style={{ display: 'grid', gap: '1rem' }}>
                                    {/* Battery Model & State */}
                                    <div style={{ padding: '1rem', backgroundColor: 'rgba(255, 255, 255, 0.05)', borderRadius: '8px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                                        <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Model pin</span>
                                        <span style={{ fontSize: '0.875rem', fontWeight: '500', color: '#e5e7eb' }}>
                                          {vehicleDetail?.batteryInfo?.model || 'VinFast 48V-20Ah'}
                                        </span>
                                      </div>
                                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Trạng thái</span>
                                        <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>
                                          {vehicleDetail?.batteryInfo?.status || 'Đang sử dụng'}
                                        </span>
                                      </div>
                                    </div>

                                    {/* State of Health (SOH) */}
                                    <div style={{ padding: '1.5rem', backgroundColor: 'rgba(59, 130, 246, 0.1)', borderRadius: '12px', border: '2px solid rgba(59, 130, 246, 0.2)' }}>
                                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" strokeWidth="2">
                                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                            <polyline points="22 4 12 14.01 9 11.01"></polyline>
                                          </svg>
                                          <span style={{ fontSize: '0.875rem', color: '#1e40af', fontWeight: '500' }}>Sức khỏe pin (SOH)</span>
                                        </div>
                                        <span style={{ fontSize: '1.5rem', color: '#3b82f6', fontWeight: '600' }}>
                                          {vehicleDetail?.batteryInfo?.sohPercent || 98}%
                                        </span>
                                      </div>
                                      <div style={{ width: '100%', height: '10px', backgroundColor: 'rgba(59, 130, 246, 0.2)', borderRadius: '999px', overflow: 'hidden', marginBottom: '0.75rem' }}>
                                        <div style={{ 
                                          width: `${vehicleDetail?.batteryInfo?.sohPercent || 98}%`, 
                                          height: '100%', 
                                          background: 'linear-gradient(90deg, #3b82f6 0%, #2563eb 100%)', 
                                          transition: 'width 0.3s ease' 
                                        }}></div>
                                      </div>
                                      <p style={{ fontSize: '0.875rem', color: '#93c5fd', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                          <polyline points="22 4 12 14.01 9 11.01"></polyline>
                                        </svg>
                                        Tình trạng: {vehicleDetail?.batteryInfo?.condition || 'Tuyệt vời'}
                                      </p>
                                    </div>
                                  </div>
                                </div>

                                {/* Vehicle Details */}
                                <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '1rem' }}>
                                  <h4 style={{ marginBottom: '0.75rem', fontSize: '0.875rem', color: '#6b7280' }}>Thông tin xe</h4>
                                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                      <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Số VIN:</span>
                                      <span style={{ fontSize: '0.875rem', fontFamily: 'monospace' }}>
                                        {vehicleDetail?.vin || vehicle.vin || 'Chưa cập nhật'}
                                      </span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                      <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Đăng ký:</span>
                                      <span style={{ fontSize: '0.875rem' }}>
                                        {vehicleDetail?.registrationDate || 
                                         (vehicle.created_at ? new Date(vehicle.created_at).toLocaleDateString('vi-VN') : 'Chưa cập nhật')}
                                      </span>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>

                        {/* Battery Swap History for Vehicle */}
                        <div className="card" style={{ marginTop: '2rem' }}>
                          <div className="card-header">
                            <h2 className="card-title">
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                                <path d="M3 3v18h18"></path>
                                <path d="M18 17V9"></path>
                                <path d="M13 17V5"></path>
                                <path d="M8 17v-3"></path>
                              </svg>
                              Lịch sử đổi pin
                            </h2>
                            <p className="card-description">Tất cả các lần đổi pin của xe {vehicles[0].plate_number}</p>
                          </div>
                          <div className="card-content">
                            {transactions.length > 0 ? (
                              <div className="table-container">
                                <table className="table">
                                  <thead>
                                    <tr>
                                      <th>Thời gian</th>
                                      <th>Tr��m đổi pin</th>
                                      <th>Pin cũ</th>
                                      <th>Pin mới</th>
                                      <th>Chi phí</th>
                                      <th>Trạng thái</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    {transactions.map((transaction, index) => (
                                      <tr key={transaction.id}>
                                        <td>{new Date(transaction.timestamp).toLocaleString('vi-VN')}</td>
                                        <td>{stations[index % stations.length]?.name || 'Trạm đổi pin'}</td>
                                        <td>
                                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                            <div style={{ width: '40px', height: '8px', backgroundColor: '#e5e7eb', borderRadius: '4px', overflow: 'hidden' }}>
                                              <div style={{ width: '15%', height: '100%', backgroundColor: '#ef4444' }}></div>
                                            </div>
                                            <span style={{ fontSize: '0.875rem', color: '#ef4444' }}>15%</span>
                                          </div>
                                        </td>
                                        <td>
                                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                            <div style={{ width: '40px', height: '8px', backgroundColor: '#e5e7eb', borderRadius: '4px', overflow: 'hidden' }}>
                                              <div style={{ width: '100%', height: '100%', backgroundColor: '#10b981' }}></div>
                                            </div>
                                            <span style={{ fontSize: '0.875rem', color: '#10b981' }}>100%</span>
                                          </div>
                                        </td>
                                        <td>25.000đ</td>
                                        <td>
                                          <span className="badge badge-success">Hoàn thành</span>
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            ) : (
                              <p className="empty-state">Chưa có lịch sử đổi pin nào</p>
                            )}
                          </div>
                        </div>


                      </>
                    ) : (
                      <div className="card" style={{ textAlign: 'center', padding: '3rem 1rem' }}>
                        <div style={{ marginBottom: '1.5rem' }}>
                          <div style={{ 
                            width: '80px', 
                            height: '80px', 
                            margin: '0 auto',
                            borderRadius: '50%', 
                            backgroundColor: '#f3f4f6',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                          }}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2">
                              <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                              <circle cx="7" cy="17" r="2"></circle>
                              <circle cx="17" cy="17" r="2"></circle>
                            </svg>
                          </div>
                        </div>
                        <h2 style={{ marginBottom: '0.5rem' }}>Chưa có xe nào</h2>
                        <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
                          Bạn chưa đăng ký xe nào. Hãy thêm xe đầu tiên để bắt đầu sử dụng dịch vụ đổi pin.
                        </p>
                        <button className="btn btn-primary">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                            <line x1="12" y1="5" x2="12" y2="19"></line>
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                          </svg>
                          Đăng ký xe đầu tiên
                        </button>
                      </div>
                    )}
                  </>
                )}
                {/* Stations Tab */}
                {activeMenu === 'stations' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div>
                        <h1>Tìm trạm đổi pin</h1>
                        <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Tìm trạm đổi pin gần bạn nhất</p>
                      </div>
                    </div>

                    {isLoadingStations ? (
                      <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
                        <div style={{ marginBottom: '1.5rem' }}>
                          <div style={{ 
                            width: '80px', 
                            height: '80px', 
                            margin: '0 auto',
                            borderRadius: '50%', 
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            animation: 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite'
                          }}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                              <circle cx="12" cy="10" r="3"></circle>
                            </svg>
                          </div>
                        </div>
                        <h2 style={{ marginBottom: '0.5rem', color: '#667eea' }}>Đang tìm kiếm trạm gần nhất...</h2>
                        <p style={{ color: '#6b7280' }}>Vui lòng chờ trong giây lát</p>
                      </div>
                    ) : stations.length > 0 ? (
                      <>
                        {/* Stats Summary */}
                        <div className="stats-cards-row" style={{ marginBottom: '2rem' }}>
                          <div className="stat-card-modern">
                            <div className="stat-card-header-modern">
                              <div>
                                <div className="stat-label-modern">Tổng số trạm</div>
                                <div className="stat-value-modern">{stations.length}</div>
                              </div>
                              <div className="stat-card-icon purple">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                  <circle cx="12" cy="10" r="3"></circle>
                                </svg>
                              </div>
                            </div>
                          </div>

                          <div className="stat-card-modern">
                            <div className="stat-card-header-modern">
                              <div>
                                <div className="stat-label-modern">Trạm hoạt động</div>
                                <div className="stat-value-modern">{stations.filter(s => s.status === 'active').length}</div>
                              </div>
                              <div className="stat-card-icon green">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                                </svg>
                              </div>
                            </div>
                          </div>

                          <div className="stat-card-modern">
                            <div className="stat-card-header-modern">
                              <div>
                                <div className="stat-label-modern">Khoảng cách gần nhất</div>
                                <div className="stat-value-modern" style={{ fontSize: '1.5rem' }}>2.3 km</div>
                              </div>
                              <div className="stat-card-icon blue">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <circle cx="12" cy="10" r="3"></circle>
                                  <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
                                </svg>
                              </div>
                            </div>
                          </div>
                        </div>

                        {/* Map Placeholder */}
                        <div className="card" style={{ marginBottom: '2rem', overflow: 'hidden' }}>
                          <div style={{ 
                            height: '400px', 
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            position: 'relative'
                          }}>
                            <div style={{ textAlign: 'center', color: 'white', zIndex: 1 }}>
                              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ margin: '0 auto 1rem' }}>
                                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                <circle cx="12" cy="10" r="3"></circle>
                              </svg>
                              <h3 style={{ marginBottom: '0.5rem' }}>Bản đồ trạm đổi pin</h3>
                              <p style={{ opacity: 0.9 }}>Tích hợp Google Maps sẽ được bổ sung trong phiên bản tiếp theo</p>
                            </div>
                            {/* Decorative circles */}
                            <div style={{ position: 'absolute', top: '10%', left: '20%', width: '100px', height: '100px', borderRadius: '50%', background: 'rgba(255,255,255,0.1)' }}></div>
                            <div style={{ position: 'absolute', bottom: '20%', right: '15%', width: '150px', height: '150px', borderRadius: '50%', background: 'rgba(255,255,255,0.1)' }}></div>
                          </div>
                        </div>

                        {/* Station List */}
                        <div className="card">
                          <div className="card-header-sticky">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <div>
                                <h2 className="card-title">
                                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                                    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                                  </svg>
                                  Danh sách trạm đổi pin
                                </h2>
                                <p className="card-description">Tất cả {stations.length} trạm trong hệ thống</p>
                              </div>
                              <button 
                                className="btn btn-primary"
                                onClick={() => loadStations(true)}
                                disabled={isLoadingStations}
                              >
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                                  <polyline points="23 4 23 10 17 10"></polyline>
                                  <polyline points="1 20 1 14 7 14"></polyline>
                                  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                                </svg>
                                Làm mới
                              </button>
                            </div>
                          </div>
                          <div className="card-content">
                            <div className="content-grid">
                              {stations.map((station, index) => (
                                <div key={station.id} className="card" style={{ border: '2px solid #e5e7eb', transition: 'all 0.3s ease' }}>
                                  <div className="card-header" style={{ borderBottom: '1px solid #e5e7eb', paddingBottom: '1rem' }}>
                                    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '1rem' }}>
                                      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flex: 1 }}>
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
                                        <div style={{ flex: 1, minWidth: 0 }}>
                                          <h3 style={{ marginBottom: '0.25rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                            {station.name}
                                          </h3>
                                          <p style={{ color: '#6b7280', fontSize: '0.875rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                            {station.address}
                                          </p>
                                        </div>
                                      </div>
                                      <span className={`badge ${station.status === 'active' ? 'badge-success' : 'badge-secondary'}`}>
                                        {systemConfig?.stationStatuses?.statuses?.find((s: any) => s.value === station.status)?.label || 
                                         (station.status === 'active' ? 'Hoạt động' : 'Không hoạt động')}
                                      </span>
                                    </div>
                                  </div>

                                  <div className="card-content">
                                    {/* Station Details */}
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                                      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2" style={{ flexShrink: 0, marginTop: '0.125rem' }}>
                                          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                          <circle cx="12" cy="10" r="3"></circle>
                                        </svg>
                                        <div style={{ flex: 1 }}>
                                          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Địa chỉ</p>
                                          <p style={{ fontSize: '0.875rem' }}>{station.address}</p>
                                        </div>
                                      </div>

                                      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2" style={{ flexShrink: 0, marginTop: '0.125rem' }}>
                                          <circle cx="12" cy="12" r="10"></circle>
                                          <path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20"></path>
                                          <path d="M2 12h20"></path>
                                        </svg>
                                        <div style={{ flex: 1 }}>
                                          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Tọa độ GPS</p>
                                          <p style={{ fontSize: '0.875rem', fontFamily: 'monospace' }}>
                                            {station.latitude}, {station.longitude}
                                          </p>
                                        </div>
                                      </div>

                                      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2" style={{ flexShrink: 0, marginTop: '0.125rem' }}>
                                          <circle cx="12" cy="10" r="3"></circle>
                                          <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
                                        </svg>
                                        <div style={{ flex: 1 }}>
                                          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Khoảng cách</p>
                                          <p style={{ fontSize: '0.875rem', color: '#3b82f6' }}>
                                            {(Math.random() * 5 + 1).toFixed(1)} km
                                          </p>
                                        </div>
                                      </div>

                                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginTop: '0.5rem' }}>
                                        <div style={{ padding: '0.75rem', backgroundColor: '#f9fafb', borderRadius: '8px', textAlign: 'center' }}>
                                          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Pin sẵn có</p>
                                          <p style={{ fontSize: '1.25rem', color: '#10b981' }}>
                                            {Math.floor(Math.random() * 15) + 5}
                                          </p>
                                        </div>
                                        <div style={{ padding: '0.75rem', backgroundColor: '#f9fafb', borderRadius: '8px', textAlign: 'center' }}>
                                          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Đang sử dụng</p>
                                          <p style={{ fontSize: '1.25rem', color: '#3b82f6' }}>
                                            {Math.floor(Math.random() * 5)}
                                          </p>
                                        </div>
                                      </div>
                                    </div>

                                    {/* Action Buttons */}
                                    <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid #e5e7eb' }}>
                                      <button 
                                        className="btn btn-primary" 
                                        style={{ flex: 1 }}
                                        onClick={() => showToast('Chức năng đặt trước đang phát triển', 'success')}
                                      >
                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                                          <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                                          <polyline points="17 2 12 7 7 2"></polyline>
                                        </svg>
                                        Đặt trước
                                      </button>
                                      <button 
                                        className="btn btn-secondary" 
                                        style={{ flex: 1 }}
                                        onClick={() => showToast('Đang mở chỉ đường...', 'success')}
                                      >
                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                                          <path d="m3 11 18-5v12L3 14v-3z"></path>
                                          <path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"></path>
                                        </svg>
                                        Chỉ đường
                                      </button>
                                    </div>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        </div>
                      </>
                    ) : (
                      <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
                        <div style={{ marginBottom: '1.5rem' }}>
                          <div style={{ 
                            width: '80px', 
                            height: '80px', 
                            margin: '0 auto',
                            borderRadius: '50%', 
                            backgroundColor: '#f3f4f6',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                          }}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2">
                              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                              <circle cx="12" cy="10" r="3"></circle>
                            </svg>
                          </div>
                        </div>
                        <h2 style={{ marginBottom: '0.5rem' }}>Chưa có trạm nào</h2>
                        <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
                          Hiện tại chưa có trạm đổi pin nào trong hệ thống.
                        </p>
                        <button 
                          className="btn btn-primary"
                          onClick={() => loadStations(true)}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                            <polyline points="23 4 23 10 17 10"></polyline>
                            <polyline points="1 20 1 14 7 14"></polyline>
                            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                          </svg>
                          Tải lại
                        </button>
                      </div>
                    )}
                  </>
                )}

                {/* Reservation Tab - Đặt lịch */}
                {activeMenu === 'reservation' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div>
                        <h1>Đặt chỗ pin</h1>
                        <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Đặt chỗ pin trước khi đến trạm</p>
                      </div>
                    </div>

                    {/* Select Station Section */}
                    <div className="card" style={{ marginBottom: '2rem' }}>
                      <div className="card-header" style={{ borderBottom: '1px solid #e5e7eb', paddingBottom: '1rem' }}>
                        <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Chọn trạm muốn đặt chỗ</h3>
                      </div>
                      <div className="card-content">
                        {/* Station Dropdown */}
                        <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                          <select
                            className="form-input"
                            style={{ 
                              width: '100%', 
                              padding: '0.75rem 1rem',
                              border: '1px solid #e5e7eb',
                              borderRadius: '8px',
                              fontSize: '0.875rem',
                              cursor: 'pointer'
                            }}
                            value={selectedStation?.id || ''}
                            onChange={(e) => {
                              const station = stations.find((s: any) => s.id === e.target.value);
                              setSelectedStation(station || null);
                            }}
                          >
                            <option value="">Chọn trạm đổi pin</option>
                            {stations.map((station, index) => (
                              <option key={station.id} value={station.id}>
                                {station.name} - {(Math.random() * 3 + 0.5).toFixed(1)}km
                              </option>
                            ))}
                          </select>
                        </div>

                        {/* Selected Station Info */}
                        {selectedStation && (
                          <div style={{ 
                            padding: '1.5rem', 
                            backgroundColor: '#f9fafb', 
                            borderRadius: '12px',
                            border: '1px solid #e5e7eb'
                          }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                              <div style={{ flex: 1 }}>
                                <h4 style={{ marginBottom: '0.5rem', fontSize: '1.125rem' }}>{selectedStation.name}</h4>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#6b7280', fontSize: '0.875rem', marginBottom: '0.5rem' }}>
                                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                    <circle cx="12" cy="10" r="3"></circle>
                                  </svg>
                                  <span>{selectedStation.address}</span>
                                </div>
                                <div style={{ display: 'flex', gap: '1.5rem', marginTop: '0.75rem' }}>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2">
                                      <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                                      <polyline points="17 2 12 7 7 2"></polyline>
                                    </svg>
                                    <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                      Pin khả dụng: <strong style={{ color: '#10b981' }}>5/10</strong>
                                    </span>
                                  </div>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" strokeWidth="2">
                                      <circle cx="12" cy="10" r="3"></circle>
                                      <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
                                    </svg>
                                    <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                      Cách: <strong style={{ color: '#3b82f6' }}>{(Math.random() * 3 + 0.5).toFixed(1)}km</strong>
                                    </span>
                                  </div>
                                </div>
                              </div>
                              <button 
                                className="btn btn-primary"
                                style={{ marginLeft: '1rem' }}
                                onClick={async () => {
                                  if (!vehicles.length) {
                                    showToast('Bạn cần đăng ký xe trước', 'error');
                                    return;
                                  }
                                  
                                  setIsCreatingReservation(true);
                                  try {
                                    const response = await fetch(
                                      'http://localhost:8080/api/v1/reservations',
                                      {
                                        method: 'POST',
                                        headers: {
                                          'Authorization': `Bearer ${accessToken}`,
                                          'Content-Type': 'application/json',
                                        },
                                        body: JSON.stringify({
                                          vehicle_id: vehicles[0].id,
                                          station_id: selectedStation.id,
                                          reserved_start: new Date().toISOString()
                                        }),
                                      }
                                    );

                                    const data = await response.json();
                                    
                                    if (response.ok && data.success) {
                                      showToast('Đặt chỗ thành công!', 'success');
                                      loadReservations();
                                      setSelectedStation(null);
                                    } else {
                                      showToast(data.error || 'Có lỗi xảy ra', 'error');
                                    }
                                  } catch (error) {
                                    showToast('Có lỗi xảy ra khi đặt chỗ', 'error');
                                  } finally {
                                    setIsCreatingReservation(false);
                                  }
                                }}
                                disabled={isCreatingReservation}
                              >
                                {isCreatingReservation ? 'Đang xử lý...' : 'Đặt chỗ'}
                              </button>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* My Reservations Section */}
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line>
                            <line x1="3" y1="10" x2="21" y2="10"></line>
                          </svg>
                          Đặt chỗ của tôi
                        </h2>
                        <p className="card-description">Danh sách các chỗ đã đặt</p>
                      </div>
                      <div className="card-content">
                        {reservations.length > 0 ? (
                          <div style={{ display: 'grid', gap: '1rem' }}>
                            {reservations
                              .filter(r => r.status !== 'cancelled' && r.status !== 'expired')
                              .map((reservation) => {
                                const station = stations.find((s: any) => s.id === reservation.station_id);
                                const isActive = reservation.status === 'confirmed' || reservation.status === 'pending';
                                
                                return (
                                  <div 
                                    key={reservation.id} 
                                    style={{ 
                                      padding: '1.5rem', 
                                      backgroundColor: '#ffffff', 
                                      borderRadius: '12px',
                                      border: '2px solid #e5e7eb'
                                    }}
                                  >
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                                      <div style={{ flex: 1 }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
                                          <h4 style={{ fontSize: '1.125rem' }}>{station?.name || 'Trạm đổi pin'}</h4>
                                          {isActive && (
                                            <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>
                                              Đang hoạt động
                                            </span>
                                          )}
                                        </div>
                                        
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginTop: '0.75rem' }}>
                                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: '#6b7280' }}>
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                              <circle cx="12" cy="12" r="10"></circle>
                                              <polyline points="12 6 12 12 16 14"></polyline>
                                            </svg>
                                            <span>
                                              Đặt lúc: <strong style={{ color: '#1f2937' }}>
                                                {new Date(reservation.reserved_start).toLocaleString('vi-VN', {
                                                  year: 'numeric',
                                                  month: '2-digit',
                                                  day: '2-digit',
                                                  hour: '2-digit',
                                                  minute: '2-digit'
                                                })}
                                              </strong>
                                            </span>
                                          </div>
                                          
                                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: '#6b7280' }}>
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                              <circle cx="12" cy="12" r="10"></circle>
                                              <polyline points="12 6 12 12 16 14"></polyline>
                                            </svg>
                                            <span>
                                              Hết hạn lúc: <strong style={{ color: '#ef4444' }}>
                                                {new Date(reservation.reserved_end).toLocaleString('vi-VN', {
                                                  year: 'numeric',
                                                  month: '2-digit',
                                                  day: '2-digit',
                                                  hour: '2-digit',
                                                  minute: '2-digit'
                                                })}
                                              </strong>
                                            </span>
                                          </div>
                                        </div>
                                      </div>
                                      
                                      <button 
                                        className="btn btn-secondary"
                                        style={{ marginLeft: '1rem', whiteSpace: 'nowrap' }}
                                        onClick={async () => {
                                          if (!window.confirm('Bạn có chắc muốn hủy đặt chỗ này?')) return;
                                          
                                          try {
                                            const response = await fetch(
                                              `http://localhost:8080/api/v1/reservations/${reservation.id}`,
                                              {
                                                method: 'DELETE',
                                                headers: {
                                                  'Authorization': `Bearer ${accessToken}`,
                                                },
                                              }
                                            );

                                            if (response.ok) {
                                              showToast('Đã hủy đặt chỗ', 'success');
                                              loadReservations();
                                            } else {
                                              showToast('Có lỗi xảy ra', 'error');
                                            }
                                          } catch (error) {
                                            showToast('Có lỗi xảy ra', 'error');
                                          }
                                        }}
                                      >
                                        Hủy đặt chỗ
                                      </button>
                                    </div>
                                  </div>
                                );
                              })}
                          </div>
                        ) : (
                          <p className="empty-state">Chưa có đặt chỗ nào</p>
                        )}
                      </div>
                    </div>
                  </>
                )}
                {/* Swap Tab */}
                {activeMenu === 'swap' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div>
                        <h1>Đổi pin nhanh chóng</h1>
                        <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Thực hiện đổi pin chỉ trong 3 phút</p>
                      </div>
                    </div>

                    {/* Progress Steps */}
                    <div style={{ marginBottom: '2rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', position: 'relative' }}>
                        {/* Progress line */}
                        <div style={{ position: 'absolute', top: '20px', left: '40px', right: '40px', height: '2px', backgroundColor: '#e5e7eb', zIndex: 0 }}>
                          <div style={{ height: '100%', width: `${((swapStep - 1) / 3) * 100}%`, backgroundColor: '#10b981', transition: 'width 0.3s ease' }}></div>
                        </div>

                        {/* Steps */}
                        {[
                          { step: 1, label: 'Chọn trạm', icon: <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg> },
                          { step: 2, label: 'Chọn pin', icon: <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect><polyline points="17 2 12 7 7 2"></polyline></svg> },
                          { step: 3, label: 'Xác nhận', icon: <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg> },
                          { step: 4, label: 'Hoàn tất', icon: <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg> }
                        ].map((item) => (
                          <div key={item.step} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative', zIndex: 1, flex: 1 }}>
                            <div style={{
                              width: '40px',
                              height: '40px',
                              borderRadius: '50%',
                              backgroundColor: swapStep >= item.step ? '#10b981' : '#ffffff',
                              border: `2px solid ${swapStep >= item.step ? '#10b981' : '#e5e7eb'}`,
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              color: swapStep >= item.step ? '#ffffff' : '#9ca3af',
                              transition: 'all 0.3s ease',
                              marginBottom: '0.5rem'
                            }}>
                              {item.icon}
                            </div>
                            <span style={{ fontSize: '0.875rem', color: swapStep >= item.step ? '#10b981' : '#6b7280' }}>{item.label}</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Step 1: Select Station */}
                    {swapStep === 1 && (
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
                                  setSwapStep(2);
                                  loadAvailableBatteries();
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

                    {/* Step 2: Select Battery */}
                    {swapStep === 2 && (
                      <div className="card">
                        <div className="card-header">
                          <h2 className="card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                              <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                              <polyline points="17 2 12 7 7 2"></polyline>
                            </svg>
                            Chọn pin thay thế
                          </h2>
                          <p className="card-description">Pin có sẵn tại {selectedStation?.name}</p>
                        </div>
                        <div className="card-content">
                          {availableBatteries.length > 0 ? (
                            <div className="content-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))' }}>
                              {availableBatteries.map((battery) => (
                                <div 
                                  key={battery.id} 
                                  className="card" 
                                  style={{ 
                                    border: selectedBattery?.id === battery.id ? '2px solid #10b981' : '2px solid #e5e7eb',
                                    cursor: 'pointer',
                                    transition: 'all 0.3s ease'
                                  }}
                                  onClick={() => setSelectedBattery(battery)}
                                >
                                  <div className="card-content">
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                                      <div style={{ 
                                        width: '48px', 
                                        height: '48px', 
                                        borderRadius: '12px', 
                                        background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        color: 'white'
                                      }}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                          <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                                          <polyline points="17 2 12 7 7 2"></polyline>
                                        </svg>
                                      </div>
                                      {selectedBattery?.id === battery.id && (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2">
                                          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                          <polyline points="22 4 12 14.01 9 11.01"></polyline>
                                        </svg>
                                      )}
                                    </div>

                                    <h4 style={{ marginBottom: '0.5rem' }}>{battery.model}</h4>
                                    
                                    <div style={{ marginTop: '1rem' }}>
                                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                        <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Dung lượng pin</span>
                                        <span style={{ fontSize: '0.875rem', color: '#10b981' }}>{battery.soc_percent}%</span>
                                      </div>
                                      <div style={{ width: '100%', height: '8px', backgroundColor: '#e5e7eb', borderRadius: '999px', overflow: 'hidden' }}>
                                        <div style={{ width: `${battery.soc_percent}%`, height: '100%', background: 'linear-gradient(90deg, #10b981 0%, #059669 100%)' }}></div>
                                      </div>
                                    </div>

                                    <div style={{ marginTop: '1rem' }}>
                                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                        <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Sức khỏe pin</span>
                                        <span style={{ fontSize: '0.875rem', color: '#3b82f6' }}>{battery.soh_percent}%</span>
                                      </div>
                                      <div style={{ width: '100%', height: '8px', backgroundColor: '#e5e7eb', borderRadius: '999px', overflow: 'hidden' }}>
                                        <div style={{ width: `${battery.soh_percent}%`, height: '100%', background: 'linear-gradient(90deg, #3b82f6 0%, #2563eb 100%)' }}></div>
                                      </div>
                                    </div>

                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid #e5e7eb' }}>
                                      <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Ước tính quãng đường</span>
                                      <span style={{ fontSize: '0.875rem', color: '#1f2937' }}>~{Math.floor(battery.soc_percent * 0.8)} km</span>
                                    </div>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <p className="empty-state">Không có pin sẵn có tại trạm này</p>
                          )}

                          <div style={{ marginTop: '2rem', textAlign: 'right', display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                            <button 
                              className="btn btn-secondary"
                              onClick={() => {
                                setSwapStep(1);
                                setSelectedBattery(null);
                                setAvailableBatteries([]);
                              }}
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                                <line x1="19" y1="12" x2="5" y2="12"></line>
                                <polyline points="12 19 5 12 12 5"></polyline>
                              </svg>
                              Quay lại
                            </button>
                            {selectedBattery && (
                              <button 
                                className="btn btn-primary"
                                onClick={() => {
                                  if (vehicles.length > 0) {
                                    setSelectedVehicle(vehicles[0]);
                                  }
                                  setSwapStep(3);
                                }}
                              >
                                Tiếp tục
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

                    {/* Step 3: Confirm */}
                    {swapStep === 3 && (
                      <div className="card">
                        <div className="card-header">
                          <h2 className="card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                              <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                            Xác nhận đổi pin
                          </h2>
                          <p className="card-description">Kiểm tra lại thông tin trước khi đổi pin</p>
                        </div>
                        <div className="card-content">
                          <div style={{ display: 'grid', gap: '1.5rem' }}>
                            {/* Vehicle Info */}
                            <div style={{ padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '12px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                                  <circle cx="7" cy="17" r="2"></circle>
                                  <circle cx="17" cy="17" r="2"></circle>
                                </svg>
                                <h4>Thông tin xe</h4>
                              </div>
                              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                <div>
                                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Model</p>
                                  <p>{selectedVehicle?.model}</p>
                                </div>
                                <div>
                                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Biển số</p>
                                  <p>{selectedVehicle?.plate_number}</p>
                                </div>
                              </div>
                            </div>

                            {/* Station Info */}
                            <div style={{ padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '12px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                  <circle cx="12" cy="10" r="3"></circle>
                                </svg>
                                <h4>Trạm đổi pin</h4>
                              </div>
                              <div>
                                <p>{selectedStation?.name}</p>
                                <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem' }}>{selectedStation?.address}</p>
                              </div>
                            </div>

                            {/* Battery Info */}
                            <div style={{ padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '12px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                                  <polyline points="17 2 12 7 7 2"></polyline>
                                </svg>
                                <h4>Thông tin pin mới</h4>
                              </div>
                              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
                                <div>
                                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Model</p>
                                  <p>{selectedBattery?.model}</p>
                                </div>
                                <div>
                                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Dung lượng</p>
                                  <p style={{ color: '#10b981' }}>{selectedBattery?.soc_percent}%</p>
                                </div>
                                <div>
                                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Sức khỏe</p>
                                  <p style={{ color: '#3b82f6' }}>{selectedBattery?.soh_percent}%</p>
                                </div>
                              </div>
                            </div>

                            {/* Cost */}
                            <div style={{ padding: '1.5rem', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', borderRadius: '12px', color: 'white' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                  <p style={{ opacity: 0.9, marginBottom: '0.25rem' }}>Chi phí đổi pin</p>
                                  <h2>25.000 VND</h2>
                                </div>
                                <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ opacity: 0.7 }}>
                                  <line x1="12" y1="1" x2="12" y2="23"></line>
                                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                                </svg>
                              </div>
                            </div>
                          </div>

                          <div style={{ marginTop: '2rem', textAlign: 'right', display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                            <button 
                              className="btn btn-secondary"
                              onClick={() => {
                                setSwapStep(2);
                              }}
                              disabled={isSwapping}
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                                <line x1="19" y1="12" x2="5" y2="12"></line>
                                <polyline points="12 19 5 12 12 5"></polyline>
                              </svg>
                              Quay lại
                            </button>
                            <button 
                              className="btn btn-primary"
                              onClick={handleSwapBattery}
                              disabled={isSwapping}
                              style={{ minWidth: '150px' }}
                            >
                              {isSwapping ? (
                                <>
                                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem', animation: 'spin 1s linear infinite' }}>
                                    <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
                                  </svg>
                                  Đang xử lý...
                                </>
                              ) : (
                                <>
                                  Xác nhận đổi pin
                                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginLeft: '0.5rem' }}>
                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                                  </svg>
                                </>
                              )}
                            </button>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Step 4: Success */}
                    {swapStep === 4 && swapResult && (
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
                          <h1 style={{ marginBottom: '1rem', color: '#10b981' }}>Đổi pin thành công!</h1>
                          <p style={{ color: '#6b7280', fontSize: '1.125rem' }}>
                            Giao dịch đã hoàn tất. Chúc bạn có hành trình an toàn!
                          </p>
                        </div>

                        {/* Transaction Summary */}
                        <div style={{ padding: '2rem', backgroundColor: '#f9fafb', borderRadius: '12px', marginBottom: '2rem', textAlign: 'left' }}>
                          <h3 style={{ marginBottom: '1.5rem', textAlign: 'center' }}>Chi tiết giao dịch</h3>
                          
                          <div style={{ display: 'grid', gap: '1rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                              <span style={{ color: '#6b7280' }}>Mã giao dịch</span>
                              <span style={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>{swapResult.swap.id.substring(0, 8).toUpperCase()}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                              <span style={{ color: '#6b7280' }}>Trạm</span>
                              <span>{selectedStation?.name}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                              <span style={{ color: '#6b7280' }}>Pin cũ (Thu vào)</span>
                              <span>{swapResult.old_battery.soc_percent}% → Đang sạc</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                              <span style={{ color: '#6b7280' }}>Pin mới (Đưa ra)</span>
                              <span style={{ color: '#10b981' }}>{swapResult.new_battery.soc_percent}% - {swapResult.new_battery.model}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                              <span style={{ color: '#6b7280' }}>Thời gian</span>
                              <span>{new Date(swapResult.swap.swapped_at).toLocaleString('vi-VN')}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <span style={{ color: '#6b7280' }}>Tổng chi phí</span>
                              <span style={{ fontSize: '1.5rem', color: '#10b981' }}>25.000 VND</span>
                            </div>
                          </div>
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
                            onClick={resetSwapFlow}
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                              <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                              <polyline points="17 2 12 7 7 2"></polyline>
                            </svg>
                            Đổi pin tiếp
                          </button>
                        </div>
                      </div>
                    )}
                  </>
                )}
                {/* Payment Tab */}
                {activeMenu === 'payment' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div>
                        <h1>Thanh toán & Ví điện tử</h1>
                        <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Quản lý ví, hóa đơn và lịch sử giao dịch</p>
                      </div>
                    </div>

                    {/* Wallet Stats */}
                    <div className="stats-cards-row" style={{ marginBottom: '2rem' }}>
                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Số dư ví</div>
                            <div className="stat-value-modern" style={{ fontSize: '2rem', color: '#10b981' }}>0đ</div>
                          </div>
                          <div className="stat-card-icon green">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <line x1="12" y1="1" x2="12" y2="23"></line>
                              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Tổng đổi pin</div>
                            <div className="stat-value-modern">{transactions.length} lượt</div>
                          </div>
                          <div className="stat-card-icon blue">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                              <polyline points="17 2 12 7 7 2"></polyline>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Chi phí tháng này</div>
                            <div className="stat-value-modern" style={{ fontSize: '1.5rem' }}>
                              {new Intl.NumberFormat('vi-VN').format(thisMonthCost)}đ
                            </div>
                          </div>
                          <div className="stat-card-icon purple">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M3 3v18h18"></path>
                              <path d="M18 17V9"></path>
                              <path d="M13 17V5"></path>
                              <path d="M8 17v-3"></path>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Chi phí TB/lượt</div>
                            <div className="stat-value-modern" style={{ fontSize: '1.5rem' }}>
                              {new Intl.NumberFormat('vi-VN').format(averageCostPerSwap)}đ
                            </div>
                          </div>
                          <div className="stat-card-icon orange">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <circle cx="12" cy="12" r="10"></circle>
                              <polyline points="12 6 12 12 16 14"></polyline>
                            </svg>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Top Up Card */}
                    <div className="card" style={{ marginBottom: '2rem', background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white', border: 'none' }}>
                      <div className="card-header" style={{ borderBottom: '1px solid rgba(255,255,255,0.2)' }}>
                        <h2 className="card-title" style={{ color: 'white' }}>
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <line x1="12" y1="1" x2="12" y2="23"></line>
                            <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                          </svg>
                          Nạp tiền vào ví
                        </h2>
                      </div>
                      <div className="card-content">
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr auto', gap: '1rem', alignItems: 'end' }}>
                          <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'rgba(255,255,255,0.9)' }}>Số tiền nạp</label>
                            <input
                              type="number"
                              className="form-input"
                              placeholder="Nhập số tiền"
                              value={topUpAmount}
                              onChange={(e) => setTopUpAmount(e.target.value)}
                              style={{ backgroundColor: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.3)', color: 'white' }}
                            />
                          </div>
                          <button 
                            className="btn"
                            style={{ backgroundColor: 'white', color: '#10b981', border: 'none' }}
                            onClick={() => {
                              if (topUpAmount && parseFloat(topUpAmount) > 0) {
                                // Generate transaction ID
                                const transactionId = `TXN${Date.now().toString().substring(5)}`;
                                setPaymentTransactionId(transactionId);
                                setShowPaymentModal(true);
                              } else {
                                showToast('Vui lòng nhập số tiền hợp lệ', 'error');
                              }
                            }}
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                              <line x1="12" y1="5" x2="12" y2="19"></line>
                              <line x1="5" y1="12" x2="19" y2="12"></line>
                            </svg>
                            Nạp tiền
                          </button>
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.75rem', marginTop: '1.5rem' }}>
                          {[50000, 100000, 200000, 500000].map(amount => (
                            <button
                              key={amount}
                              className="btn"
                              style={{ backgroundColor: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.3)', color: 'white' }}
                              onClick={() => setTopUpAmount(amount.toString())}
                            >
                              {(amount / 1000).toLocaleString('vi-VN')}k
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>

                    {/* Transaction History */}
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12 6 12 12 16 14"></polyline>
                          </svg>
                          Lịch sử giao dịch
                        </h2>
                        <p className="card-description">Tất cả giao dịch đổi pin và thanh toán</p>
                      </div>
                      <div className="card-content">
                        {transactions.length > 0 ? (
                          <div className="table-container">
                            <table className="table">
                              <thead>
                                <tr>
                                  <th>Thời gian</th>
                                  <th>Loại giao dịch</th>
                                  <th>Trạm</th>
                                  <th>Số tiền</th>
                                  <th>Trạng thái</th>
                                  <th>Hành động</th>
                                </tr>
                              </thead>
                              <tbody>
                                {transactions.map((transaction, index) => (
                                  <tr key={transaction.id}>
                                    <td>{new Date(transaction.timestamp).toLocaleString('vi-VN')}</td>
                                    <td>
                                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                          <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                                          <polyline points="17 2 12 7 7 2"></polyline>
                                        </svg>
                                        Đổi pin
                                      </div>
                                    </td>
                                    <td>{stations[index % stations.length]?.name || 'Trạm đổi pin'}</td>
                                    <td style={{ color: '#ef4444' }}>-25.000đ</td>
                                    <td>
                                      <span className="badge badge-success">Hoàn thành</span>
                                    </td>
                                    <td>
                                      <button 
                                        className="btn btn-secondary" 
                                        style={{ fontSize: '0.875rem', padding: '0.375rem 0.75rem' }}
                                        onClick={() => {
                                          setSelectedTransactionForFeedback(transaction);
                                          setModalType('feedback' as any);
                                          setShowModal(true);
                                        }}
                                      >
                                        Đánh giá
                                      </button>
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        ) : (
                          <p className="empty-state">Chưa có giao dịch nào</p>
                        )}
                      </div>
                    </div>
                  </>
                )}

                {/* Support Tab */}
                {activeMenu === 'support' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <h1>Hỗ trợ kỹ thuật</h1>
                          <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Gửi yêu cầu hỗ trợ và đánh giá dịch vụ</p>
                        </div>
                        <button 
                          className="btn btn-primary"
                          onClick={() => openModal('ticket')}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                            <line x1="12" y1="5" x2="12" y2="19"></line>
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                          </svg>
                          Tạo yêu cầu hỗ trợ
                        </button>
                      </div>
                    </div>

                    {/* Support Stats */}
                    <div className="stats-cards-row" style={{ marginBottom: '2rem' }}>
                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Yêu cầu đang mở</div>
                            <div className="stat-value-modern">{tickets.filter(t => t.status === 'open').length}</div>
                          </div>
                          <div className="stat-card-icon orange">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <circle cx="12" cy="12" r="10"></circle>
                              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                              <line x1="12" y1="17" x2="12.01" y2="17"></line>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Đã giải quyết</div>
                            <div className="stat-value-modern">{tickets.filter(t => t.status === 'resolved').length}</div>
                          </div>
                          <div className="stat-card-icon green">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                              <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Tổng yêu cầu</div>
                            <div className="stat-value-modern">{tickets.length}</div>
                          </div>
                          <div className="stat-card-icon blue">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                              <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                            </svg>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* My Feedbacks Section */}
                    <div className="card" style={{ marginBottom: '2rem' }}>
                      <div className="card-header">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                          </svg>
                          Đánh giá của tôi
                        </h2>
                        <p className="card-description">Các đánh giá bạn đã gửi về dịch vụ</p>
                      </div>
                      <div className="card-content">
                        {feedbacks.length > 0 ? (
                          <div style={{ display: 'grid', gap: '1rem' }}>
                            {feedbacks.map((feedback) => {
                              const station = stations.find((s: any) => s.id === feedback.station_id);
                              return (
                                <div 
                                  key={feedback.id} 
                                  className="card" 
                                  style={{ border: '2px solid #e5e7eb' }}
                                >
                                  <div className="card-content">
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                                      <div style={{ flex: 1 }}>
                                        {/* Rating Stars */}
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                                          <div style={{ display: 'flex', gap: '0.25rem' }}>
                                            {[1, 2, 3, 4, 5].map((star) => (
                                              <svg 
                                                key={star}
                                                xmlns="http://www.w3.org/2000/svg" 
                                                width="20" 
                                                height="20" 
                                                viewBox="0 0 24 24" 
                                                fill={star <= feedback.rating ? '#fbbf24' : 'none'}
                                                stroke={star <= feedback.rating ? '#fbbf24' : '#d1d5db'}
                                                strokeWidth="2"
                                              >
                                                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                                              </svg>
                                            ))}
                                          </div>
                                          <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                            ({feedback.rating}/5)
                                          </span>
                                        </div>

                                        {/* Station Info */}
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2">
                                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                            <circle cx="12" cy="10" r="3"></circle>
                                          </svg>
                                          <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                            {station?.name || 'Trạm đổi pin'}
                                          </span>
                                        </div>

                                        {/* Comment */}
                                        {feedback.comment && (
                                          <p style={{ color: '#1f2937', marginBottom: '0.75rem', fontSize: '0.95rem' }}>
                                            "{feedback.comment}"
                                          </p>
                                        )}

                                        {/* Time */}
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: '#9ca3af' }}>
                                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                            <circle cx="12" cy="12" r="10"></circle>
                                            <polyline points="12 6 12 12 16 14"></polyline>
                                          </svg>
                                          <span>{new Date(feedback.created_at).toLocaleString('vi-VN')}</span>
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        ) : (
                          <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
                            <div style={{ 
                              width: '80px', 
                              height: '80px', 
                              margin: '0 auto 1.5rem',
                              borderRadius: '50%', 
                              backgroundColor: '#f3f4f6',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center'
                            }}>
                              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2">
                                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                              </svg>
                            </div>
                            <h2 style={{ marginBottom: '0.5rem' }}>Chưa có đánh giá nào</h2>
                            <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
                              Sau khi đổi pin, bạn có thể đánh giá dịch vụ từ lịch sử giao dịch.
                            </p>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Tickets List */}
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                          </svg>
                          Danh sách yêu cầu hỗ trợ
                        </h2>
                        <p className="card-description">Tất cả yêu cầu hỗ trợ của bạn</p>
                      </div>
                      <div className="card-content">
                        {tickets.length > 0 ? (
                          <div style={{ display: 'grid', gap: '1rem' }}>
                            {tickets.map((ticket) => (
                              <div 
                                key={ticket.id} 
                                className="card" 
                                style={{ border: '2px solid #e5e7eb' }}
                              >
                                <div className="card-content">
                                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                                    <div style={{ flex: 1 }}>
                                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
                                        <h3 style={{ fontSize: '1.125rem' }}>{ticket.subject}</h3>
                                        <span className={`badge ${
                                          ticket.status === 'open' ? 'badge-primary' : 
                                          ticket.status === 'resolved' ? 'badge-success' : 'badge-secondary'
                                        }`}>
                                          {ticket.status === 'open' ? 'Đang xử lý' : 
                                           ticket.status === 'resolved' ? 'Đã giải quyết' : 'Đóng'}
                                        </span>
                                      </div>
                                      <p style={{ color: '#6b7280', marginBottom: '0.75rem' }}>{ticket.description}</p>
                                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: '#9ca3af' }}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                          <circle cx="12" cy="12" r="10"></circle>
                                          <polyline points="12 6 12 12 16 14"></polyline>
                                        </svg>
                                        <span>{new Date(ticket.created_at).toLocaleString('vi-VN')}</span>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
                            <div style={{ 
                              width: '80px', 
                              height: '80px', 
                              margin: '0 auto 1.5rem',
                              borderRadius: '50%', 
                              backgroundColor: '#f3f4f6',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center'
                            }}>
                              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                                <line x1="12" y1="17" x2="12.01" y2="17"></line>
                              </svg>
                            </div>
                            <h2 style={{ marginBottom: '0.5rem' }}>Chưa có yêu cầu hỗ trợ nào</h2>
                            <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
                              Gặp vấn đề? Tạo yêu cầu hỗ trợ để được hỗ trợ nhanh chóng.
                            </p>
                            <button 
                              className="btn btn-primary"
                              onClick={() => openModal('ticket')}
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                                <line x1="12" y1="5" x2="12" y2="19"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                              </svg>
                              Tạo yêu cầu đầu tiên
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </>
                )}
              </>
            )}
            {/* Admin Dashboard */}
            {currentUser.role === 'admin' && (
              <>
                {/* Overview Tab */}
                {activeMenu === 'overview' && (
                  <>
                    {/* Stats Cards */}
                    <div className="stats-cards-row">
                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Tổng người dùng</div>
                            <div className="stat-value-modern">{users.length}</div>
                          </div>
                          <div className="stat-card-icon purple">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                              <circle cx="9" cy="7" r="4"></circle>
                              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Tổng Driver</div>
                            <div className="stat-value-modern">{users.filter(u => u.role === 'driver').length}</div>
                          </div>
                          <div className="stat-card-icon green">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                              <circle cx="7" cy="17" r="2"></circle>
                              <circle cx="17" cy="17" r="2"></circle>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Tổng Staff</div>
                            <div className="stat-value-modern">{users.filter(u => u.role === 'staff').length}</div>
                          </div>
                          <div className="stat-card-icon blue">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                              <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Trạm đổi pin</div>
                            <div className="stat-value-modern">{stations.length}</div>
                          </div>
                          <div className="stat-card-icon orange">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                              <circle cx="12" cy="10" r="3"></circle>
                            </svg>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Quick Actions */}
                    <div className="content-grid" style={{ marginTop: '2rem', gridTemplateColumns: 'repeat(2, 1fr)' }}>
                      <div className="card">
                        <div className="card-header">
                          <h2 className="card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                              <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            Hành động nhanh
                          </h2>
                        </div>
                        <div className="card-content">
                          <div style={{ display: 'grid', gap: '1rem' }}>
                            <button 
                              className="btn btn-primary" 
                              style={{ width: '100%', justifyContent: 'flex-start' }}
                              onClick={() => openModal('staff')}
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.75rem' }}>
                                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                                <circle cx="9" cy="7" r="4"></circle>
                                <line x1="19" y1="8" x2="19" y2="14"></line>
                                <line x1="22" y1="11" x2="16" y2="11"></line>
                              </svg>
                              Tạo tài khoản Staff
                            </button>
                            <button 
                              className="btn btn-secondary" 
                              style={{ width: '100%', justifyContent: 'flex-start' }}
                              onClick={() => openModal('station')}
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.75rem' }}>
                                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                <circle cx="12" cy="10" r="3"></circle>
                              </svg>
                              Thêm trạm đổi pin
                            </button>
                          </div>
                        </div>
                      </div>

                      <div className="card">
                        <div className="card-header">
                          <h2 className="card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                              <line x1="18" y1="20" x2="18" y2="10"></line>
                              <line x1="12" y1="20" x2="12" y2="4"></line>
                              <line x1="6" y1="20" x2="6" y2="14"></line>
                            </svg>
                            Thống kê hệ thống
                          </h2>
                        </div>
                        <div className="card-content">
                          <div style={{ display: 'grid', gap: '1rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', backgroundColor: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                              <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Trạm hoạt động</span>
                              <span style={{ fontSize: '1.125rem', color: '#10b981', fontWeight: '600' }}>{stations.filter(s => s.status === 'active').length}/{stations.length}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', backgroundColor: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                              <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Tổng giao dịch</span>
                              <span style={{ fontSize: '1.125rem', color: '#3b82f6', fontWeight: '600' }}>{transactions.length}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', backgroundColor: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid rgba(255, 255, 255, 0.1)' }}>
                              <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Yêu cầu hỗ trợ</span>
                              <span style={{ fontSize: '1.125rem', color: '#f59e0b', fontWeight: '600' }}>{tickets.filter(t => t.status === 'open').length}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Recent Activity */}
                    <div className="card" style={{ marginTop: '2rem' }}>
                      <div className="card-header">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12 6 12 12 16 14"></polyline>
                          </svg>
                          Người dùng mới nhất
                        </h2>
                      </div>
                      <div className="card-content">
                        {users.length > 0 ? (
                          <div className="table-container">
                            <table className="table">
                              <thead>
                                <tr>
                                  <th>Họ tên</th>
                                  <th>Email</th>
                                  <th>Vai trò</th>
                                  <th>Ngày tạo</th>
                                </tr>
                              </thead>
                              <tbody>
                                {users.slice(0, 5).map((user) => (
                                  <tr key={user.id}>
                                    <td>{user.full_name}</td>
                                    <td>{user.email}</td>
                                    <td>
                                      <span className={`badge ${user.role === 'admin' ? 'badge-primary' : user.role === 'staff' ? 'badge-secondary' : 'badge-success'}`}>
                                        {user.role === 'admin' ? 'Admin' : user.role === 'staff' ? 'Staff' : 'Driver'}
                                      </span>
                                    </td>
                                    <td>{new Date(user.created_at).toLocaleDateString('vi-VN')}</td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        ) : (
                          <p className="empty-state">Chưa có người dùng nào</p>
                        )}
                      </div>
                    </div>
                  </>
                )}

                {/* Users Tab */}
                {activeMenu === 'users' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <h1>Quản lý người dùng</h1>
                          <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Quản lý tài khoản Driver và Staff trong hệ thống</p>
                        </div>
                        <button 
                          className="btn btn-primary"
                          onClick={() => openModal('staff')}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <line x1="19" y1="8" x2="19" y2="14"></line>
                            <line x1="22" y1="11" x2="16" y2="11"></line>
                          </svg>
                          Tạo tài khoản Staff
                        </button>
                      </div>
                    </div>

                    {/* User Stats */}
                    <div className="stats-cards-row" style={{ marginBottom: '2rem' }}>
                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Tổng người dùng</div>
                            <div className="stat-value-modern">{users.length}</div>
                          </div>
                          <div className="stat-card-icon purple">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                              <circle cx="9" cy="7" r="4"></circle>
                              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Driver</div>
                            <div className="stat-value-modern">{users.filter(u => u.role === 'driver').length}</div>
                          </div>
                          <div className="stat-card-icon green">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                              <circle cx="7" cy="17" r="2"></circle>
                              <circle cx="17" cy="17" r="2"></circle>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Staff</div>
                            <div className="stat-value-modern">{users.filter(u => u.role === 'staff').length}</div>
                          </div>
                          <div className="stat-card-icon blue">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                              <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Admin</div>
                            <div className="stat-value-modern">{users.filter(u => u.role === 'admin').length}</div>
                          </div>
                          <div className="stat-card-icon orange">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                            </svg>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Users Table */}
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                          </svg>
                          Danh sách người dùng
                        </h2>
                        <p className="card-description">Tất cả {users.length} người dùng trong hệ thống</p>
                      </div>
                      <div className="card-content">
                        {users.length > 0 ? (
                          <div className="table-container">
                            <table className="table">
                              <thead>
                                <tr>
                                  <th>Họ tên</th>
                                  <th>Email</th>
                                  <th>Điện thoại</th>
                                  <th>Vai trò</th>
                                  <th>Ngày tạo</th>
                                </tr>
                              </thead>
                              <tbody>
                                {users.map((user) => (
                                  <tr key={user.id}>
                                    <td>
                                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                        <div style={{ 
                                          width: '40px', 
                                          height: '40px', 
                                          borderRadius: '50%', 
                                          background: user.role === 'admin' ? 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)' : 
                                                     user.role === 'staff' ? 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)' :
                                                     'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                                          display: 'flex',
                                          alignItems: 'center',
                                          justifyContent: 'center',
                                          color: 'white',
                                          fontSize: '0.875rem',
                                          fontWeight: '600'
                                        }}>
                                          {user.full_name?.charAt(0)?.toUpperCase() || 'U'}
                                        </div>
                                        <span>{user.full_name}</span>
                                      </div>
                                    </td>
                                    <td style={{ fontSize: '0.875rem' }}>{user.email}</td>
                                    <td>{user.phone}</td>
                                    <td>
                                      <span className={`badge ${user.role === 'admin' ? 'badge-primary' : user.role === 'staff' ? 'badge-secondary' : 'badge-success'}`}>
                                        {user.role === 'admin' ? 'Admin' : user.role === 'staff' ? 'Staff' : 'Driver'}
                                      </span>
                                    </td>
                                    <td>{new Date(user.created_at).toLocaleDateString('vi-VN')}</td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        ) : (
                          <p className="empty-state">Chưa có người dùng nào</p>
                        )}
                      </div>
                    </div>
                  </>
                )}

                {/* Stations Tab */}
                {activeMenu === 'stations' && (
                  <>
                    <div className="dashboard-header" style={{ marginBottom: '2rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <h1>Quản lý trạm đổi pin</h1>
                          <p style={{ color: '#6b7280', marginTop: '0.5rem' }}>Quản lý tất cả trạm đổi pin trong hệ thống</p>
                        </div>
                        <button 
                          className="btn btn-primary"
                          onClick={() => openModal('station')}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                            <line x1="12" y1="5" x2="12" y2="19"></line>
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                          </svg>
                          Thêm trạm mới
                        </button>
                      </div>
                    </div>

                    {/* Station Stats */}
                    <div className="stats-cards-row" style={{ marginBottom: '2rem' }}>
                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Tổng trạm</div>
                            <div className="stat-value-modern">{stations.length}</div>
                          </div>
                          <div className="stat-card-icon purple">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                              <circle cx="12" cy="10" r="3"></circle>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Trạm hoạt động</div>
                            <div className="stat-value-modern">{stations.filter(s => s.status === 'active').length}</div>
                          </div>
                          <div className="stat-card-icon green">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                              <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Trạm bảo trì</div>
                            <div className="stat-value-modern">{stations.filter(s => s.status === 'maintenance').length}</div>
                          </div>
                          <div className="stat-card-icon orange">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"></path>
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="stat-card-modern">
                        <div className="stat-card-header-modern">
                          <div>
                            <div className="stat-label-modern">Trung bình pin</div>
                            <div className="stat-value-modern" style={{ fontSize: '1.5rem' }}>25/trạm</div>
                          </div>
                          <div className="stat-card-icon blue">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                              <polyline points="17 2 12 7 7 2"></polyline>
                            </svg>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Stations Grid */}
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.5rem' }}>
                            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                          </svg>
                          Danh sách trạm đổi pin
                        </h2>
                        <p className="card-description">Tất cả {stations.length} trạm trong hệ thống</p>
                      </div>
                      <div className="card-content">
                        {stations.length > 0 ? (
                          <div className="table-container">
                            <table className="table">
                              <thead>
                                <tr>
                                  <th>Tên trạm</th>
                                  <th>Địa chỉ</th>
                                  <th>Tọa độ</th>
                                  <th>Trạng thái</th>
                                  <th>Ngày tạo</th>
                                </tr>
                              </thead>
                              <tbody>
                                {stations.map((station, index) => (
                                  <tr key={station.id}>
                                    <td>
                                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                        <div style={{ 
                                          width: '40px', 
                                          height: '40px', 
                                          borderRadius: '10px', 
                                          background: index % 3 === 0 ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : 
                                                     index % 3 === 1 ? 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' :
                                                     'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                                          display: 'flex',
                                          alignItems: 'center',
                                          justifyContent: 'center',
                                          color: 'white'
                                        }}>
                                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                            <circle cx="12" cy="10" r="3"></circle>
                                          </svg>
                                        </div>
                                        <span>{station.name}</span>
                                      </div>
                                    </td>
                                    <td style={{ fontSize: '0.875rem', maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{station.address}</td>
                                    <td style={{ fontSize: '0.75rem', fontFamily: 'monospace', color: '#9ca3af' }}>{station.latitude}, {station.longitude}</td>
                                    <td>
                                      <span className={`badge ${station.status === 'active' ? 'badge-success' : 'badge-secondary'}`}>
                                        {systemConfig?.stationStatuses?.statuses?.find((s: any) => s.value === station.status)?.label || 
                                         (station.status === 'active' ? 'Hoạt động' : 'Bảo trì')}
                                      </span>
                                    </td>
                                    <td>{new Date(station.created_at).toLocaleDateString('vi-VN')}</td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        ) : (
                          <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
                            <div style={{ 
                              width: '80px', 
                              height: '80px', 
                              margin: '0 auto 1.5rem',
                              borderRadius: '50%', 
                              backgroundColor: '#f3f4f6',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center'
                            }}>
                              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2">
                                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                                <circle cx="12" cy="10" r="3"></circle>
                              </svg>
                            </div>
                            <h2 style={{ marginBottom: '0.5rem' }}>Chưa có trạm nào</h2>
                            <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
                              Thêm trạm đổi pin đầu tiên vào hệ thống
                            </p>
                            <button 
                              className="btn btn-primary"
                              onClick={() => openModal('station')}
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                                <line x1="12" y1="5" x2="12" y2="19"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                              </svg>
                              Thêm trạm đầu tiên
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </>
                )}
              </>
            )}
            </div>
        </div>
        {/* Right Sidebar - User Info (for all roles in dashboard) */}
        {/* Right Sidebar */}
          <div className={`right-sidebar ${showRightSidebar ? 'open' : ''}`}>
            <div className="right-sidebar-header">
              <h2 className="right-sidebar-title">Thông tin tài khoản</h2>
              <button className="close-sidebar-btn" onClick={() => setShowRightSidebar(false)}>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>

            <div className="right-sidebar-content">
              {/* User Profile Section */}
              <div className="user-profile-section">
                <div className="user-profile-avatar">
                  {currentUser.full_name?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                <h3 className="user-profile-name">{currentUser.full_name}</h3>
                <p className="user-profile-email">{currentUser.email}</p>
                <span className="user-profile-badge">
                  {currentUser.role === 'admin' ? (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.25rem' }}>
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                      </svg>
                      Admin
                    </>
                  ) : currentUser.role === 'staff' ? (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.25rem' }}>
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                      Staff
                    </>
                  ) : (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: '0.25rem' }}>
                        <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                        <circle cx="7" cy="17" r="2"></circle>
                        <circle cx="17" cy="17" r="2"></circle>
                      </svg>
                      Tài xế
                    </>
                  )}
                </span>
              </div>

              {/* Driver Information */}
              {currentUser.role === 'driver' && (
                <div className="sidebar-section">
                  <h3 className="sidebar-section-title">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    Thông tin tài xế
                  </h3>

                  <div className="sidebar-info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <div className="sidebar-info-item-content">
                      <div className="sidebar-info-label">Họ và tên</div>
                      <div className="sidebar-info-value">{currentUser.full_name}</div>
                    </div>
                  </div>

                  <div className="sidebar-info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                      <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                    </svg>
                    <div className="sidebar-info-item-content">
                      <div className="sidebar-info-label">Email</div>
                      <div className="sidebar-info-value" style={{ fontSize: '0.875rem', wordBreak: 'break-all' }}>{currentUser.email}</div>
                    </div>
                  </div>

                  <div className="sidebar-info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                    </svg>
                    <div className="sidebar-info-item-content">
                      <div className="sidebar-info-label">Số điện thoại</div>
                      <div className="sidebar-info-value">{currentUser.phone}</div>
                    </div>
                  </div>

                  <div className="sidebar-info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                      <path d="M7 15h0M2 9.5h20"></path>
                    </svg>
                    <div className="sidebar-info-item-content">
                      <div className="sidebar-info-label">Bằng lái xe</div>
                      <div className="sidebar-info-value" style={{ color: '#9ca3af' }}>Chưa cập nhật</div>
                    </div>
                  </div>
                </div>
              )}

              {/* Vehicle Information for Driver */}
              {currentUser.role === 'driver' && vehicles.length > 0 && (
                <div className="sidebar-section">
                  <h3 className="sidebar-section-title">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                      <circle cx="7" cy="17" r="2"></circle>
                      <circle cx="17" cy="17" r="2"></circle>
                    </svg>
                    Thông tin xe
                  </h3>

                  <div className="sidebar-info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M14 16H9m10 0h3v-3.15a1 1 0 0 0-.84-.99L16 11l-2.7-3.6a1 1 0 0 0-.8-.4H5.24a2 2 0 0 0-1.8 1.1l-.8 1.63A6 6 0 0 0 2 12.42V16h2"></path>
                      <circle cx="6.5" cy="16.5" r="2.5"></circle>
                      <circle cx="16.5" cy="16.5" r="2.5"></circle>
                    </svg>
                    <div className="sidebar-info-item-content">
                      <div className="sidebar-info-label">Model xe</div>
                      <div className="sidebar-info-value">{vehicles[0].model}</div>
                    </div>
                  </div>

                  <div className="sidebar-info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                      <path d="M7 15h10M7 11h10"></path>
                    </svg>
                    <div className="sidebar-info-item-content">
                      <div className="sidebar-info-label">Biển số</div>
                      <div className="sidebar-info-value">{vehicles[0].plate_number}</div>
                    </div>
                  </div>

                  <div className="sidebar-info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
                      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
                    </svg>
                    <div className="sidebar-info-item-content">
                      <div className="sidebar-info-label">Số VIN</div>
                      <div className="sidebar-info-value" style={{ fontSize: '0.75rem', fontFamily: 'monospace' }}>{vehicles[0].vin}</div>
                    </div>
                  </div>
                </div>
              )}

              {/* Status & Balance */}
              <div className="sidebar-section">
                <h3 className="sidebar-section-title">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="12" y1="1" x2="12" y2="23"></line>
                    <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                  </svg>
                  Tài chính
                </h3>

                <div className="sidebar-info-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                  <div className="sidebar-info-item-content">
                    <div className="sidebar-info-label">Trạng thái</div>
                    <div className="sidebar-info-value" style={{ color: '#10b981' }}>Hoạt động</div>
                  </div>
                </div>

                <div className="sidebar-info-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="12" y1="1" x2="12" y2="23"></line>
                    <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                  </svg>
                  <div className="sidebar-info-item-content">
                    <div className="sidebar-info-label">Số dư ví</div>
                    <div className="sidebar-balance">0 VND</div>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="sidebar-section">
                <h3 className="sidebar-section-title">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="M12 1v6m0 6v6m5.2-13.2l-4.2 4.2m0 6l-4.2 4.2m13.2-5.2h-6m-6 0H1m5.2 5.2l4.2-4.2m0-6l4.2-4.2"></path>
                  </svg>
                  Cài đặt
                </h3>

                <button className="sidebar-info-item" onClick={() => {
                  setEditFullName(currentUser.full_name);
                  setEditEmail(currentUser.email);
                  setEditPhone(currentUser.phone);
                  setShowProfileModal(true);
                }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  <div className="sidebar-info-item-content">
                    <div className="sidebar-info-value">Chỉnh sửa hồ sơ</div>
                  </div>
                </button>

                <button className="sidebar-info-item" onClick={() => setShowSettingsModal(true)}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="M12 1v6m0 6v6m5.2-13.2l-4.2 4.2m0 6l-4.2 4.2m13.2-5.2h-6m-6 0H1m5.2 5.2l4.2-4.2m0-6l4.2-4.2"></path>
                  </svg>
                  <div className="sidebar-info-item-content">
                    <div className="sidebar-info-value">Cài đặt</div>
                  </div>
                </button>

                <button className="sidebar-info-item" onClick={() => setShowSecurityModal(true)}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                  </svg>
                  <div className="sidebar-info-item-content">
                    <div className="sidebar-info-value">Bảo mật</div>
                  </div>
                </button>
              </div>

              {/* Logout */}
              <div className="sidebar-section">
                <button className="logout-button-full" onClick={handleLogout}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                  </svg>
                  <span>Đăng xuất</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Old Admin Dashboard - needs to be wrapped in new layout too */}
      {false && currentPage === 'dashboard' && currentUser && (
        <div className="dashboard">
          <div className="dashboard-container">
            {/* Admin Dashboard */}
            {currentUser.role === 'admin' && (
              <>
                <div className="dashboard-header">
                  <h1>Bảng điều khiển Admin</h1>
                  <p>Quản lý toàn bộ hệ thống EV SWAP</p>
                </div>

                <div className="stats-grid">
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Tổng Driver</span>
                    </div>
                    <div className="stat-value">{users.filter(u => u.role === 'driver').length}</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Tổng Staff</span>
                    </div>
                    <div className="stat-value">{users.filter(u => u.role === 'staff').length}</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Trạm đổi pin</span>
                    </div>
                    <div className="stat-value">{stations.length}</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Tổng người dùng</span>
                    </div>
                    <div className="stat-value">{users.length}</div>
                  </div>
                </div>

                <div className="tabs">
                  <div className="tabs-list">
                    <button 
                      className={`tab-trigger ${activeTab === 'users' ? 'active' : ''}`}
                      onClick={() => setActiveTab('users')}
                    >
                      Người dùng
                    </button>
                    <button 
                      className={`tab-trigger ${activeTab === 'stations' ? 'active' : ''}`}
                      onClick={() => setActiveTab('stations')}
                    >
                      Trạm đổi pin
                    </button>
                  </div>

                  <div className={`tab-content ${activeTab === 'users' ? 'active' : ''}`}>
                    <div className="card">
                      <div className="card-header">
                        <div className="card-actions">
                          <div>
                            <h2 className="card-title">Quản lý người dùng</h2>
                            <p className="card-description">Quản lý tài khoản Driver và Staff</p>
                          </div>
                          <button className="btn btn-primary" onClick={() => openModal('staff')}>
                            ➕ Tạo Staff
                          </button>
                        </div>
                      </div>
                      <div className="card-content">
                        <div className="table-container">
                          <table className="table">
                            <thead>
                              <tr>
                                <th>Họ tên</th>
                                <th>Email</th>
                                <th>Điện thoại</th>
                                <th>Vai trò</th>
                                <th>Ngày tạo</th>
                              </tr>
                            </thead>
                            <tbody>
                              {users.map((user) => (
                                <tr key={user.id}>
                                  <td>{user.full_name}</td>
                                  <td>{user.email}</td>
                                  <td>{user.phone}</td>
                                  <td>
                                    <span className={`badge ${user.role === 'admin' ? 'badge-primary' : user.role === 'staff' ? 'badge-secondary' : 'badge-outline'}`}>
                                      {user.role === 'admin' ? 'Admin' : user.role === 'staff' ? 'Staff' : 'Driver'}
                                    </span>
                                  </td>
                                  <td>{new Date(user.created_at).toLocaleDateString('vi-VN')}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className={`tab-content ${activeTab === 'stations' ? 'active' : ''}`}>
                    <div className="card">
                      <div className="card-header">
                        <div className="card-actions">
                          <div>
                            <h2 className="card-title">Quản lý trạm đổi pin</h2>
                            <p className="card-description">Danh sách các trạm đổi pin trong hệ thống</p>
                          </div>
                          <button className="btn btn-primary" onClick={() => openModal('station')}>
                            ➕ Tạo trạm mới
                          </button>
                        </div>
                      </div>
                      <div className="card-content">
                        <div className="table-container">
                          <table className="table">
                            <thead>
                              <tr>
                                <th>Tên trạm</th>
                                <th>Địa chỉ</th>
                                <th>Tọa độ</th>
                                <th>Trạng thái</th>
                                <th>Ngày tạo</th>
                              </tr>
                            </thead>
                            <tbody>
                              {stations.map((station) => (
                                <tr key={station.id}>
                                  <td>{station.name}</td>
                                  <td>{station.address}</td>
                                  <td style={{ fontSize: '0.875rem' }}>{station.latitude}, {station.longitude}</td>
                                  <td>
                                    <span className="badge badge-primary">Hoạt động</span>
                                  </td>
                                  <td>{new Date(station.created_at).toLocaleDateString('vi-VN')}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </>
            )}

            {/* Staff Dashboard */}
            {currentUser.role === 'staff' && (
              <>
                <div className="dashboard-header">
                  <h1>Bảng điều khiển Staff</h1>
                  <p>Xin chào, {currentUser.full_name} - Nhân viên trạm đổi pin</p>
                </div>

                <div className="stats-grid">
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Trạm hoạt động</span>
                    </div>
                    <div className="stat-value">{stations.length}</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Giao dịch</span>
                    </div>
                    <div className="stat-value">{transactions.length}</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Yêu cầu hỗ trợ</span>
                    </div>
                    <div className="stat-value">{tickets.length}</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-header">
                      <span className="stat-title">Ticket đang mở</span>
                    </div>
                    <div className="stat-value">{tickets.filter(t => t.status === 'open').length}</div>
                  </div>
                </div>

                <div className="tabs">
                  <div className="tabs-list">
                    <button 
                      className={`tab-trigger ${activeTab === 'tickets' ? 'active' : ''}`}
                      onClick={() => setActiveTab('tickets')}
                    >
                      Hỗ trợ kỹ thuật
                    </button>
                    <button 
                      className={`tab-trigger ${activeTab === 'transactions' ? 'active' : ''}`}
                      onClick={() => setActiveTab('transactions')}
                    >
                      Giao dịch
                    </button>
                    <button 
                      className={`tab-trigger ${activeTab === 'stations' ? 'active' : ''}`}
                      onClick={() => setActiveTab('stations')}
                    >
                      Trạm đổi pin
                    </button>
                  </div>

                  <div className={`tab-content ${activeTab === 'tickets' ? 'active' : ''}`}>
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">Quản lý yêu cầu hỗ trợ</h2>
                        <p className="card-description">Xử lý các yêu cầu hỗ trợ từ Driver</p>
                      </div>
                      <div className="card-content">
                        <div className="table-container">
                          <table className="table">
                            <thead>
                              <tr>
                                <th>Tiêu đề</th>
                                <th>Mô tả</th>
                                <th>Ngày tạo</th>
                                <th>Trạng thái</th>
                                <th>Hành động</th>
                              </tr>
                            </thead>
                            <tbody>
                              {tickets.map((ticket) => (
                                <tr key={ticket.id}>
                                  <td>{ticket.subject}</td>
                                  <td style={{ maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                    {ticket.description}
                                  </td>
                                  <td>{new Date(ticket.created_at).toLocaleString('vi-VN')}</td>
                                  <td>
                                    <span className={`badge ${ticket.status === 'open' ? 'badge-primary' : 'badge-secondary'}`}>
                                      {ticket.status === 'open' ? 'Đang mở' : 
                                       ticket.status === 'resolved' ? 'Đã giải quyết' : 'Đóng'}
                                    </span>
                                  </td>
                                  <td>
                                    <select 
                                      className="form-select" 
                                      value={ticket.status}
                                      style={{ width: '130px', fontSize: '0.875rem', padding: '0.375rem 2rem 0.375rem 0.5rem' }}
                                      onChange={(e) => handleUpdateTicketStatus(ticket.id, e.target.value)}
                                    >
                                      <option value="open">Đang mở</option>
                                      <option value="resolved">Đã giải quyết</option>
                                      <option value="closed">Đóng</option>
                                    </select>
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                          {tickets.length === 0 && (
                            <p className="empty-state">Chưa có yêu cầu hỗ trợ nào</p>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className={`tab-content ${activeTab === 'transactions' ? 'active' : ''}`}>
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">Lịch sử giao dịch đổi pin</h2>
                        <p className="card-description">Tất cả giao dịch đổi pin trong hệ thống</p>
                      </div>
                      <div className="card-content">
                        <div className="table-container">
                          <table className="table">
                            <thead>
                              <tr>
                                <th>ID Giao dịch</th>
                                <th>Thời gian</th>
                                <th>Trạng thái</th>
                              </tr>
                            </thead>
                            <tbody>
                              {transactions.map((transaction) => (
                                <tr key={transaction.id}>
                                  <td style={{ fontSize: '0.875rem' }}>{transaction.id.substring(0, 8)}...</td>
                                  <td>{new Date(transaction.timestamp).toLocaleString('vi-VN')}</td>
                                  <td>
                                    <span className="badge badge-primary">Hoàn thành</span>
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                          {transactions.length === 0 && (
                            <p className="empty-state">Chưa có giao dịch nào</p>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className={`tab-content ${activeTab === 'stations' ? 'active' : ''}`}>
                    <div className="card">
                      <div className="card-header">
                        <h2 className="card-title">Danh sách trạm đổi pin</h2>
                        <p className="card-description">Các trạm đổi pin trong hệ thống</p>
                      </div>
                      <div className="card-content">
                        {stations.map((station) => (
                          <div key={station.id} className="list-item">
                            <div className="flex justify-between">
                              <div style={{ flex: 1 }}>
                                <div className="flex items-center gap-2 mb-2">
                                  <span>📍</span>
                                  <h3 style={{ fontSize: '1.125rem' }}>{station.name}</h3>
                                </div>
                                <p style={{ color: '#6b7280', marginBottom: '0.5rem' }}>{station.address}</p>
                                <p style={{ fontSize: '0.875rem', color: '#9ca3af' }}>
                                  Tọa độ: {station.latitude}, {station.longitude}
                                </p>
                              </div>
                              <span className="badge badge-primary">Hoạt động</span>
                            </div>
                          </div>
                        ))}
                        {stations.length === 0 && (
                          <p className="empty-state">Chưa có trạm nào</p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      )}
      {/* Modals */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            {modalType === 'staff' && (
              <>
                <div className="modal-header">
                  <h2 className="modal-title">Tạo tài khoản Staff</h2>
                  <p className="modal-description">Tạo tài khoản mới cho nhân viên trạm đổi pin</p>
                </div>
                <div>
                  <div className="form-group">
                    <label className="form-label">Họ và tên</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="Nguyễn Văn B"
                      value={staffFullName}
                      onChange={(e) => setStaffFullName(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Email</label>
                    <input
                      type="email"
                      className="form-input"
                      placeholder="staff@evswap.com"
                      value={staffEmail}
                      onChange={(e) => setStaffEmail(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Số điện thoại</label>
                    <input
                      type="tel"
                      className="form-input"
                      placeholder="0123456789"
                      value={staffPhone}
                      onChange={(e) => setStaffPhone(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Mật khẩu</label>
                    <input
                      type="password"
                      className="form-input"
                      placeholder="••••••••"
                      value={staffPassword}
                      onChange={(e) => setStaffPassword(e.target.value)}
                    />
                  </div>
                  <button className="btn btn-primary" style={{ width: '100%' }} onClick={handleCreateStaff}>
                    Tạo tài khoản
                  </button>
                </div>
              </>
            )}

            {modalType === 'station' && (
              <>
                <div className="modal-header">
                  <h2 className="modal-title">Tạo trạm đổi pin mới</h2>
                  <p className="modal-description">Thêm trạm đổi pin mới vào hệ thống</p>
                </div>
                <div>
                  <div className="form-group">
                    <label className="form-label">Tên trạm</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder={systemConfig?.stationNameTemplates?.templates?.[0] || "Trạm đổi pin Quận 1"}
                      value={stationName}
                      onChange={(e) => setStationName(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Địa chỉ</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="123 Nguyễn Huệ, Quận 1, TP.HCM"
                      value={stationAddress}
                      onChange={(e) => setStationAddress(e.target.value)}
                    />
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                    <div className="form-group">
                      <label className="form-label">Vĩ độ</label>
                      <input
                        type="number"
                        step="any"
                        className="form-input"
                        placeholder="10.762622"
                        value={stationLat}
                        onChange={(e) => setStationLat(e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Kinh độ</label>
                      <input
                        type="number"
                        step="any"
                        className="form-input"
                        placeholder="106.660172"
                        value={stationLng}
                        onChange={(e) => setStationLng(e.target.value)}
                      />
                    </div>
                  </div>
                  <button className="btn btn-primary" style={{ width: '100%' }} onClick={handleCreateStation}>
                    Tạo trạm
                  </button>
                </div>
              </>
            )}

            {modalType === 'ticket' && (
              <>
                <div className="modal-header">
                  <h2 className="modal-title">Tạo yêu cầu hỗ trợ</h2>
                  <p className="modal-description">Mô tả vấn đề bạn gặp phải, đội ngũ hỗ trợ sẽ liên hệ sớm nhất</p>
                </div>
                <div>
                  <div className="form-group">
                    <label className="form-label">Tiêu đề</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="Vấn đề về..."
                      value={ticketSubject}
                      onChange={(e) => setTicketSubject(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Mô tả chi tiết</label>
                    <textarea
                      className="form-textarea"
                      placeholder="Mô tả vấn đề của bạn..."
                      value={ticketDescription}
                      onChange={(e) => setTicketDescription(e.target.value)}
                      rows={4}
                    />
                  </div>
                  <button className="btn btn-primary" style={{ width: '100%' }} onClick={handleCreateTicket}>
                    Gửi yêu cầu
                  </button>
                </div>
              </>
            )}

            {modalType === 'vehicle' && (
              <>
                <div className="modal-header">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                    <div style={{ 
                      width: '64px', 
                      height: '64px', 
                      borderRadius: '16px', 
                      background: 'linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      boxShadow: '0 8px 24px rgba(167, 139, 250, 0.3)'
                    }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                        <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path>
                        <circle cx="7" cy="17" r="2"></circle>
                        <circle cx="17" cy="17" r="2"></circle>
                      </svg>
                    </div>
                    <div>
                      <h2 className="modal-title" style={{ marginBottom: '0.25rem' }}>Thêm xe mới</h2>
                      <p className="modal-description" style={{ margin: 0 }}>Đăng ký thêm xe điện vào tài khoản của bạn</p>
                    </div>
                  </div>
                </div>
                <div style={{ 
                  padding: '2rem', 
                  maxHeight: '70vh', 
                  overflowY: 'auto',
                  display: 'flex',
                  flexDirection: 'column'
                }}>
                  {/* Info Box */}
                  <div style={{ 
                    padding: '1rem 1.25rem', 
                    background: 'rgba(167, 139, 250, 0.1)', 
                    borderRadius: '12px',
                    border: '1px solid rgba(167, 139, 250, 0.2)',
                    marginBottom: '1.5rem',
                    display: 'flex',
                    alignItems: 'start',
                    gap: '0.75rem',
                    flexShrink: 0
                  }}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="16" x2="12" y2="12"></line>
                      <line x1="12" y1="8" x2="12.01" y2="8"></line>
                    </svg>
                    <p style={{ fontSize: '0.875rem', color: '#c4b5fd', margin: 0, lineHeight: '1.5' }}>
                      Thông tin xe sẽ được sử dụng để xác nhận khi đổi pin tại trạm. Vui lòng nhập chính xác.
                    </p>
                  </div>

                  <div className="form-group">
                    <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M14 16H9m10 0h3v-3.15a1 1 0 0 0-.84-.99L16 11l-2.7-3.6a1 1 0 0 0-.8-.4H5.24a2 2 0 0 0-1.8 1.1l-.8 1.63A6 6 0 0 0 2 12.42V16h2"></path>
                        <circle cx="6.5" cy="16.5" r="2.5"></circle>
                        <circle cx="16.5" cy="16.5" r="2.5"></circle>
                      </svg>
                      Model xe
                    </label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder={systemConfig?.vehicleModels?.models?.slice(0, 3).join(', ') || "VD: VinFast Klara, Yadea G5, Pega..."}
                      value={newVehicleModel}
                      onChange={(e) => setNewVehicleModel(e.target.value)}
                      style={{ fontSize: '0.9375rem' }}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
                        <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
                      </svg>
                      Số VIN (Số khung xe)
                    </label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="VD: VF1XXXXXXXXXXXXXXX"
                      value={newVehicleVin}
                      onChange={(e) => setNewVehicleVin(e.target.value)}
                      style={{ fontSize: '0.9375rem', fontFamily: 'monospace' }}
                    />
                    <p style={{ fontSize: '0.8125rem', color: '#9ca3af', marginTop: '0.5rem', marginLeft: '0.25rem' }}>
                      Vehicle Identification Number - Số định danh xe
                    </p>
                  </div>

                  <div className="form-group">
                    <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                        <path d="M7 15h10M7 11h10"></path>
                      </svg>
                      Biển số xe
                    </label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="VD: 29A1-12345 hoặc 51F1-67890"
                      value={newVehiclePlateNumber}
                      onChange={(e) => setNewVehiclePlateNumber(e.target.value)}
                      style={{ fontSize: '0.9375rem', fontFamily: 'monospace' }}
                    />
                  </div>

                  {/* Preview Card */}
                  {(newVehicleModel || newVehicleVin || newVehiclePlateNumber) && (
                    <div style={{ 
                      marginTop: '1.5rem',
                      padding: '1.25rem', 
                      background: 'rgba(255, 255, 255, 0.03)', 
                      borderRadius: '12px',
                      border: '1px solid rgba(255, 255, 255, 0.1)'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" strokeWidth="2">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                          <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                        <span style={{ fontSize: '0.875rem', color: '#9ca3af', fontWeight: '500' }}>Xem trước thông tin</span>
                      </div>
                      <div style={{ display: 'grid', gap: '0.75rem' }}>
                        {newVehicleModel && (
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Model:</span>
                            <span style={{ fontSize: '0.875rem', color: '#e5e7eb', fontWeight: '500' }}>{newVehicleModel}</span>
                          </div>
                        )}
                        {newVehicleVin && (
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Số VIN:</span>
                            <span style={{ fontSize: '0.8125rem', color: '#e5e7eb', fontWeight: '500', fontFamily: 'monospace' }}>{newVehicleVin}</span>
                          </div>
                        )}
                        {newVehiclePlateNumber && (
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ fontSize: '0.875rem', color: '#9ca3af' }}>Biển số:</span>
                            <span style={{ fontSize: '0.875rem', color: '#e5e7eb', fontWeight: '500', fontFamily: 'monospace' }}>{newVehiclePlateNumber}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  <div style={{ marginTop: 'auto', paddingTop: '1.5rem', flexShrink: 0 }}>
                    <button 
                      className="btn btn-primary" 
                      style={{ width: '100%', padding: '0.875rem', fontSize: '0.9375rem' }} 
                      onClick={handleAddNewVehicle}
                      disabled={isAddingVehicle || !newVehicleModel || !newVehicleVin || !newVehiclePlateNumber}
                    >
                      {isAddingVehicle ? (
                        <>
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem', animation: 'spin 1s linear infinite' }}>
                            <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
                          </svg>
                          Đang thêm xe...
                        </>
                      ) : (
                        <>
                          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                            <polyline points="22 4 12 14.01 9 11.01"></polyline>
                          </svg>
                          Hoàn tất thêm xe
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </>
            )}

            {modalType === 'feedback' && selectedTransactionForFeedback && (
              <>
                <div className="modal-header">
                  <h2 className="modal-title">Đánh giá dịch vụ</h2>
                  <p className="modal-description">Chia sẻ trải nghiệm của bạn về dịch vụ đổi pin</p>
                </div>
                <div>
                  {/* Transaction Info */}
                  <div style={{ padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '8px', marginBottom: '1.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <rect x="2" y="7" width="20" height="15" rx="2" ry="2"></rect>
                        <polyline points="17 2 12 7 7 2"></polyline>
                      </svg>
                      <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Giao dịch đổi pin</span>
                    </div>
                    <p style={{ fontSize: '0.875rem' }}>
                      {new Date(selectedTransactionForFeedback.timestamp).toLocaleString('vi-VN')}
                    </p>
                  </div>

                  {/* Rating */}
                  <div className="form-group">
                    <label className="form-label">Đánh giá</label>
                    <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center', padding: '1rem 0' }}>
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          onClick={() => setFeedbackRating(star)}
                          style={{ 
                            background: 'none', 
                            border: 'none', 
                            cursor: 'pointer',
                            padding: '0.5rem',
                            transition: 'transform 0.2s'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.2)'}
                          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                        >
                          <svg 
                            xmlns="http://www.w3.org/2000/svg" 
                            width="32" 
                            height="32" 
                            viewBox="0 0 24 24" 
                            fill={star <= feedbackRating ? '#fbbf24' : 'none'}
                            stroke={star <= feedbackRating ? '#fbbf24' : '#d1d5db'}
                            strokeWidth="2"
                          >
                            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                          </svg>
                        </button>
                      ))}
                    </div>
                    <p style={{ textAlign: 'center', color: '#6b7280', fontSize: '0.875rem' }}>
                      {feedbackRating === 1 && 'Rất không hài lòng'}
                      {feedbackRating === 2 && 'Không hài lòng'}
                      {feedbackRating === 3 && 'Bình thường'}
                      {feedbackRating === 4 && 'Hài lòng'}
                      {feedbackRating === 5 && 'Rất hài lòng'}
                    </p>
                  </div>

                  {/* Comment */}
                  <div className="form-group">
                    <label className="form-label">Nhận xét (không bắt buộc)</label>
                    <textarea
                      className="form-textarea"
                      placeholder="Chia sẻ thêm về trải nghiệm của bạn..."
                      value={feedbackComment}
                      onChange={(e) => setFeedbackComment(e.target.value)}
                      rows={4}
                    />
                  </div>

                  <button 
                    className="btn btn-primary" 
                    style={{ width: '100%' }} 
                    onClick={async () => {
                      try {
                        // Get station from transaction (mock for now, would come from actual transaction data)
                        const stationId = stations.length > 0 ? stations[0].id : 'unknown';
                        
                        const response = await fetch(
                          'http://localhost:8080/api/v1/feedbacks',
                          {
                            method: 'POST',
                            headers: {
                              'Authorization': `Bearer ${accessToken}`,
                              'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                              transaction_id: selectedTransactionForFeedback.id,
                              station_id: stationId,
                              rating: feedbackRating,
                              comment: feedbackComment
                            }),
                          }
                        );

                        const data = await response.json();
                        if (data.success) {
                          showToast(`Cảm ơn bạn đã đánh giá ${feedbackRating} sao!`, 'success');
                          loadFeedbacks(); // Reload feedbacks
                          setShowModal(false);
                          setSelectedTransactionForFeedback(null);
                          setFeedbackRating(5);
                          setFeedbackComment('');
                        } else {
                          showToast(data.error || 'Có lỗi xảy ra', 'error');
                        }
                      } catch (error) {
                        showToast('Có lỗi xảy ra khi gửi đánh giá', 'error');
                      }
                    }}
                  >
                    Gửi đánh giá
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
      {/* Payment Modal */}
      {showPaymentModal && (
        <div className="modal-overlay" onClick={() => setShowPaymentModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '650px', maxHeight: '90vh', display: 'flex', flexDirection: 'column' }}>
            <div className="modal-header" style={{ position: 'relative', flexShrink: 0 }}>
              <div>
                <h2 className="modal-title">💳 Nạp tiền vào ví</h2>
                <p className="modal-description">Chọn phương thức thanh toán phù hợp với bạn</p>
              </div>
              <button 
                onClick={() => setShowPaymentModal(false)}
                style={{
                  position: 'absolute',
                  top: '1.5rem',
                  right: '1.5rem',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '0.5rem',
                  borderRadius: '6px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'background-color 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
            <div style={{ padding: '0 2rem', overflowY: 'auto', flex: 1 }}>
              {/* Transaction Summary */}
              <div style={{ padding: '1.5rem', background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', borderRadius: '12px', color: 'white', marginBottom: '1.5rem', boxShadow: '0 4px 12px rgba(16, 185, 129, 0.25)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <div>
                    <p style={{ opacity: 0.9, marginBottom: '0.25rem', fontSize: '0.875rem' }}>Số tiền nạp</p>
                    <h2 style={{ fontSize: '2.25rem', margin: 0 }}>{parseFloat(topUpAmount).toLocaleString('vi-VN')}đ</h2>
                  </div>
                  <div style={{ 
                    width: '56px', 
                    height: '56px', 
                    borderRadius: '12px', 
                    backgroundColor: 'rgba(255,255,255,0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="12" y1="1" x2="12" y2="23"></line>
                      <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                    </svg>
                  </div>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '1rem', borderTop: '1px solid rgba(255,255,255,0.2)' }}>
                  <span style={{ opacity: 0.9, fontSize: '0.875rem' }}>Mã giao dịch</span>
                  <span style={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>{paymentTransactionId}</span>
                </div>
              </div>

              {/* Payment Method Selection */}
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label className="form-label" style={{ marginBottom: '0.75rem', display: 'block' }}>Chọn phương thức thanh toán</label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
                  {[
                    { id: 'momo', name: 'MoMo', color: '#A50064', icon: 'M', desc: 'Ví điện tử' },
                    { id: 'zalopay', name: 'ZaloPay', color: '#0068FF', icon: 'Z', desc: 'Ví điện tử' },
                    { id: 'bank', name: 'Ngân hàng', color: '#10b981', icon: '🏦', desc: 'Chuyển khoản' },
                    { id: 'cash', name: 'Tiền mặt', color: '#f59e0b', icon: '💵', desc: 'Tại trạm' }
                  ].map((method) => (
                    <button
                      key={method.id}
                      onClick={() => setSelectedPaymentMethod(method.id)}
                      style={{
                        padding: '1rem',
                        borderRadius: '12px',
                        border: selectedPaymentMethod === method.id ? `2px solid ${method.color}` : '2px solid #e5e7eb',
                        backgroundColor: selectedPaymentMethod === method.id ? `${method.color}15` : '#ffffff',
                        cursor: 'pointer',
                        transition: 'all 0.2s',
                        textAlign: 'center',
                        position: 'relative',
                        overflow: 'hidden'
                      }}
                      onMouseEnter={(e) => {
                        if (selectedPaymentMethod !== method.id) {
                          e.currentTarget.style.borderColor = method.color;
                          e.currentTarget.style.transform = 'translateY(-2px)';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (selectedPaymentMethod !== method.id) {
                          e.currentTarget.style.borderColor = '#e5e7eb';
                          e.currentTarget.style.transform = 'translateY(0)';
                        }
                      }}
                    >
                      {selectedPaymentMethod === method.id && (
                        <div style={{
                          position: 'absolute',
                          top: '0.5rem',
                          right: '0.5rem',
                          width: '20px',
                          height: '20px',
                          borderRadius: '50%',
                          backgroundColor: method.color,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center'
                        }}>
                          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3">
                            <polyline points="20 6 9 17 4 12"></polyline>
                          </svg>
                        </div>
                      )}
                      <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
                        {method.id === 'bank' || method.id === 'cash' ? method.icon : (
                          <div style={{ 
                            width: '48px', 
                            height: '48px', 
                            margin: '0 auto',
                            borderRadius: '10px', 
                            backgroundColor: method.color,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: 'white',
                            fontSize: '1.75rem',
                            fontWeight: 'bold'
                          }}>
                            {method.icon}
                          </div>
                        )}
                      </div>
                      <p style={{ fontSize: '0.9375rem', fontWeight: '600', color: selectedPaymentMethod === method.id ? method.color : '#1f2937', marginBottom: '0.25rem' }}>
                        {method.name}
                      </p>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', margin: 0 }}>
                        {method.desc}
                      </p>
                    </button>
                  ))}
                </div>
              </div>

              {/* QR Code Section - Only show for non-cash methods */}
              {selectedPaymentMethod !== 'cash' && (
                <div style={{ padding: '2rem', backgroundColor: '#f9fafb', borderRadius: '12px', textAlign: 'center', marginBottom: '1.5rem', border: '1px solid #e5e7eb' }}>
                  <div style={{ 
                    width: '200px', 
                    height: '200px', 
                    margin: '0 auto 1rem',
                    backgroundColor: 'white',
                    borderRadius: '12px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    border: '2px dashed #d1d5db'
                  }}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2">
                      <rect x="3" y="3" width="7" height="7"></rect>
                      <rect x="14" y="3" width="7" height="7"></rect>
                      <rect x="14" y="14" width="7" height="7"></rect>
                      <rect x="3" y="14" width="7" height="7"></rect>
                    </svg>
                  </div>
                  <p style={{ color: '#6b7280', fontSize: '0.9375rem', marginBottom: '0.5rem', fontWeight: '500' }}>
                    Quét mã QR bằng ứng dụng {selectedPaymentMethod === 'momo' ? 'MoMo' : selectedPaymentMethod === 'zalopay' ? 'ZaloPay' : 'ngân hàng của bạn'}
                  </p>
                  <p style={{ color: '#9ca3af', fontSize: '0.8125rem' }}>
                    Mã QR sẽ được tạo khi tích hợp thanh toán thực tế
                  </p>
                </div>
              )}

              {/* Payment Info - Bank Transfer */}
              {selectedPaymentMethod === 'bank' && (
                <div style={{ padding: '1.5rem', backgroundColor: '#eff6ff', borderRadius: '12px', marginBottom: '1.5rem', border: '2px solid #bfdbfe' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                    <div style={{
                      width: '32px',
                      height: '32px',
                      borderRadius: '8px',
                      backgroundColor: '#3b82f6',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                        <rect x="2" y="5" width="20" height="14" rx="2"></rect>
                        <line x1="2" y1="10" x2="22" y2="10"></line>
                      </svg>
                    </div>
                    <h4 style={{ margin: 0, color: '#1e40af', fontSize: '1.125rem' }}>Thông tin chuyển khoản</h4>
                  </div>
                  <div style={{ display: 'grid', gap: '1rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', backgroundColor: 'white', borderRadius: '8px' }}>
                      <span style={{ color: '#6b7280', fontSize: '0.875rem', fontWeight: '500' }}>Ngân hàng</span>
                      <span style={{ fontSize: '0.9375rem', fontWeight: '600', color: '#1f2937' }}>Techcombank</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', backgroundColor: 'white', borderRadius: '8px' }}>
                      <span style={{ color: '#6b7280', fontSize: '0.875rem', fontWeight: '500' }}>Số tài khoản</span>
                      <span style={{ fontSize: '0.9375rem', fontFamily: 'monospace', fontWeight: '600', color: '#1f2937' }}>19036262888888</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', backgroundColor: 'white', borderRadius: '8px' }}>
                      <span style={{ color: '#6b7280', fontSize: '0.875rem', fontWeight: '500' }}>Chủ tài khoản</span>
                      <span style={{ fontSize: '0.9375rem', fontWeight: '600', color: '#1f2937' }}>CÔNG TY EV SWAP</span>
                    </div>
                    <div style={{ padding: '1rem', backgroundColor: '#fef3c7', borderRadius: '8px', border: '2px dashed #fbbf24' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                        <span style={{ color: '#92400e', fontSize: '0.875rem', fontWeight: '600' }}>Nội dung chuyển khoản</span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#92400e" strokeWidth="2">
                          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                          <line x1="12" y1="9" x2="12" y2="13"></line>
                          <line x1="12" y1="17" x2="12.01" y2="17"></line>
                        </svg>
                      </div>
                      <p style={{ fontSize: '1.125rem', fontFamily: 'monospace', color: '#ef4444', letterSpacing: '0.05em', fontWeight: '700', margin: 0 }}>
                        {paymentTransactionId}
                      </p>
                    </div>
                  </div>
                  <div style={{ marginTop: '1rem', padding: '0.75rem', backgroundColor: '#dbeafe', borderRadius: '8px', display: 'flex', gap: '0.75rem', alignItems: 'start' }}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1e40af" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="16" x2="12" y2="12"></line>
                      <line x1="12" y1="8" x2="12.01" y2="8"></line>
                    </svg>
                    <p style={{ fontSize: '0.8125rem', color: '#1e40af', margin: 0 }}>
                      Vui lòng ghi <strong>chính xác</strong> nội dung chuyển khoản để được nạp tiền tự động
                    </p>
                  </div>
                </div>
              )}

              {/* Payment Info - Cash */}
              {selectedPaymentMethod === 'cash' && (
                <div style={{ padding: '1.5rem', backgroundColor: '#fef3c7', borderRadius: '12px', marginBottom: '1.5rem', border: '2px solid #fbbf24' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
                    <div style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '8px',
                      backgroundColor: '#f59e0b',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}>
                      <span style={{ fontSize: '1.25rem' }}>💵</span>
                    </div>
                    <h4 style={{ margin: 0, color: '#92400e', fontSize: '1.125rem', fontWeight: '600' }}>
                      Hướng dẫn thanh toán tiền mặt
                    </h4>
                  </div>
                  
                  <div style={{ marginBottom: '1.25rem' }}>
                    {/* Step 1 */}
                    <div style={{ display: 'flex', alignItems: 'start', gap: '1rem', marginBottom: '1.25rem', padding: '1rem', backgroundColor: 'white', borderRadius: '10px' }}>
                      <div style={{ 
                        width: '32px', 
                        height: '32px', 
                        borderRadius: '50%', 
                        backgroundColor: '#f59e0b',
                        color: 'white',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '0.875rem',
                        fontWeight: '700',
                        flexShrink: 0
                      }}>
                        1
                      </div>
                      <div style={{ flex: 1 }}>
                        <p style={{ fontSize: '0.9375rem', color: '#92400e', marginBottom: '0.5rem', fontWeight: '600' }}>
                          Đến trạm đổi pin gần nhất
                        </p>
                        <p style={{ fontSize: '0.875rem', color: '#78350f', margin: 0 }}>
                          Sử dụng tab <strong>"Tìm trạm"</strong> để tìm trạm EV SWAP gần bạn nhất
                        </p>
                      </div>
                    </div>

                    {/* Step 2 */}
                    <div style={{ display: 'flex', alignItems: 'start', gap: '1rem', marginBottom: '1.25rem', padding: '1rem', backgroundColor: 'white', borderRadius: '10px' }}>
                      <div style={{ 
                        width: '32px', 
                        height: '32px', 
                        borderRadius: '50%', 
                        backgroundColor: '#f59e0b',
                        color: 'white',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '0.875rem',
                        fontWeight: '700',
                        flexShrink: 0
                      }}>
                        2
                      </div>
                      <div style={{ flex: 1 }}>
                        <p style={{ fontSize: '0.9375rem', color: '#92400e', marginBottom: '0.75rem', fontWeight: '600' }}>
                          Xuất trình mã giao dịch cho nhân viên
                        </p>
                        <div style={{ 
                          padding: '1rem', 
                          backgroundColor: '#fff7ed', 
                          borderRadius: '8px',
                          border: '2px dashed #fb923c',
                          textAlign: 'center'
                        }}>
                          <p style={{ fontSize: '0.75rem', color: '#9a3412', marginBottom: '0.5rem', fontWeight: '500', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                            Mã giao dịch
                          </p>
                          <p style={{ fontSize: '1.5rem', fontFamily: 'monospace', color: '#c2410c', letterSpacing: '0.15em', fontWeight: '700', margin: 0 }}>
                            {paymentTransactionId}
                          </p>
                        </div>
                      </div>
                    </div>

                    {/* Step 3 */}
                    <div style={{ display: 'flex', alignItems: 'start', gap: '1rem', padding: '1rem', backgroundColor: 'white', borderRadius: '10px' }}>
                      <div style={{ 
                        width: '32px', 
                        height: '32px', 
                        borderRadius: '50%', 
                        backgroundColor: '#f59e0b',
                        color: 'white',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '0.875rem',
                        fontWeight: '700',
                        flexShrink: 0
                      }}>
                        3
                      </div>
                      <div style={{ flex: 1 }}>
                        <p style={{ fontSize: '0.9375rem', color: '#92400e', marginBottom: '0.5rem', fontWeight: '600' }}>
                          Thanh toán tiền mặt
                        </p>
                        <p style={{ fontSize: '0.875rem', color: '#78350f', margin: 0 }}>
                          Thanh toán <strong style={{ fontSize: '1rem', color: '#92400e' }}>{parseFloat(topUpAmount).toLocaleString('vi-VN')}đ</strong> cho nhân viên trạm
                        </p>
                      </div>
                    </div>
                  </div>

                  <div style={{ 
                    padding: '1rem', 
                    backgroundColor: '#fed7aa', 
                    borderRadius: '8px',
                    display: 'flex',
                    alignItems: 'start',
                    gap: '0.75rem'
                  }}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#92400e" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="16" x2="12" y2="12"></line>
                      <line x1="12" y1="8" x2="12.01" y2="8"></line>
                    </svg>
                    <p style={{ fontSize: '0.8125rem', color: '#78350f', margin: 0, lineHeight: '1.5' }}>
                      Số tiền sẽ được nạp vào ví <strong>ngay lập tức</strong> sau khi nhân viên xác nhận thanh toán
                    </p>
                  </div>
                </div>
              )}

            </div>
            
            {/* Action Buttons - Fixed Footer */}
            <div style={{ padding: '1.5rem 2rem', borderTop: '1px solid #e5e7eb', flexShrink: 0, backgroundColor: 'white' }}>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <button 
                  className="btn btn-secondary" 
                  style={{ 
                    flex: 1,
                    padding: '0.875rem 1.5rem',
                    fontSize: '0.9375rem',
                    fontWeight: '600',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '0.5rem'
                  }}
                  onClick={() => {
                    setShowPaymentModal(false);
                    setTopUpAmount('');
                    setSelectedPaymentMethod('momo');
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                  Hủy bỏ
                </button>
                <button 
                  className="btn btn-primary" 
                  style={{ 
                    flex: 2,
                    padding: '0.875rem 1.5rem',
                    fontSize: '0.9375rem',
                    fontWeight: '600',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '0.5rem',
                    boxShadow: '0 4px 12px rgba(16, 185, 129, 0.25)'
                  }}
                  onClick={() => {
                    const methodName = selectedPaymentMethod === 'momo' ? 'MoMo' : 
                                      selectedPaymentMethod === 'zalopay' ? 'ZaloPay' : 
                                      selectedPaymentMethod === 'bank' ? 'Chuyển khoản ngân hàng' : 
                                      'Tiền mặt tại trạm';
                    
                    showToast(`Đang xử lý thanh toán qua ${methodName}...`, 'success');
                    setTimeout(() => {
                      if (selectedPaymentMethod === 'cash') {
                        showToast(`Vui lòng đến trạm để hoàn tất thanh toán ${parseFloat(topUpAmount).toLocaleString('vi-VN')}đ`, 'success');
                      } else {
                        showToast(`Nạp thành công ${parseFloat(topUpAmount).toLocaleString('vi-VN')}đ vào ví`, 'success');
                      }
                      setShowPaymentModal(false);
                      setTopUpAmount('');
                      setSelectedPaymentMethod('momo');
                    }, 2000);
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                  </svg>
                  {selectedPaymentMethod === 'cash' ? 'Xác nhận & Đi tới trạm' : 'Xác nhận thanh toán'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Profile Edit Modal */}
      {showProfileModal && (
        <div className="modal-overlay" onClick={() => setShowProfileModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                <div style={{ 
                  width: '64px', 
                  height: '64px', 
                  borderRadius: '16px', 
                  background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 8px 24px rgba(59, 130, 246, 0.3)'
                }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </div>
                <div>
                  <h2 className="modal-title" style={{ marginBottom: '0.25rem' }}>Chỉnh sửa hồ sơ</h2>
                  <p className="modal-description" style={{ margin: 0 }}>Cập nhật thông tin cá nhân của bạn</p>
                </div>
              </div>
            </div>
            <div style={{ padding: '2rem' }}>
              {/* Avatar Preview */}
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '2rem' }}>
                <div style={{ 
                  width: '100px', 
                  height: '100px', 
                  borderRadius: '50%', 
                  background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontSize: '2.5rem',
                  fontWeight: '600',
                  marginBottom: '1rem',
                  boxShadow: '0 8px 24px rgba(59, 130, 246, 0.3)'
                }}>
                  {editFullName ? editFullName?.charAt(0)?.toUpperCase() || 'U' : currentUser.full_name?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                  Ảnh đại diện sẽ được cập nhật trong phiên bản tiếp theo
                </p>
              </div>

              <div className="form-group">
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  Họ và tên
                </label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Nhập họ và tên"
                  value={editFullName}
                  onChange={(e) => setEditFullName(e.target.value)}
                  style={{ fontSize: '0.9375rem' }}
                />
              </div>

              <div className="form-group">
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                  </svg>
                  Email
                </label>
                <input
                  type="email"
                  className="form-input"
                  placeholder="email@example.com"
                  value={editEmail}
                  onChange={(e) => setEditEmail(e.target.value)}
                  style={{ fontSize: '0.9375rem' }}
                />
              </div>

              <div className="form-group">
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                  </svg>
                  Số điện thoại
                </label>
                <input
                  type="tel"
                  className="form-input"
                  placeholder="0123456789"
                  value={editPhone}
                  onChange={(e) => setEditPhone(e.target.value)}
                  style={{ fontSize: '0.9375rem' }}
                />
              </div>

              {/* Info Box */}
              <div style={{ 
                padding: '1rem 1.25rem', 
                background: 'rgba(59, 130, 246, 0.1)', 
                borderRadius: '12px',
                border: '1px solid rgba(59, 130, 246, 0.2)',
                marginTop: '1.5rem',
                display: 'flex',
                alignItems: 'start',
                gap: '0.75rem'
              }}>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="16" x2="12" y2="12"></line>
                  <line x1="12" y1="8" x2="12.01" y2="8"></line>
                </svg>
                <p style={{ fontSize: '0.875rem', color: '#93c5fd', margin: 0, lineHeight: '1.5' }}>
                  Thông tin của bạn sẽ được cập nhật ngay lập tức và áp dụng cho tất cả các dịch vụ
                </p>
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                <button 
                  className="btn btn-secondary" 
                  style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
                  onClick={() => {
                    setShowProfileModal(false);
                    setEditFullName('');
                    setEditEmail('');
                    setEditPhone('');
                  }}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                  </svg>
                  Quay lại
                </button>
                <button 
                  className="btn btn-primary" 
                  style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
                  onClick={async () => {
                    if (!editFullName || !editEmail || !editPhone) {
                      showToast('Vui lòng điền đầy đủ thông tin', 'error');
                      return;
                    }

                    setIsUpdatingProfile(true);
                    try {
                      // Simulate API call
                      await new Promise(resolve => setTimeout(resolve, 1500));
                      
                      // Update current user
                      setCurrentUser({
                        ...currentUser,
                        full_name: editFullName,
                        email: editEmail,
                        phone: editPhone
                      });
                      
                      showToast('Cập nhật hồ sơ thành công!', 'success');
                      setShowProfileModal(false);
                    } catch (error) {
                      showToast('Có lỗi xảy ra khi cập nhật hồ sơ', 'error');
                    } finally {
                      setIsUpdatingProfile(false);
                    }
                  }}
                  disabled={isUpdatingProfile}
                >
                  {isUpdatingProfile ? (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ animation: 'spin 1s linear infinite' }}>
                        <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
                      </svg>
                      Đang xử lý...
                    </>
                  ) : (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                      </svg>
                      Xác nhận
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
      {/* Settings Modal */}
      {showSettingsModal && (
        <div className="modal-overlay" onClick={() => setShowSettingsModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                <div style={{ 
                  width: '64px', 
                  height: '64px', 
                  borderRadius: '16px', 
                  background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 8px 24px rgba(139, 92, 246, 0.3)'
                }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="M12 1v6m0 6v6m5.2-13.2l-4.2 4.2m0 6l-4.2 4.2m13.2-5.2h-6m-6 0H1m5.2 5.2l4.2-4.2m0-6l4.2-4.2"></path>
                  </svg>
                </div>
                <div>
                  <h2 className="modal-title" style={{ marginBottom: '0.25rem' }}>Cài đặt ứng dụng</h2>
                  <p className="modal-description" style={{ margin: 0 }}>Tùy chỉnh trải nghiệm sử dụng của bạn</p>
                </div>
              </div>
            </div>
            <div style={{ padding: '2rem' }}>
              {/* Language Settings */}
              <div className="form-group">
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="2" y1="12" x2="22" y2="12"></line>
                    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
                  </svg>
                  Ngôn ngữ
                </label>
                <select
                  className="form-input"
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                  style={{ fontSize: '0.9375rem', cursor: 'pointer' }}
                >
                  <option value="vi">Tiếng Việt</option>
                  <option value="en">English</option>
                </select>
              </div>

              {/* Notifications Settings */}
              <div style={{ marginTop: '2rem' }}>
                <h3 style={{ fontSize: '1rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                  </svg>
                  Thông báo
                </h3>

                <div style={{ display: 'grid', gap: '1rem' }}>
                  {/* Push Notifications */}
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    padding: '1rem',
                    backgroundColor: 'rgba(255, 255, 255, 0.03)',
                    borderRadius: '12px',
                    border: '1px solid rgba(255, 255, 255, 0.1)'
                  }}>
                    <div>
                      <p style={{ fontSize: '0.9375rem', marginBottom: '0.25rem', fontWeight: '500' }}>Thông báo đẩy</p>
                      <p style={{ fontSize: '0.8125rem', color: '#9ca3af', margin: 0 }}>
                        Nhận thông báo về trạng thái đổi pin, ưu đãi
                      </p>
                    </div>
                    <label style={{ 
                      position: 'relative', 
                      display: 'inline-block', 
                      width: '48px', 
                      height: '26px',
                      cursor: 'pointer'
                    }}>
                      <input
                        type="checkbox"
                        checked={notificationsEnabled}
                        onChange={(e) => setNotificationsEnabled(e.target.checked)}
                        style={{ opacity: 0, width: 0, height: 0 }}
                      />
                      <span style={{
                        position: 'absolute',
                        cursor: 'pointer',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: notificationsEnabled ? '#8b5cf6' : '#374151',
                        transition: '0.4s',
                        borderRadius: '34px'
                      }}>
                        <span style={{
                          position: 'absolute',
                          content: '',
                          height: '18px',
                          width: '18px',
                          left: notificationsEnabled ? '26px' : '4px',
                          bottom: '4px',
                          backgroundColor: 'white',
                          transition: '0.4s',
                          borderRadius: '50%'
                        }}></span>
                      </span>
                    </label>
                  </div>

                  {/* Email Notifications */}
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    padding: '1rem',
                    backgroundColor: 'rgba(255, 255, 255, 0.03)',
                    borderRadius: '12px',
                    border: '1px solid rgba(255, 255, 255, 0.1)'
                  }}>
                    <div>
                      <p style={{ fontSize: '0.9375rem', marginBottom: '0.25rem', fontWeight: '500' }}>Thông báo email</p>
                      <p style={{ fontSize: '0.8125rem', color: '#9ca3af', margin: 0 }}>
                        Nhận email về giao dịch, hóa đơn
                      </p>
                    </div>
                    <label style={{ 
                      position: 'relative', 
                      display: 'inline-block', 
                      width: '48px', 
                      height: '26px',
                      cursor: 'pointer'
                    }}>
                      <input
                        type="checkbox"
                        checked={emailNotifications}
                        onChange={(e) => setEmailNotifications(e.target.checked)}
                        style={{ opacity: 0, width: 0, height: 0 }}
                      />
                      <span style={{
                        position: 'absolute',
                        cursor: 'pointer',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: emailNotifications ? '#8b5cf6' : '#374151',
                        transition: '0.4s',
                        borderRadius: '34px'
                      }}>
                        <span style={{
                          position: 'absolute',
                          content: '',
                          height: '18px',
                          width: '18px',
                          left: emailNotifications ? '26px' : '4px',
                          bottom: '4px',
                          backgroundColor: 'white',
                          transition: '0.4s',
                          borderRadius: '50%'
                        }}></span>
                      </span>
                    </label>
                  </div>
                </div>
              </div>

              {/* Info Box */}
              <div style={{ 
                padding: '1rem 1.25rem', 
                background: 'rgba(139, 92, 246, 0.1)', 
                borderRadius: '12px',
                border: '1px solid rgba(139, 92, 246, 0.2)',
                marginTop: '2rem',
                display: 'flex',
                alignItems: 'start',
                gap: '0.75rem'
              }}>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="16" x2="12" y2="12"></line>
                  <line x1="12" y1="8" x2="12.01" y2="8"></line>
                </svg>
                <p style={{ fontSize: '0.875rem', color: '#c4b5fd', margin: 0, lineHeight: '1.5' }}>
                  Các cài đặt sẽ được lưu tự động và áp dụng ngay lập tức
                </p>
              </div>

              <button 
                className="btn btn-primary" 
                style={{ width: '100%', marginTop: '2rem' }}
                onClick={() => {
                  showToast('Đã lưu cài đặt thành công!', 'success');
                  setShowSettingsModal(false);
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Security Modal */}
      {showSecurityModal && (
        <div className="modal-overlay" onClick={() => setShowSecurityModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                <div style={{ 
                  width: '64px', 
                  height: '64px', 
                  borderRadius: '16px', 
                  background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 8px 24px rgba(16, 185, 129, 0.3)'
                }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                  </svg>
                </div>
                <div>
                  <h2 className="modal-title" style={{ marginBottom: '0.25rem' }}>Bảo mật tài khoản</h2>
                  <p className="modal-description" style={{ margin: 0 }}>Thay đổi mật khẩu để bảo vệ tài khoản</p>
                </div>
              </div>
            </div>
            <div style={{ padding: '2rem' }}>
              {/* Security Info */}
              <div style={{ 
                padding: '1.25rem', 
                background: 'rgba(16, 185, 129, 0.1)', 
                borderRadius: '12px',
                border: '1px solid rgba(16, 185, 129, 0.2)',
                marginBottom: '2rem',
                display: 'flex',
                alignItems: 'start',
                gap: '0.75rem'
              }}>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }}>
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
                <div>
                  <p style={{ fontSize: '0.9375rem', color: '#6ee7b7', margin: 0, marginBottom: '0.5rem', fontWeight: '500' }}>
                    Bảo mật tài khoản
                  </p>
                  <p style={{ fontSize: '0.8125rem', color: '#a7f3d0', margin: 0, lineHeight: '1.5' }}>
                    Mật khẩu mạnh nên có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt
                  </p>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                  </svg>
                  Mật khẩu hiện tại
                </label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="Nhập mật khẩu hiện tại"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  style={{ fontSize: '0.9375rem' }}
                />
              </div>

              <div className="form-group">
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                  </svg>
                  Mật khẩu mới
                </label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="Nhập mật khẩu mới"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  style={{ fontSize: '0.9375rem' }}
                />
              </div>

              <div className="form-group">
                <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                  </svg>
                  Xác nhận mật khẩu mới
                </label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="Nhập lại mật khẩu mới"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  style={{ fontSize: '0.9375rem' }}
                />
              </div>

              {/* Password Strength Indicator */}
              {newPassword && (
                <div style={{ marginTop: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '0.8125rem', color: '#9ca3af' }}>Độ mạnh mật khẩu</span>
                    <span style={{ 
                      fontSize: '0.8125rem', 
                      fontWeight: '600',
                      color: newPassword.length >= 8 ? '#10b981' : newPassword.length >= 6 ? '#f59e0b' : '#ef4444'
                    }}>
                      {newPassword.length >= 8 ? 'Mạnh' : newPassword.length >= 6 ? 'Trung bình' : 'Yếu'}
                    </span>
                  </div>
                  <div style={{ width: '100%', height: '6px', backgroundColor: '#374151', borderRadius: '999px', overflow: 'hidden' }}>
                    <div style={{ 
                      width: `${Math.min((newPassword.length / 8) * 100, 100)}%`, 
                      height: '100%', 
                      background: newPassword.length >= 8 ? 
                        'linear-gradient(90deg, #10b981 0%, #059669 100%)' : 
                        newPassword.length >= 6 ? 
                        'linear-gradient(90deg, #f59e0b 0%, #d97706 100%)' : 
                        'linear-gradient(90deg, #ef4444 0%, #dc2626 100%)',
                      transition: 'width 0.3s ease'
                    }}></div>
                  </div>
                </div>
              )}

              {/* Password Requirements */}
              <div style={{ 
                marginTop: '1.5rem',
                padding: '1rem',
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
                borderRadius: '10px',
                border: '1px solid rgba(255, 255, 255, 0.1)'
              }}>
                <p style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '0.75rem', fontWeight: '500' }}>
                  Yêu cầu mật khẩu:
                </p>
                <div style={{ display: 'grid', gap: '0.5rem' }}>
                  {[
                    { text: 'Ít nhất 8 ký tự', met: newPassword.length >= 8 },
                    { text: 'Chứa chữ hoa và chữ thường', met: /[a-z]/.test(newPassword) && /[A-Z]/.test(newPassword) },
                    { text: 'Chứa số', met: /\d/.test(newPassword) },
                    { text: 'Mật khẩu khớp nhau', met: newPassword && newPassword === confirmPassword }
                  ].map((req, index) => (
                    <div key={index} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      {req.met ? (
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10b981" strokeWidth="2">
                          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                          <polyline points="22 4 12 14.01 9 11.01"></polyline>
                        </svg>
                      ) : (
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2">
                          <circle cx="12" cy="12" r="10"></circle>
                        </svg>
                      )}
                      <span style={{ fontSize: '0.8125rem', color: req.met ? '#10b981' : '#9ca3af' }}>
                        {req.text}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                <button 
                  className="btn btn-secondary" 
                  style={{ flex: 1 }}
                  onClick={() => {
                    setShowSecurityModal(false);
                    setCurrentPassword('');
                    setNewPassword('');
                    setConfirmPassword('');
                  }}
                >
                  Hủy
                </button>
                <button 
                  className="btn btn-primary" 
                  style={{ flex: 1 }}
                  onClick={async () => {
                    if (!currentPassword || !newPassword || !confirmPassword) {
                      showToast('Vui lòng điền đầy đủ thông tin', 'error');
                      return;
                    }

                    if (newPassword.length < 6) {
                      showToast('Mật khẩu phải có ít nhất 6 ký tự', 'error');
                      return;
                    }

                    if (newPassword !== confirmPassword) {
                      showToast('Mật khẩu xác nhận không khớp', 'error');
                      return;
                    }

                    setIsChangingPassword(true);
                    try {
                      // Simulate API call
                      await new Promise(resolve => setTimeout(resolve, 1500));
                      
                      showToast('Đổi mật khẩu thành công!', 'success');
                      setShowSecurityModal(false);
                      setCurrentPassword('');
                      setNewPassword('');
                      setConfirmPassword('');
                    } catch (error) {
                      showToast('Có lỗi xảy ra khi đổi mật khẩu', 'error');
                    } finally {
                      setIsChangingPassword(false);
                    }
                  }}
                  disabled={isChangingPassword}
                >
                  {isChangingPassword ? (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem', animation: 'spin 1s linear infinite' }}>
                        <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
                      </svg>
                      Đang xử lý...
                    </>
                  ) : (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '0.5rem' }}>
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                      </svg>
                      Đổi mật khẩu
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}