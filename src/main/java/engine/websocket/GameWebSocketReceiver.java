package engine.websocket;

import engine.Lobby;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.Session;

@ServerEndpoint("/GameWebSocket")
public class GameWebSocketReceiver {

    private Lobby lobby;

    @OnOpen
    public void playerEnter(Session session) throws InterruptedException {
        //foreach player that enter the game, start a new thread that will send him the new states of the game
        System.out.println("Player " + session.getId() + " entered the game.");
        lobby = Lobby.startGame();
        lobby.addPlayer(session.getId());
        lobby.addAction(session.getId(), "enter");
        GameWebSocketSender gameWebSocketSender = new GameWebSocketSender(session, lobby);
        Thread threadGameWebSocketSender = new Thread(gameWebSocketSender);
        threadGameWebSocketSender.start();
    }

    @OnMessage
    public void recieveAction(String action, Session session) {
        //each action recieved from a player will be added to the actions buffer of the game
        lobby.addAction(session.getId(), action);
    }
    
    @OnClose
    public void playerExit(Session session) {
        System.out.println("Player " + session.getId() + " leave the game.");
        lobby.removePlayer(session.getId());
        lobby.addAction(session.getId(), "leave");
    }
    
    @OnError
    public void onError(Throwable error) {
        System.err.println(error.getMessage());
        error.printStackTrace();
    }
}
