# 🎨 EV SWAP - Animation Effects Guide

## ✨ Các hiệu ứng animation đã được thêm vào hệ thống

### 1. **Keyframe Animations**

#### Fade Animations
```css
.animate-fade-in      /* Hiện dần 0.3s */
.animate-fade-out     /* Mờ dần 0.3s */
```

#### Slide Animations
```css
.animate-slide-up     /* Trượt lên từ dưới 0.4s */
.animate-slide-down   /* Trượt xuống từ trên 0.3s */
```

#### Scale Animations
```css
.animate-scale-in     /* Phóng to từ 0.9 → 1.0 */
```

#### Motion Animations
```css
.animate-bounce       /* Nhảy lên xuống liên tục */
.animate-spin         /* Quay vòng liên tục */
.animate-pulse        /* Nhấp nháy liên tục */
```

### 2. **Stagger Animations** (Hiệu ứng xếp tầng)

Áp dụng cho danh sách items để xuất hiện lần lượt:

```tsx
<div className="stagger-item">Item 1</div>  // delay 0.05s
<div className="stagger-item">Item 2</div>  // delay 0.1s
<div className="stagger-item">Item 3</div>  // delay 0.15s
// ... tới item thứ 8
```

**Sử dụng cho:**
- Stats cards (4 thẻ thống kê)
- Feature cards
- Station list
- Transaction history

### 3. **Hover Effects**

#### Lift Effect
```tsx
<div className="hover-lift">
  // Card sẽ nhấc lên và có shadow khi hover
</div>
```

#### Scale Effect
```tsx
<button className="hover-scale">
  // Button phóng to 1.05x khi hover
</button>
```

#### Glow Effect
```tsx
<div className="hover-glow">
  // Phát sáng xanh lá khi hover
</div>
```

### 4. **Button Ripple Effect**

```tsx
<button className="btn btn-primary btn-ripple">
  Nạp tiền
</button>
```

Tạo hiệu ứng sóng lan tỏa khi click button.

### 5. **Modal Animations**

Tất cả modal tự động có:
- **Overlay**: Fade in 0.2s
- **Modal content**: Slide up + scale 0.3s

### 6. **Toast Notifications**

Tự động slide down từ trên xuống khi xuất hiện.

### 7. **Sidebar Animations**

- **Left Drawer**: Slide in from left
- **Right Sidebar**: Slide in from right
- Smooth transition 0.3s với cubic-bezier

### 8. **Loading States**

#### Skeleton Loading
```tsx
<div className="skeleton" style={{ width: '100%', height: '40px', borderRadius: '8px' }}>
  // Shimmer effect
</div>
```

#### Progress Bar
```tsx
<div className="progress-animated" style={{ width: '75%' }}>
  // Animated fill from 0 → 75%
</div>
```

### 9. **Special Effects**

#### Glass Effect
```tsx
<div className="glass">
  // Frosted glass với backdrop blur
</div>
```

#### Card Entrance
```tsx
<div className="card card-enter">
  // Scale in khi mount
</div>
```

---

## 🎯 Hướng dẫn sử dụng trong các components

### Stats Cards (Dashboard)
```tsx
<div className="stats-cards-row">
  <div className="stat-card-modern stagger-item hover-lift">
    {/* Content */}
  </div>
  {/* Repeat for 4 cards */}
</div>
```

### Action Buttons
```tsx
<button className="btn btn-primary btn-ripple hover-glow">
  <svg>...</svg>
  Đặt lịch ngay
</button>
```

### Station Cards
```tsx
<div className="card stagger-item hover-lift" onClick={...}>
  {/* Station info */}
</div>
```

### Modal với smooth transition
```tsx
{showModal && (
  <div className="modal-overlay animate-fade-in">
    <div className="modal animate-scale-in">
      {/* Modal content */}
    </div>
  </div>
)}
```

### Loading Skeleton
```tsx
{isLoading ? (
  <div className="skeleton" style={{ height: '200px' }} />
) : (
  <div className="card-enter">
    {/* Real content */}
  </div>
)}
```

---

## 🚀 Performance Tips

1. **Chỉ animate transform và opacity** - Hiệu suất tốt nhất
2. **Sử dụng `will-change`** cho animations phức tạp
3. **Giới hạn concurrent animations** - Tối đa 8 items stagger
4. **Hardware acceleration** - Tự động với transform

---

## 🎨 Customization

### Thay đổi thời gian animation:
```css
.custom-animation {
  animation: fadeIn 0.5s ease-out; /* Thay đổi từ 0.3s → 0.5s */
}
```

### Thay đổi easing function:
```css
.smooth-bounce {
  transition: transform 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}
```

---

## ✅ Checklist áp dụng animations

- [x] Modal overlay & content
- [x] Toast notifications
- [x] Sidebar/Drawer
- [x] Stats cards stagger
- [x] Button hover effects
- [x] Button ripple on click
- [x] Card hover lift
- [x] Skeleton loading
- [x] Progress bars
- [x] Dropdown menus
- [x] Tab transitions
- [x] Form input focus
- [x] Badge pulse
- [x] Icon spin (loading)

---

**Tất cả animations đã được tối ưu cho performance và tương thích với mọi trình duyệt hiện đại!** 🎉
