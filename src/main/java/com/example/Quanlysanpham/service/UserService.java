package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.mapper.UserResponseMapper;
import com.example.Quanlysanpham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserResponseMapper mapper;


    // Lấy danh sách người dùng (ADMIN)
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users
                .stream()
                .map(mapper::toResponse).toList();

    }
}
