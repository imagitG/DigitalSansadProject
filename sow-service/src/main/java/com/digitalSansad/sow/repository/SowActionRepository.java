package com.digitalSansad.sow.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.digitalSansad.sow.entity.SowAction;

public interface SowActionRepository
    extends JpaRepository<SowAction, UUID> {
}
