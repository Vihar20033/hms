package com.hms.clinical.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.exception.AppointmentNotFoundException;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.clinical.dto.request.VitalsRequestDTO;
import com.hms.clinical.dto.response.VitalsResponseDTO;
import com.hms.clinical.entity.Vitals;
import com.hms.clinical.exception.VitalsNotFoundException;
import com.hms.clinical.mapper.VitalsMapper;
import com.hms.clinical.repository.VitalsRepository;
import com.hms.clinical.service.VitalsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VitalsServiceImpl implements VitalsService {

    private final VitalsRepository vitalsRepository;
    private final AppointmentRepository appointmentRepository;
    private final VitalsMapper vitalsMapper;

    @Override
    @Transactional
    public VitalsResponseDTO recordVitals(VitalsRequestDTO dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + dto.getAppointmentId()));

        Vitals vitals = vitalsMapper.toEntity(dto);
        vitals.setAppointment(appointment);

        Vitals saved = vitalsRepository.save(vitals);
        return vitalsMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VitalsResponseDTO getVitalsByAppointment(Long appointmentId) {
        return vitalsRepository.findByAppointmentId(appointmentId)
                .map(vitalsMapper::toDto)
                .orElseThrow(() -> new VitalsNotFoundException("Vitals not found for appointment: " + appointmentId));
    }

    @Override
    @Transactional
    public VitalsResponseDTO updateVitals(Long id, VitalsRequestDTO dto) {
        Vitals vitals = vitalsRepository.findById(id)
                .orElseThrow(() -> new VitalsNotFoundException("Vitals record not found", id.toString()));

        vitals.setTemperature(dto.getTemperature());
        vitals.setSystolicBP(dto.getSystolicBP());
        vitals.setDiastolicBP(dto.getDiastolicBP());
        vitals.setPulseRate(dto.getPulseRate());
        vitals.setRespiratoryRate(dto.getRespiratoryRate());
        vitals.setSpo2(dto.getSpo2());
        vitals.setWeight(dto.getWeight());
        vitals.setHeight(dto.getHeight());
        vitals.setNotes(dto.getNotes());

        Vitals saved = vitalsRepository.save(vitals);
        return vitalsMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalsResponseDTO> getVitalsByPatientId(Long patientId) {
        return vitalsRepository.findByAppointmentPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(vitalsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteVitals(Long id) {
        if (!vitalsRepository.existsById(id)) {
            throw new VitalsNotFoundException("Vitals record not found", id.toString());
        }
        vitalsRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalsResponseDTO> getAllVitals() {
        return vitalsRepository.findAll()
                .stream()
                .map(vitalsMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
