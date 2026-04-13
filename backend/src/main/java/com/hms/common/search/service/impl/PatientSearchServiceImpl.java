package com.hms.common.search.service.impl;

import com.hms.common.search.document.PatientDocument;
import com.hms.common.search.dto.SearchResultDTO;
import com.hms.common.search.repository.PatientSearchRepository;
import com.hms.common.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elrepo.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.client.elrepo.ElasticsearchRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.InfoResponse;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.QueryBuilder;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

/**
 * Service implementation for Patient search operations.
 * Provides fuzzy, phonetic, and exact search capabilities for patients.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientSearchServiceImpl implements SearchService<PatientDocument> {

    private final PatientSearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(PatientDocument document) {
        try {
            repository.save(document);
            log.debug("Indexed patient document with id: {}", document.getId());
        } catch (Exception e) {
            log.error("Error indexing patient document with id: {}", document.getId(), e);
            throw new RuntimeException("Failed to index patient document", e);
        }
    }

    @Override
    public void indexBulk(Iterable<PatientDocument> documents) {
        try {
            repository.saveAll(documents);
            log.debug("Bulk indexed patient documents");
        } catch (Exception e) {
            log.error("Error bulk indexing patient documents", e);
            throw new RuntimeException("Failed to bulk index patient documents", e);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            log.debug("Deleted patient document with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting patient document with id: {}", id, e);
            throw new RuntimeException("Failed to delete patient document", e);
        }
    }

    @Override
    public SearchResultDTO<PatientDocument> fuzzySearch(String query, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Multi-field fuzzy search using BEST_FIELDS strategy
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields("nameSearchable^2", "contactNumberSearchable", "emailSearchable", "address")
                    .query(query)
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<PatientDocument> results = repository.search(searchQuery, pageable);

            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing fuzzy search for patients with query: {}", query, e);
            throw new RuntimeException("Fuzzy search failed", e);
        }
    }

    @Override
    public SearchResultDTO<PatientDocument> exactSearch(String field, String value, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Query searchQuery = new QueryBuilder(QueryBuilders.term()
                    .field(field)
                    .value(value)
                    ._toQuery()).build();
            
            Page<PatientDocument> results = repository.search(searchQuery, pageable);
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing exact search for patients with field: {}, value: {}", field, value, e);
            throw new RuntimeException("Exact search failed", e);
        }
    }

    @Override
    public SearchResultDTO<PatientDocument> multiFieldSearch(String query, String[] fields, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields(fields)
                    .query(query)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<PatientDocument> results = repository.search(searchQuery, pageable);
            
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing multi-field search for patients", e);
            throw new RuntimeException("Multi-field search failed", e);
        }
    }

    @Override
    public void clearIndex() {
        try {
            repository.deleteAll();
            log.info("Cleared all patient documents from index");
        } catch (Exception e) {
            log.error("Error clearing patient index", e);
            throw new RuntimeException("Failed to clear patient index", e);
        }
    }

    /**
     * Maps Elasticsearch page results to SearchResultDTO.
     */
    private SearchResultDTO<PatientDocument> mapToSearchResult(Page<PatientDocument> page, int currentPage, int pageSize) {
        return SearchResultDTO.<PatientDocument>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(currentPage)
                .pageSize(pageSize)
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
