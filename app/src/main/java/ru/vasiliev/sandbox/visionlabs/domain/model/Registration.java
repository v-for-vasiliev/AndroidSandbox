package ru.vasiliev.sandbox.visionlabs.domain.model;

import java.util.List;

public class Registration {

    private String login;

    private String password;

    private Person person;

    private String photo;

    private List<String> personListPersons;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<String> getPersonListPersons() {
        return personListPersons;
    }

    public void setPersonListPersons(List<String> personListPersons) {
        this.personListPersons = personListPersons;
    }
}