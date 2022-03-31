package example.runner;

import example.HelloWorldWorkflow;
import example.HelloWorldWorkflowImpl;
import example.OutputActivityImpl;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.util.Scanner;
import java.util.UUID;


public class ExampleRunner {
  public static final String TASK_QUEUE = "MY_TEST_QUEUE";
  public static final String ACTIVITY_TASK_QUEUE = "MY_ACTIVITY_TEST_QUEUE";

  private static String[] workflowTaskQueues = new String[] {TASK_QUEUE, ACTIVITY_TASK_QUEUE};

  public static void main(String[] args) {
    WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();

    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker wfWorker = factory.newWorker(TASK_QUEUE);
    wfWorker.registerWorkflowImplementationTypes(HelloWorldWorkflowImpl.class);

    Worker activityWorker = factory.newWorker(ACTIVITY_TASK_QUEUE);
    activityWorker.registerActivitiesImplementations(new OutputActivityImpl());

    // Start listening to the Task Queue.
    factory.start();

    // wait for command

    Scanner scanner = new Scanner(System.in);

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();

      if (line.equalsIgnoreCase("submit")) {

        WorkflowOptions options =
            WorkflowOptions.newBuilder()
                .setWorkflowIdReusePolicy(
                    WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE)
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowId("hello-world-" + UUID.randomUUID().toString())
                .build();

        HelloWorldWorkflow workflow = client.newWorkflowStub(HelloWorldWorkflow.class, options);
        WorkflowExecution we = WorkflowClient.start(workflow::sayHello, "your name");
        System.out.println("\nHelloWorld submitted");
      } else if (line.equalsIgnoreCase("suspend")) {
        for (String queue : workflowTaskQueues) {
          Worker foundWorker = factory.getWorker(queue);
          if (foundWorker != null) {
            if (foundWorker.isSuspended()) {
              System.out.println(queue + ": Already suspended");
            } else {
              foundWorker.suspendPolling();
              System.out.println(queue + ": Suspended ");
            }
          }
        }
      } else if (line.equalsIgnoreCase("resume")) {
        for (String queue : workflowTaskQueues) {
          Worker foundWorker = factory.getWorker(queue);
          if (foundWorker != null) {
            if (!foundWorker.isSuspended()) {
              System.out.println(queue + ": Already polling");
            } else {
              foundWorker.resumePolling();
              System.out.println(queue + ": Resumed ");
            }
          }
        }
      } else {
        System.err.println("Invalid command");
      }
    }
  }
}
