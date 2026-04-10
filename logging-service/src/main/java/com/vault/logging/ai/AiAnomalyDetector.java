package com.vault.logging.ai;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class AiAnomalyDetector {

    @Autowired
    private MqttClient mqttClient;

    @Value("${mqtt.topic}")
    private String alertTopic;

    public void analyzeLog(String log) {
        System.out.println("Analyzing log snippet using simulated AI model...");
        
        // Simulating Anomaly detection (e.g., if a user uploads a banned/suspicious file type, or too frequently)
        boolean isAnomaly = log.contains("suspicious");

        if (isAnomaly) {
            System.err.println("!!! ANOMALY DETECTED !!! Dispatching Alert to MQTT Topic: " + alertTopic);
            try {
                MqttMessage message = new MqttMessage(("QUARANTINE_ALERT: " + log).getBytes(StandardCharsets.UTF_8));
                message.setQos(1);
                mqttClient.publish(alertTopic, message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Log passed anomaly detection.");
        }
    }
}
