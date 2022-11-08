package com.isoft.rfid.service;

import com.isoft.rfid.domain.*; // for static metamodels
import com.isoft.rfid.domain.Tasks;
import com.isoft.rfid.repository.TasksRepository;
import com.isoft.rfid.repository.search.TasksSearchRepository;
import com.isoft.rfid.service.criteria.TasksCriteria;
import com.isoft.rfid.service.dto.TasksDTO;
import com.isoft.rfid.service.mapper.TasksMapper;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Tasks} entities in the database.
 * The main input is a {@link TasksCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link TasksDTO} or a {@link Page} of {@link TasksDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TasksQueryService extends QueryService<Tasks> {

    private final Logger log = LoggerFactory.getLogger(TasksQueryService.class);

    private final TasksRepository tasksRepository;

    private final TasksMapper tasksMapper;

    private final TasksSearchRepository tasksSearchRepository;

    public TasksQueryService(TasksRepository tasksRepository, TasksMapper tasksMapper, TasksSearchRepository tasksSearchRepository) {
        this.tasksRepository = tasksRepository;
        this.tasksMapper = tasksMapper;
        this.tasksSearchRepository = tasksSearchRepository;
    }

    /**
     * Return a {@link List} of {@link TasksDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<TasksDTO> findByCriteria(TasksCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Tasks> specification = createSpecification(criteria);
        return tasksMapper.toDto(tasksRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link TasksDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TasksDTO> findByCriteria(TasksCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Tasks> specification = createSpecification(criteria);
        return tasksRepository.findAll(specification, page).map(tasksMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TasksCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Tasks> specification = createSpecification(criteria);
        return tasksRepository.count(specification);
    }

    /**
     * Function to convert {@link TasksCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Tasks> createSpecification(TasksCriteria criteria) {
        Specification<Tasks> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Tasks_.id));
            }
            if (criteria.getNameEn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getNameEn(), Tasks_.nameEn));
            }
            if (criteria.getNameAr() != null) {
                specification = specification.and(buildStringSpecification(criteria.getNameAr(), Tasks_.nameAr));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getStatus(), Tasks_.status));
            }
            if (criteria.getCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCode(), Tasks_.code));
            }
        }
        return specification;
    }
}
