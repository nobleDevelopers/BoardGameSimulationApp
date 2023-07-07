package de.tabit.test.alexandria.engine.dummy;

import de.tabit.test.alexandria.engine.api.IAlexandriaGameEngine;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class DummyEngine implements IAlexandriaGameEngine {
    private final List<Player> players = new ArrayList<>();
    private final Map<Player, Integer> playerPositions = new HashMap<>();
    private final Map<Player, Boolean> jokerFlags = new HashMap<>();
    private final Map<Player, Boolean> skipTurnFlags = new HashMap<>();
    private final Set<Integer> bonusFields = new HashSet<>();
    private final Set<Integer> trapFields = new HashSet<>();
    private final Random random = new Random();
    private boolean gameRunning = false;
    private AtomicInteger currentTurn = new AtomicInteger(0);

    @Override
    public String startGame(Integer numberOfPlayers) {
        players.clear();
        playerPositions.clear();
        bonusFields.clear();
        trapFields.clear();
        jokerFlags.clear();
        skipTurnFlags.clear();
        for (int i = 1; i <= numberOfPlayers; i++) {
            Player player = new Player("Player " + i);
            players.add(player);
            playerPositions.put(player, 0);
        }
        Set<Integer> specialFields = new HashSet<>();
        while (specialFields.size() < 10) {
            specialFields.add(random.nextInt(30) + 1);
        }
        Iterator<Integer> it = specialFields.iterator();
        for (int i = 0; i < 5; i++) {
            bonusFields.add(it.next());
            trapFields.add(it.next());
        }
        gameRunning = true;
        return "Game has been started with " + numberOfPlayers + " players.";
    }

    @Override
    public boolean gameIsRunning() {
        return gameRunning;
    }

    @Override
    public String nextPlayer() {
        Player nextPlayer = players.get(currentTurn.get());
        while (skipTurnFlags.getOrDefault(nextPlayer, false)) {
            skipTurnFlags.put(nextPlayer, false);
            currentTurn.set((currentTurn.get() + 1) % players.size());
            nextPlayer = players.get(currentTurn.get());
        }
        return nextPlayer.getName();
    }

    @Override
    public String nextPlayerTurn(Integer diceNumber) {
        Player currentPlayer = players.get(currentTurn.get());
        int currentPosition = playerPositions.get(currentPlayer);
        int newPosition = currentPosition + diceNumber;
        if (newPosition > 30) {
            gameRunning = false;
            return format("Congratulations! %s has won the game by reaching field %d.", currentPlayer.getName(), newPosition);
        }
        boolean hasActivatedSpecialField = false;
        String specialFieldMessage = "";
        if (bonusFields.contains(newPosition)) {
            int bonusType = random.nextInt(3) + 1;
            switch (bonusType) {
                case 1:
                    newPosition += 2;
                    specialFieldMessage = " has activated a Type 1 Bonus field and moved forward 2 additional fields.";
                    break;
                case 2:
                    players.stream()
                            .filter(player -> !player.equals(currentPlayer))
                            .forEach(player -> {
                                int _newPosition = Math.max(0, playerPositions.get(player) - 2);
                                playerPositions.put(player, _newPosition);
                            });
                    specialFieldMessage = " has activated a Type 2 Bonus field and moved all other players 2 fields back.";
                    break;

                case 3:
                    if (!jokerFlags.getOrDefault(currentPlayer, false)) {
                        jokerFlags.put(currentPlayer, true);
                        specialFieldMessage = " has activated a Type 3 Bonus field and received a Joker for the next trap.";
                    } else {
                        specialFieldMessage = " has activated a Type 3 Bonus field, but already used a Joker.";
                    }
                    break;
            }
            hasActivatedSpecialField = true;
        }
        if (trapFields.contains(newPosition) && !hasActivatedSpecialField) {
            int trapType = random.nextInt(3) + 1;
            switch (trapType) {
                case 1:
                    if (!jokerFlags.getOrDefault(currentPlayer, false)) {
                        newPosition = Math.max(0, newPosition - 2);
                        specialFieldMessage = " has activated a Type 1 Trap field and moved back 2 fields.";
                    }
                    else{
                        jokerFlags.put(currentPlayer, false);
                        specialFieldMessage = " has activated a Type 1 Trap field, but a Joker saved them.";
                    }
                    break;
                case 2:
                    if (!jokerFlags.getOrDefault(currentPlayer, false)) {
                        players.stream()
                                .filter(player -> !player.equals(currentPlayer))
                                .forEach(player -> {
                                    int _newPosition = Math.min(30, playerPositions.get(player) + 2);
                                    playerPositions.put(player, _newPosition);
                                });
                        specialFieldMessage = " has activated a Type 2 Trap field and moved all other players 2 fields forward.";
                    } else {
                        jokerFlags.put(currentPlayer, false);
                        specialFieldMessage = " has activated a Type 2 Trap field, but a Joker saved them.";
                    }
                    break;

                case 3:
                    if (!jokerFlags.getOrDefault(currentPlayer, false)) {
                        skipTurnFlags.put(currentPlayer, true);
                        specialFieldMessage = " has activated a Type 3 Trap field and will skip the next round.";
                    } else {
                        jokerFlags.put(currentPlayer, false);
                        specialFieldMessage = " has activated a Type 3 Trap field, but a Joker saved them.";
                    }
                    break;
            }
        }
        playerPositions.put(currentPlayer, newPosition);
        currentTurn.set((currentTurn.get() + 1) % players.size());
        return format("%s moved from field %d to field %d%s", currentPlayer.getName(), currentPosition, newPosition, specialFieldMessage);
    }




    private class Player {
        private final String name;
        Player(String name) {
            this.name = name;
        }
        String getName() {
            return name;
        }
    }
}
