package com.genflow.backend.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
    
@Column(nullable = false)
private String name;

@Column(nullable = false, unique = true)
private String email;

@Column(nullable = false)
private String password;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Role role = Role.USER;

@Column(nullable = false, updatable = false)
private LocalDateTime createdAt = LocalDateTime.now();
}