package engine;

import engine.Action;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONObject;

public abstract class State {

    public String id;
    public String name;
    private LinkedList<State> record;
    private LinkedList<String> events;
    public boolean hasChanged;
    public boolean destroy;

    public State(String name, boolean destroy, String id) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.record = new LinkedList<>();
        this.events = new LinkedList<>();
        this.hasChanged = false;
        this.destroy = destroy;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        //TODO in concrete class
        return null;
    }

    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        //TODO in concrete class
        hasChanged = false;
        return this;
    }

    public void createState(State newState) {
        //record.add((State) this.clone());
        this.setState(newState);
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

    public State getState(int numState) {
        return record.get(numState);
    }

    public void setState(State newState) {
        id = newState.id;
        name = newState.name;
        destroy = newState.destroy;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public JSONObject toJSON() {
        JSONObject jsonState = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("id", id);
        jsonAttrs.put("name", name);
        jsonAttrs.put("destroy", destroy);
        jsonState.put("State", jsonAttrs);
        return jsonState;
    }

    public JSONObject toJSON(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions, JSONObject lastState) {
        return lastState == null || hasChanged ? toJSON() : null;
    }

    protected JSONObject toJSONRemover() {
        JSONObject jsonState = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("id", id);
        jsonState.put("Remove", jsonAttrs);
        return jsonState;
    }

    protected boolean isJSONRemover(JSONObject json) {
        return json.get("Remove") != null
                && ((JSONObject) json.get("Remove")).get("id") != null
                && ((JSONObject) json.get("Remove")).get("id").equals(id);
    }

    @Override
    protected Object clone() {
        //TODO in concrete class
        return null;
    }

    public void addEvent(String event) {
        events.add(event);
    }

    public LinkedList<String> getEvents() {
        return events;
    }

    public void clearEvents() {
        events.clear();
    }

}
