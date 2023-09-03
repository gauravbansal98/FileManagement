package org.example.controllers;

import org.example.entities.FileMetadata;
import org.example.services.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@Controller
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listFiles(Model model) {
        List<FileMetadata> files = fileService.listFiles();
       return ResponseEntity.ok(files);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "metadata", required = false) String metadata) {
        String fileId = fileService.uploadFile(file, metadata);
git        return ResponseEntity.ok().body(fileId);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileId) throws FileNotFoundException {
        Resource resource = fileService.downloadFile(fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId, RedirectAttributes redirectAttributes) {
        fileService.deleteFile(fileId);
        return ResponseEntity.accepted().body("File Deleted");
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<String> updateFile(@PathVariable String fileId,
                                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                                   @RequestParam(value = "metadata", required = false) String metadata) {
        fileService.updateFile(fileId, file, metadata);
        return ResponseEntity.ok().body("Updated the file");
    }
}
