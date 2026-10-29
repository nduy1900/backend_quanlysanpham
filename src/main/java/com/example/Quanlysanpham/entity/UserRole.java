package com.example.Quanlysanpham.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    // role_id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
