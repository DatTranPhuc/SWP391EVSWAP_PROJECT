import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Users, Battery, MapPin, UserPlus, Building2 } from "lucide-react";
import { projectId } from "../utils/supabase/info";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "./ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "./ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { toast } from "sonner@2.0.3";

interface AdminDashboardProps {
  user: any;
  accessToken: string;
}

export function AdminDashboard({ user, accessToken }: AdminDashboardProps) {
  const [users, setUsers] = useState<any[]>([]);
  const [stations, setStations] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Staff creation
  const [staffDialogOpen, setStaffDialogOpen] = useState(false);
  const [staffEmail, setStaffEmail] = useState("");
  const [staffPassword, setStaffPassword] = useState("");
  const [staffFullName, setStaffFullName] = useState("");
  const [staffPhone, setStaffPhone] = useState("");

  // Station creation
  const [stationDialogOpen, setStationDialogOpen] = useState(false);
  const [stationName, setStationName] = useState("");
  const [stationAddress, setStationAddress] = useState("");
  const [stationLatitude, setStationLatitude] = useState("");
  const [stationLongitude, setStationLongitude] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadUsers(),
        loadStations()
      ]);
    } finally {
      setLoading(false);
    }
  };

  const loadUsers = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/admin/users`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );
      const data = await response.json();
      if (data.users) {
        setUsers(data.users);
      }
    } catch (error) {
      console.error('Error loading users:', error);
    }
  };

  const loadStations = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/stations`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );
      const data = await response.json();
      if (data.stations) {
        setStations(data.stations);
      }
    } catch (error) {
      console.error('Error loading stations:', error);
    }
  };

  const handleCreateStaff = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/admin/staff`,
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
        toast.success('Đã tạo tài khoản Staff thành công');
        setStaffEmail("");
        setStaffPassword("");
        setStaffFullName("");
        setStaffPhone("");
        setStaffDialogOpen(false);
        loadUsers();
      } else {
        toast.error(data.error || 'Có lỗi xảy ra');
      }
    } catch (error) {
      console.error('Error creating staff:', error);
      toast.error('Có lỗi xảy ra');
    }
  };

  const handleCreateStation = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/admin/stations`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: stationName,
            address: stationAddress,
            latitude: parseFloat(stationLatitude),
            longitude: parseFloat(stationLongitude),
          }),
        }
      );

      const data = await response.json();
      if (data.success) {
        toast.success('Đã tạo trạm đổi pin thành công');
        setStationName("");
        setStationAddress("");
        setStationLatitude("");
        setStationLongitude("");
        setStationDialogOpen(false);
        loadStations();
      } else {
        toast.error(data.error || 'Có lỗi xảy ra');
      }
    } catch (error) {
      console.error('Error creating station:', error);
      toast.error('Có lỗi xảy ra');
    }
  };

  const drivers = users.filter(u => u.role === 'driver');
  const staff = users.filter(u => u.role === 'staff');

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-500">Đang tải...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Welcome Section */}
        <div className="mb-8">
          <h1 className="text-3xl mb-2">
            Bảng điều khiển Admin
          </h1>
          <p className="text-gray-600">
            Quản lý toàn bộ hệ thống EV SWAP
          </p>
        </div>

        {/* Stats Overview */}
        <div className="grid md:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Tổng Driver</CardTitle>
              <Users className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{drivers.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Tổng Staff</CardTitle>
              <UserPlus className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{staff.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Trạm đổi pin</CardTitle>
              <Building2 className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{stations.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Tổng người dùng</CardTitle>
              <Users className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{users.length}</div>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="users" className="space-y-6">
          <TabsList>
            <TabsTrigger value="users">Người dùng</TabsTrigger>
            <TabsTrigger value="stations">Trạm đổi pin</TabsTrigger>
          </TabsList>

          <TabsContent value="users" className="space-y-6">
            {/* Create Staff */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Quản lý người dùng</CardTitle>
                    <CardDescription>
                      Quản lý tài khoản Driver và Staff
                    </CardDescription>
                  </div>
                  <Dialog open={staffDialogOpen} onOpenChange={setStaffDialogOpen}>
                    <DialogTrigger asChild>
                      <Button>
                        <UserPlus className="h-4 w-4 mr-2" />
                        Tạo Staff
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Tạo tài khoản Staff</DialogTitle>
                        <DialogDescription>
                          Tạo tài khoản mới cho nhân viên trạm đổi pin
                        </DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <Label htmlFor="staffFullName">Họ và tên</Label>
                          <Input
                            id="staffFullName"
                            placeholder="Nguyễn Văn B"
                            value={staffFullName}
                            onChange={(e) => setStaffFullName(e.target.value)}
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="staffEmail">Email</Label>
                          <Input
                            id="staffEmail"
                            type="email"
                            placeholder="staff@evswap.com"
                            value={staffEmail}
                            onChange={(e) => setStaffEmail(e.target.value)}
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="staffPhone">Số điện thoại</Label>
                          <Input
                            id="staffPhone"
                            placeholder="0123456789"
                            value={staffPhone}
                            onChange={(e) => setStaffPhone(e.target.value)}
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="staffPassword">Mật khẩu</Label>
                          <Input
                            id="staffPassword"
                            type="password"
                            placeholder="••••••••"
                            value={staffPassword}
                            onChange={(e) => setStaffPassword(e.target.value)}
                          />
                        </div>
                        <Button onClick={handleCreateStaff} className="w-full">
                          Tạo tài khoản
                        </Button>
                      </div>
                    </DialogContent>
                  </Dialog>
                </div>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Họ tên</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Điện thoại</TableHead>
                      <TableHead>Vai trò</TableHead>
                      <TableHead>Ngày tạo</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {users.map((user) => (
                      <TableRow key={user.id}>
                        <TableCell>{user.full_name}</TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>{user.phone}</TableCell>
                        <TableCell>
                          <Badge 
                            variant={
                              user.role === 'admin' ? 'default' : 
                              user.role === 'staff' ? 'secondary' : 
                              'outline'
                            }
                          >
                            {user.role === 'admin' ? 'Admin' : 
                             user.role === 'staff' ? 'Staff' : 'Driver'}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-sm text-gray-600">
                          {new Date(user.created_at).toLocaleDateString('vi-VN')}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="stations" className="space-y-6">
            {/* Stations Management */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Quản lý trạm đổi pin</CardTitle>
                    <CardDescription>
                      Danh sách các trạm đổi pin trong hệ thống
                    </CardDescription>
                  </div>
                  <Dialog open={stationDialogOpen} onOpenChange={setStationDialogOpen}>
                    <DialogTrigger asChild>
                      <Button>
                        <Building2 className="h-4 w-4 mr-2" />
                        Tạo trạm mới
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Tạo trạm đổi pin mới</DialogTitle>
                        <DialogDescription>
                          Thêm trạm đổi pin mới vào hệ thống
                        </DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <Label htmlFor="stationName">Tên trạm</Label>
                          <Input
                            id="stationName"
                            placeholder="Trạm đổi pin Quận 1"
                            value={stationName}
                            onChange={(e) => setStationName(e.target.value)}
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="stationAddress">Địa chỉ</Label>
                          <Input
                            id="stationAddress"
                            placeholder="123 Nguyễn Huệ, Quận 1, TP.HCM"
                            value={stationAddress}
                            onChange={(e) => setStationAddress(e.target.value)}
                          />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                          <div className="space-y-2">
                            <Label htmlFor="stationLatitude">Vĩ độ</Label>
                            <Input
                              id="stationLatitude"
                              type="number"
                              step="any"
                              placeholder="10.762622"
                              value={stationLatitude}
                              onChange={(e) => setStationLatitude(e.target.value)}
                            />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="stationLongitude">Kinh độ</Label>
                            <Input
                              id="stationLongitude"
                              type="number"
                              step="any"
                              placeholder="106.660172"
                              value={stationLongitude}
                              onChange={(e) => setStationLongitude(e.target.value)}
                            />
                          </div>
                        </div>
                        <Button onClick={handleCreateStation} className="w-full">
                          Tạo trạm
                        </Button>
                      </div>
                    </DialogContent>
                  </Dialog>
                </div>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Tên trạm</TableHead>
                      <TableHead>Địa chỉ</TableHead>
                      <TableHead>Tọa độ</TableHead>
                      <TableHead>Trạng thái</TableHead>
                      <TableHead>Ngày tạo</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {stations.map((station) => (
                      <TableRow key={station.id}>
                        <TableCell>{station.name}</TableCell>
                        <TableCell>{station.address}</TableCell>
                        <TableCell className="text-sm text-gray-600">
                          {station.latitude}, {station.longitude}
                        </TableCell>
                        <TableCell>
                          <Badge className="bg-green-600">
                            Hoạt động
                          </Badge>
                        </TableCell>
                        <TableCell className="text-sm text-gray-600">
                          {new Date(station.created_at).toLocaleDateString('vi-VN')}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
