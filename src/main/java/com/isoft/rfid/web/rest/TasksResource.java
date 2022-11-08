package com.isoft.rfid.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.isoft.rfid.repository.TasksRepository;
import com.isoft.rfid.service.TasksQueryService;
import com.isoft.rfid.service.TasksService;
import com.isoft.rfid.service.criteria.TasksCriteria;
import com.isoft.rfid.service.dto.TasksDTO;
import com.isoft.rfid.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.isoft.rfid.domain.Tasks}.
 */
@RestController
@RequestMapping("/api")
public class TasksResource {

    private final Logger log = LoggerFactory.getLogger(TasksResource.class);

    private static final String ENTITY_NAME = "tasks";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TasksService tasksService;

    private final TasksRepository tasksRepository;

    private final TasksQueryService tasksQueryService;

    public TasksResource(TasksService tasksService, TasksRepository tasksRepository, TasksQueryService tasksQueryService) {
        this.tasksService = tasksService;
        this.tasksRepository = tasksRepository;
        this.tasksQueryService = tasksQueryService;
    }

    /**
     * {@code POST  /tasks} : Create a new tasks.
     *
     * @param tasksDTO the tasksDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new tasksDTO, or with status {@code 400 (Bad Request)} if the tasks has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/tasks")
    public ResponseEntity<TasksDTO> createTasks(@Valid @RequestBody TasksDTO tasksDTO) throws URISyntaxException {
        log.debug("REST request to save Tasks : {}", tasksDTO);
        if (tasksDTO.getId() != null) {
            throw new BadRequestAlertException("A new tasks cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TasksDTO result = tasksService.save(tasksDTO);
        return ResponseEntity
            .created(new URI("/api/tasks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /tasks/:id} : Updates an existing tasks.
     *
     * @param id the id of the tasksDTO to save.
     * @param tasksDTO the tasksDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tasksDTO,
     * or with status {@code 400 (Bad Request)} if the tasksDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the tasksDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TasksDTO> updateTasks(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TasksDTO tasksDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Tasks : {}, {}", id, tasksDTO);
        if (tasksDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tasksDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tasksRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        TasksDTO result = tasksService.update(tasksDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, tasksDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /tasks/:id} : Partial updates given fields of an existing tasks, field will ignore if it is null
     *
     * @param id the id of the tasksDTO to save.
     * @param tasksDTO the tasksDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tasksDTO,
     * or with status {@code 400 (Bad Request)} if the tasksDTO is not valid,
     * or with status {@code 404 (Not Found)} if the tasksDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the tasksDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/tasks/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TasksDTO> partialUpdateTasks(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TasksDTO tasksDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Tasks partially : {}, {}", id, tasksDTO);
        if (tasksDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tasksDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tasksRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TasksDTO> result = tasksService.partialUpdate(tasksDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, tasksDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /tasks} : get all the tasks.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of tasks in body.
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<TasksDTO>> getAllTasks(
        TasksCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Tasks by criteria: {}", criteria);
        Page<TasksDTO> page = tasksQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /tasks/count} : count all the tasks.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/tasks/count")
    public ResponseEntity<Long> countTasks(TasksCriteria criteria) {
        log.debug("REST request to count Tasks by criteria: {}", criteria);
        return ResponseEntity.ok().body(tasksQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /tasks/:id} : get the "id" tasks.
     *
     * @param id the id of the tasksDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the tasksDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TasksDTO> getTasks(@PathVariable Long id) {
        log.debug("REST request to get Tasks : {}", id);
        Optional<TasksDTO> tasksDTO = tasksService.findOne(id);
        return ResponseUtil.wrapOrNotFound(tasksDTO);
    }

    /**
     * {@code DELETE  /tasks/:id} : delete the "id" tasks.
     *
     * @param id the id of the tasksDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTasks(@PathVariable Long id) {
        log.debug("REST request to delete Tasks : {}", id);
        tasksService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/tasks?query=:query} : search for the tasks corresponding
     * to the query.
     *
     * @param query the query of the tasks search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/tasks")
    public ResponseEntity<List<TasksDTO>> searchTasks(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of Tasks for query {}", query);
        Page<TasksDTO> page = tasksService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
