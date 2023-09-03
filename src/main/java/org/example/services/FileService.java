package org.example.services;

import org.example.entities.FileMetadata;
import org.example.exceptions.FileStorageException;
import org.example.exceptions.ResourceNotFoundException;
import org.example.repositories.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    public FileService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public List<FileMetadata> listFiles() {
        return fileMetadataRepository.findAll();
    }

    public String uploadFile(MultipartFile file, String metadata) {
        try {
            byte[] fileData = file.getBytes();
            String fileId = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();

            // Define the directory where you want to store the files
            String uploadDirectory = uploadDir;

            // Create a unique file path
            String filePath = uploadDirectory + File.separator + fileId + "_" + fileName;

            // Save the file to the local storage
            Path fileDestination = Path.of(filePath);
            Files.createDirectories(fileDestination.getParent());
            Files.write(fileDestination, fileData);

            FileMetadata fileMetadata = new FileMetadata(fileId, fileName, System.currentTimeMillis(), fileData.length, fileType, filePath, metadata);
            fileMetadataRepository.save(fileMetadata);

            return fileId;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store the file: " + e.getMessage());
        }
    }

    public Resource downloadFile(String fileId) {
        Optional<FileMetadata> optionalFileMetadata = fileMetadataRepository.findById(fileId);

        if (optionalFileMetadata.isPresent()) {
            FileMetadata fileMetadata = optionalFileMetadata.get();
            Path filePath = Path.of(fileMetadata.getFilePath());

            try {
                FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
                Resource resource = new InputStreamResource(fileInputStream);
                return resource;
            } catch (Exception e){
                throw new FileStorageException("Unable to read the file with ID: "+fileId);
            }
        } else {
            throw new ResourceNotFoundException("File not found with ID: " + fileId);
        }
    }

    public void deleteFile(String fileId) {
        Optional<FileMetadata> optionalFileMetadata = fileMetadataRepository.findById(fileId);

        if (optionalFileMetadata.isPresent()) {
            FileMetadata fileMetadata = optionalFileMetadata.get();
            Path filePath = Path.of(fileMetadata.getFilePath());

            try {
                Files.deleteIfExists(filePath);
                fileMetadataRepository.delete(fileMetadata);
            } catch (IOException e) {
                throw new FileStorageException("Failed to delete the file: " + e.getMessage());
            }
        } else {
            throw new ResourceNotFoundException("File not found with ID: " + fileId);
        }
    }

    public void updateFile(String fileId, MultipartFile file, String metadata) {
        Optional<FileMetadata> optionalFileMetadata = fileMetadataRepository.findById(fileId);

        if (optionalFileMetadata.isPresent()) {
            FileMetadata fileMetadata = optionalFileMetadata.get();

            if (file != null) {
                try {
                    byte[] fileData = file.getBytes();
                    String fileName = file.getOriginalFilename();
                    String filePath = uploadDir + File.separator + fileId + "_" + fileName;
                    Path fileDestination = Path.of(filePath);
                    Files.createDirectories(fileDestination.getParent());
                    Files.write(fileDestination, fileData);
                    fileMetadata.setFilePath(filePath);
                    fileMetadata.setFileName(fileName);
                    fileMetadata.setSize(fileData.length);
                    fileMetadata.setFileType(file.getContentType());
                } catch (IOException e) {
                    throw new FileStorageException("Failed to update the file: " + e.getMessage());
                }
            }

            if (metadata != null) {
                fileMetadata.setMetadata(metadata);
            }

            fileMetadataRepository.save(fileMetadata);
        } else {
            throw new ResourceNotFoundException("File not found with ID: " + fileId);
        }
    }
}
