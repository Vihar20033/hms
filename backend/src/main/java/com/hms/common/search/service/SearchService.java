package com.hms.common.search.service;

import com.hms.common.search.dto.SearchResultDTO;

/**
 * Generic interface for search operations.
 * Provides common search methods for all entity types.
 */
public interface SearchService<T> {

    /**
     * Index or re-index a single document.
     */
    void index(T document);

    /**
     * Bulk index documents.
     */
    void indexBulk(Iterable<T> documents);

    /**
     * Delete an indexed document.
     */
    void delete(Long id);

    /**
     * Perform fuzzy search with pagination.
     * Supports typo tolerance and phonetic matching.
     */
    SearchResultDTO<T> fuzzySearch(String query, int page, int size);

    /**
     * Perform exact search with pagination.
     */
    SearchResultDTO<T> exactSearch(String field, String value, int page, int size);

    /**
     * Perform multi-field search.
     */
    SearchResultDTO<T> multiFieldSearch(String query, String[] fields, int page, int size);

    /**
     * Clear all documents from the index.
     */
    void clearIndex();
}
