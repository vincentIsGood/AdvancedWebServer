package com.vincentcodes.webserver.dispatcher.operation;

public class OperationResult<T> {
    private T result;
    private OperationResultStatus status;

    private OperationResult(T result, OperationResultStatus status){
        this.result = result;
        this.status = status;
    }

    public static <T> OperationResult<T> success(T result){
        return new OperationResult<>(result, OperationResultStatus.SUCCESS);
    }

    public static <T> OperationResult<T> failure(T result){
        return new OperationResult<>(result, OperationResultStatus.FAILURE);
    }

    public static <T> OperationResult<T> error(T result){
        return new OperationResult<>(result, OperationResultStatus.ERROR);
    }

    public T get(){
        return result;
    }

    public OperationResultStatus status(){
        return status;
    }
}
