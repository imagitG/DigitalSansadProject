package com.digitalSansad.auth.controller;

import com.digitalSansad.auth.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  private final TestService testService;

  public TestController(TestService testService) {
    this.testService = testService;
  }

  @GetMapping("/test/users/count")
  public long countUsers() {
    return testService.countUsers();
  }
}
