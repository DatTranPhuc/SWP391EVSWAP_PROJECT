package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping
    public String viewAccount(HttpSession session, Model model) {
        // Kiểm tra session
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        // Tìm lại thông tin trong DB
        Driver dbDriver = entityManager.find(Driver.class, driver.getDriverId());
        if (dbDriver == null) {
            return "redirect:/login?error=notfound";
        }

        // Lấy danh sách xe
        List<Vehicle> vehicles = entityManager.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.driver.driverId = :id", Vehicle.class)
                .setParameter("id", driver.getDriverId())
                .getResultList();

        model.addAttribute("driver", dbDriver);
        model.addAttribute("vehicles", vehicles);
        return "account"; // Thymeleaf template: account.html
    }

    @Transactional
    @PostMapping("/update")
    public String updateAccount(@ModelAttribute Driver updatedDriver, HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) return "redirect:/login";

        Driver dbDriver = entityManager.find(Driver.class, driver.getDriverId());
        if (dbDriver == null) return "redirect:/login?error=notfound";

        dbDriver.setFullName(updatedDriver.getFullName());
        dbDriver.setPhone(updatedDriver.getPhone());
        dbDriver.setEmail(updatedDriver.getEmail());

        entityManager.merge(dbDriver);
        session.setAttribute("loggedInDriver", dbDriver);

        return "redirect:/account?updated";
    }

    @Transactional
    @PostMapping("/delete")
    public String deleteAccount(HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) return "redirect:/login";

        Driver dbDriver = entityManager.find(Driver.class, driver.getDriverId());
        if (dbDriver == null) return "redirect:/login?error=notfound";

        entityManager.createQuery("DELETE FROM Vehicle v WHERE v.driver.driverId = :id")
                .setParameter("id", driver.getDriverId())
                .executeUpdate();

        entityManager.remove(entityManager.contains(dbDriver) ? dbDriver : entityManager.merge(dbDriver));
        session.invalidate();

        return "redirect:/login?deleted";
    }
}

