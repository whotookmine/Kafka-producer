//package com.example.kafka.controller;
//
//import com.example.kafka.domain.LibraryEvent;
//import com.example.kafka.util.TestUtil;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.kafka.clients.consumer.Consumer;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.common.serialization.IntegerDeserializer;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.*;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.test.EmbeddedKafkaBroker;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.kafka.test.utils.KafkaTestUtils;
//import org.springframework.test.context.TestPropertySource;
//
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
//@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
//        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
//class LibraryEventsControllerIntegrationTest {
//
//    @Autowired
//    TestRestTemplate testRestTemplate;
//    @Autowired
//    ObjectMapper objectMapper;
//    EmbeddedKafkaBroker embeddedKafkaBroker;
//    private Consumer<Integer, String > consumer;
//    @BeforeEach
//    void setUp() {
//        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
//        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
//        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
//    }
//
//    @Test
//    void postLibraryEvent(){
//        //given
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
//        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord());
//        //when
//        ResponseEntity<LibraryEvent> responseEntity = testRestTemplate
//                .exchange("/v1/libraryevent", HttpMethod.POST,
//                        httpEntity, LibraryEvent.class);
//        //then
//        assertEquals(HttpStatus.CREATED,responseEntity.getStatusCode());
//
//        ConsumerRecords<Integer, String> records = KafkaTestUtils.getRecords(consumer);
//        assert  records.count() == 1;
//        records.forEach(integerStringConsumerRecord -> {
//            LibraryEvent libraryEvent = TestUtil.parseLibraryEventRecord(objectMapper, integerStringConsumerRecord.value());
//            assertEquals(libraryEvent,TestUtil.libraryEventRecord());
//        });
//    }
//
//}