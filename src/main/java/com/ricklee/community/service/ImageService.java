package com.ricklee.community.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ricklee.community.exception.custom.FileUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * MultipartFile을 S3에 업로드하고 URL 반환
     */
    public String uploadFile(MultipartFile file, String dirName) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = dirName + "/" + UUID.randomUUID() + extension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(
                    bucketName, fileName, file.getInputStream(), metadata));
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * byte[] 데이터를 S3에 업로드하고 URL 반환 (기존 BLOB 데이터 마이그레이션용)
     */
    public String uploadBytes(byte[] data, String contentType, String dirName) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            String fileName = dirName + "/" + UUID.randomUUID() + ".jpg"; // 기본 확장자 (필요시 동적으로 결정)

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType != null ? contentType : "image/jpeg");
            metadata.setContentLength(data.length);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

            amazonS3.putObject(new PutObjectRequest(
                    bucketName, fileName, inputStream, metadata));

            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            throw new FileUploadException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            amazonS3.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new FileUploadException("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}