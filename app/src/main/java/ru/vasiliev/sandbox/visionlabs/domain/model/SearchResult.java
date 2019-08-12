package ru.vasiliev.sandbox.visionlabs.domain.model;

import java.util.List;

public class SearchResult {

    private List<SearchResultPerson> persons;

    public List<SearchResultPerson> getPersons() {
        return persons;
    }

    public void setPersons(List<SearchResultPerson> persons) {
        this.persons = persons;
    }
}
