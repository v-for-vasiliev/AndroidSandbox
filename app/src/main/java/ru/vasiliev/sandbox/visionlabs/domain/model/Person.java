package ru.vasiliev.sandbox.visionlabs.domain.model;

import android.support.annotation.Nullable;

public class Person {

    private String firstName;

    private String middleName;

    private String lastName;

    private Boolean sex;

    private String identification;

    private String placeOfBirth;

    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Nullable
    public Boolean getSex() {
        return sex;
    }

    public void setSex(@Nullable Boolean sex) {
        this.sex = sex;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }
}
