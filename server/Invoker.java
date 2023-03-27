package server;

public class Invoker {
    public boolean executeOperation(ICommand command) {
        return command.execute();
    }
}
