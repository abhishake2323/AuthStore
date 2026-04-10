package com.vault.storage.network;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class NetworkLogEmitter {

    @Value("${logging.service.host}")
    private String logHost;

    @Value("${logging.service.port}")
    private int logPort;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String generateHmac(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    // Async annotation ensures this fire-and-forget logic uses Virtual Threads (if configured)
    @Async
    public void sendAuditLog(String logMessage) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(logHost);
            
            String hmac = generateHmac(logMessage);
            String securePayload = logMessage + "|HMAC=" + hmac;
            
            byte[] buffer = securePayload.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, logPort);
            
            socket.send(packet);
            System.out.println("UDP Packet sent with HMAC signature!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
