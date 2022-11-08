package com.isoft.rfid.service.mapper;

import com.isoft.rfid.domain.Tasks;
import com.isoft.rfid.service.dto.TasksDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Tasks} and its DTO {@link TasksDTO}.
 */
@Mapper(componentModel = "spring")
public interface TasksMapper extends EntityMapper<TasksDTO, Tasks> {}
