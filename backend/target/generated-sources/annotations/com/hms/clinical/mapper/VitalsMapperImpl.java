package com.hms.clinical.mapper;

import com.hms.appointment.entity.Appointment;
import com.hms.clinical.dto.request.VitalsRequestDTO;
import com.hms.clinical.dto.response.VitalsResponseDTO;
import com.hms.clinical.entity.Vitals;
import java.util.UUID;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-24T11:43:24+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
*/
@Component
public class VitalsMapperImpl implements VitalsMapper {

    @Override
    public Vitals toEntity(VitalsRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Vitals vitals = new Vitals();

        vitals.setDiastolicBP( dto.getDiastolicBP() );
        vitals.setHeight( dto.getHeight() );
        vitals.setNotes( dto.getNotes() );
        vitals.setPulseRate( dto.getPulseRate() );
        vitals.setRespiratoryRate( dto.getRespiratoryRate() );
        vitals.setSpo2( dto.getSpo2() );
        vitals.setSystolicBP( dto.getSystolicBP() );
        vitals.setTemperature( dto.getTemperature() );
        vitals.setWeight( dto.getWeight() );

        return vitals;
    }

    @Override
    public VitalsResponseDTO toDto(Vitals entity) {
        if ( entity == null ) {
            return null;
        }

        VitalsResponseDTO vitalsResponseDTO = new VitalsResponseDTO();

        vitalsResponseDTO.setAppointmentId( entityAppointmentId( entity ) );
        vitalsResponseDTO.setCreatedAt( entity.getCreatedAt() );
        vitalsResponseDTO.setDiastolicBP( entity.getDiastolicBP() );
        vitalsResponseDTO.setHeight( entity.getHeight() );
        vitalsResponseDTO.setId( entity.getId() );
        vitalsResponseDTO.setNotes( entity.getNotes() );
        vitalsResponseDTO.setPulseRate( entity.getPulseRate() );
        vitalsResponseDTO.setRespiratoryRate( entity.getRespiratoryRate() );
        vitalsResponseDTO.setSpo2( entity.getSpo2() );
        vitalsResponseDTO.setSystolicBP( entity.getSystolicBP() );
        vitalsResponseDTO.setTemperature( entity.getTemperature() );
        vitalsResponseDTO.setWeight( entity.getWeight() );

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
