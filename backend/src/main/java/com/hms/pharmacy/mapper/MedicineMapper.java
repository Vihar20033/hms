package com.hms.pharmacy.mapper;

import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicineMapper {

    Medicine toEntity(MedicineRequestDTO dto);

    MedicineResponseDTO toDto(Medicine entity);

    void updateEntityFromDto(MedicineRequestDTO dto, @MappingTarget Medicine entity);

    List<MedicineResponseDTO> toDtoList(List<Medicine> entities);
}
