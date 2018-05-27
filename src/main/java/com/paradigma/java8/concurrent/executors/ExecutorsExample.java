package com.paradigma.java8.concurrent.executors;

import static java.time.Duration.ofSeconds;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class ExecutorsExample {

  private static void startJobsInExecutor(int numberOfJobs, ExecutorService executor) {

    CompletableFuture<Void>[] jobsSingle = startJobs(numberOfJobs, executor);
    CompletableFuture.allOf(jobsSingle)
                     .toCompletableFuture()
                     .join();

    executor.shutdown();
  }

  private static CompletableFuture<Void>[] startJobs(int numberOfJobs, Executor singleExecutor) {
    return IntStream.range(0, numberOfJobs)
                    .mapToObj(ExecutorsExample::buildJob)
                    .map(job -> CompletableFuture.runAsync(job, singleExecutor))
                    .toArray((IntFunction<CompletableFuture<Void>[]>) CompletableFuture[]::new);
  }

  public static Runnable buildJob(int instance) {
    int seconds = ThreadLocalRandom.current().nextInt(0, 10);

    return () -> {
      String threadName = Thread.currentThread().getName();

      System.out.println("[" + instance + "] Starting job: This is thread " + threadName+ ".");
      System.out.println("[" + instance + "] Waiting for " + seconds + " seconds.");

      wait(ofSeconds(seconds));

      System.out.println("[" + instance + "] End of job.");
    };
  }

  public static void wait(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String [] args) {

    int numberOfJobs = 5;

    System.out.println("Execution of " + numberOfJobs + " jobs with a single threading executor.");
    startJobsInExecutor(numberOfJobs, Executors.newSingleThreadExecutor());

    System.out.println("Execution of " + numberOfJobs + " jobs with a multi threading executor.");
    wait(ofSeconds(1L));
    startJobsInExecutor(numberOfJobs, Executors.newFixedThreadPool(numberOfJobs));
  }
}
