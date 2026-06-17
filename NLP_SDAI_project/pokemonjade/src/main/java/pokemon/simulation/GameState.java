package pokemon.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class GameState {
    private static GameState instance;
    private final Map<String, PlayerData> players = new HashMap<>();
    private final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    public static class PlayerData {
        public String playerName; 
        public PokemonCard activeCard;
        public List<PokemonCard> bench = new ArrayList<>();
        public List<PokemonCard> hand  = new ArrayList<>();  
        public int handSize;
        public int deckSize;
        public int discardPileSize;
        public PokemonCard trainerCard;
        
        public PlayerData() {
            //initialize default values
            this.activeCard = null;
            this.bench = new ArrayList<>();
            this.hand = new ArrayList<>();
            this.handSize = 0;
            this.deckSize = 0;
            this.discardPileSize = 0;
        }
    }

    private GameState() {
        /*inizialize the data for player1 and 2 at the beginning,
        in this way PlayerBoard can read them even if they have not yet been update by the agents*/
        players.put("Player1", new PlayerData());
        players.put("Player2", new PlayerData());
    }

    public static synchronized GameState getInstance() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    //method to update active card and bench
    public void updatePlayer(String name, PokemonCard active, List<PokemonCard> bench) {
        PlayerData data = players.getOrDefault(name, new PlayerData());
        data.playerName = name;
        data.activeCard = active;
        data.bench = new ArrayList<>(bench); 
        players.put(name, data);
        notifyListeners(name); 
    }

    //method to update hand
    public void updatePlayerHand(String name, List<PokemonCard> newHand) {
        PlayerData data = players.getOrDefault(name, new PlayerData());
        data.playerName = name;
        data.hand = new ArrayList<>(newHand);
        data.handSize = newHand.size();      
        players.put(name, data);
        notifyListeners(name);
    }

    //method to update the statistics of player (hand, deck, discard pile)
    public void updatePlayerStats(String name, int handSize, int deckSize, int discardPileSize) {
        PlayerData data = players.getOrDefault(name, new PlayerData());
        data.playerName = name;
        data.handSize = handSize;
        data.deckSize = deckSize;
        data.discardPileSize = discardPileSize;
        players.put(name, data);
        notifyListeners(name);
    }

    //method to update the trainer card in play
    public void updateTrainerCard(String name, PokemonCard trainer) {
        PlayerData data = players.getOrDefault(name, new PlayerData());
        data.playerName = name;
        data.trainerCard = trainer;
        players.put(name, data);
        notifyListeners(name); 
    }

    public PlayerData getPlayerData(String name) {
        return players.get(name);
    }

    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String playerName) {
        for (Consumer<String> listener : listeners) {
            listener.accept(playerName);
        }
    }

    private boolean gameOver = false;
    private String  winnerName;
    private String  looserName;

    public boolean isGameOver() {
        return gameOver;
    }
    public String getWinnerName() {
        return winnerName;
    }
    public String getLooserName() {
        return looserName;
    }

    //notify which is the winner and the looser
    public void setWinner(String playerName, String opponentName) {
        this.gameOver   = true;
        this.winnerName = playerName;
        this.looserName = opponentName;
        notifyListeners(playerName);
        notifyListeners(opponentName);
    }


}