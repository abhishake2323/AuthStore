package com.vault.logging.ai;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MachineLearningIntrusionSystem {

    private static final Logger log = LoggerFactory.getLogger(MachineLearningIntrusionSystem.class);

    @Autowired
    private MqttClient mqttClient;

    @Value("${mqtt.topic}")
    private String alertTopic;

    public void analyzeLog(String logSnippet) {
        log.info("Analyzing log snippet using simulated AI model...");
        
        // Simulating Anomaly detection (e.g., if a user uploads a banned/suspicious file type, or too frequently)
        boolean isAnomaly = logSnippet.contains("suspicious");

        if (isAnomaly) {
            log.warn("!!! ANOMALY DETECTED !!! Dispatching Alert to MQTT Topic: {}", alertTopic);
            try {
                MqttMessage message = new MqttMessage(("QUARANTINE_ALERT: " + logSnippet).getBytes(StandardCharsets.UTF_8));
                message.setQos(1);
                mqttClient.publish(alertTopic, message);
            } catch (MqttException e) {
                log.error("Failed to dispatcher MQTT payload", e);
            }
        } else {
            log.info("Log passed anomaly detection.");
        }
    }
}
