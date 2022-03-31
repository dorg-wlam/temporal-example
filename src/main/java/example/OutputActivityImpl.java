package example;

public class OutputActivityImpl implements OutputActivity{
    @Override
    public void printMessage(String name) {
        System.out.println("<<Activity>> Hello there! " + name);
    }
}
