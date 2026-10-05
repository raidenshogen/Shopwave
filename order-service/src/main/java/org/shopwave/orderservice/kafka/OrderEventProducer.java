package org.shopwave.orderservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {  // This becomes: orderEventProducer (camelCase)

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.template.default-topic:orders}")
    private String topicName;

    public void publishOrderCreated(OrderEvent event) {
        publish("ORDER_CREATED", event);
    }

    public void publishOrderCancelled(OrderEvent event) {
        publish("ORDER_CANCELLED", event);
    }

    public void publishOrderConfirmed(OrderEvent event) {
        publish("ORDER_CONFIRMED", event);
    }

    public void publishOrderShipped(OrderEvent event) {
        publish("ORDER_SHIPPED", event);
    }

    public void publishOrderDelivered(OrderEvent event) {
        publish("ORDER_DELIVERED", event);
    }

    private void publish(String eventType, OrderEvent event) {
        try {
            event.setEventType(eventType);

            if (event.getTimestamp() == null) {
                event.setTimestamp(java.time.LocalDateTime.now());
            }

            String message = objectMapper.writeValueAsString(event);

            // Send to Kafka: key=orderId, value=json
            kafkaTemplate.send(topicName,
                    event.getOrderId().toString(),
                    message);

            log.info("✅ Published [{}] for order {}", eventType, event.getOrderNumber());

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to publish {}: {}", eventType, e.getMessage());
            throw new RuntimeException("Kafka publish failed", e);
        } catch (Exception e) {
            log.error("❌ Kafka connection error: {}", e.getMessage());
            throw new RuntimeException("Kafka unavailable", e);
        }
    }
}