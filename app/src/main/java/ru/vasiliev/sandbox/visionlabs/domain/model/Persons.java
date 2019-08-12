package ru.vasiliev.sandbox.visionlabs.domain.model;

import java.util.List;

/**
 * Created by Aleksey on 29.01.2018.
 */
//FOR LUNA 2
public class Persons {

    public List<PersonInfo> persons;

    private int count;

    private String error_code;

    private String detail;

    public String getError_code() {
        return error_code;
    }

    public String getDetail() {
        return detail;
    }

    public int getCount() {
        return count;
    }
}
