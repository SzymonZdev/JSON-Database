package server;

public class GetCommand implements ICommand {
    private Receiver receiver;

    public GetCommand(Receiver receiver) {
        this.receiver = receiver;
    }
    @Override
    public boolean execute() {
        return receiver.get();
    }
}