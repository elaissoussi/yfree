ExecutorService executorService = Executors.newFixedThreadPool(numberOfparallelTasks);

StopWatch globalWatch = new StopWatch();
globalWatch.start();  

Map<String, Future<TaskResult>> futures = new HashMap();
  ((Collection)tasks).forEach((task) -> {
      Future<TaskResult> future = executorService.submit(() -> {
          return taskService.execute(task);
      });
      futures.put(task.getId(), future);
  });
  

Map<String, TaskResult> results = new HashMap();
  futures.forEach((taskId, TaskResult) -> {
      try {
          TaskResult result = (TaskResult)resultAndDurationFuture.get();
          results.put(taskId, result);
      } catch (Exception var4) {
         ///
      }

  });

globalWatch.stop();
