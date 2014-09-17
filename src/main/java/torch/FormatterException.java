package torch;

public class FormatterException extends TorchException {
    public FormatterException() {
        super();
    }

    public FormatterException(String msg) {
        super(msg);
    }

    public FormatterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
