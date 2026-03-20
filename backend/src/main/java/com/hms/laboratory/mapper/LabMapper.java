package com.hms.laboratory.mapper;

import com.hms.laboratory.dto.request.LabReportRequestDTO;
import com.hms.laboratory.dto.request.LabTestRequestDTO;
import com.hms.laboratory.dto.response.LabReportResponseDTO;
import com.hms.laboratory.dto.response.LabTestResponseDTO;
import com.hms.laboratory.entity.LabReport;
import com.hms.laboratory.entity.LabTest;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LabMapper {

    @Mapping(target = "testName", source = "testName")
    @Mapping(target = "testCode", source = "testCode")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "requestedBy", ignore = true)
    @Mapping(target = "requestedDate", ignore = true)
    @Mapping(target = "completedDate", ignore = true)
    @Mapping(target = "report", ignore = true)
    LabTest toEntity(LabTestRequestDTO dto);

    @Mapping(source = "testName", target = "testName")
    @Mapping(source = "testCode", target = "testCode")
    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "doctorName", ignore = true)
    LabTestResponseDTO toDto(LabTest entity);

    @org.mapstruct.AfterMapping
    default void mapDoctorDetails(@org.mapstruct.MappingTarget LabTestResponseDTO dto, LabTest entity) {
        if (entity.getRequestedBy() != null) {
            dto.setDoctorId(entity.getRequestedBy().getId());
            dto.setDoctorName(entity.getRequestedBy().getFirstName() + " " + entity.getRequestedBy().getLastName());
        }
    }

    List<LabTestResponseDTO> toDtoList(List<LabTest> entities);

    // LabReport Mappings
    @Mapping(target = "labTest", ignore = true)
    LabReport toReportEntity(LabReportRequestDTO dto);

    @Mapping(source = "labTest.id", target = "labTestId")
    @Mapping(source = "labTest.testName", target = "testName")
    @Mapping(source = "labTest.testCode", target = "testCode")
    @Mapping(source = "labTest.patient.name", target = "patientName")
    LabReportResponseDTO toReportDto(LabReport entity);

    List<LabReportResponseDTO> toReportDtoList(List<LabReport> entities);
}
