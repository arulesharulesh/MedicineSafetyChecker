package com.example.drugchecker.repository;

import com.example.drugchecker.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
