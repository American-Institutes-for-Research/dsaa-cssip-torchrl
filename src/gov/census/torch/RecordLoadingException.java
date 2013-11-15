package gov.census.torch;

public class RecordLoadingException extends TorchException {
    public RecordLoadingException() {
        super();
    }

    public RecordLoadingException(String msg) {
        super(msg);
    }

    public RecordLoadingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
