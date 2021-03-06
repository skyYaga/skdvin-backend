package in.skdv.skdvinbackend.service.impl;

import in.skdv.skdvinbackend.AbstractSkdvinTest;
import in.skdv.skdvinbackend.ModelMockHelper;
import in.skdv.skdvinbackend.exception.ErrorMessage;
import in.skdv.skdvinbackend.model.common.FreeSlot;
import in.skdv.skdvinbackend.model.common.GroupSlot;
import in.skdv.skdvinbackend.model.common.SlotQuery;
import in.skdv.skdvinbackend.model.entity.*;
import in.skdv.skdvinbackend.repository.JumpdayRepository;
import in.skdv.skdvinbackend.service.IAppointmentService;
import in.skdv.skdvinbackend.util.GenericResult;
import in.skdv.skdvinbackend.util.VerificationTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MongoAppointmentServiceTest extends AbstractSkdvinTest {

    @Autowired
    private JumpdayRepository jumpdayRepository;

    @Autowired
    private IAppointmentService appointmentService;

    @BeforeEach
    void setup() {
        // Set mock clock
        Clock mockClock = Clock.fixed(Instant.parse(LocalDate.now().toString() + "T00:00:00Z"), ZoneOffset.UTC);
        ReflectionTestUtils.setField(appointmentService, "clock", mockClock);

        jumpdayRepository.deleteAll();
        jumpdayRepository.save(ModelMockHelper.createJumpday());
    }

    @Test
    void testSaveAppointment() {
        Appointment appointment = ModelMockHelper.createSingleAppointment();

        assertNull(appointment.getCreatedOn());
        assertEquals(0, appointment.getAppointmentId());

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertTrue(savedAppointment.isSuccess());
        assertNotNull(savedAppointment.getPayload().getCreatedOn());
        assertNotEquals(0, savedAppointment.getPayload().getAppointmentId());
    }


    @Test
    void testSaveAdminAppointment() {
        Appointment appointment = ModelMockHelper.createSingleAppointment();
        appointment.getCustomer().setJumpers(Collections.emptyList());

        assertNull(appointment.getCreatedOn());
        assertEquals(0, appointment.getAppointmentId());

        GenericResult<Appointment> savedAppointment = appointmentService.saveAdminAppointment(appointment);

        assertTrue(savedAppointment.isSuccess());
        assertEquals(AppointmentState.CONFIRMED, savedAppointment.getPayload().getState());
    }

    @Test
    void testSaveAppointment_WithNote() {
        String note = "Price is 10% off";
        Appointment appointment = ModelMockHelper.createSingleAppointment();
        appointment.setNote(note);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertTrue(savedAppointment.isSuccess());
        assertEquals(note, savedAppointment.getPayload().getNote());
    }

    @Test
    void testSaveAppointment_NoJumpday() {
        Appointment appointment = ModelMockHelper.createSingleAppointment();
        appointment.setDate(LocalDateTime.now().plusDays(1));

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.JUMPDAY_NOT_FOUND_MSG.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_NoTandemSlotsAvailable() {
        Appointment appointment = ModelMockHelper.createAppointment(5, 0, 0, 0);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.JUMPDAY_NO_FREE_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_NoSlotsAvailable() {
        Appointment appointment = ModelMockHelper.createAppointment(5, 3, 0, 0);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.JUMPDAY_NO_FREE_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_PicOrVid_NoSlotsAvailable() {
        Appointment appointment = ModelMockHelper.createAppointment(3, 3, 0, 0);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.JUMPDAY_NO_FREE_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_PicOrVid_MoreVideoThanTandemSlots() {
        Appointment appointment = ModelMockHelper.createAppointment(3, 5, 0, 0);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.APPOINTMENT_MORE_VIDEO_THAN_TAMDEM_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_PicAndVid_NoSlotsAvailable() {
        Appointment appointment = ModelMockHelper.createAppointment(3, 0, 3, 0);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.JUMPDAY_NO_FREE_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_PicAndVid_MoreVideoThanTandemSlots() {
        Appointment appointment = ModelMockHelper.createAppointment(3, 0, 5, 0);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.APPOINTMENT_MORE_VIDEO_THAN_TAMDEM_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_Handcam_NoSlotsAvailable() {
        Appointment appointment = ModelMockHelper.createAppointment(3, 0, 0, 3);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.JUMPDAY_NO_FREE_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_Handcam_MoreVideoThanTandemSlots() {
        Appointment appointment = ModelMockHelper.createAppointment(3, 0, 0, 5);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.APPOINTMENT_MORE_VIDEO_THAN_TAMDEM_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_PicVidHandcam_MoreVideoThanTandemSlots() {
        Appointment appointment = ModelMockHelper.createAppointment(3, 2, 1, 1);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.APPOINTMENT_MORE_VIDEO_THAN_TAMDEM_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testSaveAppointment_PicAndVid_MoreCombinedVideoSlotsThanAvailable() {
        Appointment appointment = ModelMockHelper.createAppointment(4, 2, 1, 0);

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(appointment);

        assertFalse(savedAppointment.isSuccess());
        assertEquals(ErrorMessage.JUMPDAY_NO_FREE_SLOTS.toString(), savedAppointment.getMessage());
    }

    @Test
    void testFindAppointment() {
        GenericResult<Appointment> appointment = appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());
        appointmentService.saveAppointment(ModelMockHelper.createSingleAppointment());
        assertTrue(appointment.isSuccess());

        Appointment foundAppointment = appointmentService.findAppointment(appointment.getPayload().getAppointmentId());

        assertEquals(appointment.getPayload().getAppointmentId(), foundAppointment.getAppointmentId());
        assertEquals(appointment.getPayload().getTandem(), foundAppointment.getTandem());
        assertEquals(appointment.getPayload().getCustomer().getFirstName(), foundAppointment.getCustomer().getFirstName());
    }

    @Test
    void testFindAppointment_InvalidId() {
        Appointment foundAppointment = appointmentService.findAppointment(9999999);
        assertNull(foundAppointment);
    }

    @Test
    void testFindAppointmentsByDay() {
        appointmentService.saveAppointment(ModelMockHelper.createSingleAppointment());
        appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());

        List<Appointment> appointments = appointmentService.findAppointmentsByDay(LocalDate.now());

        assertEquals(2, appointments.size());
        assertNotEquals(appointments.get(0).getAppointmentId(), appointments.get(1).getAppointmentId());
    }

    @Test
    void testFindAppointmentsByDay_NoJumpday() {
        appointmentService.saveAppointment(ModelMockHelper.createSingleAppointment());
        appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());

        List<Appointment> appointments = appointmentService.findAppointmentsByDay(LocalDate.now().plus(1, ChronoUnit.DAYS));

        assertTrue(appointments.isEmpty());
    }

    @Test
    void testUpdateAppointment() {
        GenericResult<Appointment> appointment = appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());
        assertTrue(appointment.isSuccess());
        int appointmentId = appointment.getPayload().getAppointmentId();
        appointment.getPayload().getCustomer().setFirstName("Unitbob");

        GenericResult<Appointment> updatedAppointment = appointmentService.updateAppointment(appointment.getPayload());

        assertTrue(updatedAppointment.isSuccess());
        assertEquals(appointmentId, updatedAppointment.getPayload().getAppointmentId());
        assertEquals("Unitbob", updatedAppointment.getPayload().getCustomer().getFirstName());
    }

    @Test
    void testUpdateAppointment_WithNote() {
        String note = "Price is 10% off";

        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(ModelMockHelper.createSingleAppointment());
        assertTrue(savedAppointment.isSuccess());
        assertTrue(savedAppointment.getPayload().getNote().isEmpty());
        savedAppointment.getPayload().setNote(note);

        GenericResult<Appointment> updatedAppointment = appointmentService.updateAppointment(savedAppointment.getPayload());

        assertTrue(updatedAppointment.isSuccess());
        assertEquals(note, updatedAppointment.getPayload().getNote());
    }

    @Test
    void testUpdateAppointment_ChangeTime() {
        GenericResult<Appointment> appointmentResult = appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());
        assertTrue(appointmentResult.isSuccess());
        Appointment appointment = appointmentResult.getPayload();
        int appointmentId = appointment.getAppointmentId();
        LocalDateTime newDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 30));
        appointment.setDate(newDate);

        GenericResult<Appointment> updatedAppointment = appointmentService.updateAppointment(appointment);

        assertTrue(updatedAppointment.isSuccess());
        assertEquals(appointmentId, updatedAppointment.getPayload().getAppointmentId());
        assertEquals("Jane", updatedAppointment.getPayload().getCustomer().getFirstName());
        assertEquals(newDate, updatedAppointment.getPayload().getDate());
    }

    @Test
    void testUpdateAppointment_ChangeDate() {
        jumpdayRepository.save(ModelMockHelper.createJumpday(LocalDate.now().plusDays(1)));
        GenericResult<Appointment> appointmentResult = appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());
        assertTrue(appointmentResult.isSuccess());
        Appointment appointment = appointmentResult.getPayload();
        int appointmentId = appointment.getAppointmentId();
        LocalDateTime newDate = appointment.getDate().plusDays(1);
        appointment.setDate(newDate);

        GenericResult<Appointment> updatedAppointment = appointmentService.updateAppointment(appointment);

        assertTrue(updatedAppointment.isSuccess());
        assertEquals(appointmentId, updatedAppointment.getPayload().getAppointmentId());
        assertEquals("Jane", updatedAppointment.getPayload().getCustomer().getFirstName());
        assertEquals(newDate, updatedAppointment.getPayload().getDate());
    }

    @Test
    void testUpdateAppointment_ChangeDateAndTime() {
        jumpdayRepository.save(ModelMockHelper.createJumpday(LocalDate.now().plusDays(1)));
        GenericResult<Appointment> appointmentResult = appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());
        assertTrue(appointmentResult.isSuccess());
        Appointment appointment = appointmentResult.getPayload();
        int appointmentId = appointment.getAppointmentId();
        LocalDateTime newDate = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(11, 30));
        appointment.setDate(newDate);

        GenericResult<Appointment> updatedAppointment = appointmentService.updateAppointment(appointment);

        assertTrue(updatedAppointment.isSuccess());
        assertEquals(appointmentId, updatedAppointment.getPayload().getAppointmentId());
            assertEquals("Jane", updatedAppointment.getPayload().getCustomer().getFirstName());
        assertEquals(newDate, updatedAppointment.getPayload().getDate());
    }

    @Test
    void testUpdateAdminAppointment() {
        GenericResult<Appointment> appointment = appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());
        assertTrue(appointment.isSuccess());
        int appointmentId = appointment.getPayload().getAppointmentId();
        appointment.getPayload().getCustomer().setJumpers(Collections.emptyList());
        appointment.getPayload().setTandem(3);

        GenericResult<Appointment> updatedAppointment = appointmentService.updateAdminAppointment(appointment.getPayload());

        assertTrue(updatedAppointment.isSuccess());
        assertEquals(appointmentId, updatedAppointment.getPayload().getAppointmentId());
        assertEquals(0, updatedAppointment.getPayload().getCustomer().getJumpers().size());
        assertEquals(3, updatedAppointment.getPayload().getTandem());
    }

    @Test
    void testUpdateAdminAppointment_ChangeDate() {
        jumpdayRepository.save(ModelMockHelper.createJumpday(LocalDate.now().plusDays(1)));
        GenericResult<Appointment> appointment = appointmentService.saveAppointment(ModelMockHelper.createSecondAppointment());
        assertTrue(appointment.isSuccess());
        int appointmentId = appointment.getPayload().getAppointmentId();
        appointment.getPayload().getCustomer().setJumpers(Collections.emptyList());
        LocalDateTime newDate = appointment.getPayload().getDate().plusDays(1);
        appointment.getPayload().setDate(newDate);

        GenericResult<Appointment> updatedAppointment = appointmentService.updateAdminAppointment(appointment.getPayload());

        assertTrue(updatedAppointment.isSuccess());
        assertEquals(appointmentId, updatedAppointment.getPayload().getAppointmentId());
        assertEquals(0, updatedAppointment.getPayload().getCustomer().getJumpers().size());
        assertEquals(newDate, updatedAppointment.getPayload().getDate());
    }

    @Test
    void testFindFreeSlots() {
        SlotQuery slotQuery = new SlotQuery(2, 1, 0, 0);

        GenericResult<List<FreeSlot>> freeSlots = appointmentService.findFreeSlots(slotQuery);

        assertTrue(freeSlots.isSuccess());
        assertNotNull(freeSlots.getPayload());
        assertEquals(1, freeSlots.getPayload().size());
        assertEquals(LocalDate.now(), freeSlots.getPayload().get(0).getDate());
        assertEquals(2, freeSlots.getPayload().get(0).getTimes().size());
        assertEquals(LocalTime.of(10, 0), freeSlots.getPayload().get(0).getTimes().get(0));
        assertEquals(LocalTime.of(11, 30), freeSlots.getPayload().get(0).getTimes().get(1));
    }

    @Test
    void testFindFreeSlots_TooManyTandems() {
        SlotQuery slotQuery = new SlotQuery(5, 1, 0, 0);

        GenericResult<List<FreeSlot>> freeSlots = appointmentService.findFreeSlots(slotQuery);

        assertFalse(freeSlots.isSuccess());
        assertNull(freeSlots.getPayload());
        assertEquals(ErrorMessage.APPOINTMENT_NO_FREE_SLOTS.toString(), freeSlots.getMessage());
    }

    @Test
    void testFindFreeSlots_TooManyVids() {
        SlotQuery slotQuery = new SlotQuery(4, 4, 0, 0);

        GenericResult<List<FreeSlot>> freeSlots = appointmentService.findFreeSlots(slotQuery);

        assertFalse(freeSlots.isSuccess());
        assertNull(freeSlots.getPayload());
        assertEquals(ErrorMessage.APPOINTMENT_NO_FREE_SLOTS.toString(), freeSlots.getMessage());
    }

    @Test
    void testFindFreeSlots_TooManyCombinedPicVids() {
        SlotQuery slotQuery = new SlotQuery(4, 2, 1, 0);

        GenericResult<List<FreeSlot>> freeSlots = appointmentService.findFreeSlots(slotQuery);

        assertFalse(freeSlots.isSuccess());
        assertNull(freeSlots.getPayload());
        assertEquals(ErrorMessage.APPOINTMENT_NO_FREE_SLOTS.toString(), freeSlots.getMessage());
    }

    @Test
    void testUpdateAppointmentState() {
        GenericResult<Appointment> result = appointmentService.saveAppointment(ModelMockHelper.createSingleAppointment());
        GenericResult<Void> stateResult = appointmentService.updateAppointmentState(result.getPayload(), AppointmentState.CONFIRMED);
        Appointment appointment = appointmentService.findAppointment(result.getPayload().getAppointmentId());

        assertTrue(stateResult.isSuccess());
        assertEquals(AppointmentState.CONFIRMED, appointment.getState());
    }

    @Test
    void testUpdateAppointmentState_InvalidAppointment() {
        Appointment invalidAppointment = ModelMockHelper.createSingleAppointment();
        GenericResult<Void> result = appointmentService.updateAppointmentState(invalidAppointment, AppointmentState.CONFIRMED);

        assertFalse(result.isSuccess());
        assertEquals(ErrorMessage.APPOINTMENT_NOT_FOUND.toString(), result.getMessage());
    }

    @Test
    void testFindUnconfirmedAppointments_ExpiredAndUnconfirmed() {
        Appointment appointment = ModelMockHelper.createSingleAppointment();
        VerificationToken verificationToken = VerificationTokenUtil.generate();
        verificationToken.setExpiryDate(LocalDateTime.now().minus(25, ChronoUnit.HOURS));
        appointment.setVerificationToken(verificationToken);
        appointmentService.saveAppointment(appointment);

        List<Appointment> unconfirmedAppointments = appointmentService.findUnconfirmedAppointments();

        assertEquals(1, unconfirmedAppointments.size());
    }

    @Test
    void testFindUnconfirmedAppointments_ExpiredAndConfirmed() {
        Appointment appointment = ModelMockHelper.createSingleAppointment();
        VerificationToken verificationToken = VerificationTokenUtil.generate();
        verificationToken.setExpiryDate(LocalDateTime.now().minus(25, ChronoUnit.HOURS));
        appointment.setVerificationToken(verificationToken);
        appointment.setState(AppointmentState.CONFIRMED);
        appointmentService.saveAppointment(appointment);

        List<Appointment> unconfirmedAppointments = appointmentService.findUnconfirmedAppointments();

        assertEquals(0, unconfirmedAppointments.size());
    }

    @Test
    void testFindUnconfirmedAppointments_NotExpiredAndUnconfirmed() {
        Appointment appointment = ModelMockHelper.createSingleAppointment();
        appointment.setVerificationToken(VerificationTokenUtil.generate());
        appointmentService.saveAppointment(appointment);

        List<Appointment> unconfirmedAppointments = appointmentService.findUnconfirmedAppointments();

        assertEquals(0, unconfirmedAppointments.size());
    }

    @Test
    void testDeleteAppointment() {
        Appointment appointment = ModelMockHelper.createSingleAppointment();
        appointmentService.saveAppointment(appointment);

        appointmentService.deleteAppointment(appointment.getAppointmentId());

        assertNull(appointmentService.findAppointment(appointment.getAppointmentId()));
    }

    @Test
    void testFindGroupSlots() {
        SlotQuery slotQuery = new SlotQuery(6, 0, 0, 0);

        List<GroupSlot> groupSlots = appointmentService.findGroupSlots(slotQuery);

        assertNotNull(groupSlots);
        assertEquals(1, groupSlots.size());
        assertEquals(LocalDate.now(), groupSlots.get(0).getDate());
        assertEquals(LocalTime.of(10, 0), groupSlots.get(0).getFirstTime());
        assertEquals(LocalTime.of(11, 30), groupSlots.get(0).getLastTime());
        assertEquals(2, groupSlots.get(0).getTimeCount());
        assertEquals(8, groupSlots.get(0).getTandemAvailable());
        assertEquals(4, groupSlots.get(0).getPicOrVidAvailable());
        assertEquals(2, groupSlots.get(0).getPicAndVidAvailable());
        assertEquals(2, groupSlots.get(0).getHandcamAvailable());
        assertEquals(2, groupSlots.get(0).getSlots().size());
        assertEquals(LocalTime.of(10, 0), groupSlots.get(0).getSlots().get(0).getTime());
        assertEquals(LocalTime.of(11, 30), groupSlots.get(0).getSlots().get(1).getTime());
        assertEquals(4, groupSlots.get(0).getSlots().get(0).getTandemAvailable());
        assertEquals(2, groupSlots.get(0).getSlots().get(0).getPicOrVidAvailable());
        assertEquals(1, groupSlots.get(0).getSlots().get(0).getPicAndVidAvailable());
        assertEquals(1, groupSlots.get(0).getSlots().get(0).getHandcamAvailable());
    }

    @Test
    void testFindGroupSlots_MultipleDays() {
        Jumpday jumpday = ModelMockHelper.createJumpday(LocalDate.now().plus(1, ChronoUnit.DAYS));
        Slot slot = new Slot();
        slot.setTime(LocalTime.of(13, 0));
        slot.setTandemTotal(4);
        slot.setPicOrVidTotal(2);
        slot.setPicAndVidTotal(1);
        slot.setHandcamTotal(1);
        jumpday.getSlots().add(slot);
        jumpdayRepository.save(jumpday);

        SlotQuery slotQuery = new SlotQuery(6, 0, 0, 0);

        List<GroupSlot> groupSlots = appointmentService.findGroupSlots(slotQuery);

        assertNotNull(groupSlots);
        assertEquals(3, groupSlots.size());
    }

    @Test
    void testFindGroupSlots_NoFreeSlots() {
        Jumpday jumpday = ModelMockHelper.createJumpday();
        jumpday.getSlots().forEach(s -> s.setTandemTotal(0));
        jumpdayRepository.deleteAll();
        jumpdayRepository.save(jumpday);

        SlotQuery slotQuery = new SlotQuery(6, 0, 0, 0);

        List<GroupSlot> groupSlots = appointmentService.findGroupSlots(slotQuery);

        assertNotNull(groupSlots);
        assertEquals(0, groupSlots.size());
    }
    
    @Test
    void testFindAppointmentsWithinNextWeek() {
        jumpdayRepository.save(ModelMockHelper.createJumpday(LocalDate.now().minusDays(1)));
        jumpdayRepository.save(ModelMockHelper.createJumpday(LocalDate.now().plusDays(5)));
        jumpdayRepository.save(ModelMockHelper.createJumpday(LocalDate.now().plusDays(10)));

        Appointment pastAppointment = ModelMockHelper.createSingleAppointment();
        Appointment todayAppointment = ModelMockHelper.createSingleAppointment();
        Appointment thisWeekAppointment = ModelMockHelper.createSingleAppointment();
        Appointment futureAppointment = ModelMockHelper.createSingleAppointment();

        pastAppointment.setDate(LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(10, 0)));
        thisWeekAppointment.setDate(LocalDateTime.of(LocalDate.now().plusDays(5), LocalTime.of(10, 0)));
        futureAppointment.setDate(LocalDateTime.of(LocalDate.now().plusDays(10), LocalTime.of(10, 0)));

        appointmentService.saveAppointment(pastAppointment);
        appointmentService.saveAppointment(todayAppointment);
        appointmentService.saveAppointment(thisWeekAppointment);
        appointmentService.saveAppointment(futureAppointment);

        List<Appointment> appointmentsWithinNextWeek = appointmentService.findAppointmentsWithinNextWeek();

        assertEquals(2, appointmentsWithinNextWeek.size());
        assertEquals(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)), appointmentsWithinNextWeek.get(0).getDate());
        assertEquals(LocalDateTime.of(LocalDate.now().plusDays(5), LocalTime.of(10, 0)), appointmentsWithinNextWeek.get(1).getDate());
    }

    @Test
    void testReminderSent() {
        GenericResult<Appointment> savedAppointment = appointmentService.saveAppointment(ModelMockHelper.createSingleAppointment());

        appointmentService.reminderSent(savedAppointment.getPayload());

        Appointment appointment = appointmentService.findAppointment(savedAppointment.getPayload().getAppointmentId());

        assertTrue(appointment.isReminderSent());
    }

    @Test
    void testReminderSent_AdminAppointment() {
        Appointment singleAppointment = ModelMockHelper.createSingleAppointment();
        singleAppointment.getCustomer().setJumpers(Collections.emptyList());
        GenericResult<Appointment> savedAppointment = appointmentService.saveAdminAppointment(singleAppointment);

        appointmentService.reminderSent(savedAppointment.getPayload());

        Appointment appointment = appointmentService.findAppointment(savedAppointment.getPayload().getAppointmentId());

        assertTrue(appointment.isReminderSent());
    }
}
