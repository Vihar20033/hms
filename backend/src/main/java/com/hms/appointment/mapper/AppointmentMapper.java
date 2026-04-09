package com.hms.appointment.mapper;

import com.hms.appointment.dto.response.AppointmentResponseDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.dto.request.AppointmentRequestDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Instant.class})
public abstract class AppointmentMapper {

    @org.springframework.beans.factory.annotation.Autowired
    protected com.hms.prescription.repository.PrescriptionRepository prescriptionRepository;

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(target = "doctorName", expression = "java(entity.getDoctor() != null ? entity.getDoctor().getFirstName() + \" \" + entity.getDoctor().getLastName() : null)")
    @Mapping(target = "hasPrescription", expression = "java(prescriptionRepository.findByAppointmentId(entity.getId()).isPresent())")
    public abstract AppointmentResponseDTO toDto(Appointment entity);

    @Mapping(target = "appointmentTime", source = "appointmentTime")
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    public abstract Appointment toEntity(AppointmentRequestDTO dto);

    public abstract List<AppointmentResponseDTO> toDtoList(List<Appointment> entities);
}
