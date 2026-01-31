package com.digitalSansad.sow.dto;

import java.util.UUID;

public class SowUpdateFileResponse {
  public UUID id;
  public String fileName;
  public String filePath;

  public SowUpdateFileResponse(UUID id, String fileName, String filePath) {
    this.id = id;
    this.fileName = fileName;
    this.filePath = filePath;
  }
}
