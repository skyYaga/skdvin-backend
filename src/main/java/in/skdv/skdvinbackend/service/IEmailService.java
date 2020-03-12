package in.skdv.skdvinbackend.service;

import in.skdv.skdvinbackend.model.entity.Appointment;

import javax.mail.MessagingException;

public interface IEmailService {

    void sendAppointmentVerification(Appointment appointment) throws MessagingException;

    void sendAppointmentConfirmation(Appointment appointment) throws MessagingException;

    void sendAppointmentUnconfirmedCancellation(Appointment appointment) throws MessagingException;

    void sendAppointmentUpdated(Appointment appointment) throws MessagingException;
}
