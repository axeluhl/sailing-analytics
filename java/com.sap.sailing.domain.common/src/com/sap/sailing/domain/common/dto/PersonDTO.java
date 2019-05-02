package com.sap.sailing.domain.common.dto;

import java.util.Date;
import java.util.Objects;

import com.sap.sse.security.shared.dto.NamedDTO;

public class PersonDTO extends NamedDTO {
    private static final long serialVersionUID = 7248019801310303654L;
    private Date dateOfBirth;
    private String description;
    private String nationalityThreeLetterIOCAcronym;

    PersonDTO() {} // for GWT de-serialization only

    public PersonDTO(String name, Date dateOfBirth, String description, String nationalityThreeLetterIOCAcronym) {
        super(name);
        this.dateOfBirth = dateOfBirth;
        this.description = description;
        this.nationalityThreeLetterIOCAcronym = nationalityThreeLetterIOCAcronym;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getDescription() {
        return description;
    }

    public String getNationalityThreeLetterIOCAcronym() {
        return nationalityThreeLetterIOCAcronym;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(dateOfBirth, description, nationalityThreeLetterIOCAcronym);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        PersonDTO other = (PersonDTO) obj;
        return Objects.equals(dateOfBirth, other.dateOfBirth) && Objects.equals(description, other.description)
                && Objects.equals(nationalityThreeLetterIOCAcronym, other.nationalityThreeLetterIOCAcronym);
    }
}
