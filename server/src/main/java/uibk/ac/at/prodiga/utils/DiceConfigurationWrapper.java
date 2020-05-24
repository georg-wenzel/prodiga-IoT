package uibk.ac.at.prodiga.utils;

import org.javatuples.Pair;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Dice;

import java.util.*;


public class DiceConfigurationWrapper {

    private Dice dice;
    private int currentSide;
    private int currentSideFriendlyName;
    private Map<Integer, Pair<Integer, BookingCategory>> completedSides;
    private UUID feedId;
    private Map<Integer, Integer> visitedSites;

    public DiceConfigurationWrapper() {
        completedSides = new HashMap<>();
        visitedSites = new HashMap<>();
    }

    public Map<Integer, Integer> getVisitedSites() {
        return visitedSites;
    }

    public void setVisitedSites(Map<Integer, Integer> visitedSites) {
        this.visitedSites = visitedSites;
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

    public Map<Integer, Pair<Integer, BookingCategory>> getCompletedSides() {
        return completedSides;
    }

    public void setCompletedSides(Map<Integer, Pair<Integer, BookingCategory>> completedSides) {
        this.completedSides = completedSides;
    }

    public UUID getFeedId() {
        return feedId;
    }

    public void setFeedId(UUID feedId) {
        this.feedId = feedId;
    }

    public int getCurrentSideFriendlyName() {
        return currentSideFriendlyName;
    }

    public void setCurrentSideFriendlyName(int currentSideFriendlyName) {
        this.currentSideFriendlyName = currentSideFriendlyName;
    }
}
