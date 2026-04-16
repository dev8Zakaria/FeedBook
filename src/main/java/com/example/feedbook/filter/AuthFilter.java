package com.example.feedbook.filter;

import com.example.feedbook.bean.AuthBean;
import com.example.feedbook.entity.Role;
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

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse res  = (HttpServletResponse) response;

        String path = req.getRequestURI();

        boolean isLoginRequest    = path.endsWith("login.xhtml");
        boolean isRegisterRequest = path.endsWith("register.xhtml");
        boolean isResourceRequest = path.startsWith(req.getContextPath() + "/jakarta.faces.resource");
        boolean isAdminRequest    = path.contains("/admin/");

        boolean loggedIn = authBean != null && authBean.isLoggedIn();
        boolean isAdmin  = loggedIn
                && authBean.getCurrentUser() != null
                && authBean.getCurrentUser().getRole() == Role.ADMIN;

        if (loggedIn) {
            if (isLoginRequest || isRegisterRequest) {
                // Already authenticated — bounce away from login/register
                res.sendRedirect(req.getContextPath() + "/index.xhtml");
            } else if (isAdminRequest && !isAdmin) {
                // Logged in but not an admin — forbidden
                res.sendRedirect(req.getContextPath() + "/index.xhtml");
            } else {
                chain.doFilter(request, response);
            }
        } else {
            // Unauthenticated
            if (isLoginRequest || isRegisterRequest || isResourceRequest) {
                chain.doFilter(request, response);
            } else {
                res.sendRedirect(req.getContextPath() + "/login.xhtml");
            }
        }
    }
}