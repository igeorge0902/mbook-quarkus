package com.jeet.broadcasting;

import com.jeet.broadcasting.eventModel.AddMovie;
import com.jeet.broadcasting.serialization.Deserialization;
import com.jeet.service.BookingHandlerImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@ApplicationScoped
@Startup
public class KafkaMoviesListener {
    private KafkaConsumer<String, String> consumer;
    private Thread consumerThread;
    private volatile boolean running = true;

    @PostConstruct
    public void startConsumer() {
        BookingHandlerImpl bh = new BookingHandlerImpl();
        String bootstrapUrl = System.getenv().getOrDefault("BOOTSTRAP_URL", "localhost:9092");
        Deserialization deserialization = new Deserialization();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ios-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("ios-movies-notifications2"));

        consumerThread = new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    AddMovie event = deserialization.deserializeAddMovie(record.value());
                    bh.saveNewEvent(event);

                   // WebSocketServer.broadcastMessage(record.value());
                   // System.out.println("Sent to WebSocket: " + record.value());
                }
            }
        });
        consumerThread.start();
    }

    @PreDestroy
    public void stopConsumer() {
        consumer.close();
        consumerThread.interrupt();
    }
}
