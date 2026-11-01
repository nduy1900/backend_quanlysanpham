package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.user.UpdateUserRoleRequestDTO;
import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.entity.Role;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.entity.UserRole;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.UserResponseMapper;
import com.example.Quanlysanpham.repository.RoleRepository;
import com.example.Quanlysanpham.repository.UserRepository;
import com.example.Quanlysanpham.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserResponseMapper mapper;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;


    // Lấy danh sách người dùng (ADMIN)
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> dtoList = new ArrayList<>();

        for (User user : users) {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setFullName(user.getFullName());

            // Lấy Role từ bảng trung gian
            for (UserRole userRole : user.getUserRoles()) {
                Role role = roleRepository.findById(
                        userRole.getRole().getId()).orElse(null);

                dto.setRole(role.getName());
            }

            dtoList.add(dto);
        }
        return dtoList;

    }

    // Phân role và trạng thái tài khoản (ADMIN)
    @Transactional
    public void updateUser(int userId, UpdateUserRoleRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy user có id = " + userId)
        );


        // Cập nhật Role nếu đã tồn tại role
        if (request.getRoleId() != null) {
            List<Integer> rolesId = request.getRoleId();

            // Tìm các Role trong database
            List<Role> roles = roleRepository.findByIdIn(rolesId);


            // Xóa TẤT CẢ role cũ của user (an toàn, không cần SELECT)
            userRoleRepository.deleteAllByUserId(userId);

            // Tạo UserRole mới
            List<UserRole> userRoles = roles.stream()
                    .map(role -> {
                        UserRole userRole = new UserRole();

                        userRole.setUser(user);
                        userRole.setRole(role);
                        userRole.setAssignedAt(LocalDateTime.now());

                        return userRole;
                    })
                    .toList();

            // Lưu danh sách UserRole
            userRoleRepository.saveAll(userRoles);
        }

        //Cập nhật trạng thái tài khoản
        if (request.isEnabled()) {
            user.setEnabled(true);
        } else {
            user.setEnabled(false);
        }
        userRepository.save(user);
    }
}
