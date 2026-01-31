package com.digitalSansad.sow.dto;

import java.util.UUID;

public class SowSubmitResponse {
  public UUID id;
  public String status;
  public String currentOwnerRole;

  public SowSubmitResponse(UUID id, String status, String currentOwnerRole) {
    this.id = id;
    this.status = status;
    this.currentOwnerRole = currentOwnerRole;
  }
}
