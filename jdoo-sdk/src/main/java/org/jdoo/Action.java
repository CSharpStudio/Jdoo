package org.jdoo;

public abstract class Action {
    String message;

    public String getMessage() {
        return message;
    }

    public Action setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getAction() {
        return null;
    }

    public static ReloadAction reload(String message) {
        return new ReloadAction(message);
    }

    public static DialogAction dialog(String model, String type) {
        return new DialogAction(model, type);
    }

    public static JsAction js(String script) {
        return new JsAction(script);
    }

    public static ViewAction view(String model, String type) {
        return new ViewAction(model, type);
    }

    public static ServiceAction service(String model, String service) {
        return new ServiceAction(model, service);
    }
}

class ReloadAction extends Action {
    public ReloadAction(String message) {
        setMessage(message);
    }

    @Override
    public String getAction() {
        return "reload";
    }
}

class DialogAction extends Action {
    String model;
    String type;

    public DialogAction(String model, String type) {
        this.model = model;
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getAction() {
        return "dialog";
    }
}

class JsAction extends Action {
    String script;

    public JsAction(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public String getAction() {
        return "js";
    }
}

class ViewAction extends Action {
    String model;
    String type;

    public ViewAction(String model, String type) {
        this.model = model;
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getAction() {
        return "view";
    }
}

class ServiceAction extends Action {
    String model;
    String service;

    public ServiceAction(String model, String service) {
        this.model = model;
        this.service = service;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String getAction() {
        return "service";
    }
}