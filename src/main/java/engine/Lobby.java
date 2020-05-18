package engine;

import java.util.HashMap;

public class Lobby {

    private Game game;

    private static Lobby lobby;

    private Lobby() {
        game = new Game(this);
        Thread threadGame = new Thread(game);
        threadGame.start();
    }

    public synchronized static Lobby startGame() {
        if (lobby == null) {
            lobby = new Lobby();
        }
        return lobby;
    }

    public synchronized void addAction(String sessionId, String action) {
        game.addAction(sessionId, action);
    }
    
    public synchronized void addPlayer(String sessionId) {
        game.addPlayer(sessionId);
    }
    
    public synchronized void removePlayer(String sessionId) {
        game.removePlayer(sessionId);
    }

    public synchronized void stateReady() {
        notifyAll();
    }

    public synchronized String getState() throws InterruptedException {
        wait();
        return game.getGameState();
    }
    
    public synchronized String getState(String sessionId) throws InterruptedException {
        wait();
        return game.getGameState(sessionId);
    }

    public synchronized String getFullState() throws InterruptedException {
        wait();
        return game.getGameFullState();
    }

    public synchronized String getStaticState() throws InterruptedException {
        wait();
        return game.getGameStaticState();
    }

    public boolean isEndGame() {
        return game.isEndGame();
    }

}
