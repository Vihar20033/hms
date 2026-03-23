package com.hms.laboratory.mapper;

import com.hms.laboratory.dto.request.LabReportRequestDTO;
import com.hms.laboratory.dto.request.LabTestRequestDTO;
import com.hms.laboratory.dto.response.LabReportResponseDTO;
import com.hms.laboratory.dto.response.LabTestResponseDTO;
import com.hms.laboratory.entity.LabReport;
import com.hms.laboratory.entity.LabTest;
import com.hms.patient.entity.Patient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-23T18:50:32+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class LabMapperImpl implements LabMapper {

    @Override
    public LabTest toEntity(LabTestRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        LabTest labTest = new LabTest();

        labTest.setTestName( dto.getTestName() );
        labTest.setTestCode( dto.getTestCode() );
        labTest.setPrice( dto.getPrice() );
        labTest.setDescription( dto.getDescription() );
        labTest.setCategory( dto.getCategory() );

        return labTest;
    }

    @Override
    public LabTestResponseDTO toDto(LabTest entity) {
        if ( entity == null ) {
            return null;
        }

        LabTestResponseDTO labTestResponseDTO = new LabTestResponseDTO();

        labTestResponseDTO.setTestName( entity.getTestName() );
        labTestResponseDTO.setTestCode( entity.getTestCode() );
        labTestResponseDTO.setPatientId( entityPatientId( entity ) );
        labTestResponseDTO.setPatientName( entityPatientName( entity ) );
        labTestResponseDTO.setId( entity.getId() );
        labTestResponseDTO.setCategory( entity.getCategory() );
        labTestResponseDTO.setPrice( entity.getPrice() );
        labTestResponseDTO.setStatus( entity.getStatus() );
        labTestResponseDTO.setRequestedDate( entity.getRequestedDate() );
        labTestResponseDTO.setCompletedDate( entity.getCompletedDate() );

        mapDoctorDetails( labTestResponseDTO, entity );

        return labTestResponseDTO;
    }

    @Override
    public List<LabTestResponseDTO> toDtoList(List<LabTest> entities) {
        if ( entities == null ) {
            return null;
        }

        List<LabTestResponseDTO> list = new ArrayList<LabTestResponseDTO>( entities.size() );
        for ( LabTest labTest : entities ) {
            list.add( toDto( labTest ) );
        }

        return list;
    }

    @Override
    public LabReport toReportEntity(LabReportRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        LabReport labReport = new LabReport();

        labReport.setFindings( dto.getFindings() );
        labReport.setResult( dto.getResult() );
        labReport.setUnit( dto.getUnit() );
        labReport.setReferenceRange( dto.getReferenceRange() );
        labReport.setRemarks( dto.getRemarks() );
        labReport.setPerformedBy( dto.getPerformedBy() );

        return labReport;
    }

    @Override
    public LabReportResponseDTO toReportDto(LabReport entity) {
        if ( entity == null ) {
            return null;
        }

        LabReportResponseDTO labReportResponseDTO = new LabReportResponseDTO();

        labReportResponseDTO.setLabTestId( entityLabTestId( entity ) );
        labReportResponseDTO.setTestName( entityLabTestTestName( entity ) );
        labReportResponseDTO.setTestCode( entityLabTestTestCode( entity ) );
        labReportResponseDTO.setPatientName( entityLabTestPatientName( entity ) );
        labReportResponseDTO.setId( entity.getId() );
        labReportResponseDTO.setFindings( entity.getFindings() );
        labReportResponseDTO.setResult( entity.getResult() );
        labReportResponseDTO.setUnit( entity.getUnit() );
        labReportResponseDTO.setReferenceRange( entity.getReferenceRange() );
        labReportResponseDTO.setRemarks( entity.getRemarks() );
        labReportResponseDTO.setPerformedBy( entity.getPerformedBy() );
        labReportResponseDTO.setCreatedAt( entity.getCreatedAt() );
        labReportResponseDTO.setUpdatedAt( entity.getUpdatedAt() );

        return labReportResponseDTO;
    }

    @Override
    public List<LabReportResponseDTO> toReportDtoList(List<LabReport> entities) {
        if ( entities == null ) {
            return null;
        }

        List<LabReportResponseDTO> list = new ArrayList<LabReportResponseDTO>( entities.size() );
        for ( LabReport labReport : entities ) {
            list.add( toReportDto( labReport ) );
        }

        return list;
    }

    private UUID entityPatientId(LabTest labTest) {
        if ( labTest == null ) {
            return null;
        }
        Patient patient = labTest.getPatient();
        if ( patient == null ) {
            return null;
        }
        UUID id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityPatientName(LabTest labTest) {
        if ( labTest == null ) {
            return null;
        }
        Patient patient = labTest.getPatient();
        if ( patient == null ) {
            return null;
        }
        String name = patient.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID entityLabTestId(LabReport labReport) {
        if ( labReport == null ) {
            return null;
        }
        LabTest labTest = labReport.getLabTest();
        if ( labTest == null ) {
            return null;
        }
        UUID id = labTest.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityLabTestTestName(LabReport labReport) {
        if ( labReport == null ) {
            return null;
        }
        LabTest labTest = labReport.getLabTest();
        if ( labTest == null ) {
            return null;
        }
        String testName = labTest.getTestName();
        if ( testName == null ) {
            return null;
        }
        return testName;
    }

    private String entityLabTestTestCode(LabReport labReport) {
        if ( labReport == null ) {
            return null;
        }
        LabTest labTest = labReport.getLabTest();
        if ( labTest == null ) {
            return null;
        }
        String testCode = labTest.getTestCode();
        if ( testCode == null ) {
            return null;
        }
        return testCode;
    }

    private String entityLabTestPatientName(LabReport labReport) {
        if ( labReport == null ) {
            return null;
        }
        LabTest labTest = labReport.getLabTest();
        if ( labTest == null ) {
            return null;
        }
        Patient patient = labTest.getPatient();
        if ( patient == null ) {
            return null;
        }
        String name = patient.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
