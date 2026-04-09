package com.hms.common.audit;

import com.hms.common.audit.dto.EntityRevisionDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntityHistoryService {

    @PersistenceContext
    private final EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <T> List<EntityRevisionDTO<T>> getHistory(Class<T> entityClass, Long id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        List<?> results = auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();

        return results.stream().map(obj -> {
            Object[] row = (Object[]) obj;
            T entity = (T) row[0];
            org.hibernate.envers.DefaultRevisionEntity revisionEntity = (org.hibernate.envers.DefaultRevisionEntity) row[1];
            RevisionType revisionType = (RevisionType) row[2];

            return EntityRevisionDTO.<T>builder()
                    .revisionId(revisionEntity.getId())
                    .revisionDate(Instant.ofEpochMilli(revisionEntity.getTimestamp()))
                    .revisionType(revisionType.name())
                    .entity(entity)
                    .build();
        }).collect(Collectors.toList());
    }
}
