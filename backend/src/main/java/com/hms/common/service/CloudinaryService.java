package com.hms.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hms.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    @Value("${hms.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${hms.cloudinary.api-key}")
    private String apiKey;

    @Value("${hms.cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            log.warn("Cloudinary configuration is incomplete. File upload features will be skipped.");
            return;
        }
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String uploadFile(File file, String folder) {
        validateCloudinaryRequest(folder);
        if (file == null || !file.exists() || !file.isFile()) {
            throw new BadRequestException("A valid file is required for upload.");
        }
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", "hms/" + folder,
                    "resource_type", "auto"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Cloudinary upload failed for file: {}", file.getName(), e);
            return null;
        }
    }

    public String uploadBytes(byte[] bytes, String fileName, String folder) {
        validateCloudinaryRequest(folder);
        if (bytes == null || bytes.length == 0) {
            throw new BadRequestException("File content is required for upload.");
        }
        if (isBlank(fileName)) {
            throw new BadRequestException("File name is required for upload.");
        }
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "folder", "hms/" + folder,
                    "public_id", fileName,
                    "resource_type", "auto"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Cloudinary upload failed for bytes: {}", fileName, e);
            return null;
        }
    }

    /**
     * Fix #6 - PDF Report Orphaned Files
     * Deletes a file from Cloudinary given its publicId.
     */
    public void deleteFile(String publicId) {
        try {
            if (cloudinary == null) {
                log.warn("Cloudinary client is not initialized. Skipping delete for {}", publicId);
                return;
            }
            if (publicId != null && !publicId.isBlank()) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Deleted file from Cloudinary: {}", publicId);
            }
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
        }
    }

    private void validateCloudinaryRequest(String folder) {
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured.");
        }
        if (isBlank(folder)) {
            throw new BadRequestException("Upload folder is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
