package com.example.database.repository;

import com.example.database.models.Report;
import com.example.database.models.ReportType;
import com.example.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer>
{
    List<Report> findByType(ReportType type);
    List<Report> findByCreatedBy(User user);
}