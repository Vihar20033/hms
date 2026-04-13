package com.hms.common.search.repository;

import com.hms.common.search.document.AppointmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch repository for AppointmentDocument.
 * Provides search and CRUD operations for appointment documents.
 */
@Repository
public interface AppointmentSearchRepository extends ElasticsearchRepository<AppointmentDocument, Long> {
}
