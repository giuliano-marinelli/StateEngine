package engine;

import babylongamelogic.Entity;
import babylongamelogic.TestState;
import babylongamelogic.World;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Game implements Runnable {

    private LinkedList<State> states;
    private LinkedList<StaticState> staticStates;
    private HashMap<String, LinkedList<Action>> actions;
    private ConcurrentHashMap<String, HashMap<String, JSONObject>> actionsSended; //sessionid -> (actionName, actionJSON)
    private HashMap<String, GameView> gameViews;
    private ConcurrentHashMap<String, String> gameViewsSended; //sessionid -> [enter, leave]
    private Phaser viewsBarrier;
    private String gameState;
    private String gameFullState;
    private String gameStaticState;
    private boolean endGame;
    private boolean singleCore;

    private Lobby lobby;

    //constructor
    public Game(Lobby lobby) {
        this.states = new LinkedList<>();
        this.staticStates = new LinkedList<>();
        this.actions = new HashMap<>();
        this.actionsSended = new ConcurrentHashMap();
        this.gameViews = new HashMap<>();
        this.gameViewsSended = new ConcurrentHashMap<>();
        this.viewsBarrier = new Phaser(1);
        this.endGame = false;
        this.lobby = lobby;
        this.singleCore = false;
    }

    @Override
    public void run() {
        init();
        createStaticState();
        while (!endGame) {
            try {
                Thread.sleep(10); //time per frame (10 fps)
                //readPlayers();
                readActions();

                if (singleCore) {
                    //se realizan las comunicaciones a traves de eventos y 
                    //se generan nuevos estados que seran computados
                    LinkedList<State> newStates = new LinkedList<>();
                    for (State state : states) {
                        LinkedList<State> newState = state.generate(states, staticStates, actions);
                        if (newState != null) {
                            newStates.addAll(newState);
                        }
                    }
                    states.addAll(newStates);

                    //se generan los estados siguientes incluyendo los generados
                    LinkedList<State> nextStates = new LinkedList<>();
                    for (State state : states) {
                        nextStates.add(state.next(states, staticStates, actions));
                    }

                    //se crean los nuevos estados con los calculados anteriormente
                    for (int i = 0; i < states.size(); i++) {
                        states.get(i).createState(nextStates.get(i));
                        states.get(i).clearEvents();
                    }
                } else {
                    //se realizan las comunicaciones a traves de eventos y 
                    //se generan nuevos estados que seran computados
                    final ConcurrentLinkedQueue<State> newStates = new ConcurrentLinkedQueue<>();
                    states.parallelStream().forEach(new Consumer<State>() {
                        @Override
                        public void accept(State state) {
                            LinkedList<State> newState = state.generate(states, staticStates, actions);
                            if (newState != null) {
                                newStates.addAll(newState);
                            }
                        }
                    });
                    states.addAll(newStates);

                    //se generan los estados siguientes incluyendo los generados
                    final ConcurrentHashMap<State, State> nextStates = new ConcurrentHashMap<>();
                    states.parallelStream().forEach(new Consumer<State>() {
                        @Override
                        public void accept(State state) {
                            nextStates.put(state, state.next(states, staticStates, actions));
                        }
                    });

                    //se crean los nuevos estados con los calculados anteriormente
                    states.parallelStream().forEachOrdered(new Consumer<State>() {
                        @Override
                        public void accept(State state) {
                            state.createState(nextStates.get(state));
                            state.clearEvents();
                        }
                    });
                }

                //crea el nuevo estado del juego
                createState();

                //recorre los player que entran o salen del juego para agregarlos
                //o quitarlos de la lista de gameViews
                readPlayers();

                //despierta a los hilos para que generen el JSON con el estado
                //correspondiente a la visibilidad de cada jugador
                viewsBarrier.arriveAndAwaitAdvance();

                //barrera hasta que todos los hilos terminan de computar el estado
                viewsBarrier.arriveAndAwaitAdvance();
                lobby.stateReady();
                int i = 0;
                while (i < states.size()) {
                    if (states.get(i).isDestroy()) {
                        //System.out.println("State " + states.get(i).getName() + " is removed.");
                        states.remove(i);
                    } else {
                        i++;
                    }
                }

                //System.out.println("STATIC: " + gameStaticState);
                //System.out.println("DYNAMIC: " + gameFullState);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void init() {
        //TODO crear estados dinamicos y estaticos
        /*Babylon init*/
        states.add(new World(new LinkedList<String>(), "World", false, null));
        for (int i = 1; i < 1000; i++) {
            states.add(new TestState(true, 1, i, "TestState" + i, false, null));
        }
        /*Phaser init*/
        //try {
        /*File map = new File(this.getClass().getClassLoader().getResource("files/map.csv").toURI());
            loadMap(map);
            //match spawnea players cuando todos hicieron ready
            states.add(new Match(1, 2, true, false, false, 0, 4, new LinkedList<String>(),
                    new LinkedList<String>(), new LinkedList<String>(), new LinkedList<Integer>(), "Match", false, null));
            createSpawns();

        } catch (URISyntaxException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    private void createStaticState() {
        JSONObject jsonStaticStates = new JSONObject();
        int i = 0;
        for (StaticState staticState : staticStates) {
            jsonStaticStates.put(i + "", staticState.toJSON());
            i++;
        }
        gameStaticState = jsonStaticStates.toString();
    }

    private void createState() {
        JSONObject jsonFullStates = new JSONObject();
        JSONObject jsonStates = new JSONObject();
        int i = 0;
        int j = 0;
        for (State state : states) {
            jsonFullStates.put(i + "", state.toJSON());
            if (state.hasChanged()) {
                jsonStates.put(j + "", state.toJSON());
                j++;
            }
            i++;
        }
        gameFullState = jsonFullStates.toString();
        gameState = jsonStates.toString();
    }

    public void readActions() {
        actions.clear();
        for (Map.Entry<String, HashMap<String, JSONObject>> actionsSend : actionsSended.entrySet()) {
            String sessionId = actionsSend.getKey();
            HashMap<String, JSONObject> newActions = actionsSend.getValue();
            LinkedList<Action> newActionsList = new LinkedList<>();
            //esta lista es solo con proposito de testeo. Para imprimir los nombres de las acciones realizadas
            LinkedList<String> newActionsNameList = new LinkedList<>();
            for (Map.Entry<String, JSONObject> newAction : newActions.entrySet()) {
                String newActionName = newAction.getKey();
                JSONObject newActionJSON = newAction.getValue();
                Action newActionObject = null;
                try {
                    newActionObject = new Action(sessionId, newActionName);

                    JSONArray jsonParameters = (JSONArray) newActionJSON.get("parameters");
                    if (jsonParameters != null) {
                        for (int i = 0; i < jsonParameters.size(); i++) {
                            JSONObject parameter = (JSONObject) jsonParameters.get(i);
                            newActionObject.putParameter((String) parameter.get("name"), (String) parameter.get("value"));
                        }
                    }
                } catch (Exception ex) {
                    newActionObject = new Action(sessionId, newActionName);
                } finally {
                    newActionsList.add(newActionObject);
                    newActionsNameList.add(newActionName);
                }
            }
            System.out.println("Player " + sessionId + " do actions: " + newActionsNameList.toString());
            actions.put(sessionId, newActionsList);
            actionsSended.remove(sessionId);
        }
    }

    public void addAction(String sessionId, String action) {
        JSONParser parser = new JSONParser();
        JSONObject newAction;
        try {
            newAction = (JSONObject) parser.parse(action);
        } catch (ParseException ex) {
            newAction = new JSONObject();
            newAction.put("name", action);
        }
        String newActionName = newAction.get("name") != null ? (String) newAction.get("name") : null;
        int newPriority = newAction.get("priority") != null ? Integer.parseInt((String) newAction.get("priority")) : 0;
        if (newActionName != null) {
            if (actionsSended.containsKey(sessionId)) {
                JSONObject actualAction = actionsSended.get(sessionId).get(newActionName);
                if (actualAction != null) {
                    int actualPriority = actualAction.get("priority") != null ? Integer.parseInt((String) actualAction.get("priority")) : 0;;

                    if (newPriority > actualPriority) {
                        actionsSended.get(sessionId).put(newActionName, newAction);
                    }
                } else {
                    actionsSended.get(sessionId).put(newActionName, newAction);
                }
            } else {
                HashMap<String, JSONObject> newActions = new HashMap<>();
                newActions.put(newActionName, newAction);
                actionsSended.put(sessionId, newActions);
            }
        }

    }

    public void readPlayers() {
        if (gameViewsSended.size() > 0) {
            for (Map.Entry<String, String> gameViewSended : gameViewsSended.entrySet()) {
                String sessionId = gameViewSended.getKey();
                String action = gameViewSended.getValue();
                if (action == "enter") {
                    //aumento en uno los miembros de la barrera
                    //(tal ves hay que hacerlo en el hilo del gameView)
                    viewsBarrier.register();
                    //creo el nuevo hilo
                    GameView gameView = new GameView(sessionId, states, staticStates, actions, viewsBarrier);
                    Thread threadGameView = new Thread(gameView);
                    threadGameView.start();
                    //lo agrego a la lista de gridViews
                    gameViews.put(sessionId, gameView);
                } else if (action == "leave") {
                    //disminuyo en uno los miembros de la barrera
                    //(tal ves hay que hacerlo en el hilo del gameView)
                    viewsBarrier.arriveAndDeregister();
                    //mato el hilo seteando su variable de terminancion y realizando un notify
                    gameViews.get(sessionId).stop();
                    //lo elimino de la lista de gridViews
                    gameViews.remove(sessionId);
                }
            }
            gameViewsSended.clear();
        }
    }

    public void addPlayer(String sessionId) {
        gameViewsSended.put(sessionId, "enter");
    }

    public void removePlayer(String sessionId) {
        gameViewsSended.put(sessionId, "leave");
    }

    public boolean isEndGame() {
        return endGame;
    }

    public void endGame() {
        endGame = true;
    }

    public String getGameState() {
        return gameState;
    }

    public String getGameState(String sessionId) {
        return gameViews.get(sessionId) != null ? gameViews.get(sessionId).getGameState() : "{}";
    }

    public String getGameFullState() {
        return gameFullState;
    }

    public String getGameStaticState() {
        return gameStaticState;
    }

}
