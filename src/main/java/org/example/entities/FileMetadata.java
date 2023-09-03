package org.example.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    @Id
    private String id;
    private String fileName;
    private long createdAt;
    private long size;
    private String fileType;
    private String filePath;
    private String metadata;

    public FileMetadata(String id, String number, String contentType, int i) {
    }
}