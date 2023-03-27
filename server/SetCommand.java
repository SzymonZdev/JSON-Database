package server;

public class SetCommand implements ICommand {
    private Receiver receiver;

    public SetCommand(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean execute() {
        return receiver.set();
    }
}
