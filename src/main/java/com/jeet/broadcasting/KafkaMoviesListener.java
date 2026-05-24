package com.jeet.broadcasting;

import com.jeet.broadcasting.eventModel.AddMovie;
import com.jeet.broadcasting.serialization.Deserialization;
import com.jeet.service.BookingHandlerImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    @Inject
    BookingHandlerImpl bh;

    private KafkaConsumer<String, String> consumer;
    private Thread consumerThread;
    private volatile boolean running = true;

    @PostConstruct
    public void startConsumer() {

        String bootstrapUrl = System.getenv().getOrDefault("BOOTSTRAP_URL", "localhost:9092");

        Deserialization deserialization = new Deserialization();

        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "mbook-movies-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("ios-movies-notifications2"));
        consumerThread = new Thread(() -> {

            long backoff = 1000;
            while (running) {
                try {

                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    backoff = 1000;

                    for (ConsumerRecord<String, String> record : records) {

                        AddMovie event = deserialization.deserializeAddMovie(record.value());
                        bh.saveNewEvent(event);

                        WebSocketServer.broadcastMessage(record.value());
                        System.out.println("Sent to WebSocket: " + record.value());
                    }

                } catch (Exception e) {

                    System.err.println(
                        "Kafka consumer error (mbook-movies), retrying in "
                            + backoff + "ms: " + e.getMessage());

                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }

                    backoff = Math.min(backoff * 2, 30000);
                }
            }
        });

        consumerThread.start();
    }

    @PreDestroy
    public void stopConsumer() {

        running = false;

        try {
            consumerThread.join(2000);
        } catch (InterruptedException ignored) {
        }

        consumer.close();
    }
}
