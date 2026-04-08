package com.hms.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String uploadFile(File file, String folder) {
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
}
