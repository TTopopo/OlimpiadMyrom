import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @GetMapping("/admin")
    public String adminHome() {
        logger.info("[ADMIN] Открытие панели администратора");
        return "admin/index";
    }
} 