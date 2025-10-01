import { Button } from "./ui/button";
import { Battery, LogOut } from "lucide-react";
import { useEffect, useState } from "react";

interface HeaderProps {
  onLoginClick: () => void;
  onRegisterClick: () => void;
  currentUser: any;
  onLogout: () => void;
}

export function Header({ onLoginClick, onRegisterClick, currentUser, onLogout }: HeaderProps) {
  return (
    <header className="sticky top-0 z-50 w-full border-b bg-white/95 backdrop-blur supports-[backdrop-filter]:bg-white/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-2">
            <Battery className="h-8 w-8 text-green-600" />
            <span className="text-xl">EV SWAP</span>
          </div>
          
          <nav className="flex items-center gap-4">
            {!currentUser ? (
              <>
                <Button variant="ghost" onClick={onLoginClick}>
                  Đăng nhập
                </Button>
                <Button onClick={onRegisterClick}>
                  Đăng ký
                </Button>
              </>
            ) : (
              <>
                <span className="text-sm text-gray-600">
                  Xin chào, <span>{currentUser.full_name}</span>
                </span>
                <Button variant="outline" onClick={onLogout}>
                  <LogOut className="h-4 w-4 mr-2" />
                  Đăng xuất
                </Button>
              </>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
}
