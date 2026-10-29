package com.example.Quanlysanpham.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "primary_image")
    private boolean primaryImage;


    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
