package com.example.Quanlysanpham.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDTO {
    @NotBlank(message = "Tên đăng nhập không được bỏ trống")
    private String username;

    @NotBlank(message = "Password không được bỏ trống")
    private String password;
}
