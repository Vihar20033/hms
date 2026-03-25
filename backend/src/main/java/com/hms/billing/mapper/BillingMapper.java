package com.hms.billing.mapper;

import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.billing.dto.request.BillingItemRequestDTO;
import com.hms.billing.dto.response.BillingItemResponseDTO;
import com.hms.billing.entity.Billing;
import com.hms.billing.entity.BillingItem;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BillingMapper {

    Billing toEntity(BillingRequestDTO dto);

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "appointment.id", target = "appointmentId")
    BillingResponseDTO toDto(Billing entity);

    List<BillingResponseDTO> toDtoList(List<Billing> entities);

    BillingItemResponseDTO toItemDto(BillingItem entity);

    @Mapping(target = "billing", ignore = true)
    BillingItem toItemEntity(BillingItemRequestDTO dto);
}
