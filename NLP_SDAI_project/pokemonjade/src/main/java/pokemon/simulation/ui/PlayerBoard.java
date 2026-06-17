package pokemon.simulation.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pokemon.simulation.GameState;
import pokemon.simulation.PokemonCard;

public class PlayerBoard extends VBox {
    private final String playerName; 
    private Label activeCardNameLabel;
    private Label activeCardHPLabel;
    private HBox benchContainer; 
    private HBox handContainer; 
    private Label handSizeLabel;
    private Label deckSizeLabel;
    private Label discardPileSizeLabel;
    private Label trainerLabel;
    private HBox trainerContainer; 

    public PlayerBoard(String playerName) {
        this.playerName = playerName;

        //dimensione piattaforma di gioco
        double prefW = 600, prefH = 800;
        this.setPrefSize(prefW, prefH);
        this.setMinSize(prefW, prefH);
        this.setMaxSize(prefW, prefH);


        this.setSpacing(10);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-border-color: #A9A9A9; -fx-border-width: 2; -fx-padding: 15; -fx-background-color: #F8F8F8; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Nome del Giocatore
        Label nameLabel = new Label(playerName + "'s Board \n");
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.DARKBLUE);
        this.getChildren().add(nameLabel);

        trainerLabel = new Label(); 
        trainerLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        trainerLabel.setTextFill(Color.DARKBLUE);
        this.getChildren().add(trainerLabel);

        //Active Pokémon display 
        VBox activeCardBox = new VBox(5);
        activeCardBox.setAlignment(Pos.CENTER);
        activeCardBox.setPrefSize(180, 150); // Dimensione fissa per il Pokémon attivo
        activeCardBox.setStyle("-fx-border-color: #008080; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: #E0FFFF; -fx-background-radius: 5; -fx-border-radius: 5;");
        Label activeTitle = new Label("Active Pokémon");
        activeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        activeCardNameLabel = new Label("N/A");
        activeCardNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        activeCardHPLabel = new Label("HP: N/A");
        activeCardHPLabel.setFont(Font.font("Arial", 14));
        activeCardBox.getChildren().addAll(activeTitle, activeCardNameLabel, activeCardHPLabel);
        this.getChildren().add(activeCardBox);

        //Trainer card display 
        Label trainerCardTitle = new Label("Trainer Card");
        trainerCardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        trainerContainer = new HBox(5);
        trainerContainer.setAlignment(Pos.CENTER);
        this.getChildren().addAll(trainerCardTitle, trainerContainer);


        //bench display
        VBox benchVBox = new VBox(5);
        benchVBox.setAlignment(Pos.CENTER);
        Label benchTitle = new Label("Bench (Max 5)");
        benchTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        benchContainer = new HBox(5); //maximum five cards
        benchContainer.setAlignment(Pos.CENTER);
        benchContainer.setPrefHeight(100);
        benchContainer.setStyle("-fx-border-color: #2E8B57; -fx-border-width: 1; -fx-padding: 5; -fx-background-color: #E6FFE6; -fx-background-radius: 5; -fx-border-radius: 5;");
        benchVBox.getChildren().addAll(benchTitle, benchContainer);
        this.getChildren().add(benchVBox);



        Label handLabel = new Label("Hand");
        handLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        handContainer = new HBox(5);
        handContainer.setAlignment(Pos.CENTER);
        this.getChildren().addAll(handLabel, handContainer);



