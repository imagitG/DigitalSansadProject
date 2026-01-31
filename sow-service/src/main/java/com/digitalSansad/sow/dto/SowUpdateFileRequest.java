package com.digitalSansad.sow.dto;

import java.util.UUID;

public class SowUpdateFileRequest {
  public UUID sowId;

  public SowUpdateFileRequest() {
  }

  public SowUpdateFileRequest(UUID sowId) {
    this.sowId = sowId;
  }
}
