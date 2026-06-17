package pokemon.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Deck {
    private List<PokemonCard> originalCards; 
    private List<PokemonCard> currentCards; 

    public Deck(List<PokemonCard> cards) {
        this.originalCards = new ArrayList<>(cards);
        resetAndShuffle();//inizialize and shuffle during the creation 
    }


    public void resetAndShuffle() {
        this.currentCards = new ArrayList<>(originalCards); 
        Collections.shuffle(this.currentCards); 
    }
    public void Shuffle() {
        Collections.shuffle(this.currentCards);//shuffle the current card in the deck 
    }

    public List<PokemonCard> drawHand(int count) {
        List<PokemonCard> drawnHand = new ArrayList<>();
        for (int i = 0; i < count && !currentCards.isEmpty(); i++) {
            drawnHand.add(currentCards.remove(0));
        }
        return drawnHand;
    }

    //draw a card on the top of deck (return null if the deck is empty)
    public PokemonCard drawCard() {
        if (currentCards.isEmpty()) return null;
        return currentCards.remove(0);
    }

    public List<PokemonCard> getCards() {
        return currentCards;
    }


    public PokemonCard searchCard(String criteria) {
        Optional<PokemonCard> foundCard = Optional.empty();

        //way to serch a specific card in the deck
        for (Iterator<PokemonCard> iterator = currentCards.iterator(); iterator.hasNext(); ) {
            PokemonCard card = iterator.next();

            if (criteria.equalsIgnoreCase("Pokémon")) {
                if (card.isPokemon(card.getType_Card())) {
                    foundCard = Optional.of(card);
                    iterator.remove();
                    break;
                }
            } else if (criteria.equalsIgnoreCase("Energy")) {
                if (card.getType_Card() != null && card.getType_Card().equalsIgnoreCase("Energy")) {
                    foundCard = Optional.of(card);
                    iterator.remove();
                    break;
                }
            } else if (criteria.equalsIgnoreCase("Item card") || criteria.equalsIgnoreCase("Pokémon Tool card") || criteria.equalsIgnoreCase("Stadium card")) {
                if (card.getType_Card() != null && card.getType_Card().equalsIgnoreCase("Trainer") ) { //in this case we watch if is a trainer
                    foundCard = Optional.of(card);
                    iterator.remove();
                    break;
                }
            } 
            
            //other effects could be implemented in this section in the future
        }

        return foundCard.orElse(null);
    }

    public void addCard(PokemonCard card) {
        if (card != null) {
            currentCards.add(card);
            System.out.println("Card added in the deck: " + card.getPokemon_Name());
        }
    }
    
}