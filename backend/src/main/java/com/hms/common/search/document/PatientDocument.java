package com.hms.common.search.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hms.common.enums.BloodGroup;
import com.hms.common.enums.Gender;
import com.hms.common.enums.UrgencyLevel;
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
import java.time.LocalDate;

/**
 * Elasticsearch document for Patient entity.
 * Supports fuzzy search and phonetic matching on patient names and contact information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(
        indexName = "patients",
        versionType = "EXTERNAL_GTE"
)
@Setting(settingPath = "elasticsearch/patient-settings.json")
public class PatientDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String nameSearchable;

    @Field(type = FieldType.Text, analyzer = "phonetic_analyzer")
    private String namePhonetic;

    @Field(type = FieldType.Integer)
    private Integer age;

    @Field(type = FieldType.Date)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Field(type = FieldType.Keyword)
    private String gender;

    @Field(type = FieldType.Keyword)
    private String bloodGroup;

    @Field(type = FieldType.Text, fielddata = true)
    private String prescription;

    @Field(type = FieldType.Keyword)
    private String dose;

    @Field(type = FieldType.Scaled_Float)
    private BigDecimal fees;

    @Field(type = FieldType.Keyword)
    private String contactNumber;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String contactNumberSearchable;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String emailSearchable;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Keyword)
    private String urgencyLevel;

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
