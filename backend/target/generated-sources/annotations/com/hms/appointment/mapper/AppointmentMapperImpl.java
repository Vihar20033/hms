package com.hms.appointment.mapper;

import com.hms.appointment.dto.request.AppointmentRequestDTO;
import com.hms.appointment.dto.response.AppointmentResponseDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.doctor.entity.Doctor;
import com.hms.patient.entity.Patient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-24T17:22:26+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
*/
@Component
public class AppointmentMapperImpl implements AppointmentMapper {

    @Override
    public AppointmentResponseDTO toDto(Appointment entity) {
        if ( entity == null ) {
            return null;
        }

        AppointmentResponseDTO appointmentResponseDTO = new AppointmentResponseDTO();

        appointmentResponseDTO.setPatientId( entityPatientId( entity ) );
        appointmentResponseDTO.setPatientName( entityPatientName( entity ) );
        appointmentResponseDTO.setDoctorId( entityDoctorId( entity ) );
        appointmentResponseDTO.setAppointmentTime( entity.getAppointmentTime() );
        appointmentResponseDTO.setDepartment( entity.getDepartment() );
        appointmentResponseDTO.setId( entity.getId() );
        appointmentResponseDTO.setReason( entity.getReason() );
        appointmentResponseDTO.setStatus( entity.getStatus() );
        appointmentResponseDTO.setTokenNumber( entity.getTokenNumber() );

        appointmentResponseDTO.setDoctorName( entity.getDoctor() != null ? entity.getDoctor().getFirstName() + " " + entity.getDoctor().getLastName() : null );

        return appointmentResponseDTO;
    }

    @Override
    public Appointment toEntity(AppointmentRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Appointment appointment = new Appointment();

        appointment.setDepartment( dto.getDepartment() );
        appointment.setEmergency( dto.isEmergency() );
        appointment.setNotes( dto.getNotes() );
        appointment.setReason( dto.getReason() );

        appointment.setAppointmentTime( java.time.LocalDateTime.of(dto.getAppointmentDate(), dto.getAppointmentTime()) );

        return appointment;
    }

    @Override
    public List<AppointmentResponseDTO> toDtoList(List<Appointment> entities) {
        if ( entities == null ) {
            return null;
        }

        List<AppointmentResponseDTO> list = new ArrayList<AppointmentResponseDTO>( entities.size() );
        for ( Appointment appointment : entities ) {
            list.add( toDto( appointment ) );
        }

        return list;
    }

    private UUID entityPatientId(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Patient patient = appointment.getPatient();
        if ( patient == null ) {
            return null;
        }
        UUID id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityPatientName(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Patient patient = appointment.getPatient();
        if ( patient == null ) {
            return null;
        }
        String name = patient.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID entityDoctorId(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Doctor doctor = appointment.getDoctor();
        if ( doctor == null ) {
            return null;
        }
        UUID id = doctor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
