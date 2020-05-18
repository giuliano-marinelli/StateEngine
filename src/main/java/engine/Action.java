package engine;

import java.util.HashMap;

public class Action {

    private String id;
    private String name;
    private HashMap<String, String> parameters;

    public Action(String id, String name) {
        this.id = id;
        this.name = name;
        this.parameters = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String putParameter(String key, String value) {
        return parameters.put(key, value);
    }

}
