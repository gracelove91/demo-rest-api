package kr.gracelove.demorestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createEvent() throws Exception {
        EventCreateDto dto = EventCreateDto.builder()
                .name("모각코")
                .description("모여서 각자 코딩")
                .location("강남역")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 4, 20, 17, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 4, 21, 17, 0))
                .beginEventDateTime(LocalDateTime.of(2020, 5, 1, 13, 0))
                .endEventDateTime(LocalDateTime.of(2020, 5, 1, 15, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();

        mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf8")
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.free").value(false))
                .andExpect(jsonPath("$.offline").value(true))
                .andExpect(jsonPath("$.eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.query-events").exists())
                .andExpect(jsonPath("$._links.update-events").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));
    }


    @Test
    void createEvent_bad_request_empty_input() throws Exception {
        EventCreateDto dto = EventCreateDto.builder()
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_bad_request_wrong_input() throws Exception {
        EventCreateDto dto = EventCreateDto.builder()
                .name("모각코")
                .description("모여서 각자 코딩")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 4, 20, 17, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 4, 19, 17, 0))
                .beginEventDateTime(LocalDateTime.of(2020, 5, 1, 13, 0))
                .endEventDateTime(LocalDateTime.of(2020, 4, 1, 15, 0))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


}
