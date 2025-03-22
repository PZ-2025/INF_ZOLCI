package com.example.database.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private ReportType type;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "parameters")
    @JdbcTypeCode(SqlTypes.JSON)
    private String parameters;

    @Column(name = "file_name", unique = true)
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}