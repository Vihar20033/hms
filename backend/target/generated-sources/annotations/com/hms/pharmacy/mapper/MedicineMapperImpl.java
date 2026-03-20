package com.hms.pharmacy.mapper;

import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T16:41:08+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MedicineMapperImpl implements MedicineMapper {

    @Override
    public Medicine toEntity(MedicineRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Medicine medicine = new Medicine();

        medicine.setName( dto.getName() );
        medicine.setMedicineCode( dto.getMedicineCode() );
        medicine.setDescription( dto.getDescription() );
        medicine.setCategory( dto.getCategory() );
        medicine.setManufacturer( dto.getManufacturer() );
        medicine.setBatchNumber( dto.getBatchNumber() );
        medicine.setExpiryDate( dto.getExpiryDate() );
        medicine.setQuantityInStock( dto.getQuantityInStock() );
        medicine.setUnitPrice( dto.getUnitPrice() );
        medicine.setReorderLevel( dto.getReorderLevel() );
        medicine.setStorageLocation( dto.getStorageLocation() );
        medicine.setDosage( dto.getDosage() );
        medicine.setSideEffects( dto.getSideEffects() );
        medicine.setIsActive( dto.getIsActive() );
        medicine.setRequiresPrescription( dto.getRequiresPrescription() );

        return medicine;
    }

    @Override
    public MedicineResponseDTO toDto(Medicine entity) {
        if ( entity == null ) {
            return null;
        }

        MedicineResponseDTO medicineResponseDTO = new MedicineResponseDTO();

        if ( entity.getId() != null ) {
            medicineResponseDTO.setId( entity.getId().toString() );
        }
        medicineResponseDTO.setName( entity.getName() );
        medicineResponseDTO.setMedicineCode( entity.getMedicineCode() );
        medicineResponseDTO.setDescription( entity.getDescription() );
        medicineResponseDTO.setCategory( entity.getCategory() );
        medicineResponseDTO.setManufacturer( entity.getManufacturer() );
        medicineResponseDTO.setBatchNumber( entity.getBatchNumber() );
        medicineResponseDTO.setExpiryDate( entity.getExpiryDate() );
        medicineResponseDTO.setQuantityInStock( entity.getQuantityInStock() );
        medicineResponseDTO.setUnitPrice( entity.getUnitPrice() );
        medicineResponseDTO.setReorderLevel( entity.getReorderLevel() );
        medicineResponseDTO.setStorageLocation( entity.getStorageLocation() );
        medicineResponseDTO.setDosage( entity.getDosage() );
        medicineResponseDTO.setSideEffects( entity.getSideEffects() );
        medicineResponseDTO.setIsActive( entity.getIsActive() );
        medicineResponseDTO.setRequiresPrescription( entity.getRequiresPrescription() );
        medicineResponseDTO.setCreatedAt( entity.getCreatedAt() );
        medicineResponseDTO.setUpdatedAt( entity.getUpdatedAt() );

        return medicineResponseDTO;
    }

    @Override
    public void updateEntityFromDto(MedicineRequestDTO dto, Medicine entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setMedicineCode( dto.getMedicineCode() );
        entity.setDescription( dto.getDescription() );
        entity.setCategory( dto.getCategory() );
        entity.setManufacturer( dto.getManufacturer() );
        entity.setBatchNumber( dto.getBatchNumber() );
        entity.setExpiryDate( dto.getExpiryDate() );
        entity.setQuantityInStock( dto.getQuantityInStock() );
        entity.setUnitPrice( dto.getUnitPrice() );
        entity.setReorderLevel( dto.getReorderLevel() );
        entity.setStorageLocation( dto.getStorageLocation() );
        entity.setDosage( dto.getDosage() );
        entity.setSideEffects( dto.getSideEffects() );
        entity.setIsActive( dto.getIsActive() );
        entity.setRequiresPrescription( dto.getRequiresPrescription() );
    }

    @Override
    public List<MedicineResponseDTO> toDtoList(List<Medicine> entities) {
        if ( entities == null ) {
            return null;
        }

        List<MedicineResponseDTO> list = new ArrayList<MedicineResponseDTO>( entities.size() );
        for ( Medicine medicine : entities ) {
            list.add( toDto( medicine ) );
        }

        return list;
    }
}
