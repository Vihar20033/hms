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

import java.time.Instant;

/**
 * Elasticsearch document for Prescription entity.
 * Supports searching by patient, doctor, medicines, and diagnosis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(
        indexName = "prescriptions",
        versionType = "EXTERNAL_GTE"
)
@Setting(settingPath = "elasticsearch/prescription-settings.json")
public class PrescriptionDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long patientId;

    @Field(type = FieldType.Keyword)
    private String patientName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String patientNameSearchable;

    @Field(type = FieldType.Long)
    private Long doctorId;

    @Field(type = FieldType.Keyword)
    private String doctorName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String doctorNameSearchable;

    @Field(type = FieldType.Long)
    private Long appointmentId;

    @Field(type = FieldType.Text)
    private String symptoms;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String diagnosis;

    @Field(type = FieldType.Nested)
    private String medicines;

    @Field(type = FieldType.Text)
    private String advice;

    @Field(type = FieldType.Text)
    private String notes;

    @Field(type = FieldType.Keyword)
    private String reportUrl;

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
