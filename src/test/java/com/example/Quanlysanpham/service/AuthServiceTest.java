package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.refresh.RefreshTokenRequestDTO;
import com.example.Quanlysanpham.dto.request.user.UserLoginRequestDTO;
import com.example.Quanlysanpham.dto.request.user.UserRegisterRequestDTO;
import com.example.Quanlysanpham.dto.response.user.UserLoginResponseDTO;
import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.entity.RefreshToken;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.UserResponseMapper;
import com.example.Quanlysanpham.repository.UserRepository;
import com.example.Quanlysanpham.security.JwtService;
import com.example.Quanlysanpham.security.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserResponseMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;


    // register: Đăng ký tài khoản
    @Nested
    @DisplayName("register")
    class RegisterTest {
        @Test
        @DisplayName("Kiểm tra đăng ký tài khoản thành công")
        void register_ShouldReturnUserSuccessfully() {
            // Arrange
            UserRegisterRequestDTO request = new UserRegisterRequestDTO();
            request.setUsername("duynguyen");
            request.setEmail("duy@gmail.com");
            request.setFullName("Nguyen Nhan Duy");
            request.setPassword("123456");

            when(userRepository.existsByUsername("duynguyen"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("duy@gmail.com"))
                    .thenReturn(false);

            when(passwordEncoder.encode("123456"))
                    .thenReturn("hashed_password");

            UserResponseDTO responseDTO = new UserResponseDTO();

            when(userMapper.toResponse(any(User.class)))
                    .thenReturn(responseDTO);

            ArgumentCaptor<User> captor =
                    ArgumentCaptor.forClass(User.class);

            // Act
            UserResponseDTO result = authService.register(request);

            // Assert
            assertNotNull(result);

            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();

            assertEquals("duynguyen", savedUser.getUsername());
            assertEquals("duy@gmail.com", savedUser.getEmail());
            assertEquals("Nguyen Nhan Duy", savedUser.getFullName());
            assertEquals("hashed_password", savedUser.getPassword());
            assertTrue(savedUser.isEnabled());
            assertNotNull(savedUser.getCreatedAt());

            // Verify
            verify(userRepository).existsByUsername("duynguyen");
            verify(userRepository).existsByEmail("duy@gmail.com");
            verify(passwordEncoder).encode("123456");
            verify(userMapper).toResponse(savedUser);
        }

        @Test
        @DisplayName("Kiểm tra đăng ký thất bại khi username đã tồn tại")
        void register_WhenUsernameExists_ShouldThrowException() {

            // Arrange
            UserRegisterRequestDTO request = new UserRegisterRequestDTO();
            request.setUsername("duynguyen");
            request.setEmail("duy@gmail.com");
            request.setPassword("123456");

            when(userRepository.existsByUsername("duynguyen"))
                    .thenReturn(true);

            // Act & Assert
            assertThrows(
                    DuplicateResourceException.class,
                    () -> authService.register(request)
            );

            // Verify
            verify(userRepository).existsByUsername("duynguyen");

            // Do existsByUsername trả về true nên biểu thức OR không kiểm tra email
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).save(any(User.class));

            verifyNoInteractions(passwordEncoder, userMapper);
        }

        @Test
        @DisplayName("Kiểm tra đăng ký thất bại khi email đã tồn tại")
        void register_WhenEmailExists_ShouldThrowException() {
            // Arrange
            UserRegisterRequestDTO request = new UserRegisterRequestDTO();
            request.setUsername("duynguyen");
            request.setEmail("duy@gmail.com");
            request.setPassword("123456");

            when(userRepository.existsByUsername("duynguyen"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("duy@gmail.com"))
                    .thenReturn(true);

            // Act & Assert
            assertThrows(
                    DuplicateResourceException.class,
                    () -> authService.register(request)
            );

            // Verify
            verify(userRepository).existsByUsername("duynguyen");
            verify(userRepository).existsByEmail("duy@gmail.com");
            verify(userRepository, never()).save(any(User.class));

            verifyNoInteractions(passwordEncoder, userMapper);
        }

    }

    // login: Đăng nhập tài khoản
    @Nested
    @DisplayName("login")
    class LoginTest {
        @Test
        @DisplayName("Đăng nhập thành công")
        void login_ShouldReturnTokensSuccessfully() {
            // Arrange
            UserLoginRequestDTO request = new UserLoginRequestDTO();
            request.setUsername("duynguyen");
            request.setPassword("123456");

            UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("duynguyen")
                    .password("hashed_password")
                    .authorities("ROLE_ADMIN")
                    .build();

            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            when(authentication.getPrincipal()).thenReturn(userDetails);

            when(jwtService.generateAccessToken(userDetails))
                    .thenReturn("access_token");

            when(jwtService.generateRefreshToken(userDetails))
                    .thenReturn("refresh_token");

            // Act
            UserLoginResponseDTO result = authService.login(request);

            // Assert
            assertNotNull(result);
            assertEquals("access_token", result.getAccessToken());
            assertEquals("refresh_token", result.getRefreshToken());

            // Verify
            verify(authenticationManager).authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            );

            verify(jwtService).generateAccessToken(userDetails);
            verify(jwtService).generateRefreshToken(userDetails);

            verify(refreshTokenService).save(
                    eq("refresh_token"),
                    eq("duynguyen"),
                    anyString()
            );
        }

        @Test
        @DisplayName("Kiểm tra đăng nhập thất bại khi thông tin xác thực không hợp lệ")
        void login_WhenCredentialsInvalid_ShouldThrowException() {
            // Arrange
            UserLoginRequestDTO request = new UserLoginRequestDTO();
            request.setUsername("duynguyen");
            request.setPassword("wrong_password");

            when(authenticationManager.authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            )).thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            assertThrows(
                    BadCredentialsException.class,
                    () -> authService.login(request)
            );

            // Verify
            verify(authenticationManager).authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            );

            verifyNoInteractions(jwtService, refreshTokenService);
        }
    }

    // logout: Đăng xuất tài khoản
    @Nested
    @DisplayName("logout")
    class LogoutTest {
        @Test
        @DisplayName("Kiểm tra đăng xuất thành công")
        void logout_ShouldRevokeEntireTokenFamily() {
            // Arrange
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("refresh_token");

            RefreshToken refreshToken = mock(RefreshToken.class);

            when(refreshTokenService.findByToken("refresh_token"))
                    .thenReturn(refreshToken);

            when(refreshToken.getFamilyId()).thenReturn("family_123");

            // Act
            authService.logout(request);

            // Assert & Verify
            verify(refreshTokenService).findByToken("refresh_token");
            verify(refreshTokenService).revokeFamily("family_123");
        }

        @Test
        @DisplayName("Kiểm tra đăng xuất thất bại khi không tìm thấy Refresh Token")
        void logout_WhenTokenNotFound_ShouldThrowException() {
            // Arrange
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("invalid_refresh_token");

            when(refreshTokenService.findByToken("invalid_refresh_token"))
                    .thenThrow(new ResourceNotFoundException("Không tìm thấy Refresh Token"));

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> authService.logout(request)
            );

            // Verify
            verify(refreshTokenService).findByToken("invalid_refresh_token");
            verify(refreshTokenService, never()).revokeFamily(anyString());
        }
    }

}
