package pokemon.simulation.agents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import pokemon.simulation.Deck;
import pokemon.simulation.GameState;
import pokemon.simulation.HandPlayer;
import pokemon.simulation.PokemonCard;

public class PlayerAgent extends Agent {
    private HandPlayer playerHand;
    private PokemonCard activeCard;
    private AID opponent;
    private boolean isFirstPlayer;
    private Deck deck;
    private int mulliganCount = 0; 
    private List<PokemonCard> discardPile;
    private List<PokemonCard> bench;
    private boolean itemCardsBlockedForOpponent = false; 
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private boolean gameStarted = false; 

    @Override
    protected void setup() {
        Object[] args = getArguments();
        playerHand = (HandPlayer) args[0];
        deck = (Deck) args[1];
        discardPile = new ArrayList<>();
        bench = new ArrayList<>();

        String myName = getLocalName();
        isFirstPlayer = "Player1".equals(myName);
        opponent = new AID(isFirstPlayer ? "Player2" : "Player1", AID.ISLOCALNAME);
        String playername = deck.getCards().isEmpty()
                ? "UnknownTrainer"
                : deck.getCards().get(0).getTrainer_Name();

        System.out.println(myName + "(" + playername + ")" + " is preparing his/her opening hand...");

        //send a message to the GameMaster to request the opening hand
        ACLMessage initialHandRequest = new ACLMessage(ACLMessage.REQUEST);
        initialHandRequest.setContent("INITIAL_HAND_REQUEST");
        initialHandRequest.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
        send(initialHandRequest);

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    String senderName = msg.getSender().getLocalName();


                    switch (content) {
                        case "PROCESS_INITIAL_HAND":
                            //PlayerAgent receives the go to process his opening hand
                            processInitialHand();
                            break;
                        case "REDRAW_HAND": 
                            System.out.println(getLocalName() +"("+playername+")" + ": Received request to redraw hand.");
                            redrawHand(); //call the redrawHand method
                            //after redrawing, we need to process the hand again to check for valid active card
                            ACLMessage reProcessHandMsg = new ACLMessage(ACLMessage.INFORM);
                            reProcessHandMsg.setContent("PROCESS_INITIAL_HAND");
                            reProcessHandMsg.addReceiver(getAID()); 
                            send(reProcessHandMsg);
                            break;
                        case "READY": 
                            //one player has a valid hand and declare to be ready 
                            if (senderName.equals("Player1")) {
                                player1Ready = true;
                            } else if (senderName.equals("Player2")) {
                                player2Ready = true;
                            }
                            checkGameStartConditions(); 
                            break;
                        
                        case "GO": 
                        //starting the game managed by players
                        if (!gameStarted) {
                            gameStarted = true; 
                            if (isFirstPlayer) {
                                System.out.println("-------------------------------------------------");
                                System.out.println(getLocalName() + "(" + playername + ")" + ": Start of firts round. \n");
                                System.out.println("My Active Card: " + activeCard + "👾\n");

                                drawPhase();
                                selectBenchCardPhase();
                                trainerPhase();//select trainer card
                                sendAttack();//active card send attack

                                System.out.println("END TURN: " + playername + "\n");
                                System.out.println("-------------------------------------------------");
                            } else {
                                System.out.println(getLocalName() + "(" + playername + ")" + ": Waiting for the turn of Player1.\n");
                            }
                        } else {
                            System.out.println(getLocalName() +"("+playername+")" + ": Received START, but game already started. Ignore.");
                        }
                        break;
                        case "END_GAME": //message from the GameMaster to end the game
                            System.out.println(getLocalName() +"("+playername+")" + ": Received end of game message. Termination.");
                            doDelete();
                            return; 
                        default: //core of the game cycle, deals with the actual actions of the players
                            if (content.startsWith("TRAINER_EFFECT:")) {//if opponent has a card that do somthing to the other player
                                handleOpponentTrainerEffect(msg);
                            }

                            else if (content.startsWith("ATTACK")) {
                                System.out.println("TURN OF " + getLocalName() + "(" + playername + ")");
                          
                                System.out.println("My Active card is: " + (activeCard != null ? activeCard : "N/A") + "👾 \n");

                                drawPhase(); //I draw at the beginning of my turn (after being attacked)
                           
                                double dmg = Double.parseDouble(content.split(":")[1]);
                                if (activeCard != null) { //manage a possible error of null Active Card(should not happen)
                                    activeCard.receiveDamage(dmg);
                                    System.out.println(getLocalName() + "(" + playername + ")" + ": " + activeCard.getPokemon_Name() + " receives " + dmg + " damage💥 \n HP remaining: " + activeCard.getHP() + "🛡️");

                                    if (activeCard.isKnockedOut()) {
                                        System.out.println(getLocalName() +"("+playername+")" + ": " + activeCard.getPokemon_Name() + " is KO!");
                                        discardPile.add(activeCard);//the active card that goes KO must be placed in the discard pile
                                        
                                        GameState.getInstance().updatePlayerStats(getLocalName(),playerHand.getRemainingCards().size(),deck.getCards().size(), discardPile.size());//update statistics
                                                      
                                        //pokemon with a higher general strength are selected to become the next active pokemon
                                        double max = 0.0;
                                        PokemonCard best = null;
                                        //check the cards(Pokémon) in the hand
                                        for (PokemonCard card : playerHand.getRemainingCards()) {
                                            if (card.isPokemon(card.getType_Card()) && card.getHP() > 0) { 
                                                double strength = (card.getAttack() + card.getHP()) / 2.0;
                                                if (strength > max) {
                                                    max = strength;
                                                    best = card;
                                                }
                                            }
                                        }

                                        //check the cards(Pokémon) in the bench
                                        for (PokemonCard card : bench) {
                                            if (card.isPokemon(card.getType_Card()) && card.getHP() > 0) { 
                                                double strength = (card.getAttack() + card.getHP()) / 2.0;
                                                if (strength > max) {
                                                    max = strength;
                                                    best = card;
                                                }
                                            }
                                        }

                                        //the best card is removed from the hand or the bench depending on where it was taken
                                        boolean removed = false;
                                        if (best != null) { 
                                        //removes from hand
                                        for (Iterator<PokemonCard> it = playerHand.getRemainingCards().iterator(); it.hasNext(); ) {
                                            PokemonCard card = it.next();
                                            if (card == best) {
                                                it.remove();
                                                System.out.println("Removed by hand: " + best.getPokemon_Name());
                                                removed = true;
                                                break;
                                            }
                                        }

                                        //removes from bench
                                        if (!removed) {
                                            for (Iterator<PokemonCard> it = bench.iterator(); it.hasNext(); ) {
                                                PokemonCard card = it.next();
                                                if (card == best) {
                                                    it.remove();
                                                    System.out.println("Removed by bench: " + best.getPokemon_Name());
                                                    removed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        }

                                        activeCard = best;
                                        GameState.getInstance().updatePlayer(getLocalName(), activeCard, bench);
                                        GameState.getInstance().updatePlayerStats( getLocalName(),playerHand.getRemainingCards().size(),deck.getCards().size(),discardPile.size());

                                        if (activeCard == null) {
                                            System.out.println(getLocalName() + "(" + playername + ")" + " LOST. No Pokemon cards left.");
                                            String opponentAgent  = opponent.getLocalName();  
                                            GameState.PlayerData oppData  = GameState.getInstance().getPlayerData(opponentAgent);
                                            String opponentTrainer  = oppData.activeCard.getTrainer_Name();      
                                            System.out.println(opponentAgent + "(" + opponentTrainer + ")" + " WIN!");
                                            GameState.getInstance().setWinner(opponentTrainer, playername);
                                            //notify gamemaster of defeat
                                            ACLMessage defeat = new ACLMessage(ACLMessage.INFORM);
                                            defeat.setContent("DEFEAT");
                                            defeat.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                                            send(defeat);
                                            
                                            return;
                                        } else {
                                            System.out.println(getLocalName() + "(" + playername + ")" + " select new Active Card: " + activeCard.getPokemon_Name());
                                        }
                                    }
                                } else {
                                    System.err.println(getLocalName() +"("+playername+")" + ": Active Card null.");
                                    //notify gamemaster of defeat
                                    ACLMessage defeat = new ACLMessage(ACLMessage.INFORM);
                                    defeat.setContent("DEFEAT");
                                    defeat.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                                    send(defeat);
                                    
                                    return;
                                }

                                selectBenchCardPhase();
                          
                                trainerPhase();
                       
                                sendAttack(); 
                       
                                System.out.println("END TURN: " + playername + "\n");
                                System.out.println("-------------------------------------------------");

                            } else if (content.startsWith("BLOCK_ITEM_CARDS_NEXT_TURN:")) { 
                                if (msg.getSender().getLocalName().equals(opponent.getLocalName())) {
                                    itemCardsBlockedForOpponent = true;
                                    System.out.println(getLocalName() +"("+playername+")" + ": The opponent's Item cards are blocked for my next turn.");
                                }
                            } else {
                                System.out.println(getLocalName() +"("+playername+")" + ": Unrecognised message: " + content);
                            }
                            break;
                    }
                } else {
                    block();
                }
            }

            private void processInitialHand() {
                GameState.getInstance().updatePlayerHand(getLocalName(),playerHand.getRemainingCards());
                activeCard = playerHand.chooseActiveCard();
                System.out.println(getLocalName() +"("+playername+")" + " (Mulligan: " + mulliganCount + ")");
                playerHand.printHand();

                if (activeCard == null) {
                    System.out.println(getLocalName() +"("+playername+")" + ": I do not have a valid card in my hand. I request a mulligan.");
                    mulliganCount++;
                    //send message to GameMaster to ask a mulligan
                    ACLMessage mulliganRequest = new ACLMessage(ACLMessage.REQUEST);
                    mulliganRequest.setContent("MULLIGAN_REQUEST");
                    mulliganRequest.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                    send(mulliganRequest);
                } else {
                    System.out.println(getLocalName() +"("+playername+")" + ": I have selected: " + activeCard.getPokemon_Name());
                    GameState.getInstance().updatePlayer(getLocalName(), activeCard, bench);
                    GameState.getInstance().updatePlayerHand(getLocalName(),playerHand.getRemainingCards());
                    GameState.getInstance().updatePlayerStats(getLocalName(),playerHand.getRemainingCards().size(),deck.getCards().size(),discardPile.size());


                    if (isFirstPlayer) {
                        player1Ready = true;
                    } else { 
                        player2Ready = true;
                    }

                    //say to the other player that I'm ready
                    ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
                    ready.setContent("READY");
                    ready.addReceiver(opponent); 
                    send(ready);

                    checkGameStartConditions(); 
                }
            }

            private void checkGameStartConditions() {
                if (player1Ready && player2Ready && !gameStarted) {
                    System.out.println(getLocalName() +"("+playername+")" + ": I'm ready with valid hand.");


                    ACLMessage start = new ACLMessage(ACLMessage.INFORM);
                    start.setContent("START");
                    start.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                    send(start);
                    
                    
                }
            }

            private void trainerPhase() {
                //take first available trainer card in the hand
                List<PokemonCard> trainerCards = playerHand.getTrainerCardsInHand();
                if (!trainerCards.isEmpty()) {
                    PokemonCard chosenTrainerCard = trainerCards.get(0); 
                    //if it is an “Item” and blocked card, do not play it
                    if ("Item".equalsIgnoreCase(chosenTrainerCard.getType_Card()) && itemCardsBlockedForOpponent) {
                        System.out.println(getLocalName() +"("+playername+")" + ": Impossible to play " + chosenTrainerCard.getPokemon_Name() + " (Item Card blocked).");
                        return; 
                    }

                    System.out.println(getLocalName() +"("+playername+")" + ": I have selected Trainer card: " + chosenTrainerCard.getPokemon_Name() + "✨");
                    playerHand.removeCard(chosenTrainerCard);
                    discardPile.add(chosenTrainerCard); //put used Trainer card in the discard pile

                    GameState.getInstance().updateTrainerCard(getLocalName(), chosenTrainerCard);
                    GameState.getInstance().updatePlayerHand(getLocalName(),playerHand.getRemainingCards());
                    GameState.getInstance().updatePlayerStats(getLocalName(),playerHand.getRemainingCards().size(),deck.getCards().size(),discardPile.size());

                    //respond to the effect
                    applyTrainerEffect(chosenTrainerCard);
                    GameState.getInstance().updatePlayerHand(getLocalName(),playerHand.getRemainingCards());
                    
                } else {
                    System.out.println(getLocalName() +"("+playername+")" + ": No Trainer cards to be played this turn.\n");
                }

                
            }
            private void applyTrainerEffect(PokemonCard card) {
                String azione = card.getAzione();
                String chi = card.getChi();
                String aChi = card.getAChi();
                String cosa = card.getCosa();
                String azione2 = card.getAzione2();
                String effectDescription = card.getEffect();

                if(effectDescription==null){
                    System.out.println("Trainer/Special Attack effect not recognised or not implemented");
                }else{
                    System.out.println(getLocalName() +"("+playername+")" + ": Apply Trainer/Special Attack effect - Action: " + azione+ ", Who: " + chi + ", To who: " + aChi + ", What: " + cosa + ", Action 2: " + azione2);
                    System.out.println("Effect description: " + effectDescription + "\n");

                    switch (azione) {
                        case "play": 
                            if ("opponent".equals(chi) && "player".equals(aChi) && "Item card".equals(cosa) && effectDescription.contains("cant")) {
                                ACLMessage blockItemsMsg = new ACLMessage(ACLMessage.INFORM);
                                blockItemsMsg.setContent("BLOCK_ITEM_CARDS_NEXT_TURN:true");
                                blockItemsMsg.addReceiver(opponent);
                                send(blockItemsMsg);
                                System.out.println(getLocalName() +"("+playername+")" + ": The opponent cannot play Item cards in his next turn.");
                            }else{
                                System.out.println(getLocalName() +"("+playername+")" + ": Trainer/Special Attack effect not recognised or not implemented: " + azione);
                            }
                            break;

                        case "discard+draw": 
                            if ("hand".equals(chi) && "player".equals(aChi)) {
                                System.out.println(getLocalName() + "("+playername+")" + ": I discard all the cards in my hand and draw cards.");
                                
                                for (PokemonCard c : new ArrayList<>(playerHand.getRemainingCards())) {
                                    discardPile.add(c);

                                }
                                
                                int numCardsToDraw=7;
                                playerHand.removeAllCards();
                                for (int i = 0; i < numCardsToDraw; i++) {
                                    PokemonCard takedeckcard = deck.drawCard();
                                    if (takedeckcard == null) {
                                        System.err.println("No more cards in the deck, so I cannot draw.");
                                    } else {
                                        playerHand.addCard(takedeckcard);
                                    }
                                }
                            }
                            playerHand.printHand();
                            break;
                        case "shuffle":
                            if("combination".equals(chi)){
                                
                                List<PokemonCard> toRecover = new ArrayList<>();
                                int maxToRecover = 1;
                                if (effectDescription.contains("up to 3")) {
                                    maxToRecover = 3;
                                } else if (effectDescription.contains("up to 2")) {
                                    maxToRecover = 2;
                                }
                                
                                //defines up to how many cards to retrieve from discard pile
                                for (PokemonCard c : discardPile) {
                                    if ((card.isPokemon(c.getType_Card()) || c.getType_Card().equalsIgnoreCase("Energy")) && toRecover.size() < maxToRecover) {
                                        toRecover.add(c);
                                    }
                                }

                                for(PokemonCard c : toRecover){
                                    discardPile.remove(c);
                                    deck.addCard(c); 
                                }

                                deck.Shuffle();

                            }
                            if ("shuffle_deck".equals(azione2)) {
                                deck.Shuffle();
                                System.out.println(getLocalName() +"("+playername+")" + ": Shuffled deck.");
                            }
                            break;

                        case "search": 
                            if ("deck".equals(chi) && "player".equals(aChi)) {
                                System.out.println(getLocalName() +"("+playername+")" + ": Looking in the deck for " + cosa + "...");
                                List<PokemonCard> foundCards = new ArrayList<>();
                                int numCardsToSearch = 1; 
                                if (cosa.contains("up to 2 Basic Energy")) { 
                                    numCardsToSearch = 2;
                                } 

                                for (int i = 0; i < numCardsToSearch; i++) {
                                    PokemonCard foundCard = deck.searchCard(cosa); 

                                    if (foundCard != null) {
                                        foundCards.add(foundCard);
                                        System.out.println(getLocalName() +"("+playername+")" + ": Found " + foundCard.getPokemon_Name());

                                        if ((cosa.equalsIgnoreCase("Basic Pokémon")||cosa.equalsIgnoreCase("Pokémon") || cosa.contains("card")) && !effectDescription.contains("bench")) {
                                            playerHand.addCard(foundCard);
                                        }

                                        if ((cosa.equalsIgnoreCase("Basic Pokémon")||cosa.equalsIgnoreCase("Pokémon") ) && !effectDescription.contains("hand")) { 
                                            if (bench.size() < 5) { 
                                                bench.add(foundCard);
                                                System.out.println(getLocalName() +"("+playername+")" + ": " + foundCard.getPokemon_Name() + " benched.");
                                            } else {
                                                System.out.println(getLocalName() +"("+playername+")" + ": Full bench, impossible to put " + foundCard.getPokemon_Name() + ".");
                                            }
                                        }
                                    } else {
                                        System.out.println(getLocalName() +"("+playername+")" + ": No card found for " + cosa + ".");
                                        break; 
                                    }
                                }
                                if ("shuffle_deck".equals(azione2)) {
                                    deck.Shuffle();
                                    System.out.println(getLocalName() +"("+playername+")" + ": Shuffled deck.");
                                }
                            }
                            break;

                        case "put": 
                            if (("Pokémon".equalsIgnoreCase(chi) || "energy".equalsIgnoreCase(chi)) && "player".equalsIgnoreCase(aChi) && effectDescription.contains("discard pile into")) {
                                System.out.println(getLocalName()+"("+playername+")"  + ": I search the discard pile for " + chi + " or " + cosa + "...");
                                //retrieves the first Pokémon or Energy card from the discard pile.
                                Optional<PokemonCard> recoveredCard = discardPile.stream()
                                        .filter(c -> c.getType_Card().equalsIgnoreCase("Pokémon") || c.getType_Card().equalsIgnoreCase("Energy"))
                                        .findFirst(); 

                                if (recoveredCard.isPresent()) {
                                    PokemonCard cardToRecover = recoveredCard.get();
                                    discardPile.remove(cardToRecover);
                                    playerHand.addCard(cardToRecover);
                                    System.out.println(getLocalName() +"("+playername+")" + ": Retrieved " + cardToRecover.getPokemon_Name() + " from dicard pile to hand.");
                                } else {
                                    System.out.println(getLocalName() +"("+playername+")" + ": No Pokémon or Energy cards found in the discard pile.");
                                }
                            }else {
                                System.out.println(getLocalName() +"("+playername+")" + ": Trainer/Special Attack effect not recognised or not implemented: " + azione);
                            }
                            break;

                        case "switch": 
                            if ("opponent".equalsIgnoreCase(chi) && "player".equalsIgnoreCase(aChi) ) {
                                ACLMessage switchMsg = new ACLMessage(ACLMessage.INFORM);
                                switchMsg.setContent("TRAINER_EFFECT:SWITCH_OPPONENT_POKEMON:" + cosa); //pass target of switching to opponent
                                switchMsg.addReceiver(opponent);
                                send(switchMsg);
                                System.out.println(getLocalName() +"("+playername+")" + ": Requesting the opponent to exchange the active Pokémon.");
                            }else if(("Pokémon".equalsIgnoreCase(chi) && "player".equalsIgnoreCase(aChi) ) || ("card".equalsIgnoreCase(chi) && "player".equalsIgnoreCase(aChi) )){
                                if (!bench.isEmpty()) {
                                    double maxm = 0.0;
                                    PokemonCard toSwitchIn = null;
                                    for (PokemonCard cardtoswitch : bench) {
                                        if (cardtoswitch.isPokemon(cardtoswitch.getType_Card()) && cardtoswitch.getHP() > 0) { 
                                            double strength = (cardtoswitch.getAttack() + cardtoswitch.getHP()) / 2.0;
                                            if (strength > maxm) {
                                                maxm = strength;
                                                toSwitchIn = cardtoswitch;
                                            }
                                        }
                                    }
                                    //for simplicity, it takes the first Pokémon on the bench 
                                    //PokemonCard toSwitchIn = bench.get(0);

                                    //Switch the two cards
                                    bench.add(activeCard); 
                                    activeCard = toSwitchIn; 
                                    bench.remove(toSwitchIn); 

                                    System.out.println(getLocalName() +"("+playername+")" + ": I did the switch. New active Pokémon: " + activeCard.getPokemon_Name());
                                } else {
                                    System.out.println(getLocalName() +"("+playername+")" + ": No bench Pokémon available for the switch.");
                                }
                            }
                            break;

                        case "be": //add HP to the active card
                            if ("Basic".equalsIgnoreCase(chi) && "Pokémon".equalsIgnoreCase(cosa)) {
                                if (activeCard != null && activeCard.isPokemon(activeCard.getType_Card())) { 
                                    activeCard.setHP(activeCard.getHP() + 50.0); 
                                    System.out.println(getLocalName() +"("+playername+")" + ": Applied to " + activeCard.getPokemon_Name() + ". HP increased.");
                                } else {
                                    System.out.println(getLocalName() +"("+playername+")" + ": The effect cannot be applied.");
                                }
                            }
                            break;

                        default:
                            System.out.println(getLocalName() +"("+playername+")" + ": Trainer/Special Attack effect not recognised or not implemented: " + azione);
                            break;
                    }}
            }

            private void sendAttack() {
                System.out.println(activeCard.getPokemon_Name() + " attacks the opponent with " + activeCard.getName_Attack() + "->" + activeCard.getAttack() +"\n");
                if(activeCard.getAttack()==0 &&
                        "do_damage100".equals(activeCard.getAzione())){
                    activeCard.setAttack(100.0);
                }
                ACLMessage attack = new ACLMessage(ACLMessage.INFORM);
                attack.setContent("ATTACK:" + activeCard.getAttack());
                attack.addReceiver(opponent);
                send(attack);

            }

            //decided whether to put pokemon on the bench
            private void selectBenchCardPhase() {
                List<PokemonCard> handCards = new ArrayList<>(playerHand.getRemainingCards());

                for (PokemonCard card : handCards) {
                    if (card.isPokemon(card.getType_Card())) {
                        if (bench.size() >= 5) {
                            System.out.println(getLocalName() +"("+playername+")" + ": Full bench, cannot add any more Pokémon.");
                            break;
                        }
                        //50% chance of placing the card on the bench
                        if (Math.random() < 0.5) {
                            bench.add(card);
                            playerHand.removeCard(card);
                            System.out.println(getLocalName() +"("+playername+")" + ": Added " + card.getPokemon_Name() + " to the bench.");
                        } else {
                            System.out.println(getLocalName() +"("+playername+")" + ": I decided NOT to put " + card.getPokemon_Name() + " in the bench.");
                        }
                    }
                }
                printBench();
                GameState.getInstance().updatePlayer(getLocalName(), activeCard, bench);
                GameState.getInstance().updatePlayerStats(getLocalName(),playerHand.getRemainingCards().size(),deck.getCards().size(),discardPile.size());
            }

            private void printBench() {
                System.out.println(getLocalName()+"("+playername+")" + ": Current bench (" + bench.size() + "/5):");
                if (bench.isEmpty()) {
                    System.out.println(" - No Pokémon in the bench.");
                    System.out.println("\n");
                } else {
                    for (PokemonCard card : bench) {
                        System.out.println(" - " + card.getPokemon_Name() + " | Attack: " + card.getAttack() +" | HP: " + card.getHP() + " | Type: " + card.getType_Card());
                    }
                    System.out.println("\n");
                }
            }

            
            private void drawPhase() {
                PokemonCard drawn = deck.drawCard();
                itemCardsBlockedForOpponent = false;
                if (drawn != null) {
                    playerHand.addCard(drawn);
                    playerHand.printHand();
                    GameState.getInstance().updatePlayerHand(getLocalName(),playerHand.getRemainingCards());
                } else {
                    System.out.println(getLocalName() +"("+playername+")" + ": I have no more cards to draw");
                }
            }

            //method for redistributing cards
            public void redrawHand() {
                playerHand.clearHand();
                deck.resetAndShuffle(); 
                List<PokemonCard> newHandCards = deck.drawHand(7); 
                playerHand = new HandPlayer(newHandCards); //update the player's hand with new cards
                GameState.getInstance().updatePlayerHand(getLocalName(),playerHand.getRemainingCards());
                System.out.println(getLocalName() +"("+playername+")" + ": I redraw my hand of cards.");
            }

            private void handleOpponentTrainerEffect(ACLMessage msg) {
                String content = msg.getContent();

                if (content.startsWith("TRAINER_EFFECT:BLOCK_ITEM_CARDS_NEXT_TURN")) {
                    itemCardsBlockedForOpponent = true; 
                    System.out.println(getLocalName() +"("+playername+")" + ": Le carte Item dell'avversario sono bloccate per il prossimo turno.");
                }
                //exchanges an opponent's active Pokémon for the least powerful one on his bench
                else if (content.startsWith("TRAINER_EFFECT:SWITCH_OPPONENT_POKEMON:")) {
                    PokemonCard newActive = null;
                    if (!bench.isEmpty()) {

                        double min = 10000.0;
                        for (PokemonCard cardtoswitch : bench) {
                            if (cardtoswitch.isPokemon(cardtoswitch.getType_Card()) && cardtoswitch.getHP() > 0) { 
                                double strength = (cardtoswitch.getAttack() + cardtoswitch.getHP()) / 2.0;
                                if (strength < min) {
                                    min = strength;
                                    newActive = cardtoswitch;
                                }
                            }
                        }
                        //switch
                        bench.add(activeCard); 
                        activeCard = newActive; 
                        bench.remove(newActive); 
                        GameState.getInstance().updatePlayer(getLocalName(), activeCard, bench);
                        GameState.getInstance().updatePlayerStats(getLocalName(),playerHand.getRemainingCards().size(),deck.getCards().size(), discardPile.size());
                        System.out.println(getLocalName() +"("+playername+")" + ": The opponent forced me to switch my active Pokémon. My new active Pokémon is: " + activeCard.getPokemon_Name());
                        
                    } else {
                        System.out.println(getLocalName() +"("+playername+")" + ": The opponent tried to force me to switch, but I have no Pokémon on the bench.");
                    }
                }
                else {
                    System.out.println(getLocalName() +"("+playername+")" + ": Trainer/Special Attack effect not recognised or not implemented: " + content);
                }
            }
        });
    }
}