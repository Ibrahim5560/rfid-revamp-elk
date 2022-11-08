package com.isoft.rfid.service;

import com.isoft.rfid.service.dto.TasksDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.isoft.rfid.domain.Tasks}.
 */
public interface TasksService {
    /**
     * Save a tasks.
     *
     * @param tasksDTO the entity to save.
     * @return the persisted entity.
     */
    TasksDTO save(TasksDTO tasksDTO);

    /**
     * Updates a tasks.
     *
     * @param tasksDTO the entity to update.
     * @return the persisted entity.
     */
    TasksDTO update(TasksDTO tasksDTO);

    /**
     * Partially updates a tasks.
     *
     * @param tasksDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TasksDTO> partialUpdate(TasksDTO tasksDTO);

    /**
     * Get all the tasks.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TasksDTO> findAll(Pageable pageable);

    /**
     * Get the "id" tasks.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TasksDTO> findOne(Long id);

    /**
     * Delete the "id" tasks.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the tasks corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TasksDTO> search(String query, Pageable pageable);
}
