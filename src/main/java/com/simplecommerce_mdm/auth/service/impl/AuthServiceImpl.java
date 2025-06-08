package com.simplecommerce_mdm.auth.service.impl;



import com.simplecommerce_mdm.auth.dto.UserRegistrationDto;
import com.simplecommerce_mdm.auth.service.AuthService;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.UserRepository;
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

    @Override
    public void register(UserRegistrationDto registrationDto) {
        // 1. Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }

        // 2. Chuyển đổi DTO sang Entity
        User user = modelMapper.map(registrationDto, User.class);

        // 3. Mã hóa mật khẩu
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));

        // 4. Lưu User mới vào CSDL
        userRepository.save(user);
    }
}
