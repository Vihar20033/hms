package com.hms.doctor.mapper;

import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorResponseDTO;
import com.hms.doctor.entity.Doctor;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {

    Doctor toEntity(CreateDoctorRequest dto);

    DoctorResponseDTO toDto(Doctor entity);

    void updateEntity(UpdateDoctorRequest dto, @MappingTarget Doctor entity);

    List<DoctorResponseDTO> toDtoList(List<Doctor> entities);
}
