package com.documind.backend.repository;

import com.documind.backend.entity.SourceReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SourceReferenceRepository extends JpaRepository<SourceReference, Long> {

    // charge les sources de plusieurs messages en une seule requête
    List<SourceReference> findByMessageIdIn(Collection<Long> messageIds);
}