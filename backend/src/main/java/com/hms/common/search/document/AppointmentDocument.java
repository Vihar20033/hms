package com.hms.common.search.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
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
 * Elasticsearch document for Appointment entity.
 * Supports searching by patient name, doctor name, and appointment details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(
        indexName = "appointments",
        versionType = "EXTERNAL_GTE"
)
@Setting(settingPath = "elasticsearch/appointment-settings.json")
public class AppointmentDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long patientId;

    @Field(type = FieldType.Keyword)
    private String patientName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String patientNameSearchable;

    @Field(type = FieldType.Text, analyzer = "phonetic_analyzer")
    private String patientNamePhonetic;

    @Field(type = FieldType.Long)
    private Long doctorId;

    @Field(type = FieldType.Keyword)
    private String doctorName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String doctorNameSearchable;

    @Field(type = FieldType.Text, analyzer = "phonetic_analyzer")
    private String doctorNamePhonetic;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Date)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Instant appointmentTime;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String reason;

    @Field(type = FieldType.Text)
    private String notes;

    @Field(type = FieldType.Keyword)
    private String tokenNumber;

    @Field(type = FieldType.Boolean)
    private Boolean hasPrescription;

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
