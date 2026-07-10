package com.smartspend.service;

import com.smartspend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf");

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    public StoredFile storeReceiptFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, WEBP, or PDF files are allowed for receipts");
        }

        try {
            Path receiptsDir = Paths.get(uploadDir, "receipts");
            Files.createDirectories(receiptsDir);

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "receipt";
            String extension = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.'))
                    : "";
            String storedFileName = UUID.randomUUID() + extension;

            Path targetPath = receiptsDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(originalName, "/uploads/receipts/" + storedFileName,
                    file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store uploaded file: " + ex.getMessage(), ex);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replaceFirst("^/uploads/", "");
            Path path = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            // Non-fatal: the DB record is still removed even if disk cleanup fails.
        }
    }

    public record StoredFile(String originalFileName, String fileUrl, String contentType, long fileSizeBytes) {
    }
}
