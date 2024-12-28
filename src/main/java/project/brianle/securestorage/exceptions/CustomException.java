package project.brianle.securestorage.exceptions;

public class CustomException extends RuntimeException{
    public CustomException(String s){
        super(s);
    }

    public CustomException(){
        super("An error occurred");
    }
}
