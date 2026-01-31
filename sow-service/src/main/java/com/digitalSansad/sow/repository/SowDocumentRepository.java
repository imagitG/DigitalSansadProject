package com.digitalSansad.sow.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.digitalSansad.sow.entity.SowDocument;

public interface SowDocumentRepository
    extends JpaRepository<SowDocument, UUID> {

  List<SowDocument> findByStatus(String status);
}
