package com.example.kafka.producer;

import com.example.kafka.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventsProducer {
    private final KafkaTemplate<Integer,String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.topic}")
    public String topic;
    public LibraryEventsProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }


    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
        //Async approach
        CompletableFuture<SendResult<Integer, String>> completableFuture = kafkaTemplate.send(topic, key, value);
        return completableFuture
                .whenComplete((sendResult, throwable) -> {
                    if(throwable!= null){
                        handFailure(key,value,throwable);
                    }else {
                        handleSuccess(key,value,sendResult);
                    }
                });
    }
    public SendResult<Integer, String> sendLibraryEvent_approach2(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
        // BLock and wait til msg is sent to the kafka
        var sendResult = kafkaTemplate.send(topic, key, value)
//                .get();
                .get(3, TimeUnit.SECONDS);
        handleSuccess(key,value,sendResult);
        return sendResult;
    }
    public SendResult<Integer, String> sendLibraryEvent_approach3(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
        //send producerRecord instead
        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);
        // BLock and wait til msg is sent to the kafka
        var sendResult = kafkaTemplate.send(producerRecord)
//                .get();
                .get(3, TimeUnit.SECONDS);
        handleSuccess(key,value,sendResult);
        return sendResult;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
        List<Header> recordHeaders = List.of(new RecordHeader("event-srouce", "scanner".getBytes()));
        return new ProducerRecord<>(topic,null,key,value,recordHeaders);
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message Send Successfully for key : {} , value : {} and partition is {}"
                ,key,value,sendResult.getRecordMetadata().partition());

    }

    private void handFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending msg : exception is {}",throwable.getMessage(),throwable);
    }
}

