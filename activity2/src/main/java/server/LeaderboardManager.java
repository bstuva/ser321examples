package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import client.Player;

public class LeaderboardManager {
    private List<Player> leaderboard;

    public LeaderboardManager() {
        leaderboard = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        leaderboard.add(player);
    }

    public List<Player> getLeaderboard() {
        // Sort the leaderboard by wins in descending order
        Collections.sort(leaderboard, Comparator.comparingInt(Player::getWins).reversed());
        return leaderboard;
    }
    public String printLeaderboard() {

        String leaderboardString = "";

        System.out.println("Leaderboard:");
        for (Player player : leaderboard) {
            leaderboardString = player.getName() + " - Wins: " + player.getWins() + "\n";
        }

        return leaderboardString;
    }
    public Player getPlayerByName(String name) {
        for (Player player : leaderboard) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null; // Player not found
    }

}