        //Statistics hand, deck, discard pile
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-padding: 10; -fx-background-color: #F0F0F0; -fx-background-radius: 5;");
        handSizeLabel = new Label("Hand: 0");
        deckSizeLabel = new Label("Deck: 0");
        discardPileSizeLabel = new Label("Discard: 0");
        handSizeLabel.setFont(Font.font("Arial", 12));
        deckSizeLabel.setFont(Font.font("Arial", 12));
        discardPileSizeLabel.setFont(Font.font("Arial", 12));
        statsBox.getChildren().addAll(handSizeLabel, deckSizeLabel, discardPileSizeLabel);
        this.getChildren().add(statsBox);

        
        updateBoard();
    }

    

    public void updateBoard() {
        GameState.PlayerData data = GameState.getInstance().getPlayerData(playerName);
        if (data != null) {
            //Hand
            handContainer.getChildren().clear();
            for (PokemonCard card : data.hand) {
                handContainer.getChildren().add(createCardView(card));
            }

            //Active Pokémon
            if (data.activeCard != null) {
                activeCardNameLabel.setText(data.activeCard.getPokemon_Name());
                activeCardHPLabel.setText("HP: " + (int)data.activeCard.getHP() + " / " + (int)data.activeCard.getHPOriginal()); // Usa getHP() per maxHP se non hai getBaseHP()
                String trainerName;
                trainerName = data.activeCard.getTrainer_Name();
                trainerLabel.setText("Trainer: " + trainerName);
                //if is KO we change the HP color
                if (data.activeCard.isKnockedOut()) {
                    activeCardHPLabel.setTextFill(Color.RED);
                } else {
                    activeCardHPLabel.setTextFill(Color.BLACK);
                }
            } else {
                activeCardNameLabel.setText("N/A");
                activeCardHPLabel.setText("HP: N/A");
                activeCardHPLabel.setTextFill(Color.BLACK);
            }

            //bench
            benchContainer.getChildren().clear(); 
            for (PokemonCard card : data.bench) {
                benchContainer.getChildren().add(createBenchCardView(card));
            }


            //statistics
            handSizeLabel.setText("Hand: " + data.handSize);
            deckSizeLabel.setText("Deck: " + data.deckSize);
            discardPileSizeLabel.setText("Discard: " + data.discardPileSize);
        }
    }

    //bench card design
    private VBox createBenchCardView(PokemonCard card) {
        VBox cardView = new VBox(2);
        cardView.setAlignment(Pos.CENTER);
        cardView.setPrefSize(100, 120); 
        cardView.setStyle("-fx-border-color: #D3D3D3; -fx-border-width: 1; -fx-background-color: #FFFFFF; -fx-background-radius: 3; -fx-border-radius: 3;");

        Label name = new Label(card.getPokemon_Name());
        name.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        name.setWrapText(true); //wrapping for long name

        Label hp = new Label("HP: " + (int)card.getHP());
        hp.setFont(Font.font("Arial", 10));
        if (card.isKnockedOut()) {
            hp.setTextFill(Color.RED);
        }

        cardView.getChildren().addAll(name, hp);
        return cardView;
    }

    //hand card design
    private VBox createCardView(PokemonCard card) {
        VBox cardView = new VBox(2);
        cardView.setAlignment(Pos.CENTER);
        cardView.setPrefSize(100, 120); 
        cardView.setStyle("-fx-border-color: #D3D3D3; -fx-border-width: 1; -fx-background-color: #FFFFFF; -fx-background-radius: 3; -fx-border-radius: 3;");

        Label name = new Label(card.getPokemon_Name());
        name.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        name.setWrapText(true); 

        Label hp = new Label("HP: " + (int)card.getHP());
        hp.setFont(Font.font("Arial", 10));
        if (card.isKnockedOut()) {
            hp.setTextFill(Color.RED);
        }

        cardView.getChildren().addAll(name, hp);
        return cardView;
    }

    //tariner card design
    private VBox createTrainerCardView(PokemonCard card) {
        VBox cardView = new VBox(2);
        cardView.setAlignment(Pos.CENTER);
        cardView.setPrefSize(100, 120); 
        cardView.setStyle("-fx-border-color: #D3D3D3; -fx-border-width: 1; -fx-background-color: #FFFFFF; -fx-background-radius: 3; -fx-border-radius: 3;");

        Label name = new Label(card.getPokemon_Name());
        name.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        name.setWrapText(true); 

        Label eff = new Label("Effect: " + card.getEffect());
        eff.setFont(Font.font("Arial", 10));
        eff.setWrapText(true);//wrapping for long text
        cardView.getChildren().addAll(name, eff);
        return cardView;
    }

    
    
    public void applySnapshot(GameUI.Snapshot s) {
        //active card
        if (s.activeCard != null) {
            activeCardNameLabel.setText(s.activeCard.getPokemon_Name());
            activeCardHPLabel.setText("HP: " + (int)s.activeCard.getHP() + " / " + (int)s.activeCard.getHPOriginal());
            activeCardHPLabel.setTextFill(s.activeCard.isKnockedOut() ? Color.RED : Color.BLACK);

            String trainerName = s.activeCard.getTrainer_Name();
            if (trainerName != null && !trainerName.isEmpty()) {
                trainerLabel.setText("Trainer: " + trainerName);
            } else {
                trainerLabel.setText("Trainer: N/A");
            }
        } else {
            activeCardNameLabel.setText("N/A");
            activeCardHPLabel.setText("HP: N/A");
            activeCardHPLabel.setTextFill(Color.BLACK);
        }
        //trainer card
        trainerContainer.getChildren().clear();
        if (s.trainerCard != null) {
            trainerContainer.getChildren().add(createTrainerCardView(s.trainerCard));
        }

        //bench
        benchContainer.getChildren().clear();
        for (PokemonCard c : s.bench) {
            benchContainer.getChildren().add(createBenchCardView(c));
        }
    
        //statistics
        handSizeLabel.setText("Hand: " + s.handSize);
        deckSizeLabel.setText("Deck: " + s.deckSize);
        discardPileSizeLabel.setText("Discard: " + s.discardPileSize);

        handContainer.getChildren().clear();
        for (PokemonCard c : s.hand) {
            handContainer.getChildren().add(createCardView(c));
        }

    }
    
}