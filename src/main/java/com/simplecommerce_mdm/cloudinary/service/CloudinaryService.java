package com.simplecommerce_mdm.cloudinary.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * DTO chứa kết quả trả về từ Cloudinary.
     * @param publicId ID công khai của file, dùng để truy cập hoặc xóa.
     * @param url URL bảo mật của file đã upload.
     */
    public record UploadResult(String publicId, String url) {}

    // --- CÁC PHƯƠNG THỨC PUBLIC CHUYÊN BIỆT ---

    public UploadResult uploadProductImage(MultipartFile file, String productId) {
        String folder = "products/" + productId;
        return uploadFile(file, folder, Collections.singletonList("product_image"));
    }

    public UploadResult uploadAvatar(MultipartFile file, Long userId) {
        String folder = "users/avatars/" + userId;
        return uploadFile(file, folder, Collections.singletonList("user_avatar"));
    }

    public UploadResult uploadBanner(MultipartFile file) {
        return uploadFile(file, "banners", Collections.singletonList("site_banner"));
    }

    public List<UploadResult> uploadMultipleFiles(MultipartFile[] files, String folder, List<String> tags) {
        return Arrays.stream(files)
                .map(file -> uploadFile(file, folder, tags))
                .collect(Collectors.toList());
    }

    // --- PHƯƠNG THỨC ĐỂ LẤY URL ---
    
    /**
     * Generate URL from Cloudinary public ID
     * @param publicId The public ID of the uploaded file
     * @return The secure URL to access the file
     */
    public String getImageUrl(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return null;
        }
        return cloudinary.url().secure(true).generate(publicId);
    }

    // --- PHƯƠNG THỨC ĐỂ XÓA FILE ---
    
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file from Cloudinary", e);
        }
    }

    // --- PHƯƠNG THỨC LÕI (PRIVATE) ---

    private UploadResult uploadFile(MultipartFile file, String folder, List<String> tags) {
        try {
            // Tạo public_id duy nhất từ tên file gốc và timestamp
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String baseName = FilenameUtils.getBaseName(originalFilename);
            String publicId = folder + "/" + baseName + "_" + System.currentTimeMillis();

            // Xây dựng các tùy chọn upload
            Map options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "tags", tags,
                    "overwrite", true,
                    "resource_type", "auto"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            
            // Trả về cả public_id và url
            return new UploadResult(
                    uploadResult.get("public_id").toString(),
                    uploadResult.get("secure_url").toString()
            );
        } catch (IOException e) {
            throw new RuntimeException("Could not upload file to Cloudinary", e);
        }
    }
} 