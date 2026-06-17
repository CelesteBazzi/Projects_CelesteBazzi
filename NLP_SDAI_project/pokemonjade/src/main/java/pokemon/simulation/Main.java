package pokemon.simulation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import pokemon.simulation.agents.GameMasterAgent;
import pokemon.simulation.agents.PlayerAgent;
import pokemon.simulation.utils.JSONLoader;

public class Main {
public static void main(String[] args) throws Exception {
    List<PokemonCard> rawCards = JSONLoader.loadCards("src/main/resources/Pokemon.json");
    List<PokemonCard> allCards = new ArrayList<>();
    //I have a value that indicates the number of that same card in the deck, so I make them usable
    for (PokemonCard card : rawCards) {
        int copies = card.getN_cards();
        for (int i = 0; i < copies; i++) {
            allCards.add(new PokemonCard(card));
        }
    }
    Set<String> trainerNames = allCards.stream()
    .map(PokemonCard::getTrainer_Name)
    .collect(Collectors.toSet());


    List<String> trainers = new ArrayList<>(trainerNames);
    Collections.shuffle(trainers);

    //define the two deck of the players, present in the dataset
    List<PokemonCard> deck1Cards = allCards.stream()
    .filter(c -> c.getTrainer_Name().equals(trainers.get(0)))
    .limit(60).collect(Collectors.toList());


    List<PokemonCard> deck2Cards = allCards.stream()
    .filter(c -> c.getTrainer_Name().equals(trainers.get(1)))
    .limit(60).collect(Collectors.toList());

    Deck deck1 = new Deck(deck1Cards);
    Deck deck2 = new Deck(deck2Cards);

    HandPlayer hand1 = new HandPlayer(deck1.drawHand(7));
    HandPlayer hand2 = new HandPlayer(deck2.drawHand(7));

    


    jade.core.Runtime rt = jade.core.Runtime.instance();
    Profile p = new ProfileImpl();
    p.setParameter(Profile.MAIN_HOST, "localhost"); 
    p.setParameter(Profile.MAIN_PORT, "1099");      
    ContainerController cc = rt.createAgentContainer(p);


    //I pass the hand and the deck
    AgentController p1 = cc.createNewAgent("Player1",PlayerAgent.class.getName(),new Object[]{ hand1, deck1 });
    AgentController p2 = cc.createNewAgent("Player2",PlayerAgent.class.getName(),new Object[]{ hand2, deck2 });
    AgentController gm = cc.createNewAgent("GameMaster", GameMasterAgent.class.getName(), null);

    p1.start();
    p2.start();
    gm.start();
}
}