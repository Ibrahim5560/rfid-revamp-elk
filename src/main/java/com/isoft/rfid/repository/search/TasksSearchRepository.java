package com.isoft.rfid.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.isoft.rfid.domain.Tasks;
import com.isoft.rfid.repository.TasksRepository;
import java.util.List;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data Elasticsearch repository for the {@link Tasks} entity.
 */
public interface TasksSearchRepository extends ElasticsearchRepository<Tasks, Long>, TasksSearchRepositoryInternal {}

interface TasksSearchRepositoryInternal {
    Page<Tasks> search(String query, Pageable pageable);

    Page<Tasks> search(Query query);

    void index(Tasks entity);
}

class TasksSearchRepositoryInternalImpl implements TasksSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final TasksRepository repository;

    TasksSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, TasksRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Tasks> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery.setPageable(pageable));
    }

    @Override
    public Page<Tasks> search(Query query) {
        SearchHits<Tasks> searchHits = elasticsearchTemplate.search(query, Tasks.class);
        List<Tasks> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Tasks entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
