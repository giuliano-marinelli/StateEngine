package engine;

import engine.Action;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONObject;

public abstract class StaticState extends State {

    public StaticState(String name, String id) {
        super(name, false, id == null ? UUID.randomUUID().toString() : id);
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        return this;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        return null;
    }

    @Override
    public void createState(State newState) {
        //do nothing
    }

    @Override
    public State getState(int numState) {
        return this;
    }

    @Override
    public void setState(State newState) {
        //do nothing
    }

}
