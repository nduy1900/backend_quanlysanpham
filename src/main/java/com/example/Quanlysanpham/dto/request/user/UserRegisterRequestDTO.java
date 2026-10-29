package com.example.Quanlysanpham.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDTO {
    @NotBlank(message = "Tên không được bỏ trống")
    private String username;

    @Email
    private String email;

    @NotBlank(message = "Họ tên không được bỏ trống")
    private String fullName;

    @NotBlank(message = "Password không được bỏ trống")
    private String password;


}
