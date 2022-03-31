package example;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OutputActivity {
    @ActivityMethod
    void printMessage(String msg);
}
