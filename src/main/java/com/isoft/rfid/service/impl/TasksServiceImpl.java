package com.isoft.rfid.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.isoft.rfid.domain.Tasks;
import com.isoft.rfid.repository.TasksRepository;
import com.isoft.rfid.repository.search.TasksSearchRepository;
import com.isoft.rfid.service.TasksService;
import com.isoft.rfid.service.dto.TasksDTO;
import com.isoft.rfid.service.mapper.TasksMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Tasks}.
 */
@Service
@Transactional
public class TasksServiceImpl implements TasksService {

    private final Logger log = LoggerFactory.getLogger(TasksServiceImpl.class);

    private final TasksRepository tasksRepository;

    private final TasksMapper tasksMapper;

    private final TasksSearchRepository tasksSearchRepository;

    public TasksServiceImpl(TasksRepository tasksRepository, TasksMapper tasksMapper, TasksSearchRepository tasksSearchRepository) {
        this.tasksRepository = tasksRepository;
        this.tasksMapper = tasksMapper;
        this.tasksSearchRepository = tasksSearchRepository;
    }

    @Override
    public TasksDTO save(TasksDTO tasksDTO) {
        log.debug("Request to save Tasks : {}", tasksDTO);
        Tasks tasks = tasksMapper.toEntity(tasksDTO);
        tasks = tasksRepository.save(tasks);
        TasksDTO result = tasksMapper.toDto(tasks);
        tasksSearchRepository.index(tasks);
        return result;
    }

    @Override
    public TasksDTO update(TasksDTO tasksDTO) {
        log.debug("Request to update Tasks : {}", tasksDTO);
        Tasks tasks = tasksMapper.toEntity(tasksDTO);
        tasks = tasksRepository.save(tasks);
        TasksDTO result = tasksMapper.toDto(tasks);
        tasksSearchRepository.index(tasks);
        return result;
    }

    @Override
    public Optional<TasksDTO> partialUpdate(TasksDTO tasksDTO) {
        log.debug("Request to partially update Tasks : {}", tasksDTO);

        return tasksRepository
            .findById(tasksDTO.getId())
            .map(existingTasks -> {
                tasksMapper.partialUpdate(existingTasks, tasksDTO);

                return existingTasks;
            })
            .map(tasksRepository::save)
            .map(savedTasks -> {
                tasksSearchRepository.save(savedTasks);

                return savedTasks;
            })
            .map(tasksMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TasksDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Tasks");
        return tasksRepository.findAll(pageable).map(tasksMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TasksDTO> findOne(Long id) {
        log.debug("Request to get Tasks : {}", id);
        return tasksRepository.findById(id).map(tasksMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Tasks : {}", id);
        tasksRepository.deleteById(id);
        tasksSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TasksDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Tasks for query {}", query);
        return tasksSearchRepository.search(query, pageable).map(tasksMapper::toDto);
    }
}
