package main.java.flowershop.exception;

public class OperationCancelledException extends RuntimeException {

    public OperationCancelledException(String message) {
        super(message);
    }
}