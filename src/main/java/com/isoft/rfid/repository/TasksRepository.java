package com.isoft.rfid.repository;

import com.isoft.rfid.domain.Tasks;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Tasks entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TasksRepository extends JpaRepository<Tasks, Long>, JpaSpecificationExecutor<Tasks> {}
