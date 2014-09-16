package torch;

public class TorchException extends Exception {
    public TorchException() {
        super();
    }

    public TorchException(String msg) {
        super(msg);
    }

    public TorchException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
