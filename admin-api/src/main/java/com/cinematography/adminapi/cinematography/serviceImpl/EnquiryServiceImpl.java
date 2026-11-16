package com.cinematography.adminapi.cinematography.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.cinematography.adminapi.cinematography.dtos.EnquiresRequest;
import com.cinematography.adminapi.cinematography.entities.EnquiryValue;
import com.cinematography.adminapi.cinematography.repository.EnquiryRepository;
import com.cinematography.adminapi.cinematography.service.EnquiryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnquiryServiceImpl implements EnquiryService {
    private final EnquiryRepository enquiryRepository;
    private final JavaMailSender mailSender;

    @Override
    public void sendEnquiry(EnquiresRequest request) {

        EnquiryValue enquiry = EnquiryValue.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .service(request.getService())
                .eventDate(
                        request.getEventDate() != null
                                ? LocalDate.parse(request.getEventDate())
                                : null)
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        enquiryRepository.save(enquiry);

        sendEmail(request);
    }

    private void sendEmail(EnquiresRequest request) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo("admin@cinematography.com");
        mail.setReplyTo(request.getEmail());
        mail.setSubject("New Enquiry - " + request.getService());

        mail.setText(
                "Name : " + request.getFullName() + "\n" +
                        "Email : " + request.getEmail() + "\n" +
                        "Service : " + request.getService() + "\n" +
                        "Event Date : " + request.getEventDate() + "\n\n" +
                        "Message :\n" +
                        request.getMessage());

        mailSender.send(mail);
    }

}
