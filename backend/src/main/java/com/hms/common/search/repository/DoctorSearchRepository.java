package com.hms.common.search.repository;

import com.hms.common.search.document.DoctorDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch repository for DoctorDocument.
 * Provides search and CRUD operations for doctor documents.
 */
@Repository
public interface DoctorSearchRepository extends ElasticsearchRepository<DoctorDocument, Long> {
}
