package com.digitalSansad.auth.service;

import com.digitalSansad.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TestService {

  private final UserRepository userRepository;

  public TestService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public long countUsers() {
    return userRepository.count();
  }
}
