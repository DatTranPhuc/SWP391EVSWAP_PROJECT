import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Alert, AlertDescription } from "./ui/alert";
import { Car } from "lucide-react";

interface VehicleRegistrationProps {
  onAddVehicle: (model: string, vin: string, licensePlate: string) => Promise<void>;
  error: string | null;
}

export function VehicleRegistration({ onAddVehicle, error }: VehicleRegistrationProps) {
  const [model, setModel] = useState("");
  const [vin, setVin] = useState("");
  const [licensePlate, setLicensePlate] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onAddVehicle(model, vin, licensePlate);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-gray-50 to-white p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center">
              <Car className="h-8 w-8 text-blue-600" />
            </div>
          </div>
          <CardTitle className="text-2xl">Đăng ký xe điện</CardTitle>
          <CardDescription>
            Vui lòng đăng ký thông tin xe máy điện của bạn để sử dụng dịch vụ
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <div className="space-y-2">
              <Label htmlFor="model">Model xe</Label>
              <Input
                id="model"
                type="text"
                placeholder="VinFast Klara, Yadea..."
                value={model}
                onChange={(e) => setModel(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="vin">Số VIN (Số khung)</Label>
              <Input
                id="vin"
                type="text"
                placeholder="VF1XXXXXXXXXXXXXXX"
                value={vin}
                onChange={(e) => setVin(e.target.value)}
                required
              />
              <p className="text-xs text-gray-500">
                Số khung xe (Vehicle Identification Number)
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="licensePlate">Biển số xe</Label>
              <Input
                id="licensePlate"
                type="text"
                placeholder="29A1-12345"
                value={licensePlate}
                onChange={(e) => setLicensePlate(e.target.value)}
                required
              />
            </div>

            <div className="bg-blue-50 p-4 rounded-lg">
              <p className="text-sm text-blue-800">
                💡 Thông tin xe của bạn sẽ được sử dụng để xác nhận khi đổi pin tại trạm
              </p>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Đang đăng ký..." : "Hoàn tất đăng ký"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
