package com.hms.appointment.specification;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class AppointmentSpecification {

    private AppointmentSpecification() {
    }

    public static Specification<Appointment> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Appointment> hasDoctorId(UUID doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) {
                return null;
            }
            return cb.equal(root.get("doctor").get("id"), doctorId);
        };
    }

    public static Specification<Appointment> hasPatientId(UUID patientId) {
        return (root, query, cb) -> {
            if (patientId == null) {
                return null;
            }
            return cb.equal(root.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Appointment> hasStatus(AppointmentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Appointment> hasDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) {
                return null;
            }
            return cb.equal(root.get("appointmentDate"), date);
        };
    }

    public static Specification<Appointment> hasDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate != null && endDate != null) {
                return cb.between(root.get("appointmentDate"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("appointmentDate"), startDate);
            }
            return cb.lessThanOrEqualTo(root.get("appointmentDate"), endDate);
        };
    }
}

