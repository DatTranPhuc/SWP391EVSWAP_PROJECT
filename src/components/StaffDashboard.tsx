import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Battery, MapPin, Activity, Ticket } from "lucide-react";
import { projectId } from "../utils/supabase/info";
import { Button } from "./ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "./ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { toast } from "sonner@2.0.3";

interface StaffDashboardProps {
  user: any;
  accessToken: string;
}

export function StaffDashboard({ user, accessToken }: StaffDashboardProps) {
  const [stations, setStations] = useState<any[]>([]);
  const [tickets, setTickets] = useState<any[]>([]);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadStations(),
        loadTickets(),
        loadTransactions()
      ]);
    } finally {
      setLoading(false);
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

  const handleUpdateTicketStatus = async (ticketId: string, status: string) => {
    try {
      const response = await fetch(
        `https://${projectId}.supabase.co/functions/v1/make-server-c0c28b62/tickets/${ticketId}`,
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
        toast.success('Đã cập nhật trạng thái ticket');
        loadTickets();
      } else {
        toast.error(data.error || 'Có lỗi xảy ra');
      }
    } catch (error) {
      console.error('Error updating ticket:', error);
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
            Bảng điều khiển Staff
          </h1>
          <p className="text-gray-600">
            Xin chào, {user.full_name} - Nhân viên trạm đổi pin
          </p>
        </div>

        {/* Stats Overview */}
        <div className="grid md:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Trạm hoạt động</CardTitle>
              <MapPin className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{stations.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Giao dịch</CardTitle>
              <Activity className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{transactions.length}</div>
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

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm">Ticket đang mở</CardTitle>
              <Ticket className="h-4 w-4 text-gray-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">
                {tickets.filter(t => t.status === 'open').length}
              </div>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="tickets" className="space-y-6">
          <TabsList>
            <TabsTrigger value="tickets">Hỗ trợ kỹ thuật</TabsTrigger>
            <TabsTrigger value="transactions">Giao dịch</TabsTrigger>
            <TabsTrigger value="stations">Trạm đổi pin</TabsTrigger>
          </TabsList>

          <TabsContent value="tickets" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Quản lý yêu cầu hỗ trợ</CardTitle>
                <CardDescription>
                  Xử lý các yêu cầu hỗ trợ từ Driver
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Tiêu đề</TableHead>
                      <TableHead>Mô tả</TableHead>
                      <TableHead>Ngày tạo</TableHead>
                      <TableHead>Trạng thái</TableHead>
                      <TableHead>Hành động</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {tickets.map((ticket) => (
                      <TableRow key={ticket.id}>
                        <TableCell>{ticket.subject}</TableCell>
                        <TableCell className="max-w-xs truncate">
                          {ticket.description}
                        </TableCell>
                        <TableCell className="text-sm text-gray-600">
                          {new Date(ticket.created_at).toLocaleString('vi-VN')}
                        </TableCell>
                        <TableCell>
                          <Badge 
                            variant={ticket.status === 'open' ? 'default' : 'secondary'}
                          >
                            {ticket.status === 'open' ? 'Đang mở' : 
                             ticket.status === 'resolved' ? 'Đã giải quyết' : 'Đóng'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Select
                            value={ticket.status}
                            onValueChange={(value) => handleUpdateTicketStatus(ticket.id, value)}
                          >
                            <SelectTrigger className="w-[130px]">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="open">Đang mở</SelectItem>
                              <SelectItem value="resolved">Đã giải quyết</SelectItem>
                              <SelectItem value="closed">Đóng</SelectItem>
                            </SelectContent>
                          </Select>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                {tickets.length === 0 && (
                  <p className="text-center text-gray-500 py-8">
                    Chưa có yêu cầu hỗ trợ nào
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="transactions" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Lịch sử giao dịch đổi pin</CardTitle>
                <CardDescription>
                  Tất cả giao dịch đổi pin trong hệ thống
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID Giao dịch</TableHead>
                      <TableHead>Thời gian</TableHead>
                      <TableHead>Trạng thái</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {transactions.map((transaction) => (
                      <TableRow key={transaction.id}>
                        <TableCell className="text-sm">
                          {transaction.id.substring(0, 8)}...
                        </TableCell>
                        <TableCell className="text-sm text-gray-600">
                          {new Date(transaction.timestamp).toLocaleString('vi-VN')}
                        </TableCell>
                        <TableCell>
                          <Badge className="bg-green-600">
                            Hoàn thành
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                {transactions.length === 0 && (
                  <p className="text-center text-gray-500 py-8">
                    Chưa có giao dịch nào
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="stations" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Danh sách trạm đổi pin</CardTitle>
                <CardDescription>
                  Các trạm đổi pin trong hệ thống
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {stations.map((station) => (
                    <div key={station.id} className="border rounded-lg p-4">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <MapPin className="h-5 w-5 text-blue-600" />
                            <h3 className="text-lg">{station.name}</h3>
                          </div>
                          <p className="text-gray-600 mb-2">{station.address}</p>
                          <p className="text-sm text-gray-500">
                            Tọa độ: {station.latitude}, {station.longitude}
                          </p>
                        </div>
                        <Badge className="bg-green-600">
                          Hoạt động
                        </Badge>
                      </div>
                    </div>
                  ))}
                </div>
                {stations.length === 0 && (
                  <p className="text-center text-gray-500 py-8">
                    Chưa có trạm nào
                  </p>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
