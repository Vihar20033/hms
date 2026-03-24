package com.hms.appointment.specification;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentSpecification {

    private AppointmentSpecification() {
    }

    public static Specification<Appointment> hasDoctorId(UUID doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) return null;
            return cb.equal(root.get("doctor").get("id"), doctorId);
        };
    }

    public static Specification<Appointment> hasPatientId(UUID patientId) {
        return (root, query, cb) -> {
            if (patientId == null) return null;
            return cb.equal(root.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Appointment> hasStatus(AppointmentStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Appointment> hasDepartment(Department department) {
        return (root, query, cb) -> {
            if (department == null) return null;
            return cb.equal(root.get("department"), department);
        };
    }

    public static Specification<Appointment> hasTimeBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) return cb.between(root.get("appointmentTime"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("appointmentTime"), start);
            return cb.lessThanOrEqualTo(root.get("appointmentTime"), end);
        };
    }

    public static Specification<Appointment> isEmergency(Boolean isEmergency) {
        return (root, query, cb) -> {
            if (isEmergency == null) return null;
            return cb.equal(root.get("isEmergency"), isEmergency);
        };
    }

    public static Specification<Appointment> hasDoctorUserId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("doctor").get("userId"), userId);
        };
    }
}
