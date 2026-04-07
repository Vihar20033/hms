package com.hms.dashboard.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Patient, Long> {

    @Procedure(procedureName = "sp_get_dashboard_summary")
    List<Object[]> getDashboardSummary();
}
