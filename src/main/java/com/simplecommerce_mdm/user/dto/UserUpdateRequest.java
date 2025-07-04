package com.simplecommerce_mdm.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;
    private String avatarUrl; // cập nhật URL ảnh đại diện trực tiếp
    private MultipartFile avatarFile; // Nhận MultipartFile để xử lý upload lên Cloudinary
    private String metadata;
}