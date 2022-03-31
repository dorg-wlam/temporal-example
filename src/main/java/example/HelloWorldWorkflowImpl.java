package example;

import example.runner.ExampleRunner;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class HelloWorldWorkflowImpl implements HelloWorldWorkflow {

    private final RetryOptions retryoptions =
            RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(100))
                    .setBackoffCoefficient(2)
                    .setMaximumAttempts(500)
                    .build();
    private final ActivityOptions defaultActivityOptions =
            ActivityOptions.newBuilder()
                    .setTaskQueue(ExampleRunner.ACTIVITY_TASK_QUEUE)
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .setRetryOptions(retryoptions)
                    .build();

    private final OutputActivity outputActivity =
            Workflow.newActivityStub(
                    OutputActivity.class, defaultActivityOptions);

    @Override
    public void sayHello(String name) {
        outputActivity.printMessage(name);
    }
}
