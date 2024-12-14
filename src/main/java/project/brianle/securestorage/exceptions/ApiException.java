package project.brianle.securestorage.exceptions;

public class ApiException extends RuntimeException{
    public ApiException(String s){
        super(s);
    }

    public ApiException(){
        super("An error occurred");
    }
}
