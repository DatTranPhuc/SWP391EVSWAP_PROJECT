import { Battery, Zap, MapPin, Shield, Clock, Leaf, UserPlus, Users } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { ImageWithFallback } from "./figma/ImageWithFallback";

export function LandingPage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-white to-gray-50">
      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20">
        <div className="grid md:grid-cols-2 gap-12 items-center">
          <div>
            <h1 className="text-5xl mb-6">
              Hệ thống đổi pin xe máy điện thông minh
            </h1>
            <p className="text-xl text-gray-600 mb-8">
              Giải pháp đổi pin nhanh chóng, tiện lợi cho xe máy điện. 
              Chỉ cần 3 phút để có pin đầy năng lượng.
            </p>
            <div className="flex gap-4">
              <div className="flex items-center gap-2 text-green-600">
                <Zap className="h-5 w-5" />
                <span>Đổi pin chỉ 3 phút</span>
              </div>
              <div className="flex items-center gap-2 text-green-600">
                <MapPin className="h-5 w-5" />
                <span>Nhiều trạm toàn quốc</span>
              </div>
            </div>
          </div>
          <div className="relative h-[400px] rounded-lg overflow-hidden shadow-2xl">
            <ImageWithFallback
              src="https://images.unsplash.com/photo-1590753582218-70730cdf88a5?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxlbGVjdHJpYyUyMG1vdG9yY3ljbGUlMjBiYXR0ZXJ5fGVufDF8fHx8MTc1OTMxNDQxNXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
              alt="Electric motorcycle battery"
              className="w-full h-full object-cover"
            />
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="container mx-auto px-4 py-20">
        <h2 className="text-4xl text-center mb-12">
          Tính năng nổi bật
        </h2>
        
        <div className="grid md:grid-cols-3 gap-8">
          <Card>
            <CardHeader>
              <div className="w-12 h-12 rounded-lg bg-green-100 flex items-center justify-center mb-4">
                <Battery className="h-6 w-6 text-green-600" />
              </div>
              <CardTitle>Đổi pin nhanh chóng</CardTitle>
              <CardDescription>
                Chỉ cần 3 phút để thay thế pin cũ bằng pin đầy, không cần chờ sạc
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader>
              <div className="w-12 h-12 rounded-lg bg-blue-100 flex items-center justify-center mb-4">
                <MapPin className="h-6 w-6 text-blue-600" />
              </div>
              <CardTitle>Mạng lưới trạm rộng khắp</CardTitle>
              <CardDescription>
                Hệ thống trạm đổi pin phủ sóng toàn quốc, luôn sẵn sàng phục vụ
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader>
              <div className="w-12 h-12 rounded-lg bg-purple-100 flex items-center justify-center mb-4">
                <Shield className="h-6 w-6 text-purple-600" />
              </div>
              <CardTitle>An toàn & Đáng tin cậy</CardTitle>
              <CardDescription>
                Pin được kiểm tra kỹ lưỡng, đảm bảo chất lượng và an toàn tuyệt đối
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader>
              <div className="w-12 h-12 rounded-lg bg-orange-100 flex items-center justify-center mb-4">
                <Clock className="h-6 w-6 text-orange-600" />
              </div>
              <CardTitle>Hoạt động 24/7</CardTitle>
              <CardDescription>
                Trạm đổi pin hoạt động cả ngày lẫn đêm, phục vụ mọi lúc bạn cần
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader>
              <div className="w-12 h-12 rounded-lg bg-teal-100 flex items-center justify-center mb-4">
                <Leaf className="h-6 w-6 text-teal-600" />
              </div>
              <CardTitle>Thân thiện môi trường</CardTitle>
              <CardDescription>
                Góp phần giảm ô nhiễm không khí, bảo vệ môi trường sống
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader>
              <div className="w-12 h-12 rounded-lg bg-red-100 flex items-center justify-center mb-4">
                <Zap className="h-6 w-6 text-red-600" />
              </div>
              <CardTitle>Tiết kiệm chi phí</CardTitle>
              <CardDescription>
                Chi phí sử dụng thấp hơn so với xe xăng truyền thống
              </CardDescription>
            </CardHeader>
          </Card>
        </div>
      </section>

      {/* How It Works */}
      <section className="container mx-auto px-4 py-20 bg-white rounded-lg my-12">
        <h2 className="text-4xl text-center mb-12">
          Cách thức hoạt động
        </h2>
        
        <div className="grid md:grid-cols-3 gap-8">
          <div className="text-center">
            <div className="w-16 h-16 rounded-full bg-green-600 text-white flex items-center justify-center mx-auto mb-4 text-2xl">
              1
            </div>
            <h3 className="text-xl mb-2">Đăng ký tài khoản</h3>
            <p className="text-gray-600">
              Tạo tài khoản và đăng ký thông tin xe máy điện của bạn
            </p>
          </div>

          <div className="text-center">
            <div className="w-16 h-16 rounded-full bg-green-600 text-white flex items-center justify-center mx-auto mb-4 text-2xl">
              2
            </div>
            <h3 className="text-xl mb-2">Tìm trạm gần nhất</h3>
            <p className="text-gray-600">
              Sử dụng app để tìm trạm đổi pin gần bạn nhất
            </p>
          </div>

          <div className="text-center">
            <div className="w-16 h-16 rounded-full bg-green-600 text-white flex items-center justify-center mx-auto mb-4 text-2xl">
              3
            </div>
            <h3 className="text-xl mb-2">Đổi pin & Sử dụng</h3>
            <p className="text-gray-600">
              Nhân viên hỗ trợ đổi pin nhanh chóng, bạn tiếp tục hành trình
            </p>
          </div>
        </div>
      </section>

      {/* About Project */}
      <section className="container mx-auto px-4 py-20">
        <div className="grid md:grid-cols-2 gap-12 items-center">
          <div className="relative h-[400px] rounded-lg overflow-hidden shadow-2xl">
            <ImageWithFallback
              src="https://images.unsplash.com/photo-1672542128826-5f0d578713d2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxlbGVjdHJpYyUyMHZlaGljbGUlMjBjaGFyZ2luZyUyMHN0YXRpb258ZW58MXx8fHwxNzU5MzExOTc0fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
              alt="Electric vehicle charging station"
              className="w-full h-full object-cover"
            />
          </div>
          
          <div>
            <h2 className="text-4xl mb-6">Về dự án EV SWAP</h2>
            <p className="text-lg text-gray-600 mb-4">
              EV SWAP là hệ thống quản lý và điều hành trạm đổi pin xe máy điện, 
              được phát triển nhằm thúc đẩy việc sử dụng phương tiện giao thông 
              xanh, thân thiện với môi trường tại Việt Nam.
            </p>
            <p className="text-lg text-gray-600 mb-4">
              Dự án cung cấp giải pháp toàn diện cho cả người dùng (tài xế xe điện), 
              nhân viên trạm và quản trị viên hệ thống, giúp tối ưu hóa quy trình 
              đổi pin và quản lý tài nguyên pin hiệu quả.
            </p>
            
            <div className="space-y-3 mt-6">
              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-green-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <span className="text-white text-xs">✓</span>
                </div>
                <p className="text-gray-700">
                  Quản lý tài khoản đa cấp: Admin, Staff, Driver
                </p>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-green-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <span className="text-white text-xs">✓</span>
                </div>
                <p className="text-gray-700">
                  Quản lý trạm đổi pin và kho pin thông minh
                </p>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-green-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <span className="text-white text-xs">✓</span>
                </div>
                <p className="text-gray-700">
                  Theo dõi giao dịch đổi pin chi tiết
                </p>
              </div>
              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-green-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <span className="text-white text-xs">✓</span>
                </div>
                <p className="text-gray-700">
                  Hỗ trợ kỹ thuật qua hệ thống ticket
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Demo Credentials Section */}
      <section className="container mx-auto px-4 py-12">
        <Card className="border-2 border-blue-200 bg-blue-50">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-6 w-6 text-blue-600" />
              Thông tin đăng nhập Demo
            </CardTitle>
            <CardDescription>
              Sử dụng các tài khoản sau để trải nghiệm hệ thống
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-4">
              <div className="bg-white p-4 rounded-lg border">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-8 h-8 rounded-full bg-red-100 flex items-center justify-center">
                    <Shield className="h-4 w-4 text-red-600" />
                  </div>
                  <h4 className="text-lg">Admin</h4>
                </div>
                <div className="space-y-1 text-sm">
                  <p className="text-gray-600">Email:</p>
                  <p className="font-mono bg-gray-100 p-2 rounded">admin@evswap.com</p>
                  <p className="text-gray-600 mt-2">Password:</p>
                  <p className="font-mono bg-gray-100 p-2 rounded">Admin@123456</p>
                </div>
              </div>

              <div className="bg-white p-4 rounded-lg border">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center">
                    <UserPlus className="h-4 w-4 text-purple-600" />
                  </div>
                  <h4 className="text-lg">Staff</h4>
                </div>
                <div className="space-y-1 text-sm">
                  <p className="text-gray-700">
                    Tài khoản Staff được tạo bởi Admin trong dashboard
                  </p>
                </div>
              </div>

              <div className="bg-white p-4 rounded-lg border">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center">
                    <Users className="h-4 w-4 text-green-600" />
                  </div>
                  <h4 className="text-lg">Driver</h4>
                </div>
                <div className="space-y-1 text-sm">
                  <p className="text-gray-700">
                    Đăng ký tài khoản Driver mới thông qua nút "Đăng ký" ở trên
                  </p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* CTA Section */}
      <section className="container mx-auto px-4 py-20 text-center">
        <div className="bg-gradient-to-r from-green-600 to-teal-600 rounded-2xl p-12 text-white">
          <h2 className="text-4xl mb-4">
            Sẵn sàng trải nghiệm?
          </h2>
          <p className="text-xl mb-8 opacity-90">
            Đăng ký ngay hôm nay để tham gia cộng đồng xe máy điện thông minh
          </p>
          <div className="flex gap-4 justify-center items-center">
            <Battery className="h-8 w-8" />
            <span className="text-2xl">Tương lai xanh bắt đầu từ hôm nay</span>
          </div>
        </div>
      </section>
    </div>
  );
}
