package jdoo.exceptions;

public class CacheMissException extends JdooException {

    private static final long serialVersionUID = 1L;

    public CacheMissException() {
    }
    
    public CacheMissException(String message){
        super(message);
    }
}
