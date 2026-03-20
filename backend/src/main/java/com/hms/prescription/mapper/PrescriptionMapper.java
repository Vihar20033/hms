package com.hms.prescription.mapper;

import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.entity.PrescriptionMedicine;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrescriptionMapper {

    Prescription toEntity(PrescriptionRequestDTO dto);

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(target = "doctorName", expression = "java(entity.getDoctor() != null ? entity.getDoctor().getFirstName() + \" \" + entity.getDoctor().getLastName() : null)")
    @Mapping(source = "appointment.id", target = "appointmentId")
    PrescriptionResponseDTO toDto(Prescription entity);

    List<PrescriptionResponseDTO> toDtoList(List<Prescription> entities);

    PrescriptionResponseDTO.PrescriptionMedicineResponseDTO toMedicineDto(PrescriptionMedicine entity);

    @Mapping(target = "prescription", ignore = true)
    PrescriptionMedicine toMedicineEntity(PrescriptionRequestDTO.PrescriptionMedicineRequestDTO dto);
}
