package com.simplecommerce_mdm.cloudinary.controller;

import com.simplecommerce_mdm.cloudinary.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test/upload") // Dùng một đường dẫn test riêng biệt
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/avatar") // Test thử chức năng upload avatar
    public ResponseEntity<CloudinaryService.UploadResult> uploadAvatarTest(@RequestParam("file") MultipartFile file) {
        // Test bằng cách upload avatar cho user có ID là 1
        // Trong thực tế, ID này sẽ được lấy từ user đang đăng nhập
        Long testUserId = 1L;
        CloudinaryService.UploadResult result = cloudinaryService.uploadAvatar(file, testUserId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/multiple") // Test thử chức năng upload nhiều file
    public ResponseEntity<List<CloudinaryService.UploadResult>> uploadMultipleFilesTest(@RequestParam("files") MultipartFile[] files) {
        // Test bằng cách upload vào một thư mục test chung
        String testFolder = "test/multiple";
        List<CloudinaryService.UploadResult> results = cloudinaryService.uploadMultipleFiles(files, testFolder, Collections.singletonList("test_multiple_upload"));
        return ResponseEntity.ok(results);
    }
} 