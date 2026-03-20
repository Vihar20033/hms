package com.hms.clinical.mapper;

import com.hms.clinical.dto.request.VitalsRequestDTO;
import com.hms.clinical.dto.response.VitalsResponseDTO;
import com.hms.clinical.entity.Vitals;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VitalsMapper {

    @Mapping(target = "appointment", ignore = true)
    Vitals toEntity(VitalsRequestDTO dto);

    @Mapping(source = "appointment.id", target = "appointmentId")
    VitalsResponseDTO toDto(Vitals entity);
}
