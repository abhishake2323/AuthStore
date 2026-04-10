package com.vault.logging.network;

import com.vault.logging.ai.AiAnomalyDetector;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UdpServer {

    private static final Logger log = LoggerFactory.getLogger(UdpServer.class);

    @Value("${udp.port}")
    private int port;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private AiAnomalyDetector anomalyDetector;

    private String generateHmac(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    @PostConstruct
    public void startServer() {
        // Start UDP listener in a separate thread to avoid blocking application startup
        Thread serverThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                log.info("UDP Logging Server listening on port {}", port);
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // blocking call
                    
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    
                    int hmacIndex = message.lastIndexOf("|HMAC=");
                    if (hmacIndex == -1) {
                        log.warn("Dropped packet: Missing HMAC signature.");
                        continue;
                    }
                    
                    String payload = message.substring(0, hmacIndex);
                    String receivedHmac = message.substring(hmacIndex + 6);
                    
                    if (!generateHmac(payload).equals(receivedHmac)) {
                        log.error("Dropped packet: Invalid HMAC signature! Potential spoofing attempt.");
                        continue;
                    }
                    
                    log.info("Verified Datagram: {}", payload);
                    anomalyDetector.analyzeLog(payload);
                }
            } catch (Exception e) {
                log.error("UDP Server encountered a critical error", e);
            }
        });
        
        // Starting as virtual thread for high-concurrency background processing (Java 21 native support)
        Thread.ofVirtual().name("udp-server").start(serverThread);
    }
}
