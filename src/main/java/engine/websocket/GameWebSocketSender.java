package engine.websocket;

import engine.Lobby;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;

public class GameWebSocketSender implements Runnable {

    private Session session;
    private Lobby lobby;

    public GameWebSocketSender(Session session, Lobby lobby) {
        this.session = session;
        this.lobby = lobby;
    }

    @Override
    public void run() {
        try {
            //envia la identidad con la que se representara en el juego
            session.getAsyncRemote().sendText("{\"id\":\"" + session.getId() + "\"}");
            //envia estados estaticos por unica vez
            //String staticStates = lobby.getStaticState();
            //session.getAsyncRemote().sendText(staticStates);
            //System.out.println("Send statics states to player " + session.getId());
            //envia todos los estados dinamicos por unica vez
            //String fullStates = lobby.getFullState();
            //session.getAsyncRemote().sendText(fullStates);
            //System.out.println("Send full states to player " + session.getId());
            //repite hasta que el juego termina
            while (!lobby.isEndGame() && session.isOpen()) {
                //envia los estados que cambiaron en cada ciclo del juego
                String states = lobby.getState(session.getId());
                if (session.isOpen() && states != null) {
                    session.getAsyncRemote().sendText(states);
                    //System.out.println("Send state changes to player " + session.getId());
                }//String estado = "{\"cell\":{\"x\":1,\"y\":1,\"jugador\":\"" + i + "\"}}";//juego.getEstado();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(GameWebSocketSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
