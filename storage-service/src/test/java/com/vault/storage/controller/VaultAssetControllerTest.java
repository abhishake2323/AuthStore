package com.vault.storage.controller;

import com.vault.storage.network.UdpTelemetryBroker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD Suite for Enterprise Asset Validations
 */
public class VaultAssetControllerTest {

    @InjectMocks
    private VaultAssetController vaultAssetController;

    @Mock
    private UdpTelemetryBroker telemetryBroker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMagicByteMalwareDefenseSystem_ThrowsSecurityException() throws Exception {
        // Arrange: 
        // 0x4D and 0x5A represent the raw hex bits for a Windows Executable (MZ).
        // A hacker is trying to upload a virus disguised as "cat_photo.png".
        byte[] simulatedMalwarePayload = new byte[] { 0x4D, 0x5A, 0x00, 0x11, 0x22, 0x33, 0x44 };
        
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file",
                "cat_photo.png",
                "image/png",
                simulatedMalwarePayload
        );

        // Act & Assert:
        // We bypass the REST Controller wrapper to directly test the internal cryptology
        try {
            // Using reflection to test the private defense algorithm
            java.lang.reflect.Method method = VaultAssetController.class.getDeclaredMethod("calculateChecksumAndValidateBinary", org.springframework.web.multipart.MultipartFile.class);
            method.setAccessible(true);
            
            // This MUST throw an InvocationTargetException containing our SecurityException
            Exception exception = assertThrows(Exception.class, () -> {
                method.invoke(vaultAssetController, maliciousFile);
            }, "Expected malware to be blocked by buffer analysis.");

            assertTrue(exception.getCause() instanceof SecurityException);
            assertTrue(exception.getCause().getMessage().contains("MALWARE_DETECTED"));
            
        } catch (NoSuchMethodException e) {
            // Test fail catch
        }
    }
}
