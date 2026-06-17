package pokemon.simulation.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import pokemon.simulation.GameState;
import pokemon.simulation.GameState.PlayerData;
import pokemon.simulation.PokemonCard;


public class GameUI extends BorderPane {
    private PlayerBoard player1Board;
    private PlayerBoard player2Board;
    private boolean winnerShown = false;

    //update GameState snapshot
    static class Snapshot {
        final String playerName;
        final PokemonCard activeCard;
        final PokemonCard trainerCard;
        final List<PokemonCard> bench;
        final int handSize, deckSize, discardPileSize;
        public List<PokemonCard> hand;

        Snapshot(PlayerData d) {
            this.playerName = d.playerName;
            this.activeCard = d.activeCard != null
                ? new PokemonCard(d.activeCard)
                : null;
            this.bench = d.bench.stream().map(PokemonCard::new).collect(Collectors.toList());
            this.hand = d.hand.stream().map(PokemonCard::new).collect(Collectors.toList());
            this.handSize = d.handSize;
            this.deckSize = d.deckSize;
            this.discardPileSize = d.discardPileSize;
            this.trainerCard = d.trainerCard != null
            ? new PokemonCard(d.trainerCard)
            : null;
        }
    }

    //snapshot queue to reproduce them 
    private final Queue<Snapshot> eventQueue = new LinkedList<>();
    private boolean playing = false;

    public GameUI() {
        player1Board = new PlayerBoard("Player1");
        player2Board = new PlayerBoard("Player2");

        HBox gameBoards = new HBox(30);
        gameBoards.setAlignment(Pos.CENTER);
        gameBoards.setPadding(new Insets(20));
        gameBoards.getChildren().addAll(player1Board, player2Board);
        this.setCenter(gameBoards);

        //get the change in the playerAgent and create the snapshot
        GameState.getInstance().addListener(playerName -> {
            //obligation to run on JavaFX Application Thread to update the component
            Platform.runLater(() -> {
                GameState state = GameState.getInstance();
                if (!state.isGameOver()) {
                    PlayerData d = state.getPlayerData(playerName);
                    if (d != null) {
                        synchronized (eventQueue) {
                            eventQueue.add(new Snapshot(d));
                        }
                        playNext();
                    }
                }
            });
        });
    }

    //reproduce the next snapshot in the queue
    private void playNext() {
        if (playing) return;
        Snapshot next;
        synchronized (eventQueue) {
            next = eventQueue.poll();
        }
        if (next == null) {
            GameState state = GameState.getInstance();
            //understand if is the finsh
            if (state.isGameOver() && !winnerShown) {
                winnerShown = true; 
                showWinner(state.getWinnerName(), state.getLooserName());
            }
            return; 
        }
        playing = true;
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));/*allows a half-second delay between snapshots*/
        pause.setOnFinished(evt -> {
            //apply snapshot to the correct player
            if ("Player1".equals(next.playerName)) {
                player1Board.applySnapshot(next);
            } else {
                player2Board.applySnapshot(next);
            }
            playing = false;
            playNext();      
        });
        pause.play();
    }

    public void showWinner(String winnerName, String Loosername) {
        ////obligation to run on JavaFX Application Thread to update the component
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("The winner is " +winnerName + " \n" + Loosername +" lost.");
            alert.showAndWait();
        });
    }
}