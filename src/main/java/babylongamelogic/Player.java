package babylongamelogic;

import engine.StaticState;
import engine.State;
import engine.Action;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONObject;

public class Player extends Entity {

    public String playerId;
    protected boolean leave;

    public Player(String playerId, boolean leave,
            int x, int y,
            String name, boolean destroy, String id) {
        super(x, y, name, destroy, id == null ? UUID.randomUUID().toString() : id);
        this.playerId = playerId;
        this.leave = leave;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        LinkedList<State> newStates = new LinkedList<>();
        Point myFuturePosition = futurePosition(actions);
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
        hasChanged = false;
        LinkedList<Action> actionsList = actions.get(playerId);
        int newX = x;
        int newY = y;
        boolean newLeave = leave;
        boolean newDestroy = destroy;
        if (actionsList != null) {
            for (Action action : actionsList) {
                if (action != null) {
                    hasChanged = true;
                    //System.out.println("ACTION: " + action.getName());
                    switch (action.getName()) {
                        case "up":
                            newY = y - 1;
                            break;
                        case "down":
                            newY = y + 1;
                            break;
                        case "left":
                            newX = x + 1;
                            break;
                        case "right":
                            newX = x - 1;
                            break;
                    }
                    switch (action.getName()) {
                        case "enter":
                            newLeave = false;
                            break;
                        case "leave":
                            newLeave = true;
                            break;
                    }
                    //if (!((Map) staticStates.get(0)).canWalk(new Point(newX, newY))) {
                    //newX = x;
                    //newY = y;
                    //}
                }
            }
        }
        LinkedList<String> events = getEvents();
        if (!events.isEmpty()) {
            hasChanged = true;
            boolean wasRespanwn = false;
            for (String event : events) {
                switch (event) {
                    case "collide":
                        if (!wasRespanwn) {
                            newX = x;
                            newY = y;
                        }
                        break;
                    case "spawn":
                        System.out.println("Player " + playerId + " spawn in game.");
                        break;
                    case "despawn":
                        newDestroy = true;
                        System.out.println("Player " + playerId + " despawn of the game.");
                        break;
                }
            }
        }
        Player newPlayer = new Player(playerId, newLeave, newX, newY, name, newDestroy, id);
        return newPlayer;
    }

    public Point futurePosition(HashMap<String, LinkedList<Action>> actions) {
        Point position;
        LinkedList<Action> actionsList = actions.get(playerId);
        int newY = y;
        int newX = x;
        if (actionsList != null) {
            for (Action action : actionsList) {
                if (action != null) {
                    switch (action.getName()) {
                        case "up":
                            newY = y - 1;
                            break;
                        case "down":
                            newY = y + 1;
                            break;
                        case "left":
                            newX = x + 1;
                            break;
                        case "right":
                            newX = x - 1;
                            break;
                    }
                }
            }
        }
        position = new Point(newX, newY);
        return position;
    }

    @Override
    public void setState(State newPlayer) {
        super.setState(newPlayer);
        playerId = ((Player) newPlayer).playerId;
        leave = ((Player) newPlayer).leave;
    }

    @Override
    protected Object clone() {
        Player clon = new Player(playerId, leave, x, y, name, destroy, id);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonPlayer = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("id", playerId);
        jsonAttrs.put("leave", leave);
        jsonPlayer.put("Player", jsonAttrs);
        return jsonPlayer;
    }

    @Override
    public JSONObject toJSON(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions, JSONObject lastState) {
        JSONObject superJSON = super.toJSON(sessionId, states, staticStates, actions, lastState);
        return superJSON != null && !isJSONRemover(superJSON) ? toJSON() : superJSON;
    }

}
