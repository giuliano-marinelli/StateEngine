package babylongamelogic;

import engine.Action;
import engine.State;
import engine.StaticState;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONObject;

public class TestState extends Entity {

    protected boolean lastMove;

    public TestState(boolean lastMove,
            int x, int y,
            String name, boolean destroy, String id) {
        super(x, y, name, destroy, id == null ? UUID.randomUUID().toString() : id);
        this.lastMove = lastMove;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        LinkedList<State> newStates = new LinkedList<>();
        Point myFuturePosition = futurePosition();
        for (State state : states) {
            if (state != this && state.getName().equals("Player") && !((Player) state).leave) {
                Point otherFuturePosition = ((Player) state).futurePosition(actions);
                if (myFuturePosition.x == otherFuturePosition.x && myFuturePosition.y == otherFuturePosition.y) {
                    this.addEvent("collide");
                }
            }
        }
        return newStates;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        hasChanged = true;
        int newX = x + (lastMove ? 1 : -1);

        LinkedList<String> events = getEvents();
        if (!events.isEmpty()) {
            for (String event : events) {
                switch (event) {
                    case "collide":
                        newX = x + (lastMove ? -1 : 1);
                        break;
                }
            }
        }
        TestState newTestState = new TestState(!lastMove, newX, y, name, destroy, id);
        return newTestState;
    }

    public Point futurePosition() {
        Point position;
        int newY = y;
        int newX = x + (lastMove ? 1 : -1);
        position = new Point(newX, newY);
        return position;
    }

    @Override
    public void setState(State newTestState) {
        super.setState(newTestState);
        lastMove = ((TestState) newTestState).lastMove;
    }

    @Override
    protected Object clone() {
        TestState clon = new TestState(lastMove, x, y, name, destroy, id);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonTestState = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("lastMove", lastMove);
        jsonTestState.put("TestState", jsonAttrs);
        return jsonTestState;
    }

    @Override
    public JSONObject toJSON(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions, JSONObject lastState) {
        //JSONObject superJSON = super.toJSON(sessionId, states, staticStates, actions, lastState);
        return toJSON();//superJSON != null && !isJSONRemover(superJSON) ? toJSON() : superJSON;
    }

}
