package com.hms.common.search.repository;

import com.hms.common.search.document.PrescriptionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch repository for PrescriptionDocument.
 * Provides search and CRUD operations for prescription documents.
 */
@Repository
public interface PrescriptionSearchRepository extends ElasticsearchRepository<PrescriptionDocument, Long> {
}
