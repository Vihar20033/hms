package com.hms.patient.dto.request;

import lombok.Data;

@Data
public class PatientSearchCriteria {
    private String query;
    private String name;
    private String email;
    private String bloodGroup;
    private String urgencyLevel;
}
