package lv.gderuki.studytracker.mapper;

import lv.gderuki.studytracker.dto.StudyTopicDto;
import lv.gderuki.studytracker.entity.StudyTopic;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudyTopicMapper {

    StudyTopicDto mapEntityToDto(StudyTopic entity);
}
