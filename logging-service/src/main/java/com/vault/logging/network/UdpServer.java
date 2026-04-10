package com.vault.logging.network;

import com.vault.logging.ai.AiAnomalyDetector;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

@Component
public class UdpServer {

    @Value("${udp.port}")
    private int port;

    @Autowired
    private AiAnomalyDetector anomalyDetector;

    @PostConstruct
    public void startServer() {
        // Start UDP listener in a separate thread to avoid blocking application startup
        Thread serverThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                System.out.println("UDP Logging Server listening on port " + port);
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // blocking call
                    
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    System.out.println("Received Datagram: " + message);
                    
                    // Route to AI Anomaly Detector
                    anomalyDetector.analyzeLog(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // Starting as virtual thread for high-concurrency background processing (Java 21 native support)
        Thread.ofVirtual().name("udp-server").start(serverThread);
    }
}
