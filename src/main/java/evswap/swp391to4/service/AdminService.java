package evswap.swp391to4.service;

import evswap.swp391to4.entity.Admin;

public interface AdminService {
    Admin login(String email, String password);
}
