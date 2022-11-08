package com.isoft.rfid.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TasksMapperTest {

    private TasksMapper tasksMapper;

    @BeforeEach
    public void setUp() {
        tasksMapper = new TasksMapperImpl();
    }
}
