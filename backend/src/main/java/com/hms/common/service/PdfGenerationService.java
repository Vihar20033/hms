package com.hms.common.service;

import com.hms.billing.entity.Billing;
import com.hms.billing.entity.BillingItem;
import com.hms.common.exception.BadRequestException;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.entity.PrescriptionMedicine;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@Service
public class PdfGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public byte[] generateBillingPdf(Billing billing) throws IOException {
        validateBilling(billing);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        document.add(new Paragraph("HOSPITAL MANAGEMENT SYSTEM")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY));
        document.add(new Paragraph("Invoice Details")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Patient & Invoice Info
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        infoTable.addCell(createNoBorderCell("Invoice Number: " + billing.getInvoiceNumber(), true));
        infoTable.addCell(createNoBorderCell("Date: " + billing.getBillingDate().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER), true));
        infoTable.addCell(createNoBorderCell("Patient Name: " + billing.getPatient().getName(), false));
        infoTable.addCell(createNoBorderCell("Payment Status: " + billing.getPaymentStatus(), false));
        infoTable.addCell(createNoBorderCell("Payment Method: " + (billing.getPaymentMethod() != null ? billing.getPaymentMethod() : "N/A"), false));
        infoTable.setMarginBottom(20);
        document.add(infoTable);

        // Line Items Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 15, 15, 20})).useAllAvailableWidth();
        table.addHeaderCell(createHeaderCell("Description"));
        table.addHeaderCell(createHeaderCell("Quantity"));
        table.addHeaderCell(createHeaderCell("Unit Price"));
        table.addHeaderCell(createHeaderCell("Total"));

        for (BillingItem item : billing.getItems()) {
            table.addCell(new Cell().add(new Paragraph(item.getItemName())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity()))).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(item.getUnitPrice().toString())).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(item.getTotalValue().toString())).setTextAlignment(TextAlignment.RIGHT));
        }
        document.add(table);

        // Totals
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{80, 20})).useAllAvailableWidth();
        totalsTable.setMarginTop(10);
        totalsTable.addCell(createNoBorderCell("Subtotal:", true).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(createNoBorderCell(billing.getTotalAmount().toString(), false).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(createNoBorderCell("Tax:", true).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(createNoBorderCell(billing.getTaxAmount().toString(), false).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(createNoBorderCell("Discount:", true).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(createNoBorderCell(billing.getDiscountAmount().toString(), false).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(createNoBorderCell("Net Amount:", true).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(createNoBorderCell(billing.getNetAmount().toString(), true).setTextAlignment(TextAlignment.RIGHT));
        
        document.add(totalsTable);

        // Footer
        document.add(new Paragraph("Thank you for choosing our services.")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY)
                .setFontSize(10));

        document.close();
        return baos.toByteArray();
    }

    public byte[] generatePrescriptionPdf(Prescription prescription) throws IOException {
        validatePrescription(prescription);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        document.add(new Paragraph("MEDICAL PRESCRIPTION")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE));
        document.add(new Paragraph("Hospital Management System")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Details
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        infoTable.addCell(createNoBorderCell("Patient: " + prescription.getPatient().getName(), true));
        infoTable.addCell(createNoBorderCell("Date: " + prescription.getCreatedAt().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER), true));
        infoTable.addCell(createNoBorderCell("Doctor: Dr. " + prescription.getDoctor().getFirstName() + " " + prescription.getDoctor().getLastName(), false));
        infoTable.addCell(createNoBorderCell("Department: " + prescription.getDoctor().getDepartment(), false));
        infoTable.setMarginBottom(20);
        document.add(infoTable);

        // Medical Notes
        document.add(new Paragraph("Symptoms:").setBold());
        document.add(new Paragraph(prescription.getSymptoms() != null ? prescription.getSymptoms() : "N/A").setMarginBottom(10));
        
        document.add(new Paragraph("Diagnosis:").setBold());
        document.add(new Paragraph(prescription.getDiagnosis() != null ? prescription.getDiagnosis() : "N/A").setMarginBottom(10));

        // Medicines Table
        document.add(new Paragraph("Medicines:").setBold().setMarginBottom(5));
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20})).useAllAvailableWidth();
        table.addHeaderCell(createHeaderCell("Medicine"));
        table.addHeaderCell(createHeaderCell("Dosage"));
        table.addHeaderCell(createHeaderCell("Duration"));
        table.addHeaderCell(createHeaderCell("Instructions"));

        for (PrescriptionMedicine med : prescription.getMedicines()) {
            table.addCell(new Cell().add(new Paragraph(med.getMedicineName())));
            table.addCell(new Cell().add(new Paragraph(med.getDosage() != null ? med.getDosage() : "-")).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(med.getDuration() != null ? med.getDuration() : "-")).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(med.getInstructions() != null ? med.getInstructions() : "-")));
        }
        table.setMarginBottom(20);
        document.add(table);

        // Advice
        if (prescription.getAdvice() != null && !prescription.getAdvice().isEmpty()) {
            document.add(new Paragraph("Advice/Notes:").setBold());
            document.add(new Paragraph(prescription.getAdvice()).setMarginBottom(10));
        }

        // Signature
        document.add(new Paragraph("Doctor's Signature: ____________________")
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(40));

        document.close();
        return baos.toByteArray();
    }

    private Cell createNoBorderCell(String content, boolean isBold) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        if (isBold) {
            cell.setBold();
        }
        return cell;
    }

    private Cell createHeaderCell(String content) {
        return new Cell().add(new Paragraph(content).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void validateBilling(Billing billing) {
        requireNonNull(billing, "Billing data is required for PDF generation.");
        requireNonNull(billing.getInvoiceNumber(), "Billing invoice number is required for PDF generation.");
        requireNonNull(billing.getBillingDate(), "Billing date is required for PDF generation.");
        requireNonNull(billing.getPatient(), "Billing patient details are required for PDF generation.");
        requireNonNull(billing.getPatient().getName(), "Billing patient name is required for PDF generation.");
        requireNonNull(billing.getPaymentStatus(), "Billing payment status is required for PDF generation.");
        requireNonNull(billing.getTotalAmount(), "Billing total amount is required for PDF generation.");
        requireNonNull(billing.getTaxAmount(), "Billing tax amount is required for PDF generation.");
        requireNonNull(billing.getDiscountAmount(), "Billing discount amount is required for PDF generation.");
        requireNonNull(billing.getNetAmount(), "Billing net amount is required for PDF generation.");
        List<BillingItem> items = billing.getItems();
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one billing item is required for PDF generation.");
        }
        for (BillingItem item : items) {
            requireNonNull(item, "Billing item is required for PDF generation.");
            requireNonNull(item.getItemName(), "Billing item name is required for PDF generation.");
            requireNonNull(item.getQuantity(), "Billing item quantity is required for PDF generation.");
            requireNonNull(item.getUnitPrice(), "Billing item unit price is required for PDF generation.");
            requireNonNull(item.getTotalValue(), "Billing item total value is required for PDF generation.");
        }
    }

    private void validatePrescription(Prescription prescription) {
        requireNonNull(prescription, "Prescription data is required for PDF generation.");
        requireNonNull(prescription.getPatient(), "Prescription patient details are required for PDF generation.");
        requireNonNull(prescription.getPatient().getName(), "Prescription patient name is required for PDF generation.");
        requireNonNull(prescription.getCreatedAt(), "Prescription date is required for PDF generation.");
        requireNonNull(prescription.getDoctor(), "Prescription doctor details are required for PDF generation.");
        requireNonNull(prescription.getDoctor().getFirstName(), "Doctor first name is required for PDF generation.");
        requireNonNull(prescription.getDoctor().getLastName(), "Doctor last name is required for PDF generation.");
        requireNonNull(prescription.getDoctor().getDepartment(), "Doctor department is required for PDF generation.");
        List<PrescriptionMedicine> medicines = prescription.getMedicines();
        if (medicines == null || medicines.isEmpty()) {
            throw new BadRequestException("At least one prescribed medicine is required for PDF generation.");
        }
        for (PrescriptionMedicine medicine : medicines) {
            requireNonNull(medicine, "Prescribed medicine is required for PDF generation.");
            requireNonNull(medicine.getMedicineName(), "Medicine name is required for PDF generation.");
        }
    }

    private void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new BadRequestException(message);
        }
    }
}
