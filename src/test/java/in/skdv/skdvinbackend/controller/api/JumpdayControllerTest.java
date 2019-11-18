package in.skdv.skdvinbackend.controller.api;

import in.skdv.skdvinbackend.AbstractSkdvinTest;
import in.skdv.skdvinbackend.ModelMockHelper;
import in.skdv.skdvinbackend.model.entity.Appointment;
import in.skdv.skdvinbackend.model.entity.Jumpday;
import in.skdv.skdvinbackend.repository.JumpdayRepository;
import in.skdv.skdvinbackend.service.IAppointmentService;
import in.skdv.skdvinbackend.service.IJumpdayService;
import in.skdv.skdvinbackend.util.GenericResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;

import static in.skdv.skdvinbackend.config.Authorities.CREATE_JUMPDAYS;
import static in.skdv.skdvinbackend.config.Authorities.READ_JUMPDAYS;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JumpdayControllerTest extends AbstractSkdvinTest {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

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
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .apply(documentationConfiguration(this.restDocumentation))
                        .build();

        jumpdayRepository.deleteAll();
    }

    @Test
    @WithMockUser
    public void testCreateJumpday() throws Exception {
        String jumpdayJson = json(ModelMockHelper.createJumpday());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday/")
                .contentType(contentType)
                .content(jumpdayJson))
                .andDo(MockMvcResultHandlers.print())
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
                        fieldWithPath("tandemmaster").description("A list of tandem masters avalable at this date"),
                        fieldWithPath("videoflyer").description("A list of video flyers avalable at this date"),
                        fieldWithPath("slots[]").description("The list of time slots on this jumpday"),
                        fieldWithPath("slots[].time").description("The time of this slot"),
                        fieldWithPath("slots[].tandemTotal").description("The total capacity of tandem slots"),
                        fieldWithPath("slots[].picOrVidTotal").description("The total capacity of picture OR video slots"),
                        fieldWithPath("slots[].picAndVidTotal").description("The total capacity of picture AND video slots"),
                        fieldWithPath("slots[].handcamTotal").description("The total capacity of handcam slots"),
                        fieldWithPath("slots[].appointments").ignored(),
                        fieldWithPath("slots[].tandemBooked").ignored(),
                        fieldWithPath("slots[].tandemAvailable").ignored(),
                        fieldWithPath("slots[].picOrVidBooked").ignored(),
                        fieldWithPath("slots[].picAndVidBooked").ignored(),
                        fieldWithPath("slots[].handcamBooked").ignored(),
                        fieldWithPath("slots[].picOrVidAvailable").ignored(),
                        fieldWithPath("slots[].picAndVidAvailable").ignored(),
                        fieldWithPath("slots[].handcamAvailable").ignored(),
                        fieldWithPath("freeTimes").ignored()
                        ), responseFields(
                        fieldWithPath("success").description("true when the request was successful"),
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
    @WithMockUser(authorities = CREATE_JUMPDAYS)
    public void testCreateJumpday_AlreadyExists_DE() throws Exception {
        String jumpdayJson = json(ModelMockHelper.createJumpday());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday")
                .contentType(contentType)
                .content(jumpdayJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday?lang=de")
                .contentType(contentType)
                .content(jumpdayJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Sprungtag existiert bereits")));
    }

    @Test
    @WithMockUser(authorities = CREATE_JUMPDAYS)
    public void testCreateJumpday_AlreadyExists_EN() throws Exception {
        String jumpdayJson = json(ModelMockHelper.createJumpday());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday")
                .contentType(contentType)
                .content(jumpdayJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/jumpday?lang=en")
                .contentType(contentType)
                .content(jumpdayJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Jumpday already exists")));
    }


    @Test
    @WithMockUser
    public void testGetAll() throws Exception {
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(ModelMockHelper.createJumpday());
        assertTrue(result.isSuccess());
        GenericResult<Jumpday> result2 = jumpdayService.saveJumpday(ModelMockHelper.createJumpday(LocalDate.now().plusDays(1)));
        assertTrue(result2.isSuccess());

        mockMvc.perform(get("/api/jumpday/"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload", hasSize(2)))
                .andExpect(jsonPath("$.payload[0].date", is(result.getPayload().getDate().toString())))
                .andExpect(jsonPath("$.payload[1].date", is(result2.getPayload().getDate().toString())));
    }


    @Test
    @WithMockUser
    public void testGetAll_OneResult() throws Exception {
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(ModelMockHelper.createJumpday());
        Jumpday jumpday = result.getPayload();

        mockMvc.perform(get("/api/jumpday/"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
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
                .andDo(document("jumpday/get-jumpday", responseFields(
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
    @WithMockUser
    public void testGetAll_NoResult() throws Exception {
        mockMvc.perform(get("/api/jumpday/"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload", hasSize(0)));
    }

    @Test
    @WithMockUser(authorities = READ_JUMPDAYS)
    public void testGetByDate() throws Exception {
        GenericResult<Jumpday> result = jumpdayService.saveJumpday(ModelMockHelper.createJumpday());
        Jumpday jumpday = result.getPayload();

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/jumpday/{date}", jumpday.getDate().toString()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.payload.date", is(jumpday.getDate().toString())))
                .andExpect(jsonPath("$.payload.jumping", is(jumpday.isJumping())))
                .andExpect(jsonPath("$.payload.slots", hasSize(jumpday.getSlots().size())))
                .andExpect(jsonPath("$.payload.slots[0].time", is(jumpday.getSlots().get(0).getTime().toString())))
                .andExpect(jsonPath("$.payload.slots[0].tandemTotal", is(jumpday.getSlots().get(0).getTandemTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picOrVidTotal", is(jumpday.getSlots().get(0).getPicOrVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].picAndVidTotal", is(jumpday.getSlots().get(0).getPicAndVidTotal())))
                .andExpect(jsonPath("$.payload.slots[0].handcamTotal", is(jumpday.getSlots().get(0).getHandcamTotal())))
                .andDo(document("jumpday/get-jumpdays", pathParameters(
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
    @WithMockUser(authorities = READ_JUMPDAYS)
    public void testGetByDate_WithAppointments() throws Exception {
        // 4 tandem / 2 video at 10:00 and 11:30
        Jumpday jumpday = ModelMockHelper.createJumpday();
        // 1 tandem / 1 video at 10:00
        Appointment appointment1 = ModelMockHelper.createSingleAppointment();
        // 2 tandem / 0 video at 10:00
        Appointment appointment2 = ModelMockHelper.createSecondAppointment();

        jumpdayService.saveJumpday(jumpday);
        appointmentService.saveAppointment(appointment1);
        appointmentService.saveAppointment(appointment2);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/jumpday/{date}", jumpday.getDate().toString()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
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
    @WithMockUser(authorities = READ_JUMPDAYS)
    public void testGetByDate_NotFound_DE() throws Exception {
        mockMvc.perform(get("/api/jumpday/{date}?lang=de", LocalDate.now().toString()))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Sprungtag nicht gefunden")));
    }

    @Test
    @WithMockUser(authorities = READ_JUMPDAYS)
    public void testGetByDate_NotFound_EN() throws Exception {
        mockMvc.perform(get("/api/jumpday/{date}?lang=en", LocalDate.now().toString()))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Jumpday not found")));
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
