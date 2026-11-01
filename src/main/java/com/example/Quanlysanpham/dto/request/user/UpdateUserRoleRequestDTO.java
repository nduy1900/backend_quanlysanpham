package com.example.Quanlysanpham.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequestDTO {

    private List<Integer> roleId;

    @NotNull(message = "Trạng thái tài khoản không được để trống")
    private boolean enabled;
}