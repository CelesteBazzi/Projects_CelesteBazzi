package pokemon.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HandPlayer {
    private List<PokemonCard> hand;
    private PokemonCard activeCard;

    public HandPlayer(List<PokemonCard> hand) {
        this.hand = new ArrayList<>(hand); 
    }
    //chooses and removes from the hand the first card with >0 attack
    public PokemonCard chooseActiveCard() {
        Iterator<PokemonCard> it = hand.iterator();
        while (it.hasNext()) {
            PokemonCard card = it.next();
            if (card.getAttack() > 0) {
                //removes from the hand
                it.remove();
                //I mark it as active and return it
                activeCard = card;
                return activeCard;
            }
        }
        return null; 
    }


    public void removeAllCards(){
        hand.clear();
        System.out.println("Removes all cards hand");
    }

    public boolean removeCard(PokemonCard card){
        return hand.remove(card);
    }

    public PokemonCard getActiveCard() {
        return activeCard;
    }

    public boolean hasPlayableCards() {
        return hand.stream().anyMatch(card -> card.getAttack() > 0);
    }

    public List<PokemonCard> getRemainingCards() {
        return hand;
    }


    public void addCard(PokemonCard card) {
        if (card != null) {
            hand.add(card);
            System.out.println("Card added in hand: " + card.getPokemon_Name() +"🃏");
        }
    }

    public void printHand() {
        System.out.println("Hand of cards " +
                (hand.isEmpty() ? "Empty hand" : hand.get(0).getTrainer_Name()) + ":");
        for (PokemonCard c : hand) {
            System.out.println(" - " + c.getPokemon_Name() + " | Attack: " + c.getAttack() + " | HP:" + c.getHP());
        }
        System.err.println("\n");
    }

    public void clearHand() {
        hand.clear();
        activeCard = null; //also reset the active card
    }

    //return the Trainer cards present in tha hand
    public List<PokemonCard> getTrainerCardsInHand() {
        List<PokemonCard> trainers = new ArrayList<>();
        for (PokemonCard card : hand) {
            if (card.getType_Card().equals("Trainer")) {
                trainers.add(card);
            }
        }
        return trainers;
    }

}