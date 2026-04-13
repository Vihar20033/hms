package com.hms.common.search.service.impl;

import com.hms.common.search.document.PrescriptionDocument;
import com.hms.common.search.dto.SearchResultDTO;
import com.hms.common.search.repository.PrescriptionSearchRepository;
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
 * Service implementation for Prescription search operations.
 * Provides search for prescriptions by patient, doctor, diagnosis, and medicines.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionSearchServiceImpl implements SearchService<PrescriptionDocument> {

    private final PrescriptionSearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(PrescriptionDocument document) {
        try {
            repository.save(document);
            log.debug("Indexed prescription document with id: {}", document.getId());
        } catch (Exception e) {
            log.error("Error indexing prescription document with id: {}", document.getId(), e);
            throw new RuntimeException("Failed to index prescription document", e);
        }
    }

    @Override
    public void indexBulk(Iterable<PrescriptionDocument> documents) {
        try {
            repository.saveAll(documents);
            log.debug("Bulk indexed prescription documents");
        } catch (Exception e) {
            log.error("Error bulk indexing prescription documents", e);
            throw new RuntimeException("Failed to bulk index prescription documents", e);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            log.debug("Deleted prescription document with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting prescription document with id: {}", id, e);
            throw new RuntimeException("Failed to delete prescription document", e);
        }
    }

    @Override
    public SearchResultDTO<PrescriptionDocument> fuzzySearch(String query, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields("patientNameSearchable^2", "doctorNameSearchable^2", "diagnosis", "symptoms", "medicines", "advice")
                    .query(query)
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<PrescriptionDocument> results = repository.search(searchQuery, pageable);

            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing fuzzy search for prescriptions with query: {}", query, e);
            throw new RuntimeException("Fuzzy search failed", e);
        }
    }

    @Override
    public SearchResultDTO<PrescriptionDocument> exactSearch(String field, String value, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Query searchQuery = new QueryBuilder(QueryBuilders.term()
                    .field(field)
                    .value(value)
                    ._toQuery()).build();
            
            Page<PrescriptionDocument> results = repository.search(searchQuery, pageable);
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing exact search for prescriptions with field: {}, value: {}", field, value, e);
            throw new RuntimeException("Exact search failed", e);
        }
    }

    @Override
    public SearchResultDTO<PrescriptionDocument> multiFieldSearch(String query, String[] fields, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            MultiMatchQuery multiMatchQuery = QueryBuilders.multiMatch()
                    .fields(fields)
                    .query(query)
                    .fuzziness("AUTO")
                    .build();

            Query searchQuery = new QueryBuilder(multiMatchQuery._toQuery()).build();
            Page<PrescriptionDocument> results = repository.search(searchQuery, pageable);
            
            return mapToSearchResult(results, page, size);
        } catch (Exception e) {
            log.error("Error performing multi-field search for prescriptions", e);
            throw new RuntimeException("Multi-field search failed", e);
        }
    }

    @Override
    public void clearIndex() {
        try {
            repository.deleteAll();
            log.info("Cleared all prescription documents from index");
        } catch (Exception e) {
            log.error("Error clearing prescription index", e);
            throw new RuntimeException("Failed to clear prescription index", e);
        }
    }

    private SearchResultDTO<PrescriptionDocument> mapToSearchResult(Page<PrescriptionDocument> page, int currentPage, int pageSize) {
        return SearchResultDTO.<PrescriptionDocument>builder()
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
