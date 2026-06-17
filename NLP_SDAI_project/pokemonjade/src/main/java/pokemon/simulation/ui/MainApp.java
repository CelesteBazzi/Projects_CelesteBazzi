package pokemon.simulation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pokemon.simulation.Deck;
import pokemon.simulation.GameState; 
import pokemon.simulation.HandPlayer; 
import pokemon.simulation.PokemonCard;
import pokemon.simulation.agents.GameMasterAgent;
import pokemon.simulation.agents.PlayerAgent;
import pokemon.simulation.utils.JSONLoader;

public class MainApp extends Application {

    private ContainerController mainContainer; // Keeps a reference to the JADE container

    @Override
    public void start(Stage startingStage) {
        GameUI root = new GameUI();
        Scene scene = new Scene(root, 1200, 800); 
        startingStage.setTitle("Pokémon Card Game Simulation");
        startingStage.setScene(scene);
        startingStage.show();

        //start the JADE system in a separate thread so as not to block the GUI
        new Thread(() -> {
            try {
                Thread.sleep(500);
                startGameAndJade();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        //trun off the JADE container when the GUI is closed
        startingStage.setOnCloseRequest(event -> {
            if (mainContainer != null) {
                try {
                    mainContainer.kill();
                    System.out.println("JADE container turned off correctly.");
                } catch (Exception e) {
                    System.err.println("Error during shutdown of JADE container: " + e.getMessage());
                }
            }
            //close JavaFX
            Platform.exit();
            System.exit(0);
        });
    }

    //Main.java logic
    private void startGameAndJade() throws Exception {
        List<PokemonCard> rawCards = JSONLoader.loadCards("src/main/resources/Pokemon.json");
        List<PokemonCard> allCards = new ArrayList<>();

        for (PokemonCard card : rawCards) {
            int copies = card.getN_cards();
            for (int i = 0; i < copies; i++) {
                allCards.add(new PokemonCard(card)); 
            }
        }

        if (allCards.isEmpty()) {
            System.err.println("Error: No cards loaded or generated. Check Pokemon.json.");
            return; 
        }

        Set<String> trainerNames = allCards.stream()
                .map(PokemonCard::getTrainer_Name)
                .filter(name -> name != null && !name.trim().isEmpty()) 
                .collect(Collectors.toSet());

        List<String> trainers = new ArrayList<>(trainerNames);
        Collections.shuffle(trainers); 

        if (trainers.size() < 2) {
            System.err.println("Error: There are not enough unique trainer to create two separate decks..");
            System.err.println("Found only this trainer: " + trainerNames);
            return;
        }

        String trainer1 = trainers.get(0);
        String trainer2 = trainers.get(1);

        List<PokemonCard> deck1Cards = allCards.stream()
                .filter(card -> trainer1.equals(card.getTrainer_Name()))
                .limit(60) 
                .collect(Collectors.toList());

        List<PokemonCard> deck2Cards = allCards.stream()
                .filter(card -> trainer2.equals(card.getTrainer_Name()))
                .limit(60)
                .collect(Collectors.toList());

        if (deck1Cards.isEmpty() || deck2Cards.isEmpty()) {
            System.err.println("Error: empty deck");
            return;
        }
        
        System.out.println("Player1: " + trainer1 + " has a deck of " + deck1Cards.size() + " cards.");
        System.out.println("Player2: " + trainer2 + " has a deck of " + deck2Cards.size() + " cards.");


        Deck deck1 = new Deck(deck1Cards);
        Deck deck2 = new Deck(deck2Cards);

        HandPlayer hand1 = new HandPlayer(deck1.drawHand(7));
        HandPlayer hand2 = new HandPlayer(deck2.drawHand(7));

        GameState.getInstance().updatePlayerStats("Player1", hand1.getRemainingCards().size(), deck1.getCards().size(), 0);
        GameState.getInstance().updatePlayer("Player1", null, new ArrayList<>()); 

        GameState.getInstance().updatePlayerStats("Player2", hand2.getRemainingCards().size(), deck2.getCards().size(), 0);
        GameState.getInstance().updatePlayer("Player2", null, new ArrayList<>()); 

        //configuration and start-up of the JADE container
        jade.core.Runtime rt = jade.core.Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "false");//disabilitation of the JADE GUI
        p.setParameter(Profile.LOCAL_PORT, "1200");
        mainContainer = rt.createMainContainer(p); 

        AgentController gm = mainContainer.createNewAgent("GameMaster", GameMasterAgent.class.getName(), null);
        AgentController p1 = mainContainer.createNewAgent("Player1", PlayerAgent.class.getName(), new Object[]{hand1, deck1});
        AgentController p2 = mainContainer.createNewAgent("Player2", PlayerAgent.class.getName(), new Object[]{hand2, deck2});

        gm.start();
        p1.start();
        p2.start();

        System.out.println("JADE agents started.");
    }

    public static void main(String[] args) {
        launch(args); //start the JavaFX application
    }
}