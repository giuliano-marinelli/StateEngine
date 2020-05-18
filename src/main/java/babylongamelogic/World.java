package babylongamelogic;

import engine.Action;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.Map;
import engine.State;
import engine.StaticState;

public class World extends State {

    protected LinkedList<String> players;

    public World(LinkedList<String> players,
            String name, boolean destroy, String id) {
        super(name, destroy, id == null ? UUID.randomUUID().toString() : id);
        this.players = players;
    }

    private void updatePlayers(LinkedList<State> states, LinkedList<State> newStates, HashMap<String, LinkedList<Action>> actions) {
        //obtiene los puntos de spawn para personajes que atacan y defienden, y para las torres

        for (Map.Entry<String, LinkedList<Action>> actionEntry : actions.entrySet()) {
            String id = actionEntry.getKey();
            LinkedList<Action> actionsList = actionEntry.getValue();
            for (Action action : actionsList) {
                switch (action.getName()) {
                    case "enter":
                        Player newPlayer = new Player(id, false, 0, 0, "Player", false, null);
                        newStates.add(newPlayer);
                        newPlayer.addEvent("spawn");
                        break;
                    case "leave":
                        for (State state : states) {
                            if (state.getName().equals("Player") && ((Player) state).playerId == id) {
                                state.addEvent("despawn");
                                break;
                            }
                        }
                        break;
                }
            }
        }
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        LinkedList<State> newStates = new LinkedList<>();
        updatePlayers(states, newStates, actions);
        return newStates;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        hasChanged = false;
        LinkedList<String> newPlayers = (LinkedList<String>) players.clone();
        for (Map.Entry<String, LinkedList<Action>> actionEntry : actions.entrySet()) {
            String id = actionEntry.getKey();
            LinkedList<Action> actionsList = actionEntry.getValue();
            for (Action action : actionsList) {
                switch (action.getName()) {
                    case "enter":
                        hasChanged = true;
                        System.out.println("World> Player " + id + " entered the game.");
                        newPlayers.add(id);
                        break;
                    case "leave":
                        hasChanged = true;
                        newPlayers.remove(id);
                        break;
                }
            }
        }
        World newWorld = new World(newPlayers, name, destroy, id);
        return newWorld;
    }

    @Override
    public void setState(State newWorld) {
        super.setState(newWorld);
        players = ((World) newWorld).players;
    }

    @Override
    protected Object clone() {
        World clon = new World(players, name, destroy, id);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonWorld = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());

        JSONArray jsonPlayers = new JSONArray();
        for (String player : players) {
            jsonPlayers.add(player);
        }
        jsonAttrs.put("players", jsonPlayers);

        jsonWorld.put("World", jsonAttrs);
        return jsonWorld;
    }

}
