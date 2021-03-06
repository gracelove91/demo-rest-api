package kr.gracelove.demorestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.gracelove.demorestapi.common.RestDocsConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class EventControllerIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

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
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andDo(document(
                        "create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-events").description("link to update an event"),
                                linkWithRel("profile").description("event information")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of event enrollment"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of event enrollment"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of end of event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("url of new event"),
                                headerWithName("Content-Type").description("content-type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("id of new event"),
                                fieldWithPath("name").description("name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of event enrollment"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of event enrollment"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of end of event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                                fieldWithPath("offline").description("is this online event or not"),
                                fieldWithPath("free").description("is this free or not"),
                                fieldWithPath("eventStatus").description("status of event"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query events"),
                                fieldWithPath("_links.update-events.href").description("link to update an event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("_links.index").exists());
    }

    //30개 이벤트를 10개씩 두번째 페이지 조회.
    @Test
    void queryEvents() throws Exception {
        //given
        IntStream.range(0, 30).forEach(this::generateEvent);
        //when
        mockMvc.perform(get("/api/events")
                        .param("page", "1") //page는 0부터 시작.
                        .param("size", "10")
//                    .param("sort", "name.DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"));
        //then
    }

    @Test
    void 기존_이벤트_하나_조회() throws Exception {
        //given
        Event event = this.generateEvent(100);

        //when
        mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"));

        //then
    }

    @Test
    void 없는_이벤트_조회_404_응답() throws Exception {
        mockMvc.perform(get("/api/events/123123123"))
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("모각코" + index)
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

        eventRepository.save(event);

        return event;
    }

    @Test
    void 정상적인_수정() throws Exception {
        //given
        Event event = generateEvent(200);
        EventCreateDto dto = EventCreateDto.createDto(event);
        @NotEmpty String eventName = "updated Event";
        dto.setName(eventName);

        mockMvc.perform(put("/api/events/{id}", event.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists());
    }

    @Test
    void 수정_Empty_Input() throws Exception {
        //given
        Event event = generateEvent(200);
        EventCreateDto dto = EventCreateDto.createDto(event);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    void 수정_Wrong_Input() throws Exception {
        //given
        Event event = generateEvent(200);
        EventCreateDto dto = EventCreateDto.createDto(event);

        dto.setBasePrice(10000);
        dto.setBasePrice(100);

        String eventName = "updated Event";
        dto.setName(eventName);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
