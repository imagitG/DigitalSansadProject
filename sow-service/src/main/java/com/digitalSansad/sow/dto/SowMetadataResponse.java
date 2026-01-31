package com.digitalSansad.sow.dto;

import java.time.Instant;
import java.util.UUID;

public class SowMetadataResponse {
  public UUID id;
  public String title;
  public String status;
  public String refNo;
  public String currentOwnerRole;
  public Instant createdAt;
  public Instant approvedAt;
}
