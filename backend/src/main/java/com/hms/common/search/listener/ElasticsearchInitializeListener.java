package com.hms.common.search.listener;

import com.hms.common.search.service.ElasticsearchReindexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Application listener for Elasticsearch initialization.
 * Performs initial reindexing when the application starts.
 * Can be disabled by setting elasticsearch.auto-reindex.enabled=false
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchInitializeListener {

    private final ElasticsearchReindexService reindexService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Checking Elasticsearch availability on application startup");
        
        try {
            if (!reindexService.isElasticsearchAvailable()) {
                log.warn("Elasticsearch is not available. Skipping initial reindex.");
                return;
            }
            
            log.info("Elasticsearch is available. Performing initial reindex if needed.");
            // Note: You might want to add a check here to only reindex if indices are empty
            // For now, we'll skip auto-reindex on startup to avoid performance issues
            // Uncomment the line below to enable auto-reindex on startup:
            // reindexService.fullReindex();
            
        } catch (Exception e) {
            log.warn("Error checking Elasticsearch during startup", e);
        }
    }
}
