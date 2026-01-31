package com.digitalSansad.sow.dto;

import java.time.LocalDate;
import java.util.UUID;

public class SowSearchRequest {
  public String refNo;
  public String status;
  public String pendingAt; // currentOwnerRole
  public UUID createdBy;
  public LocalDate createdOn;
}