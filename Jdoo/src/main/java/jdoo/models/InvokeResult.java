package jdoo.models;

public class InvokeResult {
    private boolean success;
    private Object result;
    public InvokeResult(boolean success, Object result){
        this.success = success;
        this.result = result;
    }
    public boolean getSuccess(){
        return success;
    }
    public Object getResult(){
        return result;
    }
}
