package com.axandar.makaoCore.logic;

import com.axandar.makaoCore.utils.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Axandar on 25.01.2016.
 */
public class Deck implements Serializable {

    private List<Card> deck = new ArrayList<>();
    private final String TAG = "Operation on deck";

    public Deck(){

    }

    public Deck(int _numberOfDecks, List<List<Function>> functionsList){
        for (int x = 0; x < _numberOfDecks; x++) {
            for (int i = 0; i < 13; i++) {
                for (int j = 0; j < 4; j++) {
                    deck.add(new Card(j, i+1, functionsList.get(i).get(j)));
                }
            }
        }
    }

    public void addCardToDeck(Card card){
        deck.add(card);
    }

    public Card getCardFromDeck(){
        int cardIndex =  ThreadLocalRandom.current().nextInt(0, deck.size());
        Logger.logConsole(TAG, "Take card from deck with index: " + cardIndex);
        Card card = deck.get(cardIndex);
        Logger.logConsole(TAG, "Take card from deck with name: " + card.getIdType() + "-" + card.getIdColor());
        deck.remove(cardIndex);
        return card;
    }

    public int deckLength(){
        return deck.size();
    }
}
