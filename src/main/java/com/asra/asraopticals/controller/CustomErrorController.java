package com.asra.asraopticals.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object statusCode = request.getAttribute("jakarta.servlet.error.status_code");
        if (statusCode != null) {
            int code = Integer.parseInt(statusCode.toString());
            if (code == HttpStatus.NOT_FOUND.value()) return "error/404";
            if (code == HttpStatus.FORBIDDEN.value()) return "error/403";
        }
        return "error/500";
    }
}