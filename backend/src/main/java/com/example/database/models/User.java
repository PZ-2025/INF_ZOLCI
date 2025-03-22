package com.example.database.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "role", length = 50)
    private String role;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "manager")
    private Set<Team> managedTeams = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<TeamMember> teamMemberships = new HashSet<>();

    @OneToMany(mappedBy = "createdBy")
    private Set<Task> createdTasks = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<TaskComment> comments = new HashSet<>();

    @OneToMany(mappedBy = "changedBy")
    private Set<TaskHistory> taskChanges = new HashSet<>();

    @OneToMany(mappedBy = "createdBy")
    private Set<Report> reports = new HashSet<>();

    @OneToMany(mappedBy = "updatedBy")
    private Set<SystemSetting> updatedSettings = new HashSet<>();

}