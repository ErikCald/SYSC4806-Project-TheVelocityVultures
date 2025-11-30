package vv.pms.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import vv.pms.project.UnauthorizedAccessException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        // This will show a simple error page or message when a Professor tries to hack the URL
        ModelAndView mav = new ModelAndView("error"); // Assumes a default error view exists
        mav.addObject("errorMessage", "Access Denied: " + ex.getMessage());
        mav.addObject("status", 403);
        return mav;
    }
}