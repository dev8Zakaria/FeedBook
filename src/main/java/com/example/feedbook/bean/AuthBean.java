package com.example.feedbook.bean;

import com.example.feedbook.entity.User;
import com.example.feedbook.service.AuthService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@SessionScoped
public class AuthBean implements Serializable {

    @Inject
    private AuthService authService;

    private User currentUser;

    // Login Form Data
    private String email;
    private String password;

    // Registration Form Data
    private String regUsername;
    private String regEmail;
    private String regPassword;

    public String doLogin() {
        try {
            currentUser = authService.login(email, password);
            return "/index.xhtml?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            String detail = (e.getMessage() != null) ? e.getMessage() : e.toString();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", detail));
            return null;
        }
    }

    public String doRegister() {
        try {
            currentUser = authService.register(regUsername, regEmail, regPassword);
            return "/index.xhtml?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace(); // Log it to the server console too
            String detail = (e.getMessage() != null) ? e.getMessage() : e.toString();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", detail));
            return null;
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        currentUser = null;
        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRegUsername() { return regUsername; }
    public void setRegUsername(String regUsername) { this.regUsername = regUsername; }

    public String getRegEmail() { return regEmail; }
    public void setRegEmail(String regEmail) { this.regEmail = regEmail; }

    public String getRegPassword() { return regPassword; }
    public void setRegPassword(String regPassword) { this.regPassword = regPassword; }
}
