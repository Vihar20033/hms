package com.hms.common.search.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Elasticsearch document for Doctor entity.
 * Supports fuzzy search and phonetic matching on doctor names and specializations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(
        indexName = "doctors",
        versionType = "EXTERNAL_GTE"
)
@Setting(settingPath = "elasticsearch/doctor-settings.json")
public class DoctorDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String firstName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String firstNameSearchable;

    @Field(type = FieldType.Text, analyzer = "phonetic_analyzer")
    private String firstNamePhonetic;

    @Field(type = FieldType.Keyword)
    private String lastName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String lastNameSearchable;

    @Field(type = FieldType.Text, analyzer = "phonetic_analyzer")
    private String lastNamePhonetic;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String fullNameSearchable;

    @Field(type = FieldType.Text)
    private String specialization;

    @Field(type = FieldType.Keyword)
    private String registrationNumber;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String emailSearchable;

    @Field(type = FieldType.Text)
    private String bio;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Keyword)
    private String qualification;

    @Field(type = FieldType.Integer)
    private Integer experienceYears;

    @Field(type = FieldType.Keyword)
    private String licenseNumber;

    @Field(type = FieldType.Scaled_Float)
    private BigDecimal consultationFee;

    @Field(type = FieldType.Boolean)
    private Boolean isAvailable;

    @Field(type = FieldType.Keyword)
    private String phoneNumber;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String phoneNumberSearchable;

    @Field(type = FieldType.Keyword)
    private String designation;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

    @Field(type = FieldType.Keyword)
    private Boolean deleted;

    @Field(type = FieldType.Keyword)
    private String createdBy;

    @Field(type = FieldType.Keyword)
    private String lastModifiedBy;
}
