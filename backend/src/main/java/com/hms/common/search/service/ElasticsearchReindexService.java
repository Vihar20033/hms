package com.hms.common.search.service;

/**
 * Service interface for managing Elasticsearch reindexing operations.
 * Handles reindexing of all searchable entities from database to Elasticsearch.
 */
public interface ElasticsearchReindexService {

    /**
     * Reindex all patients from database to Elasticsearch.
     * Performs batch reindexing for better performance.
     * 
     * @return Number of patients reindexed
     */
    long reindexPatients();

    /**
     * Reindex all doctors from database to Elasticsearch.
     * 
     * @return Number of doctors reindexed
     */
    long reindexDoctors();

    /**
     * Reindex all appointments from database to Elasticsearch.
     * 
     * @return Number of appointments reindexed
     */
    long reindexAppointments();

    /**
     * Reindex all prescriptions from database to Elasticsearch.
     * 
     * @return Number of prescriptions reindexed
     */
    long reindexPrescriptions();

    /**
     * Perform a full reindex of all entities.
     * This is typically called during system maintenance or upgrades.
     * 
     * @return ReindexStatus with summary of reindexing results
     */
    ReindexStatus fullReindex();

    /**
     * Clear all Elasticsearch indices.
     * Should be used with caution as it deletes all search data.
     */
    void clearAllIndices();

    /**
     * Get the current status of Elasticsearch connectivity.
     * 
     * @return true if Elasticsearch is available, false otherwise
     */
    boolean isElasticsearchAvailable();
}
