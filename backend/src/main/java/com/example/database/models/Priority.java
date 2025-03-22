package com.example.database.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "priorities")
public class Priority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @OneToMany(mappedBy = "priority")
    private Set<Task> tasks = new HashSet<>();
}