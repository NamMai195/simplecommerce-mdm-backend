package com.simplecommerce_mdm.auth.service.impl;

import com.simplecommerce_mdm.auth.dto.LoginRequest;
import com.simplecommerce_mdm.auth.dto.RegisterRequest;
import com.simplecommerce_mdm.auth.dto.TokenResponse;
import com.simplecommerce_mdm.auth.service.AuthService;
import com.simplecommerce_mdm.auth.service.AuthenticationService;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            // Ném ra exception để controller xử lý và trả về lỗi cho client
            throw new IllegalStateException("Email already in use!");
        }

        // 2. Map từ RegisterRequest DTO sang User entity
        User user = modelMapper.map(registerRequest, User.class);

        // 3. Mã hóa mật khẩu
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        // Tạo UUID cho người dùng mới
        user.setUuid(java.util.UUID.randomUUID());

        // 4. Gán vai trò mặc định (
        // Role userRole = roleRepository.findByName("USER")
        //     .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // user.setRoles(java.util.Collections.singleton(userRole));

        // 5. Lưu người dùng vào cơ sở dữ liệu
        userRepository.save(user);
    }

    @Override
    public TokenResponse login(LoginRequest loginrequest) {
        return authenticationService.getAccessToken(loginrequest);
    }

    @Override
    public void logout(String authorizationHeader) {

    }

    @Override
    public TokenResponse loginWithGoogle(String googleToken) {
        return null;
    }

}
