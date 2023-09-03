package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controllers.FileController;
import org.example.controllers.GlobalExceptionHandler;
import org.example.entities.FileMetadata;
import org.example.exceptions.FileStorageException;
import org.example.exceptions.ResourceNotFoundException;
import org.example.services.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @MockBean
    private FileService fileService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new FileController(fileService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testListFilesSuccess() throws Exception {
        // Arrange
        List<FileMetadata> files = Arrays.asList(
                new FileMetadata("1", "file1.txt", 1234567890, 100, "text/plain", "path/to/file1.txt", "metadata1"),
                new FileMetadata("2", "file2.pdf", 1234567891, 200, "application/pdf", "path/to/file2.pdf", "metadata2")
        );
        when(fileService.listFiles()).thenReturn(files);

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/files"));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].fileName").value("file1.txt"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].fileName").value("file2.pdf"));

        verify(fileService, times(1)).listFiles();
    }

    @Test
    public void testUploadFileSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        String fileId = "12345";
        when(fileService.uploadFile(eq(file), anyString())).thenReturn(fileId);

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart("/files/upload?metadata=metadata")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(fileId));

        verify(fileService, times(1)).uploadFile(eq(file), anyString());
    }

    @Test
    public void testViewFileSuccess() throws Exception {
        // Arrange
        String fileId = "12345";
        byte[] fileData = "Test file content".getBytes();
        when(fileService.downloadFile(fileId)).thenReturn(new InputStreamResource(new ByteArrayInputStream(fileData)));

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/files/{fileId}", fileId));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(fileData));

        verify(fileService, times(1)).downloadFile(fileId);
    }

    @Test
    public void testDeleteFileSuccess() throws Exception {
        // Arrange
        String fileId = "12345";
        doNothing().when(fileService).deleteFile(fileId);

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete("/files/{fileId}", fileId));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andExpect(MockMvcResultMatchers.content().string("File Deleted"));

        verify(fileService, times(1)).deleteFile(fileId);
    }

    @Test
    public void testUpdateFileSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "updated data".getBytes());
        String fileId = "12345";
        String metadata = "Updated Metadata";
        doNothing().when(fileService).updateFile(eq(fileId), eq(file), eq(metadata));

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put("/files/"+fileId)
                .param("metadata", metadata)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Updated the file"));
    }

    @Test
    public void testUploadFileFailure() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        when(fileService.uploadFile(eq(file), anyString()))
                .thenThrow(new FileStorageException("File storage error"));

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .multipart("/files/upload?metadata=metadata")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("File storage error"));

        verify(fileService, times(1)).uploadFile(eq(file), anyString());
    }

    @Test
    public void testViewFileFailure() throws Exception {
        // Arrange
        String fileId = "12345";
        when(fileService.downloadFile(fileId))
                .thenThrow(new ResourceNotFoundException("File not found"));

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/files/{fileId}", fileId));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("File not found"));

        verify(fileService, times(1)).downloadFile(fileId);
    }

    @Test
    public void testDeleteFileFailure() throws Exception {
        // Arrange
        String fileId = "12345";
        doThrow(new ResourceNotFoundException("File not found")).when(fileService).deleteFile(fileId);

        // Act
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete("/files/{fileId}", fileId));

        // Assert
        resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("File not found"));

        verify(fileService, times(1)).deleteFile(fileId);
    }
}
