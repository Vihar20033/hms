package com.hms.common.search.service.impl;

import com.hms.common.search.document.DoctorDocument;
import com.hms.common.search.dto.SearchResultDTO;
import com.hms.common.search.repository.DoctorSearchRepository;
import com.hms.common.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.QueryBuilder;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

/**
 * Service implementation for Doctor search operations.
 * Provides fuzzy, phonetic, and exact search capabilities for doctors.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorSearchServiceImpl implements SearchService<DoctorDocument> {

    private final DoctorSearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(DoctorDocument document) {
        try {
            repository.save(document);
            log.debug("Indexed doctor document with id: {}", document.getId());
        } catch (Exception e) {
            log.error("Error indexing doctor document with id: {}", document.getId(), e);
            throw new RuntimeException("Failed to index doctor document", e);
        }
    }

    @Override
    public void indexBulk(Iterable<DoctorDocument> documents) {
        try {
            repository.saveAll(documents);
            log.debug("Bulk indexed doctor documents");
        } catch (Exception e) {
            log.error("Error bulk indexing doctor documents", e);
            throw new RuntimeException("Failed to bulk index doctor documents", e);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            log.debug("Deleted doctor document with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting doctor document with id: {}", id, e);
            throw new RuntimeException("Failed to delete doctor document", e);
        }
    }

    @Override
    public SearchResultDTO<DoctorDocument> fuzzySearch(String query, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields("firstNameSearchable^2", "lastNameSearchable^2", "fullNameSearchable^2", 
                           "specialization", "department", "qualification", "emailSearchable", "phoneNumberSearchable")
                    .query(query)
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<DoctorDocument> results = repository.search(searchQuery, pageable);

            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing fuzzy search for doctors with query: {}", query, e);
            throw new RuntimeException("Fuzzy search failed", e);
        }
    }

    @Override
    public SearchResultDTO<DoctorDocument> exactSearch(String field, String value, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Query searchQuery = new QueryBuilder(QueryBuilders.term()
                    .field(field)
                    .value(value)
                    ._toQuery()).build();
            
            Page<DoctorDocument> results = repository.search(searchQuery, pageable);
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing exact search for doctors with field: {}, value: {}", field, value, e);
            throw new RuntimeException("Exact search failed", e);
        }
    }

    @Override
    public SearchResultDTO<DoctorDocument> multiFieldSearch(String query, String[] fields, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields(fields)
                    .query(query)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<DoctorDocument> results = repository.search(searchQuery, pageable);
            
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing multi-field search for doctors", e);
            throw new RuntimeException("Multi-field search failed", e);
        }
    }

    @Override
    public void clearIndex() {
        try {
            repository.deleteAll();
            log.info("Cleared all doctor documents from index");
        } catch (Exception e) {
            log.error("Error clearing doctor index", e);
            throw new RuntimeException("Failed to clear doctor index", e);
        }
    }

    private SearchResultDTO<DoctorDocument> mapToSearchResult(Page<DoctorDocument> page, int currentPage, int pageSize) {
        return SearchResultDTO.<DoctorDocument>builder()
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
