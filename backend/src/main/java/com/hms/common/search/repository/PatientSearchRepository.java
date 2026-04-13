package com.hms.common.search.repository;

import com.hms.common.search.document.PatientDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch repository for PatientDocument.
 * Provides search and CRUD operations for patient documents.
 */
@Repository
public interface PatientSearchRepository extends ElasticsearchRepository<PatientDocument, Long> {
}
