package ru.vasiliev.sandbox.visionlabs.domain.model;

public class CreateAccountRequest {

    private String email;

    private String organization_name;

    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrganizationName() {
        return organization_name;
    }

    public void setOrganizationName(String organization_name) {
        this.organization_name = organization_name;
    }
}
