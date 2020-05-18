package babylongamelogic;

import engine.StaticState;
import engine.State;
import engine.Action;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONObject;

public class Entity extends State {

    protected int x;
    protected int y;

    public Entity(int x, int y,
            String name, boolean destroy, String id) {
        super(name, destroy, id == null ? UUID.randomUUID().toString() : id);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        return null;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        hasChanged = false;
        Entity newEntity = new Entity(x, y, name, destroy, id);
        return newEntity;
    }

    @Override
    public void setState(State newEntity) {
        super.setState(newEntity);
        x = ((Entity) newEntity).x;
        y = ((Entity) newEntity).y;
    }

    @Override
    protected Object clone() {
        Entity clon = new Entity(x, y, name, destroy, id);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonEntity = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("x", x);
        jsonAttrs.put("y", y);
        jsonEntity.put("Entity", jsonAttrs);
        return jsonEntity;
    }

    @Override
    public JSONObject toJSON(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions, JSONObject lastState) {
        Player thePlayer = getPlayer(sessionId, states);
        return (thePlayer != null && Math.abs(thePlayer.getX() - x) < 10 && Math.abs(thePlayer.getY() - y) < 10)
                ? (lastState == null || hasChanged || isJSONRemover(lastState) ? toJSON() : null)
                : (lastState != null && !isJSONRemover(lastState) ? toJSONRemover() : null);
    }
    
    protected Player getPlayer(String sessionId, LinkedList<State> states) {
        Player thePlayer = null;
        int i = 0;
        while (thePlayer == null && i < states.size()) {
            State state = states.get(i);
            if (state.getName().equals("Player") && ((Player) state).playerId.equals(sessionId)) {
                thePlayer = (Player) state;
            }
            i++;
        }
        return thePlayer;
    }

}
