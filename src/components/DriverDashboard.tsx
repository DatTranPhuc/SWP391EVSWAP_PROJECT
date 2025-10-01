import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Battery, Car, MapPin, Clock, Ticket } from "lucide-react";
import { projectId, publicAnonKey } from "../utils/supabase/info";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Textarea } from "./ui/textarea";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "./ui/dialog";
import { toast } from "sonner@2.0.3";

interface DriverDashboardProps {
  user: any;
  accessToken: string;
}

export function DriverDashboard({ user, accessToken }: DriverDashboardProps) {
  const [vehicles, setVehicles] = useState<any[]>([]);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [stations, setStations] = useState<any[]>([]);
  const [tickets, setTickets] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [ticketSubject, setTicketSubject] = useState("");
  const [ticketDescription, setTicketDescription] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadVehicles(),
        loadTransactions(),
        loadStations(),
        loadTickets()
      ]);
    } finally {
      setLoading(false);
    }
  };

  const loadVehicles = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/vehicles`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );
      const data = await response.json();
      if (data.vehicles) {
        setVehicles(data.vehicles);
      }
    } catch (error) {
      console.error('Error loading vehicles:', error);
    }
  };

  const loadTransactions = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/transactions`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );
      const data = await response.json();
      if (data.transactions) {
        setTransactions(data.transactions);
      }
    } catch (error) {
      console.error('Error loading transactions:', error);
    }
  };

  const loadStations = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/stations`,
        {
          headers: {
            'Authorization': `Bearer ${publicAnonKey}`,
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

  const loadTickets = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/tickets`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        }
      );
      const data = await response.json();
      if (data.tickets) {
        setTickets(data.tickets);
      }
    } catch (error) {
      console.error('Error loading tickets:', error);
    }
  };

  const handleCreateTicket = async () => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/tickets`,
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
        toast.success('Đã gửi yêu cầu hỗ trợ');
        setTicketSubject("");
        setTicketDescription("");
        setDialogOpen(false);
        loadTickets();
      } else {
        toast.error(data.error || 'Có lỗi xảy ra');
      }
    } catch (error) {
      console.error('Error creating ticket:', error);
      toast.error('Có lỗi xảy ra');
    }
  };

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
            Xin chào, {user.full_name}!
          </h1>
          <p className="text-gray-600">
            Chào mừng bạn đến với bảng điều khiển Driver
          </p>
        </div>

        {/* Stats Overview */}
        <div className="grid md:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Số xe đã đăng ký</CardTitle>
              <Car className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{vehicles.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Lần đổi pin</CardTitle>
              <Battery className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{transactions.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Trạm gần đây</CardTitle>
              <MapPin className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{stations.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Yêu cầu hỗ trợ</CardTitle>
              <Ticket className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{tickets.length}</div>
            </CardContent>
          </Card>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          {/* Vehicle Info */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Car className="h-5 w-5" />
                Thông tin xe
              </CardTitle>
            </CardHeader>
            <CardContent>
              {vehicles.length > 0 ? (
                <div className="space-y-4">
                  {vehicles.map((vehicle) => (
                    <div key={vehicle.id} className="border rounded-lg p-4">
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <p className="text-sm text-gray-600">Model</p>
                          <p>{vehicle.model}</p>
                        </div>
                        <div>
                          <p className="text-sm text-gray-600">Biển số</p>
                          <p>{vehicle.license_plate}</p>
                        </div>
                        <div className="col-span-2">
                          <p className="text-sm text-gray-600">Số VIN</p>
                          <p className="text-sm break-all">{vehicle.vin}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">Chưa có xe nào được đăng ký</p>
              )}
            </CardContent>
          </Card>

          {/* Recent Transactions */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Clock className="h-5 w-5" />
                Lịch sử đổi pin
              </CardTitle>
            </CardHeader>
            <CardContent>
              {transactions.length > 0 ? (
                <div className="space-y-3">
                  {transactions.slice(0, 5).map((transaction) => (
                    <div key={transaction.id} className="flex items-center justify-between border-b pb-3">
                      <div>
                        <p className="text-sm">
                          Đổi pin tại trạm
                        </p>
                        <p className="text-xs text-gray-500">
                          {new Date(transaction.timestamp).toLocaleString('vi-VN')}
                        </p>
                      </div>
                      <Badge variant="outline" className="bg-green-50">
                        Hoàn thành
                      </Badge>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">Chưa có giao dịch nào</p>
              )}
            </CardContent>
          </Card>

          {/* Nearby Stations */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="h-5 w-5" />
                Trạm đổi pin
              </CardTitle>
              <CardDescription>
                Các trạm đổi pin có sẵn
              </CardDescription>
            </CardHeader>
            <CardContent>
              {stations.length > 0 ? (
                <div className="space-y-3">
                  {stations.slice(0, 5).map((station) => (
                    <div key={station.id} className="border rounded-lg p-3">
                      <div className="flex items-start justify-between">
                        <div>
                          <p>{station.name}</p>
                          <p className="text-sm text-gray-600">{station.address}</p>
                        </div>
                        <Badge className="bg-green-600">
                          Hoạt động
                        </Badge>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">Chưa có trạm nào</p>
              )}
            </CardContent>
          </Card>

          {/* Support Tickets */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <Ticket className="h-5 w-5" />
                  Hỗ trợ kỹ thuật
                </CardTitle>
                <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
                  <DialogTrigger asChild>
                    <Button size="sm">Tạo yêu cầu</Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Tạo yêu cầu hỗ trợ</DialogTitle>
                      <DialogDescription>
                        Mô tả vấn đề bạn gặp phải, đội ngũ hỗ trợ sẽ liên hệ sớm nhất
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="subject">Tiêu đề</Label>
                        <Input
                          id="subject"
                          placeholder="Vấn đề về..."
                          value={ticketSubject}
                          onChange={(e) => setTicketSubject(e.target.value)}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="description">Mô tả chi tiết</Label>
                        <Textarea
                          id="description"
                          placeholder="Mô tả vấn đề của bạn..."
                          value={ticketDescription}
                          onChange={(e) => setTicketDescription(e.target.value)}
                          rows={4}
                        />
                      </div>
                      <Button onClick={handleCreateTicket} className="w-full">
                        Gửi yêu cầu
                      </Button>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            </CardHeader>
            <CardContent>
              {tickets.length > 0 ? (
                <div className="space-y-3">
                  {tickets.slice(0, 5).map((ticket) => (
                    <div key={ticket.id} className="border rounded-lg p-3">
                      <div className="flex items-start justify-between mb-2">
                        <p>{ticket.subject}</p>
                        <Badge 
                          variant={ticket.status === 'open' ? 'default' : 'secondary'}
                        >
                          {ticket.status === 'open' ? 'Đang xử lý' : 
                           ticket.status === 'resolved' ? 'Đã giải quyết' : 'Đóng'}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600">{ticket.description}</p>
                      <p className="text-xs text-gray-500 mt-2">
                        {new Date(ticket.created_at).toLocaleString('vi-VN')}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">Chưa có yêu cầu hỗ trợ nào</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
