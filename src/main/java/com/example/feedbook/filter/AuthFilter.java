package com.example.feedbook.filter;

import com.example.feedbook.bean.AuthBean;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Inject
    private AuthBean authBean;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        boolean isLoginRequest = path.endsWith("login.xhtml");
        boolean isRegisterRequest = path.endsWith("register.xhtml");
        boolean isResourceRequest = path.startsWith(req.getContextPath() + "/jakarta.faces.resource");
        
        // Let user proceed if logged in
        if (authBean != null && authBean.isLoggedIn()) {
            if (isLoginRequest || isRegisterRequest) {
                // Already authenticated users don't need to see login/register screens
                res.sendRedirect(req.getContextPath() + "/index.xhtml");
            } else {
                chain.doFilter(request, response);
            }
        } else {
            // Unauthenticated User
            if (isLoginRequest || isRegisterRequest || isResourceRequest) {
                // Let them access login, register natively, and any CSS/JS resources
                chain.doFilter(request, response);
            } else {
                // Anything else strictly routes them back to the login page
                res.sendRedirect(req.getContextPath() + "/login.xhtml");
            }
        }
    }
}
