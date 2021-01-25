package jdoo.exceptions;

public class CacheMissException extends JdooException {

    /**
     *
     */
    private static final long serialVersionUID = 168543100249339794L;

    public CacheMissException() {
    }
    
    public CacheMissException(String message){
        super(message);
    }
}
