package ru.vasiliev.sandbox.visionlabs.domain.model;

import android.support.annotation.Nullable;

public class SearchResultPerson {

    public String firstName;

    public String middleName;

    public String lastName;

    @Nullable
    public Boolean sex;

    public String identification;

    public String placeOfBirth;

    public String photo;

    public double similarity;
}
