package com.hms.patient.mapper;

import com.hms.patient.entity.Patient;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatientMapper {

    Patient toEntity(PatientRequestDTO dto);

    PatientResponseDTO toResponse(Patient patient);

    void updateEntity(PatientRequestDTO dto, @MappingTarget Patient patient);
}
