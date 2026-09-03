package com.banktransfer.feedback_service.mapper;

import com.banktransfer.feedback_service.dto.FeedbackRequest;
import com.banktransfer.feedback_service.dto.FeedbackResponse;
import com.banktransfer.feedback_service.model.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Feedback toEntity(FeedbackRequest request);

    FeedbackResponse toResponse(Feedback feedback);
}