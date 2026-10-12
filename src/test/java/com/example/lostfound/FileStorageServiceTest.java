package com.example.lostfound;

import com.example.lostfound.exception.BadRequestException;
import com.example.lostfound.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        fileStorageService = new FileStorageServiceImpl(tempDir.toString());
        fileStorageService.init();
    }

    @Test
    void storeFile_ShouldRejectInvalidMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/x-msdownload",
                "malicious bytes".getBytes()
        );

        assertThrows(BadRequestException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    void storeFile_ShouldRejectPathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../etc/passwd.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        assertThrows(BadRequestException.class, () -> fileStorageService.storeFile(file));
    }

    @Test
    void storeFile_ShouldStoreValidJpeg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        String storedFilename = fileStorageService.storeFile(file);
        assertNotNull(storedFilename);
        assertTrue(storedFilename.endsWith(".jpg"));
    }
}
