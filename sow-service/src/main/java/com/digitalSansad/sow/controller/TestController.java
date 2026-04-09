package com.digitalSansad.sow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  // ✅ ADD THIS (Render health check)
  @GetMapping("/")
  public String home() {
    return "SoW Service Running";
  }

  // Optional health endpoint
  @GetMapping("/health")
  public String health() {
    return "OK";
  }
}
