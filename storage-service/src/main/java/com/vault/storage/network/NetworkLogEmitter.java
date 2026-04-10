package com.vault.storage.network;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Component
public class NetworkLogEmitter {

    @Value("${logging.service.host}")
    private String logHost;

    @Value("${logging.service.port}")
    private int logPort;

    // Async annotation ensures this fire-and-forget logic uses Virtual Threads (if configured)
    @Async
    public void sendAuditLog(String logMessage) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(logHost);
            byte[] buffer = logMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, logPort);
            
            socket.send(packet);
            System.out.println("UDP Packet sent -> " + logMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
