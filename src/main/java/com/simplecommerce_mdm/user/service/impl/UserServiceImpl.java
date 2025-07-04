package com.simplecommerce_mdm.user.service.impl;

import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import com.simplecommerce_mdm.exception.ResourceNotFoundException;
import com.simplecommerce_mdm.user.dto.UserResponse;
import com.simplecommerce_mdm.user.dto.UserUpdateRequest;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-SERVICE")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public UserResponse getLoggedInUserProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest updateRequest, MultipartFile avatarFile) {
        log.info("Updating profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Cập nhật các trường có thể thay đổi
        Optional.ofNullable(updateRequest.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(updateRequest.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(updateRequest.getPhoneNumber()).ifPresent(user::setPhoneNumber);

        // Xử lý upload ảnh đại diện nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Nếu có avatar cũ, bạn có thể cân nhắc xóa nó khỏi Cloudinary
                // Ví dụ: if (user.getAvatarUrl() != null) { extract publicId and delete }
                CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadAvatar(avatarFile, userId);
                user.setAvatarUrl(uploadResult.url());
                log.info("Avatar updated for user {}. New URL: {}", userId, uploadResult.url());
            } catch (Exception e) {
                log.error("Failed to upload avatar for user {}: {}", userId, e.getMessage());
                // Xử lý lỗi upload ảnh, có thể ném ra InvalidDataException hoặc để GlobalException xử lý
                throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
            }
        } else if (updateRequest.getAvatarUrl() != null) {
            // Nếu không có file upload nhưng có URL được cung cấp (ví dụ muốn xóa avatar hoặc dùng URL có sẵn)
            // Cẩn thận với trường hợp người dùng gửi avatarUrl rỗng để xóa ảnh.
            // Nếu muốn xóa ảnh, cần một trường boolean riêng hoặc gửi avatarUrl = ""
            user.setAvatarUrl(updateRequest.getAvatarUrl()); // Cập nhật URL được gửi từ request
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for ID: {}", userId);
        return modelMapper.map(updatedUser, UserResponse.class);
    }
}