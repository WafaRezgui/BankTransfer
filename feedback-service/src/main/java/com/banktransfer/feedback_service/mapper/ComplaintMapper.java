package com.banktransfer.feedback_service.mapper;

import com.banktransfer.feedback_service.dto.ComplaintRequest;
import com.banktransfer.feedback_service.dto.ComplaintResponse;
import com.banktransfer.feedback_service.model.Complaint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComplaintMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "adminResponse", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    Complaint toEntity(ComplaintRequest request);

    ComplaintResponse toResponse(Complaint complaint);
}