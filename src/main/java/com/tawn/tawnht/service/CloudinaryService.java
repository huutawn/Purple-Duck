package com.tawn.tawnht.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tawn.tawnht.dto.response.CloudinaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.secret-key}")
    private String apiSecret;
    public String uploadFile(MultipartFile file, String relatedName, String relatedId) throws IOException {
        String fileName = relatedName + "_" + relatedId + "_" + System.currentTimeMillis();

        Map uploadResult = cloudinary
                .uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("public_id", fileName, "folder", "hope"));

        String url = (String) uploadResult.get("url");
        return url;
    }
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete video: " + e.getMessage());
        }
    }
    public CloudinaryResponse getSign(){
        CloudinaryResponse cloudinaryResponse=CloudinaryResponse.builder()
                .cloudName(cloudName)
                .apiKey(apiKey)
                .secretKey(apiKey)
                .build();
        return cloudinaryResponse;
    }
}
