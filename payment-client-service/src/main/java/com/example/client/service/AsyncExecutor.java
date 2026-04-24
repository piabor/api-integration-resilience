package com.example.client.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncExecutor {

  private static final int MAX_RETRIES = 3;

  @Async
  public void executeAsyncWithRetry(Runnable action) {
    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      action.run();
    }
  }

}
