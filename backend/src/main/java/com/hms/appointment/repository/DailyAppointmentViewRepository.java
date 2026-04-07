package com.hms.appointment.repository;

import com.hms.appointment.entity.DailyAppointmentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyAppointmentViewRepository extends JpaRepository<DailyAppointmentView, Long> {
    
    List<DailyAppointmentView> findByDepartment(String department);
    
    List<DailyAppointmentView> findByStatus(String status);
}
