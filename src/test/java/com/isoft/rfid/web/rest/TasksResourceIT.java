package com.isoft.rfid.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.isoft.rfid.IntegrationTest;
import com.isoft.rfid.domain.Tasks;
import com.isoft.rfid.repository.TasksRepository;
import com.isoft.rfid.repository.search.TasksSearchRepository;
import com.isoft.rfid.service.criteria.TasksCriteria;
import com.isoft.rfid.service.dto.TasksDTO;
import com.isoft.rfid.service.mapper.TasksMapper;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TasksResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TasksResourceIT {

    private static final String DEFAULT_NAME_EN = "AAAAAAAAAA";
    private static final String UPDATED_NAME_EN = "BBBBBBBBBB";

    private static final String DEFAULT_NAME_AR = "AAAAAAAAAA";
    private static final String UPDATED_NAME_AR = "BBBBBBBBBB";

    private static final Integer DEFAULT_STATUS = 1;
    private static final Integer UPDATED_STATUS = 2;
    private static final Integer SMALLER_STATUS = 1 - 1;

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tasks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/tasks";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private TasksMapper tasksMapper;

    @Autowired
    private TasksSearchRepository tasksSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTasksMockMvc;

    private Tasks tasks;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tasks createEntity(EntityManager em) {
        Tasks tasks = new Tasks().nameEn(DEFAULT_NAME_EN).nameAr(DEFAULT_NAME_AR).status(DEFAULT_STATUS).code(DEFAULT_CODE);
        return tasks;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Tasks createUpdatedEntity(EntityManager em) {
        Tasks tasks = new Tasks().nameEn(UPDATED_NAME_EN).nameAr(UPDATED_NAME_AR).status(UPDATED_STATUS).code(UPDATED_CODE);
        return tasks;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        tasksSearchRepository.deleteAll();
        assertThat(tasksSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        tasks = createEntity(em);
    }

    @Test
    @Transactional
    void createTasks() throws Exception {
        int databaseSizeBeforeCreate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        // Create the Tasks
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);
        restTasksMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tasksDTO)))
            .andExpect(status().isCreated());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Tasks testTasks = tasksList.get(tasksList.size() - 1);
        assertThat(testTasks.getNameEn()).isEqualTo(DEFAULT_NAME_EN);
        assertThat(testTasks.getNameAr()).isEqualTo(DEFAULT_NAME_AR);
        assertThat(testTasks.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testTasks.getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    @Transactional
    void createTasksWithExistingId() throws Exception {
        // Create the Tasks with an existing ID
        tasks.setId(1L);
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        int databaseSizeBeforeCreate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restTasksMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tasksDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameEnIsRequired() throws Exception {
        int databaseSizeBeforeTest = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        // set the field null
        tasks.setNameEn(null);

        // Create the Tasks, which fails.
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        restTasksMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tasksDTO)))
            .andExpect(status().isBadRequest());

        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameArIsRequired() throws Exception {
        int databaseSizeBeforeTest = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        // set the field null
        tasks.setNameAr(null);

        // Create the Tasks, which fails.
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        restTasksMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tasksDTO)))
            .andExpect(status().isBadRequest());

        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllTasks() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList
        restTasksMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tasks.getId().intValue())))
            .andExpect(jsonPath("$.[*].nameEn").value(hasItem(DEFAULT_NAME_EN)))
            .andExpect(jsonPath("$.[*].nameAr").value(hasItem(DEFAULT_NAME_AR)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)));
    }

    @Test
    @Transactional
    void getTasks() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get the tasks
        restTasksMockMvc
            .perform(get(ENTITY_API_URL_ID, tasks.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tasks.getId().intValue()))
            .andExpect(jsonPath("$.nameEn").value(DEFAULT_NAME_EN))
            .andExpect(jsonPath("$.nameAr").value(DEFAULT_NAME_AR))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE));
    }

    @Test
    @Transactional
    void getTasksByIdFiltering() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        Long id = tasks.getId();

        defaultTasksShouldBeFound("id.equals=" + id);
        defaultTasksShouldNotBeFound("id.notEquals=" + id);

        defaultTasksShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultTasksShouldNotBeFound("id.greaterThan=" + id);

        defaultTasksShouldBeFound("id.lessThanOrEqual=" + id);
        defaultTasksShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTasksByNameEnIsEqualToSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameEn equals to DEFAULT_NAME_EN
        defaultTasksShouldBeFound("nameEn.equals=" + DEFAULT_NAME_EN);

        // Get all the tasksList where nameEn equals to UPDATED_NAME_EN
        defaultTasksShouldNotBeFound("nameEn.equals=" + UPDATED_NAME_EN);
    }

    @Test
    @Transactional
    void getAllTasksByNameEnIsInShouldWork() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameEn in DEFAULT_NAME_EN or UPDATED_NAME_EN
        defaultTasksShouldBeFound("nameEn.in=" + DEFAULT_NAME_EN + "," + UPDATED_NAME_EN);

        // Get all the tasksList where nameEn equals to UPDATED_NAME_EN
        defaultTasksShouldNotBeFound("nameEn.in=" + UPDATED_NAME_EN);
    }

    @Test
    @Transactional
    void getAllTasksByNameEnIsNullOrNotNull() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameEn is not null
        defaultTasksShouldBeFound("nameEn.specified=true");

        // Get all the tasksList where nameEn is null
        defaultTasksShouldNotBeFound("nameEn.specified=false");
    }

    @Test
    @Transactional
    void getAllTasksByNameEnContainsSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameEn contains DEFAULT_NAME_EN
        defaultTasksShouldBeFound("nameEn.contains=" + DEFAULT_NAME_EN);

        // Get all the tasksList where nameEn contains UPDATED_NAME_EN
        defaultTasksShouldNotBeFound("nameEn.contains=" + UPDATED_NAME_EN);
    }

    @Test
    @Transactional
    void getAllTasksByNameEnNotContainsSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameEn does not contain DEFAULT_NAME_EN
        defaultTasksShouldNotBeFound("nameEn.doesNotContain=" + DEFAULT_NAME_EN);

        // Get all the tasksList where nameEn does not contain UPDATED_NAME_EN
        defaultTasksShouldBeFound("nameEn.doesNotContain=" + UPDATED_NAME_EN);
    }

    @Test
    @Transactional
    void getAllTasksByNameArIsEqualToSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameAr equals to DEFAULT_NAME_AR
        defaultTasksShouldBeFound("nameAr.equals=" + DEFAULT_NAME_AR);

        // Get all the tasksList where nameAr equals to UPDATED_NAME_AR
        defaultTasksShouldNotBeFound("nameAr.equals=" + UPDATED_NAME_AR);
    }

    @Test
    @Transactional
    void getAllTasksByNameArIsInShouldWork() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameAr in DEFAULT_NAME_AR or UPDATED_NAME_AR
        defaultTasksShouldBeFound("nameAr.in=" + DEFAULT_NAME_AR + "," + UPDATED_NAME_AR);

        // Get all the tasksList where nameAr equals to UPDATED_NAME_AR
        defaultTasksShouldNotBeFound("nameAr.in=" + UPDATED_NAME_AR);
    }

    @Test
    @Transactional
    void getAllTasksByNameArIsNullOrNotNull() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameAr is not null
        defaultTasksShouldBeFound("nameAr.specified=true");

        // Get all the tasksList where nameAr is null
        defaultTasksShouldNotBeFound("nameAr.specified=false");
    }

    @Test
    @Transactional
    void getAllTasksByNameArContainsSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameAr contains DEFAULT_NAME_AR
        defaultTasksShouldBeFound("nameAr.contains=" + DEFAULT_NAME_AR);

        // Get all the tasksList where nameAr contains UPDATED_NAME_AR
        defaultTasksShouldNotBeFound("nameAr.contains=" + UPDATED_NAME_AR);
    }

    @Test
    @Transactional
    void getAllTasksByNameArNotContainsSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where nameAr does not contain DEFAULT_NAME_AR
        defaultTasksShouldNotBeFound("nameAr.doesNotContain=" + DEFAULT_NAME_AR);

        // Get all the tasksList where nameAr does not contain UPDATED_NAME_AR
        defaultTasksShouldBeFound("nameAr.doesNotContain=" + UPDATED_NAME_AR);
    }

    @Test
    @Transactional
    void getAllTasksByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where status equals to DEFAULT_STATUS
        defaultTasksShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the tasksList where status equals to UPDATED_STATUS
        defaultTasksShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTasksByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultTasksShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the tasksList where status equals to UPDATED_STATUS
        defaultTasksShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTasksByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where status is not null
        defaultTasksShouldBeFound("status.specified=true");

        // Get all the tasksList where status is null
        defaultTasksShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllTasksByStatusIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where status is greater than or equal to DEFAULT_STATUS
        defaultTasksShouldBeFound("status.greaterThanOrEqual=" + DEFAULT_STATUS);

        // Get all the tasksList where status is greater than or equal to UPDATED_STATUS
        defaultTasksShouldNotBeFound("status.greaterThanOrEqual=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTasksByStatusIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where status is less than or equal to DEFAULT_STATUS
        defaultTasksShouldBeFound("status.lessThanOrEqual=" + DEFAULT_STATUS);

        // Get all the tasksList where status is less than or equal to SMALLER_STATUS
        defaultTasksShouldNotBeFound("status.lessThanOrEqual=" + SMALLER_STATUS);
    }

    @Test
    @Transactional
    void getAllTasksByStatusIsLessThanSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where status is less than DEFAULT_STATUS
        defaultTasksShouldNotBeFound("status.lessThan=" + DEFAULT_STATUS);

        // Get all the tasksList where status is less than UPDATED_STATUS
        defaultTasksShouldBeFound("status.lessThan=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTasksByStatusIsGreaterThanSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where status is greater than DEFAULT_STATUS
        defaultTasksShouldNotBeFound("status.greaterThan=" + DEFAULT_STATUS);

        // Get all the tasksList where status is greater than SMALLER_STATUS
        defaultTasksShouldBeFound("status.greaterThan=" + SMALLER_STATUS);
    }

    @Test
    @Transactional
    void getAllTasksByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where code equals to DEFAULT_CODE
        defaultTasksShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the tasksList where code equals to UPDATED_CODE
        defaultTasksShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllTasksByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where code in DEFAULT_CODE or UPDATED_CODE
        defaultTasksShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the tasksList where code equals to UPDATED_CODE
        defaultTasksShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllTasksByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where code is not null
        defaultTasksShouldBeFound("code.specified=true");

        // Get all the tasksList where code is null
        defaultTasksShouldNotBeFound("code.specified=false");
    }

    @Test
    @Transactional
    void getAllTasksByCodeContainsSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where code contains DEFAULT_CODE
        defaultTasksShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the tasksList where code contains UPDATED_CODE
        defaultTasksShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllTasksByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        // Get all the tasksList where code does not contain DEFAULT_CODE
        defaultTasksShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the tasksList where code does not contain UPDATED_CODE
        defaultTasksShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTasksShouldBeFound(String filter) throws Exception {
        restTasksMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tasks.getId().intValue())))
            .andExpect(jsonPath("$.[*].nameEn").value(hasItem(DEFAULT_NAME_EN)))
            .andExpect(jsonPath("$.[*].nameAr").value(hasItem(DEFAULT_NAME_AR)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)));

        // Check, that the count call also returns 1
        restTasksMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTasksShouldNotBeFound(String filter) throws Exception {
        restTasksMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTasksMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTasks() throws Exception {
        // Get the tasks
        restTasksMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTasks() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();
        tasksSearchRepository.save(tasks);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());

        // Update the tasks
        Tasks updatedTasks = tasksRepository.findById(tasks.getId()).get();
        // Disconnect from session so that the updates on updatedTasks are not directly saved in db
        em.detach(updatedTasks);
        updatedTasks.nameEn(UPDATED_NAME_EN).nameAr(UPDATED_NAME_AR).status(UPDATED_STATUS).code(UPDATED_CODE);
        TasksDTO tasksDTO = tasksMapper.toDto(updatedTasks);

        restTasksMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tasksDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(tasksDTO))
            )
            .andExpect(status().isOk());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        Tasks testTasks = tasksList.get(tasksList.size() - 1);
        assertThat(testTasks.getNameEn()).isEqualTo(UPDATED_NAME_EN);
        assertThat(testTasks.getNameAr()).isEqualTo(UPDATED_NAME_AR);
        assertThat(testTasks.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testTasks.getCode()).isEqualTo(UPDATED_CODE);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Tasks> tasksSearchList = IterableUtils.toList(tasksSearchRepository.findAll());
                Tasks testTasksSearch = tasksSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testTasksSearch.getNameEn()).isEqualTo(UPDATED_NAME_EN);
                assertThat(testTasksSearch.getNameAr()).isEqualTo(UPDATED_NAME_AR);
                assertThat(testTasksSearch.getStatus()).isEqualTo(UPDATED_STATUS);
                assertThat(testTasksSearch.getCode()).isEqualTo(UPDATED_CODE);
            });
    }

    @Test
    @Transactional
    void putNonExistingTasks() throws Exception {
        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        tasks.setId(count.incrementAndGet());

        // Create the Tasks
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTasksMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tasksDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(tasksDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchTasks() throws Exception {
        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        tasks.setId(count.incrementAndGet());

        // Create the Tasks
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTasksMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(tasksDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTasks() throws Exception {
        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        tasks.setId(count.incrementAndGet());

        // Create the Tasks
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTasksMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tasksDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateTasksWithPatch() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();

        // Update the tasks using partial update
        Tasks partialUpdatedTasks = new Tasks();
        partialUpdatedTasks.setId(tasks.getId());

        partialUpdatedTasks.nameAr(UPDATED_NAME_AR);

        restTasksMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTasks.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTasks))
            )
            .andExpect(status().isOk());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        Tasks testTasks = tasksList.get(tasksList.size() - 1);
        assertThat(testTasks.getNameEn()).isEqualTo(DEFAULT_NAME_EN);
        assertThat(testTasks.getNameAr()).isEqualTo(UPDATED_NAME_AR);
        assertThat(testTasks.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testTasks.getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    @Transactional
    void fullUpdateTasksWithPatch() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);

        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();

        // Update the tasks using partial update
        Tasks partialUpdatedTasks = new Tasks();
        partialUpdatedTasks.setId(tasks.getId());

        partialUpdatedTasks.nameEn(UPDATED_NAME_EN).nameAr(UPDATED_NAME_AR).status(UPDATED_STATUS).code(UPDATED_CODE);

        restTasksMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTasks.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTasks))
            )
            .andExpect(status().isOk());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        Tasks testTasks = tasksList.get(tasksList.size() - 1);
        assertThat(testTasks.getNameEn()).isEqualTo(UPDATED_NAME_EN);
        assertThat(testTasks.getNameAr()).isEqualTo(UPDATED_NAME_AR);
        assertThat(testTasks.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testTasks.getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    @Transactional
    void patchNonExistingTasks() throws Exception {
        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        tasks.setId(count.incrementAndGet());

        // Create the Tasks
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTasksMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tasksDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(tasksDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTasks() throws Exception {
        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        tasks.setId(count.incrementAndGet());

        // Create the Tasks
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTasksMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(tasksDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTasks() throws Exception {
        int databaseSizeBeforeUpdate = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        tasks.setId(count.incrementAndGet());

        // Create the Tasks
        TasksDTO tasksDTO = tasksMapper.toDto(tasks);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTasksMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(tasksDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Tasks in the database
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteTasks() throws Exception {
        // Initialize the database
        tasksRepository.saveAndFlush(tasks);
        tasksRepository.save(tasks);
        tasksSearchRepository.save(tasks);

        int databaseSizeBeforeDelete = tasksRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the tasks
        restTasksMockMvc
            .perform(delete(ENTITY_API_URL_ID, tasks.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Tasks> tasksList = tasksRepository.findAll();
        assertThat(tasksList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(tasksSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchTasks() throws Exception {
        // Initialize the database
        tasks = tasksRepository.saveAndFlush(tasks);
        tasksSearchRepository.save(tasks);

        // Search the tasks
        restTasksMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + tasks.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tasks.getId().intValue())))
            .andExpect(jsonPath("$.[*].nameEn").value(hasItem(DEFAULT_NAME_EN)))
            .andExpect(jsonPath("$.[*].nameAr").value(hasItem(DEFAULT_NAME_AR)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)));
    }
}
