package com.example.kafka.controller;

import com.example.kafka.domain.LibraryEvent;
import com.example.kafka.domain.LibraryEventType;
import com.example.kafka.producer.LibraryEventsProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
public class LibraryEventsController {
    private final LibraryEventsProducer libraryEventsProducer;

    public LibraryEventsController(LibraryEventsProducer libraryEventsProducer) {
        this.libraryEventsProducer = libraryEventsProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> post(
            @RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("lib event : {} ",libraryEvent);
        //invoke kafka producer
//        libraryEventsProducer.sendLibraryEvent(libraryEvent);
        libraryEventsProducer.sendLibraryEvent_approach2(libraryEvent);
//        libraryEventsProducer.sendLibraryEvent_approach3(libraryEvent);
        log.info("After sending lib event: ");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> put(
            @RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("lib event : {} ",libraryEvent);

        ResponseEntity<String> badReq = validateLibRequest(libraryEvent);
        if (badReq != null) return badReq;

        libraryEventsProducer.sendLibraryEvent_approach2(libraryEvent);
        log.info("After sending lib event: ");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    private static ResponseEntity<String> validateLibRequest(LibraryEvent libraryEvent) {
        if(libraryEvent.libraryEventId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please Pass to  Lib Id");
        }
        if(!libraryEvent.libraryEventType().equals(LibraryEventType.UPDATE)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ONLY UPDATE!!");
        }
        return null;
    }
}
