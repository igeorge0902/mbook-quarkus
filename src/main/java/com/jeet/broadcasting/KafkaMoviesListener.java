package com.jeet.broadcasting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jeet.api.Logins;
import com.jeet.broadcasting.eventModel.AddMovie;
import com.jeet.broadcasting.serialization.Deserialization;
import com.jeet.service.NewsService;
import com.jeet.service.AccountService;
import com.jeet.service.NewsSettingsService;
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
import java.util.Map;
import java.util.Set;
import org.jboss.logging.Logger;

@ApplicationScoped
@Startup
public class KafkaMoviesListener {
    private static final Logger LOG = Logger.getLogger(KafkaMoviesListener.class);

    @Inject
    NewsService newsService;

    @Inject
    ActiveSessionResolver resolver;

    @Inject
    AccountService accounts;

    @Inject
    NewsSettingsService settings;

    @Inject
    ObjectMapper mapper;

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

                        try {
                            AddMovie event = deserialization.deserializeAddMovie(record.value());
                            if (event == null) {
                                throw new IllegalStateException("Screen-news deserialization failed");
                            }
                            newsService.saveNewEvent(event);
                            deliverScreenNews(event);
                        } catch (RuntimeException e) {
                            LOG.error("Screen-news record skipped after processing failure", e);
                        }
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

    private void deliverScreenNews(AddMovie event) {
        Map<String, Set<String>> active;
        try {
            active = resolver.deviceUsers();
        } catch (RuntimeException e) {
            LOG.error("Screen news skipped: active sessions unavailable", e);
            return;
        }
        // Kafka carries Base64 Java serialization, not JSON. Expose only the wire fields.
        ObjectNode payload = mapper.createObjectNode();
        payload.put("movie", event.movie);
        payload.put("date", event.date);
        payload.put("venue", event.venue);
        payload.put("screeningId", event.screeningId);
        payload.put("screeningDatesId", event.screeningDatesId);
        payload.put("eventType", event.eventType == null ? "created" : event.eventType);
        payload.put("category", "screening");
        String message = payload.toString();
        for (String deviceId : WebSocketServer.registeredDevices()) {
            Set<String> users = active.getOrDefault(deviceId, Set.of());
            if (users.size() != 1) {
                continue;
            }
            try {
                Logins login = accounts.getUser(users.iterator().next());
                if (login.getId() == 0) {
                    continue;
                }
                if (settings.isScreenNewsEnabled(login.getId())) {
                    WebSocketServer.sendToDevice(deviceId, message);
                }
            } catch (RuntimeException e) {
                LOG.error("Screen news skipped: recipient lookup failed", e);
            }
        }
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
