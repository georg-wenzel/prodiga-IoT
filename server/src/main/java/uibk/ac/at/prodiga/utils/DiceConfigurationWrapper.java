package uibk.ac.at.prodiga.utils;

import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Dice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class DiceConfigurationWrapper {

    private Dice dice;
    private int currentSide;
    private Map<Integer, BookingCategory> completedSides;
    private UUID feedId;

    public DiceConfigurationWrapper() {
        completedSides = new HashMap<>();
    }

    public Dice getDice() {
        return dice;
    }

    public void setDice(Dice dice) {
        this.dice = dice;
    }

    public int getCurrentSide() {
        return currentSide;
    }

    public void setCurrentSide(int currentSide) {
        this.currentSide = currentSide;
    }

    public Map<Integer, BookingCategory> getCompletedSides() {
        return completedSides;
    }

    public void setCompletedSides(Map<Integer, BookingCategory> completedSides) {
        this.completedSides = completedSides;
    }

    public UUID getFeedId() {
        return feedId;
    }

    public void setFeedId(UUID feedId) {
        this.feedId = feedId;
    }
}
