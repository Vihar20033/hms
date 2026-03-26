package com.hms.appointment.specification;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;


public class AppointmentSpecification {

    private AppointmentSpecification() {
    }

    public static Specification<Appointment> hasDoctorId(Long doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) return null;
            return cb.equal(root.get("doctor").get("id"), doctorId);
        };
    }

    public static Specification<Appointment> hasPatientId(Long patientId) {
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

    public static Specification<Appointment> hasDoctorUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("doctor").get("userId"), userId);
        };
    }

    /**
     * Fuzzy search for appointments by patient name or doctor name.
     */
    public static Specification<Appointment> fuzzySearch(String query) {
        return (root, query1, cb) -> {
            if (query == null || query.isBlank()) {
                return null;
            }

            String pattern = "%" + query.toLowerCase() + "%";

            // Search patient name
            Specification<Appointment> patientName = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("patient").get("name")), pattern);

            // Search doctor first/last name
            Specification<Appointment> doctorFirstName = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("doctor").get("firstName")), pattern);
            Specification<Appointment> doctorLastName = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("doctor").get("lastName")), pattern);

            return cb.or(
                patientName.toPredicate(root, query1, cb),
                doctorFirstName.toPredicate(root, query1, cb),
                doctorLastName.toPredicate(root, query1, cb)
            );
        };
    }
}
