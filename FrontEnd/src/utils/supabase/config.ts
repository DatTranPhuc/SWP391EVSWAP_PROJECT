/**
 * Supabase Configuration
 * 
 * File này đọc thông tin Supabase từ environment variables.
 * Sử dụng để tương thích khi chạy local với Vite.
 * 
 * Fallback về giá trị mặc định nếu chạy trong Figma Make (không có import.meta.env)
 */

// Kiểm tra xem import.meta.env có tồn tại không (chỉ có trong Vite local)
const env = typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env : {};

export const projectId = env.VITE_SUPABASE_PROJECT_ID || 'yktfgqpcmdgtycnyxpby';
export const publicAnonKey = env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlrdGZncXBjbWRndHljbnl4cGJ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkzMTAyMTIsImV4cCI6MjA3NDg4NjIxMn0.SVPPUbJXcyOuuc7_uXkKEyCoXIMpF9TaN-xS5Hyr170';
