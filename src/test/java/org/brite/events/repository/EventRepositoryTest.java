package org.brite.events.repository;

import org.brite.events.dto.EventEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs on embedded H2 (the real table lives in MySQL; main config uses ddl-auto=none). */
@DataJpaTest
@ContextConfiguration(classes = org.brite.EventServiceApplication.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=",
        "spring.datasource.password="
})
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void savesAndFindsEventById() {
        eventRepository.save(new EventEntity("evt_1", "v1", ZonedDateTime.now(), "u1", "u1@example.com"));
        assertTrue(eventRepository.findById("evt_1").isPresent());
        assertEquals(1, eventRepository.count());
    }

    @Test
    void unknownIdIsAbsent() {
        assertTrue(eventRepository.findById("evt_missing").isEmpty());
    }
}
