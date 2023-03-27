package server;

public class DeleteCommand implements ICommand {
    private Receiver receiver;

    public DeleteCommand(Receiver receiver) {
        this.receiver = receiver;
    }
    @Override
    public boolean execute() {
        return receiver.delete();
    }
}
