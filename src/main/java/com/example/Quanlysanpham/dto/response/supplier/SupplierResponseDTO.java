package com.example.Quanlysanpham.dto.response.supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDTO {
    private int id;
    private String name;
    private String phone;
    private String email;
}
