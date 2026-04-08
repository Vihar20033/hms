package com.hms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends JpaRepository<T, ID> {
    // Hibernate @SoftDelete handles delete() → UPDATE deleted=true automatically.
    // Each concrete repository should add its own restore() native query if required.
}
