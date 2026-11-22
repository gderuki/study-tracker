package lv.gderuki.studytracker.mapper;

import lv.gderuki.studytracker.dto.StudySessionResponseDto;
import lv.gderuki.studytracker.dto.StudyTopicResponseDto;
import lv.gderuki.studytracker.entity.StudySession;
import lv.gderuki.studytracker.entity.StudyTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudyTrackerMapper {

    StudyTopicResponseDto mapStudyTopicEntityToDto(StudyTopic entity);

    @Mapping(target = "studyTopic", source = "topic")
    StudySessionResponseDto mapStudySessionEntityToDto(StudySession entity);
}
