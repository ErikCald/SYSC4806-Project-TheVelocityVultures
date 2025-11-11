package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUserName")
    public String currentUserName(HttpSession session) {
        if (session == null) return null;
        Object o = session.getAttribute("currentUserName");
        return o == null ? null : o.toString();
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole(HttpSession session) {
        if (session == null) return null;
        Object o = session.getAttribute("currentUserRole");
        return o == null ? null : o.toString();
    }
}
