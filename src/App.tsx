import { useState, useEffect } from "react";
import { Header } from "./components/Header";
import { LandingPage } from "./components/LandingPage";
import { LoginPage } from "./components/LoginPage";
import { RegisterPage } from "./components/RegisterPage";
import { VehicleRegistration } from "./components/VehicleRegistration";
import { DriverDashboard } from "./components/DriverDashboard";
import { AdminDashboard } from "./components/AdminDashboard";
import { StaffDashboard } from "./components/StaffDashboard";
import { Toaster } from "./components/ui/sonner";
import { projectId, publicAnonKey } from "./utils/supabase/info";
import { toast } from "sonner@2.0.3";

type Page = 'landing' | 'login' | 'register' | 'vehicle-registration' | 'dashboard';

export default function App() {
  const [currentPage, setCurrentPage] = useState<Page>('landing');
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [accessToken, setAccessToken] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [needsVehicleRegistration, setNeedsVehicleRegistration] = useState(false);

  useEffect(() => {
    // Check if user has active session
    checkSession();
  }, []);

  const checkSession = async () => {
    const storedToken = localStorage.getItem('access_token');
    if (storedToken) {
      try {
        const response = await fetch(
          `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/me`,
          {
            headers: {
              'Authorization': `Bearer ${storedToken}`,
            },
          }
        );
        
        if (response.ok) {
          const data = await response.json();
          setCurrentUser(data.user);
          setAccessToken(storedToken);
          
          // Check if driver needs to register vehicle
          if (data.user.role === 'driver') {
            const vehicleResponse = await fetch(
              `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/vehicles`,
              {
                headers: {
                  'Authorization': `Bearer ${storedToken}`,
                },
              }
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
        console.error('Error checking session:', error);
        localStorage.removeItem('access_token');
      }
    }
  };

  const handleLogin = async (email: string, password: string) => {
    setError(null);
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/login`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${publicAnonKey}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ email, password }),
        }
      );

      const data = await response.json();
      
      if (response.ok && data.success) {
        setCurrentUser(data.user);
        setAccessToken(data.access_token);
        localStorage.setItem('access_token', data.access_token);
        
        // Check if driver needs to register vehicle
        if (data.user.role === 'driver') {
          const vehicleResponse = await fetch(
            `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/vehicles`,
            {
              headers: {
                'Authorization': `Bearer ${data.access_token}`,
              },
            }
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
        
        toast.success('Đăng nhập thành công!');
      } else {
        setError(data.error || 'Đăng nhập thất bại');
        toast.error(data.error || 'Đăng nhập thất bại');
      }
    } catch (error) {
      console.error('Login error:', error);
      setError('Có lỗi xảy ra khi đăng nhập');
      toast.error('Có lỗi xảy ra khi đăng nhập');
    }
  };

  const handleRegister = async (email: string, password: string, fullName: string, phone: string) => {
    setError(null);
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/signup`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${publicAnonKey}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ 
            email, 
            password, 
            full_name: fullName, 
            phone 
          }),
        }
      );

      const data = await response.json();
      
      if (response.ok && data.success) {
        toast.success('Đăng ký thành công! Vui lòng đăng ký thông tin xe.');
        
        // Auto login after registration
        await handleLogin(email, password);
      } else {
        setError(data.error || 'Đăng ký thất bại');
        toast.error(data.error || 'Đăng ký thất bại');
      }
    } catch (error) {
      console.error('Registration error:', error);
      setError('Có lỗi xảy ra khi đăng ký');
      toast.error('Có lỗi xảy ra khi đăng ký');
    }
  };

  const handleAddVehicle = async (model: string, vin: string, licensePlate: string) => {
    setError(null);
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/vehicles`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ 
            model, 
            vin, 
            license_plate: licensePlate 
          }),
        }
      );

      const data = await response.json();
      
      if (response.ok && data.success) {
        toast.success('Đăng ký xe thành công!');
        setCurrentPage('dashboard');
      } else {
        setError(data.error || 'Đăng ký xe thất bại');
        toast.error(data.error || 'Đăng ký xe thất bại');
      }
    } catch (error) {
      console.error('Vehicle registration error:', error);
      setError('Có lỗi xảy ra khi đăng ký xe');
      toast.error('Có lỗi xảy ra khi đăng ký xe');
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setAccessToken('');
    localStorage.removeItem('access_token');
    setCurrentPage('landing');
    toast.success('Đã đăng xuất');
  };

  return (
    <div className="min-h-screen">
      <Toaster position="top-right" />
      
      {currentPage !== 'landing' && currentPage !== 'login' && currentPage !== 'register' && currentPage !== 'vehicle-registration' && (
        <Header
          onLoginClick={() => setCurrentPage('login')}
          onRegisterClick={() => setCurrentPage('register')}
          currentUser={currentUser}
          onLogout={handleLogout}
        />
      )}

      {currentPage === 'landing' && (
        <>
          <Header
            onLoginClick={() => {
              setError(null);
              setCurrentPage('login');
            }}
            onRegisterClick={() => {
              setError(null);
              setCurrentPage('register');
            }}
            currentUser={currentUser}
            onLogout={handleLogout}
          />
          <LandingPage />
        </>
      )}

      {currentPage === 'login' && (
        <LoginPage
          onLogin={handleLogin}
          onSwitchToRegister={() => {
            setError(null);
            setCurrentPage('register');
          }}
          error={error}
        />
      )}

      {currentPage === 'register' && (
        <RegisterPage
          onRegister={handleRegister}
          onSwitchToLogin={() => {
            setError(null);
            setCurrentPage('login');
          }}
          error={error}
        />
      )}

      {currentPage === 'vehicle-registration' && (
        <VehicleRegistration
          onAddVehicle={handleAddVehicle}
          error={error}
        />
      )}

      {currentPage === 'dashboard' && currentUser && (
        <>
          {currentUser.role === 'driver' && (
            <DriverDashboard user={currentUser} accessToken={accessToken} />
          )}
          {currentUser.role === 'admin' && (
            <AdminDashboard user={currentUser} accessToken={accessToken} />
          )}
          {currentUser.role === 'staff' && (
            <StaffDashboard user={currentUser} accessToken={accessToken} />
          )}
        </>
      )}
    </div>
  );
}
