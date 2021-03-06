package in.skdv.skdvinbackend.controller.api;

import in.skdv.skdvinbackend.AbstractSkdvinTest;
import in.skdv.skdvinbackend.MockJwtDecoder;
import in.skdv.skdvinbackend.ModelMockHelper;
import in.skdv.skdvinbackend.model.converter.JumpdayConverter;
import in.skdv.skdvinbackend.model.dto.JumpdayDTO;
import in.skdv.skdvinbackend.model.dto.TandemmasterDTO;
import in.skdv.skdvinbackend.model.dto.VideoflyerDTO;
import in.skdv.skdvinbackend.model.entity.Appointment;
import in.skdv.skdvinbackend.model.entity.Jumpday;
import in.skdv.skdvinbackend.model.entity.Tandemmaster;
import in.skdv.skdvinbackend.model.entity.Videoflyer;
import in.skdv.skdvinbackend.repository.JumpdayRepository;
import in.skdv.skdvinbackend.repository.TandemmasterRepository;
import in.skdv.skdvinbackend.repository.VideoflyerRepository;
import in.skdv.skdvinbackend.service.IAppointmentService;
import in.skdv.skdvinbackend.service.IJumpdayService;
import in.skdv.skdvinbackend.util.GenericResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;

import static in.skdv.skdvinbackend.config.Authorities.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest
class JumpdayControllerTest extends AbstractSkdvinTest {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8);

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private MockMvc mockMvc;

    @Autowired
    private IJumpdayService jumpdayService;

    @Autowired
    private IAppointmentService appointmentService;

    @Autowired
    private JumpdayRepository jumpdayRepository;

    @Autowired
    private TandemmasterRepository tandemmasterRepository;

    @Autowired
    private VideoflyerRepository videoflyerRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        AssertionErrors.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @BeforeEach
    void setup(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .apply(documentationConfiguration(restDocumentation))
                        .build();

        jumpdayRepository.deleteAll();
    }

    @Test
    void testCreateJumpday() throws Exception {
        String jumpdayJson = json(ModelMockHelper.createJumpday());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday/")
                .header("Authorization", MockJwtDecoder.addHeader(CREATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload.date", is(LocalDate.now().toString())))
                .andExpect(jsonPath("$.payload.jumping", is(true)))
                .andExpect(jsonPath("$.payload.slots", hasSize(2)))
                .andExpect(jsonPath("$.payload.slots[0].tandemTotal", is(4)))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidTotal", is(2)))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidTotal", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].handcamTotal", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].time", is("10:00")))
                .andExpect(jsonPath("$.payload.slots[1].time", is("11:30")))
                .andDo(document("jumpday/create-jumpday", requestFields(
                        fieldWithPath("date").description("The date of the jumpday"),
                        fieldWithPath("jumping").description("true when it's a jumpday"),
                        fieldWithPath("slots[]").description("The list of time slots on this jumpday"),
                        fieldWithPath("slots[].time").description("The time of this slot"),
                        fieldWithPath("slots[].tandemTotal").description("The total capacity of tandem slots"),
                        fieldWithPath("slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                        fieldWithPath("slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                        fieldWithPath("slots[].handcamTotal").description("The total capacity of handcam slots"),
                        fieldWithPath("slots[].tandemBooked").ignored(),
                        fieldWithPath("slots[].tandemAvailable").ignored(),
                        fieldWithPath("slots[].picOrVidBooked").ignored(),
                        fieldWithPath("slots[].picAndVidBooked").ignored(),
                        fieldWithPath("slots[].handcamBooked").ignored(),
                        fieldWithPath("slots[].picOrVidAvailable").ignored(),
                        fieldWithPath("slots[].picAndVidAvailable").ignored(),
                        fieldWithPath("slots[].handcamAvailable").ignored(),
                        fieldWithPath("slots[].appointments").ignored(),
                        fieldWithPath("tandemmaster[]").ignored(),
                        fieldWithPath("videoflyer[]").ignored(),
                        fieldWithPath("freeTimes").ignored()
                        ), responseFields(
                        fieldWithPath("success").description("true when the request was successful"),
                        fieldWithPath("payload.date").description("The date of the jumpday"),
                        fieldWithPath("payload.jumping").description("true when it's a jumpday"),
                        fieldWithPath("payload.videoflyer[]").description("A list of video flyers avalable at this date"),
                        fieldWithPath("payload.tandemmaster[]").description("A list of tandemmasters avalable at this date"),
                        fieldWithPath("payload.slots[]").description("The list of time slots on this jumpday"),
                        fieldWithPath("payload.slots[].time").description("The time of this slot"),
                        fieldWithPath("payload.slots[].tandemTotal").description("The total capacity of tandem slots"),
                        fieldWithPath("payload.slots[].tandemBooked").description("The total booked tandem slots"),
                        fieldWithPath("payload.slots[].tandemAvailable").description("The total available tandem slots"),
                        fieldWithPath("payload.slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                        fieldWithPath("payload.slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                        fieldWithPath("payload.slots[].handcamTotal").description("The total capacity of handcam slots"),
                        fieldWithPath("payload.slots[].handcamBooked").description("The total booked handcam slots"),
                        fieldWithPath("payload.slots[].handcamAvailable").description("The total available handcam slots"),
                        fieldWithPath("message").ignored(),
                        fieldWithPath("exception").ignored(),
                        fieldWithPath("payload.freeTimes").ignored(),
                        fieldWithPath("payload.slots[].appointments").ignored()
                )));
    }

    @Test
    void testCreateJumpday_AlreadyExists_DE() throws Exception {
        String jumpdayJson = json(ModelMockHelper.createJumpday());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday")
                .header("Authorization", MockJwtDecoder.addHeader(CREATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isCreated());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday")
                .header("Authorization", MockJwtDecoder.addHeader(CREATE_JUMPDAYS))
                .header("Accept-Language", "de-DE")
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Sprungtag existiert bereits")));
    }

    @Test
    void testCreateJumpday_AlreadyExists_EN() throws Exception {
        String jumpdayJson = json(ModelMockHelper.createJumpday());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday")
                .header("Authorization", MockJwtDecoder.addHeader(CREATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isCreated());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday")
                .header("Authorization", MockJwtDecoder.addHeader(CREATE_JUMPDAYS))
                .header("Accept-Language", "en-US")
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Jumpday already exists")));
    }

    @Test
    void testCreateJumpday_NoDateSet() throws Exception {
        Jumpday jumpday = ModelMockHelper.createJumpday();
        jumpday.setDate(null);
        String jumpdayJson = json(jumpday);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday")
                .header("Authorization", MockJwtDecoder.addHeader(CREATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAll() throws Exception {
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(ModelMockHelper.createJumpday());
        assertTrue(result.isSuccess());
        GenericResult<Jumpday> result2 = jumpdayService.saveJumpday(ModelMockHelper.createJumpday(LocalDate.now().plusDays(1)));
        assertTrue(result2.isSuccess());

        mockMvc.perform(get("/api/jumpday/")
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload", hasSize(2)))
                .andExpect(jsonPath("$.payload[0].date", is(result.getPayload().getDate().toString())))
                .andExpect(jsonPath("$.payload[1].date", is(result2.getPayload().getDate().toString())));
    }


    @Test
    void testGetAll_OneResult() throws Exception {
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(ModelMockHelper.createJumpday());
        Jumpday jumpday = result.getPayload();

        mockMvc.perform(get("/api/jumpday/")
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload", hasSize(1)))
                .andExpect(jsonPath("$.payload[0].date", is(jumpday.getDate().toString())))
                .andExpect(jsonPath("$.payload[0].jumping", is(jumpday.isJumping())))
                .andExpect(jsonPath("$.payload[0].slots", hasSize(jumpday.getSlots().size())))
                .andExpect(jsonPath("$.payload[0].slots[0].time", is(jumpday.getSlots().get(0).getTime().toString())))
                .andExpect(jsonPath("$.payload[0].slots[0].tandemTotal", is(jumpday.getSlots().get(0).getTandemTotal())))
                .andExpect(jsonPath("$.payload[0].slots[0].picOrVidTotal", is(jumpday.getSlots().get(0).getPicOrVidTotal())))
                .andExpect(jsonPath("$.payload[0].slots[0].picAndVidTotal", is(jumpday.getSlots().get(0).getPicAndVidTotal())))
                .andExpect(jsonPath("$.payload[0].slots[0].handcamTotal", is(jumpday.getSlots().get(0).getHandcamTotal())))
                .andDo(document("jumpday/get-jumpdays", responseFields(
                        fieldWithPath("success").description("true when the request was successful"),
                        fieldWithPath("payload[]").description("The list of jumpdays"),
                        fieldWithPath("payload[].date").description("The date of the jumpday"),
                        fieldWithPath("payload[].jumping").description("true when it's a jumpday"),
                        fieldWithPath("payload[].tandemmaster").description("A list of tandem masters avalable at this date"),
                        fieldWithPath("payload[].videoflyer").description("A list of video flyers avalable at this date"),
                        fieldWithPath("payload[].slots[]").description("The list of time slots on this jumpday"),
                        fieldWithPath("payload[].slots[].time").description("The time of this slot"),
                        fieldWithPath("payload[].slots[].tandemTotal").description("The total capacity of tandem slots"),
                        fieldWithPath("payload[].slots[].tandemBooked").description("The total booked tandem slots"),
                        fieldWithPath("payload[].slots[].tandemAvailable").description("The total available tandem slots"),
                        fieldWithPath("payload[].slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                        fieldWithPath("payload[].slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                        fieldWithPath("payload[].slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                        fieldWithPath("payload[].slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                        fieldWithPath("payload[].slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                        fieldWithPath("payload[].slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                        fieldWithPath("payload[].slots[].handcamTotal").description("The total capacity of handcam slots"),
                        fieldWithPath("payload[].slots[].handcamBooked").description("The total booked handcam slots"),
                        fieldWithPath("payload[].slots[].handcamAvailable").description("The total available handcam slots"),
                        fieldWithPath("message").ignored(),
                        fieldWithPath("exception").ignored(),
                        fieldWithPath("payload[].freeTimes").ignored(),
                        fieldWithPath("payload[].slots[].appointments").ignored()
                )));
    }


    @Test
    void testGetAll_NoResult() throws Exception {
        mockMvc.perform(get("/api/jumpday/")
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload", hasSize(0)));
    }

    @Test
    void testGetByDate() throws Exception {
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(ModelMockHelper.createJumpday());
        Jumpday jumpday = result.getPayload();

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/jumpday/{date}", jumpday.getDate().toString())
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload.date", is(jumpday.getDate().toString())))
                .andExpect(jsonPath("$.payload.jumping", is(jumpday.isJumping())))
                .andExpect(jsonPath("$.payload.slots", hasSize(jumpday.getSlots().size())))
                .andExpect(jsonPath("$.payload.slots[0].time", is(jumpday.getSlots().get(0).getTime().toString())))
                .andExpect(jsonPath("$.payload.slots[0].tandemTotal", is(jumpday.getSlots().get(0).getTandemTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidTotal", is(jumpday.getSlots().get(0).getPicOrVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidTotal", is(jumpday.getSlots().get(0).getPicAndVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].handcamTotal", is(jumpday.getSlots().get(0).getHandcamTotal())))
                .andDo(document("jumpday/get-jumpday", pathParameters(
                        parameterWithName("date").description("The date of the requested jumpday")
                ), responseFields(
                        fieldWithPath("success").description("true when the request was successful"),
                        fieldWithPath("payload").description("The list of jumpdays"),
                        fieldWithPath("payload.date").description("The date of the jumpday"),
                        fieldWithPath("payload.jumping").description("true when it's a jumpday"),
                        fieldWithPath("payload.tandemmaster").description("A list of tandem masters avalable at this date"),
                        fieldWithPath("payload.videoflyer").description("A list of video flyers avalable at this date"),
                        fieldWithPath("payload.slots[]").description("The list of time slots on this jumpday"),
                        fieldWithPath("payload.slots[].time").description("The time of this slot"),
                        fieldWithPath("payload.slots[].tandemTotal").description("The total capacity of tandem slots"),
                        fieldWithPath("payload.slots[].tandemBooked").description("The total booked tandem slots"),
                        fieldWithPath("payload.slots[].tandemAvailable").description("The total available tandem slots"),
                        fieldWithPath("payload.slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                        fieldWithPath("payload.slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                        fieldWithPath("payload.slots[].handcamTotal").description("The total capacity of handcam slots"),
                        fieldWithPath("payload.slots[].handcamBooked").description("The total booked handcam slots"),
                        fieldWithPath("payload.slots[].handcamAvailable").description("The total available handcam slots"),
                        fieldWithPath("message").ignored(),
                        fieldWithPath("exception").ignored(),
                        fieldWithPath("payload.freeTimes").ignored(),
                        fieldWithPath("payload.slots[].appointments").ignored()
                )));

    }

    @Test
    void testGetByDate_WithTandemmaster() throws Exception {
        Tandemmaster tandemmaster = tandemmasterRepository.save(ModelMockHelper.createTandemmaster());
        Jumpday unsavedJumpday = ModelMockHelper.createJumpday();
        unsavedJumpday.setTandemmaster(Collections.singletonList(ModelMockHelper.createAssignment(tandemmaster)));
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(unsavedJumpday);
        Jumpday jumpday = result.getPayload();

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/jumpday/{date}", jumpday.getDate().toString())
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload.date", is(jumpday.getDate().toString())))
                .andExpect(jsonPath("$.payload.jumping", is(jumpday.isJumping())))
                .andExpect(jsonPath("$.payload.slots", hasSize(jumpday.getSlots().size())))
                .andExpect(jsonPath("$.payload.slots[0].time", is(jumpday.getSlots().get(0).getTime().toString())))
                .andExpect(jsonPath("$.payload.slots[0].tandemTotal", is(jumpday.getSlots().get(0).getTandemTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidTotal", is(jumpday.getSlots().get(0).getPicOrVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidTotal", is(jumpday.getSlots().get(0).getPicAndVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].handcamTotal", is(jumpday.getSlots().get(0).getHandcamTotal())))
                .andDo(document("jumpday/get-jumpdays-with-tm", pathParameters(
                        parameterWithName("date").description("The date of the requested jumpday")
                ), responseFields(
                        fieldWithPath("success").description("true when the request was successful"),
                        fieldWithPath("payload").description("The list of jumpdays"),
                        fieldWithPath("payload.date").description("The date of the jumpday"),
                        fieldWithPath("payload.jumping").description("true when it's a jumpday"),
                        fieldWithPath("payload.tandemmaster[]").description("A list of tandem masters avalable at this date"),
                        fieldWithPath("payload.tandemmaster[].assigned").description("true if the flyer is assigned to this date"),
                        fieldWithPath("payload.tandemmaster[].allday").description("true if the flyer is assigned all day"),
                        fieldWithPath("payload.tandemmaster[].from").description("From time if the flyer is not assigned all day"),
                        fieldWithPath("payload.tandemmaster[].to").description("To time if the flyer is not assigned all day"),
                        fieldWithPath("payload.tandemmaster[].flyer.firstName").description("Tandemmaster's first name"),
                        fieldWithPath("payload.tandemmaster[].flyer.lastName").description("Tandemmaster's last name"),
                        fieldWithPath("payload.tandemmaster[].flyer.email").description("Tandemmaster's email"),
                        fieldWithPath("payload.tandemmaster[].flyer.tel").description("Tandemmaster's phone number"),
                        fieldWithPath("payload.tandemmaster[].flyer.handcam").description("Tandemmaster's handcam"),
                        fieldWithPath("payload.tandemmaster[].flyer.id").description("Tandemmaster's id"),
                        fieldWithPath("payload.videoflyer").description("A list of video flyers avalable at this date"),
                        fieldWithPath("payload.slots[]").description("The list of time slots on this jumpday"),
                        fieldWithPath("payload.slots[].time").description("The time of this slot"),
                        fieldWithPath("payload.slots[].tandemTotal").description("The total capacity of tandem slots"),
                        fieldWithPath("payload.slots[].tandemBooked").description("The total booked tandem slots"),
                        fieldWithPath("payload.slots[].tandemAvailable").description("The total available tandem slots"),
                        fieldWithPath("payload.slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                        fieldWithPath("payload.slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                        fieldWithPath("payload.slots[].handcamTotal").description("The total capacity of handcam slots"),
                        fieldWithPath("payload.slots[].handcamBooked").description("The total booked handcam slots"),
                        fieldWithPath("payload.slots[].handcamAvailable").description("The total available handcam slots"),
                        fieldWithPath("message").ignored(),
                        fieldWithPath("exception").ignored(),
                        fieldWithPath("payload.freeTimes").ignored(),
                        fieldWithPath("payload.slots[].appointments").ignored()
                )));

    }

    @Test
    void testGetByDate_WithVideoflyer() throws Exception {
        Videoflyer videoflyer = videoflyerRepository.save(ModelMockHelper.createVideoflyer());
        Jumpday unsavedJumpday = ModelMockHelper.createJumpday();
        unsavedJumpday.setVideoflyer(Collections.singletonList(ModelMockHelper.createAssignment(videoflyer)));
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(unsavedJumpday);
        Jumpday jumpday = result.getPayload();

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/jumpday/{date}", jumpday.getDate().toString())
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload.date", is(jumpday.getDate().toString())))
                .andExpect(jsonPath("$.payload.jumping", is(jumpday.isJumping())))
                .andExpect(jsonPath("$.payload.slots", hasSize(jumpday.getSlots().size())))
                .andExpect(jsonPath("$.payload.slots[0].time", is(jumpday.getSlots().get(0).getTime().toString())))
                .andExpect(jsonPath("$.payload.slots[0].tandemTotal", is(jumpday.getSlots().get(0).getTandemTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidTotal", is(jumpday.getSlots().get(0).getPicOrVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidTotal", is(jumpday.getSlots().get(0).getPicAndVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].handcamTotal", is(jumpday.getSlots().get(0).getHandcamTotal())))
                .andDo(document("jumpday/get-jumpdays-with-videoflyer", pathParameters(
                        parameterWithName("date").description("The date of the requested jumpday")
                ), responseFields(
                        fieldWithPath("success").description("true when the request was successful"),
                        fieldWithPath("payload").description("The list of jumpdays"),
                        fieldWithPath("payload.date").description("The date of the jumpday"),
                        fieldWithPath("payload.jumping").description("true when it's a jumpday"),
                        fieldWithPath("payload.videoflyer[]").description("A list of videoflyer avalable at this date"),
                        fieldWithPath("payload.videoflyer[].assigned").description("true if the flyer is assigned to this date"),
                        fieldWithPath("payload.videoflyer[].allday").description("true if the flyer is assigned all day"),
                        fieldWithPath("payload.videoflyer[].from").description("From time if the flyer is not assigned all day"),
                        fieldWithPath("payload.videoflyer[].to").description("To time if the flyer is not assigned all day"),
                        fieldWithPath("payload.videoflyer[].flyer.firstName").description("videoflyer's first name"),
                        fieldWithPath("payload.videoflyer[].flyer.lastName").description("videoflyer's last name"),
                        fieldWithPath("payload.videoflyer[].flyer.email").description("videoflyer's email"),
                        fieldWithPath("payload.videoflyer[].flyer.tel").description("videoflyer's phone number"),
                        fieldWithPath("payload.videoflyer[].flyer.picAndVid").description("videoflyer's pic and video setting"),
                        fieldWithPath("payload.videoflyer[].flyer.id").description("videoflyer's id"),
                        fieldWithPath("payload.tandemmaster").description("A list of tandemmasters avalable at this date"),
                        fieldWithPath("payload.slots[]").description("The list of time slots on this jumpday"),
                        fieldWithPath("payload.slots[].time").description("The time of this slot"),
                        fieldWithPath("payload.slots[].tandemTotal").description("The total capacity of tandem slots"),
                        fieldWithPath("payload.slots[].tandemBooked").description("The total booked tandem slots"),
                        fieldWithPath("payload.slots[].tandemAvailable").description("The total available tandem slots"),
                        fieldWithPath("payload.slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                        fieldWithPath("payload.slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                        fieldWithPath("payload.slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                        fieldWithPath("payload.slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                        fieldWithPath("payload.slots[].handcamTotal").description("The total capacity of handcam slots"),
                        fieldWithPath("payload.slots[].handcamBooked").description("The total booked handcam slots"),
                        fieldWithPath("payload.slots[].handcamAvailable").description("The total available handcam slots"),
                        fieldWithPath("message").ignored(),
                        fieldWithPath("exception").ignored(),
                        fieldWithPath("payload.freeTimes").ignored(),
                        fieldWithPath("payload.slots[].appointments").ignored()
                )));

    }

    @Test
    void testGetByDate_WithAppointments() throws Exception {
        // 4 tandem / 2 video at 10:00 and 11:30
        Jumpday jumpday = ModelMockHelper.createJumpday();
        // 1 tandem / 1 video at 10:00
        Appointment appointment1 = ModelMockHelper.createSingleAppointment();
        // 2 tandem / 0 video at 10:00
        Appointment appointment2 = ModelMockHelper.createSecondAppointment();

        jumpdayService.saveJumpday(jumpday);
        appointmentService.saveAppointment(appointment1);
        appointmentService.saveAppointment(appointment2);

        mockMvc.perform(get("/api/jumpday/{date}", jumpday.getDate().toString())
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload.date", is(jumpday.getDate().toString())))
                .andExpect(jsonPath("$.payload.jumping", is(jumpday.isJumping())))
                .andExpect(jsonPath("$.payload.slots", hasSize(jumpday.getSlots().size())))
                .andExpect(jsonPath("$.payload.slots[0].time", is("10:00")))
                .andExpect(jsonPath("$.payload.slots[0].tandemTotal", is(4)))
                .andExpect(jsonPath("$.payload.slots[0].tandemBooked", is(3)))
                .andExpect(jsonPath("$.payload.slots[0].tandemAvailable", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidTotal", is(2)))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidBooked", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidAvailable", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidTotal", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidBooked", is(0)))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidAvailable", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].handcamTotal", is(1)))
                .andExpect(jsonPath("$.payload.slots[0].handcamBooked", is(0)))
                .andExpect(jsonPath("$.payload.slots[0].handcamAvailable", is(1)))
                .andExpect(jsonPath("$.payload.slots[1].time", is(jumpday.getSlots().get(1).getTime().toString())))
                .andExpect(jsonPath("$.payload.slots[1].tandemTotal", is(jumpday.getSlots().get(1).getTandemTotal())))
                .andExpect(jsonPath("$.payload.slots[1].tandemBooked", is(jumpday.getSlots().get(1).getTandemBooked())))
                .andExpect(jsonPath("$.payload.slots[1].tandemAvailable", is(jumpday.getSlots().get(1).getTandemAvailable())))
                .andExpect(jsonPath("$.payload.slots[1].picOrVidTotal", is(jumpday.getSlots().get(1).getPicOrVidTotal())))
                .andExpect(jsonPath("$.payload.slots[1].picOrVidBooked", is(jumpday.getSlots().get(1).getPicOrVidBooked())))
                .andExpect(jsonPath("$.payload.slots[1].picOrVidAvailable", is(jumpday.getSlots().get(1).getPicOrVidAvailable())))
                .andExpect(jsonPath("$.payload.slots[1].picAndVidTotal", is(jumpday.getSlots().get(1).getPicAndVidTotal())))
                .andExpect(jsonPath("$.payload.slots[1].picAndVidBooked", is(jumpday.getSlots().get(1).getPicAndVidBooked())))
                .andExpect(jsonPath("$.payload.slots[1].picAndVidAvailable", is(jumpday.getSlots().get(1).getPicAndVidAvailable())))
                .andExpect(jsonPath("$.payload.slots[1].handcamTotal", is(jumpday.getSlots().get(1).getHandcamTotal())))
                .andExpect(jsonPath("$.payload.slots[1].handcamBooked", is(jumpday.getSlots().get(1).getHandcamBooked())))
                .andExpect(jsonPath("$.payload.slots[1].handcamAvailable", is(jumpday.getSlots().get(1).getHandcamAvailable())));
    }

    @Test
    void testGetByDate_NotFound_DE() throws Exception {
        mockMvc.perform(get("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS))
                .header("Accept-Language", "de-DE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Sprungtag nicht gefunden")));
    }

    @Test
    void testGetByDate_NotFound_EN() throws Exception {
        mockMvc.perform(get("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Authorization", MockJwtDecoder.addHeader(READ_JUMPDAYS))
                .header("Accept-Language", "en-US"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Jumpday not found")));
    }

    @Test
    void testUpdateJumpday() throws Exception {
        int newCount = 4;
        Jumpday jumpday = ModelMockHelper.createJumpday();
        Jumpday savedJumpday = jumpdayRepository.save(jumpday);
        assertNotNull(savedJumpday);

        savedJumpday.getSlots().get(0).setTandemTotal(newCount);

        String jumpdayJson = json(savedJumpday);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload.slots[0].tandemTotal", is(newCount)))
                .andDo(document("jumpday/update-jumpday",
                        pathParameters(
                                parameterWithName("date").description("The date of the updated jumpday")
                        ),
                        requestFields(
                                fieldWithPath("date").description("The date of the jumpday"),
                                fieldWithPath("jumping").description("true when it's a jumpday"),
                                fieldWithPath("slots[]").description("The list of time slots on this jumpday"),
                                fieldWithPath("slots[].time").description("The time of this slot"),
                                fieldWithPath("slots[].tandemTotal").description("The total capacity of tandem slots"),
                                fieldWithPath("slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                                fieldWithPath("slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                                fieldWithPath("slots[].handcamTotal").description("The total capacity of handcam slots"),
                                fieldWithPath("slots[].tandemBooked").ignored(),
                                fieldWithPath("slots[].tandemAvailable").ignored(),
                                fieldWithPath("slots[].picOrVidBooked").ignored(),
                                fieldWithPath("slots[].picAndVidBooked").ignored(),
                                fieldWithPath("slots[].handcamBooked").ignored(),
                                fieldWithPath("slots[].picOrVidAvailable").ignored(),
                                fieldWithPath("slots[].picAndVidAvailable").ignored(),
                                fieldWithPath("slots[].handcamAvailable").ignored(),
                                fieldWithPath("slots[].appointments").ignored(),
                                fieldWithPath("tandemmaster[]").ignored(),
                                fieldWithPath("videoflyer[]").ignored(),
                                fieldWithPath("freeTimes").ignored()
                        ), responseFields(
                                fieldWithPath("success").description("true when the request was successful"),
                                fieldWithPath("payload.date").description("The date of the jumpday"),
                                fieldWithPath("payload.jumping").description("true when it's a jumpday"),
                                fieldWithPath("payload.tandemmaster[]").description("A list of tandem masters avalable at this date"),
                                fieldWithPath("payload.videoflyer[]").description("A list of video flyers avalable at this date"),
                                fieldWithPath("payload.slots[]").description("The list of time slots on this jumpday"),
                                fieldWithPath("payload.slots[].time").description("The time of this slot"),
                                fieldWithPath("payload.slots[].tandemTotal").description("The total capacity of tandem slots"),
                                fieldWithPath("payload.slots[].tandemBooked").description("The total booked tandem slots"),
                                fieldWithPath("payload.slots[].tandemAvailable").description("The total available tandem slots"),
                                fieldWithPath("payload.slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                                fieldWithPath("payload.slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                                fieldWithPath("payload.slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                                fieldWithPath("payload.slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                                fieldWithPath("payload.slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                                fieldWithPath("payload.slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                                fieldWithPath("payload.slots[].handcamTotal").description("The total capacity of handcam slots"),
                                fieldWithPath("payload.slots[].handcamBooked").description("The total booked handcam slots"),
                                fieldWithPath("payload.slots[].handcamAvailable").description("The total available handcam slots"),
                                fieldWithPath("message").ignored(),
                                fieldWithPath("exception").ignored(),
                                fieldWithPath("payload.freeTimes").ignored(),
                                fieldWithPath("payload.slots[].appointments").ignored()
                        )));
    }

    @Test
    void testUpdateJumpday_WithTandemmaster() throws Exception {
        Tandemmaster tandemmaster = tandemmasterRepository.save(ModelMockHelper.createTandemmaster());
        Jumpday unsavedJumpday = ModelMockHelper.createJumpday();
        unsavedJumpday.setTandemmaster(Collections.singletonList(ModelMockHelper.createAssignment(tandemmaster)));
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(unsavedJumpday);
        Jumpday jumpday = result.getPayload();

        JumpdayConverter jumpdayConverter = new JumpdayConverter();
        JumpdayDTO jumpdayDTO = jumpdayConverter.convertToDto(jumpday);

        // don't set fields
        TandemmasterDTO tandemmasterDTO = jumpdayDTO.getTandemmaster().get(0).getFlyer();
        tandemmasterDTO.setEmail("");
        tandemmasterDTO.setFirstName("");
        tandemmasterDTO.setLastName("");
        tandemmasterDTO.setTel("");

        int newCount = 4;
        jumpdayDTO.getSlots().get(0).setTandemTotal(newCount);

        String jumpdayJson = json(jumpdayDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andDo(document("jumpday/update-jumpday-with-tm",
                        pathParameters(
                                parameterWithName("date").description("The date of the updated jumpday")
                        ),
                        requestFields(
                                fieldWithPath("date").description("The date of the jumpday"),
                                fieldWithPath("jumping").description("true when it's a jumpday"),
                                fieldWithPath("videoflyer").description("A list of video flyers available at this date"),
                                fieldWithPath("tandemmaster[]").description("A list of tandemmasters available at this date"),
                                fieldWithPath("tandemmaster[].assigned").description("Assignment state of the tandemmaster"),
                                fieldWithPath("tandemmaster[].allday").description("true if the tandemmaster is assigned all-day"),
                                fieldWithPath("tandemmaster[].from").description("From time, if the tandemmaster is not assigned all-day"),
                                fieldWithPath("tandemmaster[].to").description("To time, if the tandemmaster is not assigned all-day"),
                                fieldWithPath("tandemmaster[].flyer.id").description("The tandemmaster's id"),
                                fieldWithPath("slots[]").description("The list of time slots on this jumpday"),
                                fieldWithPath("slots[].time").description("The time of this slot"),
                                fieldWithPath("slots[].tandemTotal").description("The total capacity of tandem slots"),
                                fieldWithPath("slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                                fieldWithPath("slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                                fieldWithPath("slots[].handcamTotal").description("The total capacity of handcam slots"),
                                fieldWithPath("slots[].tandemBooked").ignored(),
                                fieldWithPath("slots[].tandemAvailable").ignored(),
                                fieldWithPath("slots[].picOrVidBooked").ignored(),
                                fieldWithPath("slots[].picAndVidBooked").ignored(),
                                fieldWithPath("slots[].handcamBooked").ignored(),
                                fieldWithPath("slots[].picOrVidAvailable").ignored(),
                                fieldWithPath("slots[].picAndVidAvailable").ignored(),
                                fieldWithPath("slots[].handcamAvailable").ignored(),
                                fieldWithPath("slots[].appointments").ignored(),
                                fieldWithPath("freeTimes").ignored(),
                                fieldWithPath("tandemmaster[].flyer.firstName").ignored(),
                                fieldWithPath("tandemmaster[].flyer.lastName").ignored(),
                                fieldWithPath("tandemmaster[].flyer.email").ignored(),
                                fieldWithPath("tandemmaster[].flyer.tel").ignored(),
                                fieldWithPath("tandemmaster[].flyer.handcam").ignored()
                        ), responseFields(
                                fieldWithPath("success").description("true when the request was successful"),
                                fieldWithPath("payload.date").description("The date of the jumpday"),
                                fieldWithPath("payload.jumping").description("true when it's a jumpday"),
                                fieldWithPath("payload.tandemmaster[]").description("A list of tandemmasters available at this date"),
                                fieldWithPath("payload.tandemmaster[].assigned").description("Assignment state of the tandemmaster"),
                                fieldWithPath("payload.tandemmaster[].allday").description("true if the tandemmaster is assigned all-day"),
                                fieldWithPath("payload.tandemmaster[].from").description("From time, if the tandemmaster is not assigned all-day"),
                                fieldWithPath("payload.tandemmaster[].to").description("To time, if the tandemmaster is not assigned all-day"),
                                fieldWithPath("payload.tandemmaster[].flyer.id").description("The tandemmaster's id"),
                                fieldWithPath("payload.tandemmaster[].flyer.firstName").description("Tandemmaster's first name"),
                                fieldWithPath("payload.tandemmaster[].flyer.lastName").description("Tandemmaster's last name"),
                                fieldWithPath("payload.tandemmaster[].flyer.email").description("Tandemmaster's email"),
                                fieldWithPath("payload.tandemmaster[].flyer.tel").description("Tandemmaster's phone number"),
                                fieldWithPath("payload.tandemmaster[].flyer.handcam").description("Tandemmaster's handcam"),
                                fieldWithPath("payload.videoflyer").description("A list of video flyers avalable at this date"),
                                fieldWithPath("payload.slots[]").description("The list of time slots on this jumpday"),
                                fieldWithPath("payload.slots[].time").description("The time of this slot"),
                                fieldWithPath("payload.slots[].tandemTotal").description("The total capacity of tandem slots"),
                                fieldWithPath("payload.slots[].tandemBooked").description("The total booked tandem slots"),
                                fieldWithPath("payload.slots[].tandemAvailable").description("The total available tandem slots"),
                                fieldWithPath("payload.slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                                fieldWithPath("payload.slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                                fieldWithPath("payload.slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                                fieldWithPath("payload.slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                                fieldWithPath("payload.slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                                fieldWithPath("payload.slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                                fieldWithPath("payload.slots[].handcamTotal").description("The total capacity of handcam slots"),
                                fieldWithPath("payload.slots[].handcamBooked").description("The total booked handcam slots"),
                                fieldWithPath("payload.slots[].handcamAvailable").description("The total available handcam slots"),
                                fieldWithPath("message").ignored(),
                                fieldWithPath("exception").ignored(),
                                fieldWithPath("payload.freeTimes").ignored(),
                                fieldWithPath("payload.slots[].appointments").ignored()
                        )));
    }

    @Test
    void testUpdateJumpday_WithVideoflyer() throws Exception {
        Videoflyer videoflyer = videoflyerRepository.save(ModelMockHelper.createVideoflyer());
        Jumpday unsavedJumpday = ModelMockHelper.createJumpday();
        unsavedJumpday.setVideoflyer(Collections.singletonList(ModelMockHelper.createAssignment(videoflyer)));
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(unsavedJumpday);
        Jumpday jumpday = result.getPayload();

        JumpdayConverter jumpdayConverter = new JumpdayConverter();
        JumpdayDTO jumpdayDTO = jumpdayConverter.convertToDto(jumpday);

        // don't set fields
        VideoflyerDTO videoflyerDTO = jumpdayDTO.getVideoflyer().get(0).getFlyer();
        videoflyerDTO.setEmail("");
        videoflyerDTO.setFirstName("");
        videoflyerDTO.setLastName("");
        videoflyerDTO.setTel("");

        int newCount = 4;
        jumpdayDTO.getSlots().get(0).setTandemTotal(newCount);

        String jumpdayJson = json(jumpdayDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andDo(document("jumpday/update-jumpday-with-videoflyer",
                        pathParameters(
                                parameterWithName("date").description("The date of the updated jumpday")
                        ),
                        requestFields(
                                fieldWithPath("date").description("The date of the jumpday"),
                                fieldWithPath("jumping").description("true when it's a jumpday"),
                                fieldWithPath("tandemmaster").description("A list of tandemmasters available at this date"),
                                fieldWithPath("videoflyer[]").description("A list of videoflyer available at this date"),
                                fieldWithPath("videoflyer[].assigned").description("Assignment state of the videoflyer"),
                                fieldWithPath("videoflyer[].allday").description("true if the videoflyer is assigned all-day"),
                                fieldWithPath("videoflyer[].from").description("From time, if the videoflyer is not assigned all-day"),
                                fieldWithPath("videoflyer[].to").description("To time, if the videoflyer is not assigned all-day"),
                                fieldWithPath("videoflyer[].flyer.id").description("The videoflyer's id"),
                                fieldWithPath("slots[]").description("The list of time slots on this jumpday"),
                                fieldWithPath("slots[].time").description("The time of this slot"),
                                fieldWithPath("slots[].tandemTotal").description("The total capacity of tandem slots"),
                                fieldWithPath("slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                                fieldWithPath("slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                                fieldWithPath("slots[].handcamTotal").description("The total capacity of handcam slots"),
                                fieldWithPath("slots[].tandemBooked").ignored(),
                                fieldWithPath("slots[].tandemAvailable").ignored(),
                                fieldWithPath("slots[].picOrVidBooked").ignored(),
                                fieldWithPath("slots[].picAndVidBooked").ignored(),
                                fieldWithPath("slots[].handcamBooked").ignored(),
                                fieldWithPath("slots[].picOrVidAvailable").ignored(),
                                fieldWithPath("slots[].picAndVidAvailable").ignored(),
                                fieldWithPath("slots[].handcamAvailable").ignored(),
                                fieldWithPath("slots[].appointments").ignored(),
                                fieldWithPath("freeTimes").ignored(),
                                fieldWithPath("videoflyer[].flyer.firstName").ignored(),
                                fieldWithPath("videoflyer[].flyer.lastName").ignored(),
                                fieldWithPath("videoflyer[].flyer.email").ignored(),
                                fieldWithPath("videoflyer[].flyer.tel").ignored(),
                                fieldWithPath("videoflyer[].flyer.picAndVid").ignored()
                        ), responseFields(
                                fieldWithPath("success").description("true when the request was successful"),
                                fieldWithPath("payload.date").description("The date of the jumpday"),
                                fieldWithPath("payload.jumping").description("true when it's a jumpday"),
                                fieldWithPath("payload.videoflyer[]").description("A list of videoflyer available at this date"),
                                fieldWithPath("payload.videoflyer[].assigned").description("Assignment state of the videoflyer"),
                                fieldWithPath("payload.videoflyer[].allday").description("true if the videoflyer is assigned all-day"),
                                fieldWithPath("payload.videoflyer[].from").description("From time, if the videoflyer is not assigned all-day"),
                                fieldWithPath("payload.videoflyer[].to").description("To time, if the videoflyer is not assigned all-day"),
                                fieldWithPath("payload.videoflyer[].flyer.id").description("The videoflyer's id"),
                                fieldWithPath("payload.videoflyer[].flyer.firstName").description("videoflyer's first name"),
                                fieldWithPath("payload.videoflyer[].flyer.lastName").description("videoflyer's last name"),
                                fieldWithPath("payload.videoflyer[].flyer.email").description("videoflyer's email"),
                                fieldWithPath("payload.videoflyer[].flyer.tel").description("videoflyer's phone number"),
                                fieldWithPath("payload.videoflyer[].flyer.picAndVid").description("videoflyer's pic and video settings"),
                                fieldWithPath("payload.tandemmaster").description("A list of tandemmasters avalable at this date"),
                                fieldWithPath("payload.slots[]").description("The list of time slots on this jumpday"),
                                fieldWithPath("payload.slots[].time").description("The time of this slot"),
                                fieldWithPath("payload.slots[].tandemTotal").description("The total capacity of tandem slots"),
                                fieldWithPath("payload.slots[].tandemBooked").description("The total booked tandem slots"),
                                fieldWithPath("payload.slots[].tandemAvailable").description("The total available tandem slots"),
                                fieldWithPath("payload.slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                                fieldWithPath("payload.slots[].picOrVidBooked").description("The total booked picture OR video slots"),
                                fieldWithPath("payload.slots[].picOrVidAvailable").description("The total available picture OR video slots"),
                                fieldWithPath("payload.slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                                fieldWithPath("payload.slots[].picAndVidBooked").description("The total booked picture AND video slots"),
                                fieldWithPath("payload.slots[].picAndVidAvailable").description("The total available picture AND video slots"),
                                fieldWithPath("payload.slots[].handcamTotal").description("The total capacity of handcam slots"),
                                fieldWithPath("payload.slots[].handcamBooked").description("The total booked handcam slots"),
                                fieldWithPath("payload.slots[].handcamAvailable").description("The total available handcam slots"),
                                fieldWithPath("message").ignored(),
                                fieldWithPath("exception").ignored(),
                                fieldWithPath("payload.freeTimes").ignored(),
                                fieldWithPath("payload.slots[].appointments").ignored()
                        )));
    }

    @Test
    void testUpdateJumpday_Unauthorized() throws Exception {
        Jumpday jumpday = ModelMockHelper.createJumpday();
        String jumpdayJson = json(jumpday);

        mockMvc.perform(put("/api/jumpday/{date}", LocalDate.now().toString())
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateJumpday_NotExisting() throws Exception {
        Jumpday jumpday = ModelMockHelper.createJumpday();
        jumpday.setDate(LocalDate.now().plus(1, ChronoUnit.YEARS));
        String jumpdayJson = json(jumpday);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Accept-Language", "en-US")
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Jumpday not found")));
    }

    @Test
    void testUpdateJumpday_InvalidChange() throws Exception {
        Jumpday savedJumpday = jumpdayRepository.save(ModelMockHelper.createJumpday());
        assertNotNull(savedJumpday);

        savedJumpday.getSlots().get(0).setTandemTotal(1);

        String jumpdayJson = json(savedJumpday);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Accept-Language", "en-US")
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Jumpday invalid")));
    }

    @Test
    void testUpdateJumpday_AppointmentExists() throws Exception {
        Jumpday savedJumpday = jumpdayRepository.save(ModelMockHelper.createJumpday());
        appointmentService.saveAppointment(ModelMockHelper.createAppointment(3, 0, 0, 0));
        assertNotNull(savedJumpday);

        savedJumpday.getSlots().get(0).setTandemTotal(2);

        String jumpdayJson = json(savedJumpday);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/jumpday/{date}", LocalDate.now().toString())
                .header("Accept-Language", "en-US")
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType)
                .content(jumpdayJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("The changed slot has too many appointments")));
    }

    @Test
    void testDeleteJumpday() throws Exception {
        Jumpday jumpday = ModelMockHelper.createJumpday();
        Jumpday savedJumpday = jumpdayRepository.save(jumpday);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/jumpday/{date}", savedJumpday.getDate())
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andDo(document("jumpday/delete-jumpday",
                        pathParameters(
                                parameterWithName("date").description("The date of the jumpday to delete")
                        ),
                        responseFields(
                                fieldWithPath("success").description("true when the request was successful"),
                                fieldWithPath("message").description("message in case of error"),
                                fieldWithPath("exception").ignored(),
                                fieldWithPath("payload").ignored()
                        )));
    }

    @Test
    void testDeleteJumpday_Unauthorized() throws Exception {
        Jumpday jumpday = ModelMockHelper.createJumpday();
        Jumpday savedJumpday = jumpdayRepository.save(jumpday);

        mockMvc.perform(delete("/api/jumpday/{date}", savedJumpday.getDate())
                .contentType(contentType))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteJumpday_NotFound() throws Exception {
        mockMvc.perform(delete("/api/jumpday/{date}", LocalDate.now().plus(1, ChronoUnit.YEARS))
                .header("Accept-Language", "en-US")
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Jumpday not found")));
    }

    @Test
    void testDeleteJumpday_AppointmentExists() throws Exception {
        Jumpday jumpday = ModelMockHelper.createJumpday();
        Jumpday savedJumpday = jumpdayRepository.save(jumpday);
        GenericResult<Appointment> result = appointmentService.saveAppointment(ModelMockHelper.createSingleAppointment());
        assertTrue(result.isSuccess());

        mockMvc.perform(delete("/api/jumpday/{date}", savedJumpday.getDate())
                .header("Accept-Language", "en-US")
                .header("Authorization", MockJwtDecoder.addHeader(UPDATE_JUMPDAYS))
                .contentType(contentType))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("The jumpday still has appointments")));
    }


    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    @TestConfiguration
    static class CustomizationConfiguration implements RestDocsMockMvcConfigurationCustomizer {
        @Override
        public void customize(MockMvcRestDocumentationConfigurer configurer) {
            configurer.operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint());
        }
        @Bean
        public RestDocumentationResultHandler restDocumentation() {
            return MockMvcRestDocumentation.document("{method-name}");
        }
    }
}
