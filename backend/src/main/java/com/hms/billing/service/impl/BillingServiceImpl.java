package com.hms.billing.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.billing.entity.Billing;
import com.hms.billing.entity.BillingItem;
import com.hms.billing.exception.BillingNotFoundException;
import com.hms.billing.mapper.BillingMapper;
import com.hms.billing.repository.BillingRepository;
import com.hms.billing.service.BillingService;
import com.hms.common.audit.AuditLogService;
import com.hms.common.service.CloudinaryService;
import com.hms.common.service.PdfGenerationService;
import com.hms.common.enums.PaymentMethod;
import com.hms.common.enums.PaymentStatus;
import com.hms.common.enums.Role;
import com.hms.common.util.SecurityUtils;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillingMapper billingMapper;
    private final AuditLogService auditLogService;
    private final PdfGenerationService pdfGenerationService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public BillingResponseDTO createBilling(BillingRequestDTO dto) {
        Billing billing = billingMapper.toEntity(dto);
        
        // Ensure net amount is calculated if not provided
        if (billing.getNetAmount() == null) {
            billing.setNetAmount(
                    billing.getTotalAmount().add(billing.getTaxAmount()).subtract(billing.getDiscountAmount()));
        }

        Billing savedBilling = billingRepository.save(billing);
        
        // Generate and Upload PDF
        try {
            byte[] pdfBytes = pdfGenerationService.generateBillingPdf(savedBilling);
            String reportUrl = cloudinaryService.uploadBytes(pdfBytes, "bill_" + savedBilling.getInvoiceNumber(), "billings");
            savedBilling.setReportUrl(reportUrl);
            savedBilling = billingRepository.save(savedBilling);
        } catch (Exception e) {
            log.error("Failed to generate/upload billing PDF", e);
        }

        auditLogService.log(SecurityUtils.getCurrentUsername(), "BILLING_CREATE", "Billing", savedBilling.getId().toString(),
                "invoice=" + savedBilling.getInvoiceNumber());
        return billingMapper.toDto(savedBilling);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO getBillingById(Long id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found: " + id, id.toString()));
        checkOwnership(billing);
        return billingMapper.toDto(billing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingResponseDTO> getAllBillings() {
        return billingRepository.findAll().stream()
                .map(billingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<BillingResponseDTO> getBillingSlice(int page, int size) {
        return getBillingSlice(page, size, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<BillingResponseDTO> getBillingSlice(int page, int size, String query) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (query != null && !query.isBlank()) {
            return billingRepository.searchBillings(query.trim(), request).map(billingMapper::toDto);
        }
        return billingRepository.findAll(request).map(billingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingResponseDTO> getBillingsByPatientId(Long patientId) {
        List<Billing> billings = billingRepository.findByPatientId(patientId);
        if (!billings.isEmpty()) {
            checkOwnership(billings.get(0));
        }
        return billings.stream()
                .map(billingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingResponseDTO> getCurrentPatientBillings() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("No authenticated user found.");
        }
        Patient patient = patientRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new AccessDeniedException("Your patient profile is not linked yet."));
        return getBillingsByPatientId(patient.getId());
    }

    @Override
    @Transactional
    public BillingResponseDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found: " + id, id.toString()));
        billing.setPaymentStatus(status);
        Billing saved = billingRepository.save(billing);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "BILLING_PAYMENT_UPDATE", "Billing", id.toString(),
                "status=" + status);
        return billingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public BillingResponseDTO payCurrentPatientBill(Long id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found: " + id, id.toString()));
        checkOwnership(billing);
        billing.setPaymentStatus(PaymentStatus.PAID);
        billing.setPaymentMethod(PaymentMethod.ONLINE);
        Billing saved = billingRepository.save(billing);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_BILL_PAYMENT", "Billing", id.toString(), "status=PAID");
        return billingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteBilling(Long id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found: " + id, id.toString()));
        billingRepository.delete(billing);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "BILLING_DELETE", "Billing", id.toString(), "deleted=true");
    }

    @Override
    @Transactional
    public BillingResponseDTO generateBillingFromAppointment(Long appointmentId) {
        if (billingRepository.existsByAppointmentId(appointmentId)) {
            log.warn("Billing already exists for appointment: {}", appointmentId);
        }
        Billing billing = prepareBillingFromAppointment(appointmentId);
        
        Billing saved = billingRepository.save(billing);
        
        // Generate and Upload PDF
        try {
            byte[] pdfBytes = pdfGenerationService.generateBillingPdf(saved);
            String reportUrl = cloudinaryService.uploadBytes(pdfBytes, "bill_" + saved.getInvoiceNumber(), "billings");
            saved.setReportUrl(reportUrl);
            saved = billingRepository.save(saved);
        } catch (Exception e) {
            log.error("Failed to generate/upload billing PDF from appointment", e);
        }
        
        return billingMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO calculatePreviewBilling(Long appointmentId) {
        Billing billing = prepareBillingFromAppointment(appointmentId);
        return billingMapper.toDto(billing);
    }

    private Billing prepareBillingFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BillingNotFoundException("Appointment not found", appointmentId.toString()));

        BigDecimal consultationFee = (appointment.getDoctor() != null && appointment.getDoctor().getConsultationFee() != null) 
                ? appointment.getDoctor().getConsultationFee() : new BigDecimal("500.00");
        
        BigDecimal registrationFee = new BigDecimal("150.00");
        
        Billing billing = Billing.builder()
                .patient(appointment.getPatient())
                .appointment(appointment)
                .invoiceNumber("INV-" + System.currentTimeMillis() + "-" + appointmentId)
                .paymentStatus(PaymentStatus.UNPAID)
                .billingDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .items(new ArrayList<>())
                .build();

        billing.addItem(BillingItem.builder()
                .itemName("Consultation Fee - " + (appointment.getDoctor() != null ? appointment.getDoctor().getFirstName() : "General"))
                .quantity(1)
                .unitPrice(consultationFee)
                .totalValue(consultationFee)
                .build());

        billing.addItem(BillingItem.builder()
                .itemName("Registration Fee")
                .quantity(1)
                .unitPrice(registrationFee)
                .totalValue(registrationFee)
                .build());

        BigDecimal subTotal = consultationFee.add(registrationFee);
        BigDecimal taxAmount = subTotal.multiply(new BigDecimal("0.18"));
        BigDecimal discountAmount = BigDecimal.ZERO;

        billing.setTotalAmount(subTotal);
        billing.setTaxAmount(taxAmount);
        billing.setDiscountAmount(discountAmount);
        billing.setNetAmount(subTotal.add(taxAmount).subtract(discountAmount));

        return billing;
    }

    private void checkOwnership(Billing billing) {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("No authenticated user found.");
        }
        Role role = user.getRole();
        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            return;
        }
        if (role == Role.PATIENT && user.getEmail().equalsIgnoreCase(billing.getPatient().getEmail())) {
            return;
        }
        throw new AccessDeniedException("You do not have permission to access this billing record.");
    }
}
