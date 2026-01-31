package com.digitalSansad.sow.dto;

import java.util.UUID;

public class SowReturnResponse {
  public UUID id;
  public String status;
  public String currentOwnerRole;

  public SowReturnResponse(UUID id, String status, String currentOwnerRole) {
    this.id = id;
    this.status = status;
    this.currentOwnerRole = currentOwnerRole;
  }
}
