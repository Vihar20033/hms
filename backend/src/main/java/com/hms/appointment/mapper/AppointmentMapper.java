package com.hms.appointment.mapper;

import com.hms.appointment.dto.response.AppointmentResponseDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.dto.request.AppointmentRequestDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(target = "doctorName", expression = "java(entity.getDoctor() != null ? entity.getDoctor().getFirstName() + \" \" + entity.getDoctor().getLastName() : null)")
    AppointmentResponseDTO toDto(Appointment entity);

    @Mapping(target = "appointmentTime", expression = "java(java.time.LocalDateTime.of(dto.getAppointmentDate(), dto.getAppointmentTime()))")
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    Appointment toEntity(AppointmentRequestDTO dto);

    List<AppointmentResponseDTO> toDtoList(List<Appointment> entities);
}
