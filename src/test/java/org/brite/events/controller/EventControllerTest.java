package org.brite.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.brite.events.domain.EventRequestDto;
import org.brite.events.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest {

    private static final String VALID = """
            {"version":"v1","eventId":"evt_123abc","timestamp":"2026-09-17T10:00:00Z",
             "payload":{"userId":"u123","email":"user@example.com"}}""";

    private EventService eventService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        eventService = Mockito.mock(EventService.class);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new EventController(eventService, mapper)).build();
    }

    @Test
    void validV1EventIsSavedAndReturns200() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("processed and saved")));

        ArgumentCaptor<EventRequestDto> captor = ArgumentCaptor.forClass(EventRequestDto.class);
        verify(eventService).processAndSaveEvent(captor.capture());
        assertEquals("evt_123abc", captor.getValue().eventId());
        assertEquals("u123", captor.getValue().payload().userId());
    }

    @Test
    void eventFailingSchemaReturns400AndIsNotSaved() throws Exception {
        String badId = VALID.replace("evt_123abc", "bad-id");
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(badId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Schema Validation Failed")));
        verify(eventService, never()).processAndSaveEvent(any());
    }

    @Test
    void invalidEmailReturns400() throws Exception {
        String badEmail = VALID.replace("user@example.com", "not-an-email");
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(badEmail))
                .andExpect(status().isBadRequest());
        verify(eventService, never()).processAndSaveEvent(any());
    }

    @Test
    void unsupportedVersionReturns400() throws Exception {
        String v2 = VALID.replace("\"v1\"", "\"v2\"");
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(v2))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unsupported DTO version: v2")));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Malformed JSON")));
    }

    @Test
    void serviceFailureReturns500() throws Exception {
        Mockito.doThrow(new IllegalStateException("db down")).when(eventService).processAndSaveEvent(any());
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("db down")));
    }
}
