package com.example.feedbook.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@ApplicationScoped
public class ImageService {

    // Store directly in the project's webapp/resources directory so it is permanent across redeploys
    private static final String UPLOAD_DIR = "c:/Users/zakar/IdeaProjects/FeedBook/src/main/webapp/resources/uploads/";

    public String saveImage(Part filePart) throws Exception {
        if (filePart == null || filePart.getSize() == 0 || filePart.getSubmittedFileName() == null || filePart.getSubmittedFileName().isEmpty()) {
            return null;
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalName = filePart.getSubmittedFileName();
        String extension = "";
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex);
        }
        
        String newFilename = UUID.randomUUID().toString() + extension;
        File targetFile = new File(uploadDir, newFilename);

        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return "uploads/" + newFilename;
    }
}
