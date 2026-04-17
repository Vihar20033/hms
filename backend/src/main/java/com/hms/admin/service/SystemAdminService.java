package com.hms.admin.service;

import com.hms.billing.exception.BillingNotFoundException;
import com.hms.billing.repository.BillingRepository;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.exception.UserNotFoundException;
import com.hms.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemAdminService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final BillingRepository billingRepository;

    public SystemAdminService(
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            BillingRepository billingRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.billingRepository = billingRepository;
    }

    @CacheEvict(value = "patients", allEntries = true)
    public void restorePatient(Long id) {
        int updated = patientRepository.restore(id);
        if (updated == 0) {
            throw new PatientNotFoundException("Patient not found for restore: " + id);
        }
    }

    @CacheEvict(value = "doctors", allEntries = true)
    public void restoreDoctor(Long id) {
        int updated = doctorRepository.restore(id);
        if (updated == 0) {
            throw new DoctorNotFoundException("Doctor not found for restore: " + id, id.toString());
        }
    }

    public void restoreUser(Long id) {
        int updated = userRepository.restore(id);
        if (updated == 0) {
            throw new UserNotFoundException("User not found for restore: " + id, id.toString());
        }
    }

    public void restoreBilling(Long id) {
        int updated = billingRepository.restore(id);
        if (updated == 0) {
            throw new BillingNotFoundException("Billing record not found for restore: " + id, id.toString());
        }
    }
}