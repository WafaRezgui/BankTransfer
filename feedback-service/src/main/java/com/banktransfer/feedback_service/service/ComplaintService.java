package com.banktransfer.feedback_service.service;

import com.banktransfer.feedback_service.dto.*;
import com.banktransfer.feedback_service.event.ComplaintCreatedEvent;
import com.banktransfer.feedback_service.event.ComplaintResolvedEvent;
import com.banktransfer.feedback_service.kafka.ComplaintEventProducer;
import com.banktransfer.feedback_service.mapper.ComplaintMapper;
import com.banktransfer.feedback_service.model.Complaint;
import com.banktransfer.feedback_service.model.ComplaintPriority;
import com.banktransfer.feedback_service.model.ComplaintStatus;
import com.banktransfer.feedback_service.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintMapper complaintMapper;
    private final ComplaintEventProducer eventProducer;

    // Mots-clés déclenchant une priorité HIGH automatique.
    // Base volontairement simple (règles), extensible plus tard
    // vers une classification par modèle de langage.
    private static final Set<String> URGENT_KEYWORDS = Set.of(
            "urgent", "fraude", "bloqué", "bloque", "vol", "hacké", "hack",
            "argent disparu", "disparu", "piraté", "pirate"
    );

    public ComplaintResponse createComplaint(ComplaintRequest request) {
        Complaint complaint = complaintMapper.toEntity(request);
        complaint.setPriority(detectPriority(request.getSubject(), request.getDescription()));

        Complaint saved = complaintRepository.save(complaint);

        eventProducer.publishCreated(
                ComplaintCreatedEvent.builder()
                        .complaintId(saved.getId())
                        .userId(saved.getUserId())
                        .subject(saved.getSubject())
                        .priority(saved.getPriority().name())
                        .build()
        );

        return complaintMapper.toResponse(saved);
    }

    // Analyse simple par mots-clés : si le sujet ou la description
    // contient un terme sensible, la réclamation est priorisée HIGH.
    private ComplaintPriority detectPriority(String subject, String description) {
        String combined = (subject + " " + description).toLowerCase(Locale.FRENCH);

        boolean isUrgent = URGENT_KEYWORDS.stream().anyMatch(combined::contains);

        return isUrgent ? ComplaintPriority.HIGH : ComplaintPriority.MEDIUM;
    }

    public ComplaintResponse resolveComplaint(Long id, ComplaintResolveRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réclamation introuvable"));

        complaint.setAdminResponse(request.getAdminResponse());
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        eventProducer.publishResolved(
                ComplaintResolvedEvent.builder()
                        .complaintId(updated.getId())
                        .userId(updated.getUserId())
                        .subject(updated.getSubject())
                        .adminResponse(updated.getAdminResponse())
                        .build()
        );

        return complaintMapper.toResponse(updated);
    }

    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réclamation introuvable"));
        return complaintMapper.toResponse(complaint);
    }

    public List<ComplaintResponse> getComplaintsByUserId(Long userId) {
        return complaintRepository.findByUserId(userId).stream()
                .map(complaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(complaintMapper::toResponse)
                .collect(Collectors.toList());
    }
}