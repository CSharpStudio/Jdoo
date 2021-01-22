package jdoo.apis;

public class CacheMiss extends Error {

    /**
     *
     */
    private static final long serialVersionUID = 168543100249339794L;

    public CacheMiss() {
    }
    
    public CacheMiss(String message){
        super(message);
    }
}
