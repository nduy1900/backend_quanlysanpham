package com.example.Quanlysanpham.dto.response.product_image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponseDTO {
    private int id;
    private String url;
    private boolean primaryImage;
}
