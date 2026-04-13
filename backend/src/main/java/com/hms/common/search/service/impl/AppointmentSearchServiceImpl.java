package com.hms.common.search.service.impl;

import com.hms.common.search.document.AppointmentDocument;
import com.hms.common.search.dto.SearchResultDTO;
import com.hms.common.search.repository.AppointmentSearchRepository;
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
 * Service implementation for Appointment search operations.
 * Provides fuzzy search for appointments by patient/doctor name and details.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentSearchServiceImpl implements SearchService<AppointmentDocument> {

    private final AppointmentSearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(AppointmentDocument document) {
        try {
            repository.save(document);
            log.debug("Indexed appointment document with id: {}", document.getId());
        } catch (Exception e) {
            log.error("Error indexing appointment document with id: {}", document.getId(), e);
            throw new RuntimeException("Failed to index appointment document", e);
        }
    }

    @Override
    public void indexBulk(Iterable<AppointmentDocument> documents) {
        try {
            repository.saveAll(documents);
            log.debug("Bulk indexed appointment documents");
        } catch (Exception e) {
            log.error("Error bulk indexing appointment documents", e);
            throw new RuntimeException("Failed to bulk index appointment documents", e);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            log.debug("Deleted appointment document with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting appointment document with id: {}", id, e);
            throw new RuntimeException("Failed to delete appointment document", e);
        }
    }

    @Override
    public SearchResultDTO<AppointmentDocument> fuzzySearch(String query, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields("patientNameSearchable^2", "doctorNameSearchable^2", "reason", "department", "notes")
                    .query(query)
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<AppointmentDocument> results = repository.search(searchQuery, pageable);

            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing fuzzy search for appointments with query: {}", query, e);
            throw new RuntimeException("Fuzzy search failed", e);
        }
    }

    @Override
    public SearchResultDTO<AppointmentDocument> exactSearch(String field, String value, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Query searchQuery = new QueryBuilder(QueryBuilders.term()
                    .field(field)
                    .value(value)
                    ._toQuery()).build();
            
            Page<AppointmentDocument> results = repository.search(searchQuery, pageable);
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing exact search for appointments with field: {}, value: {}", field, value, e);
            throw new RuntimeException("Exact search failed", e);
        }
    }

    @Override
    public SearchResultDTO<AppointmentDocument> multiFieldSearch(String query, String[] fields, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields(fields)
                    .query(query)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<AppointmentDocument> results = repository.search(searchQuery, pageable);
            
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing multi-field search for appointments", e);
            throw new RuntimeException("Multi-field search failed", e);
        }
    }

    @Override
    public void clearIndex() {
        try {
            repository.deleteAll();
            log.info("Cleared all appointment documents from index");
        } catch (Exception e) {
            log.error("Error clearing appointment index", e);
            throw new RuntimeException("Failed to clear appointment index", e);
        }
    }

    private SearchResultDTO<AppointmentDocument> mapToSearchResult(Page<AppointmentDocument> page, int currentPage, int pageSize) {
        return SearchResultDTO.<AppointmentDocument>builder()
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
