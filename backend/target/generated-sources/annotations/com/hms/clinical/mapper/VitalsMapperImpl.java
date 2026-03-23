package com.hms.clinical.mapper;

import com.hms.appointment.entity.Appointment;
import com.hms.clinical.dto.request.VitalsRequestDTO;
import com.hms.clinical.dto.response.VitalsResponseDTO;
import com.hms.clinical.entity.Vitals;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-23T16:42:10+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class VitalsMapperImpl implements VitalsMapper {

    @Override
    public Vitals toEntity(VitalsRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Vitals vitals = new Vitals();

        vitals.setTemperature( dto.getTemperature() );
        vitals.setSystolicBP( dto.getSystolicBP() );
        vitals.setDiastolicBP( dto.getDiastolicBP() );
        vitals.setPulseRate( dto.getPulseRate() );
        vitals.setRespiratoryRate( dto.getRespiratoryRate() );
        vitals.setSpo2( dto.getSpo2() );
        vitals.setWeight( dto.getWeight() );
        vitals.setHeight( dto.getHeight() );
        vitals.setNotes( dto.getNotes() );

        return vitals;
    }

    @Override
    public VitalsResponseDTO toDto(Vitals entity) {
        if ( entity == null ) {
            return null;
        }

        VitalsResponseDTO vitalsResponseDTO = new VitalsResponseDTO();

        vitalsResponseDTO.setAppointmentId( entityAppointmentId( entity ) );
        vitalsResponseDTO.setId( entity.getId() );
        vitalsResponseDTO.setTemperature( entity.getTemperature() );
        vitalsResponseDTO.setSystolicBP( entity.getSystolicBP() );
        vitalsResponseDTO.setDiastolicBP( entity.getDiastolicBP() );
        vitalsResponseDTO.setPulseRate( entity.getPulseRate() );
        vitalsResponseDTO.setRespiratoryRate( entity.getRespiratoryRate() );
        vitalsResponseDTO.setSpo2( entity.getSpo2() );
        vitalsResponseDTO.setWeight( entity.getWeight() );
        vitalsResponseDTO.setHeight( entity.getHeight() );
        vitalsResponseDTO.setNotes( entity.getNotes() );
        vitalsResponseDTO.setCreatedAt( entity.getCreatedAt() );

        return vitalsResponseDTO;
    }

    private UUID entityAppointmentId(Vitals vitals) {
        if ( vitals == null ) {
            return null;
        }
        Appointment appointment = vitals.getAppointment();
        if ( appointment == null ) {
            return null;
        }
        UUID id = appointment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
