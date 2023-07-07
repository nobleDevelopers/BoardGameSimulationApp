import de.tabit.test.alexandria.engine.dummy.DummyEngine;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyEngineTest {

    @Test
    public void testStartGame() {
        DummyEngine engine = new DummyEngine();
        String gameInfo = engine.startGame(4);
        assertThat(gameInfo).isEqualTo("Game has been started with 4 players.");
    }

    @Test
    public void testNextPlayer() {
        DummyEngine engine = new DummyEngine();
        engine.startGame(3);

        String player1 = engine.nextPlayer();
        assertThat(player1).isEqualTo("Player 1");

        String player2 = engine.nextPlayer();
        assertThat(player2).isEqualTo("Player 2");

        String player3 = engine.nextPlayer();
        assertThat(player3).isEqualTo("Player 3");

        String player4 = engine.nextPlayer();
        assertThat(player4).isEqualTo("Player 1"); 
    }

    @Test
    public void testNextPlayerTurn() {
        DummyEngine engine = new DummyEngine();
        engine.startGame(2);

        String player = engine.nextPlayer();
        String turnInfo = engine.nextPlayerTurn(3);

        assertThat(turnInfo).startsWith(player + " moved from field");

        player = engine.nextPlayer();
        turnInfo = engine.nextPlayerTurn(4);

        assertThat(turnInfo).startsWith(player + " moved from field");
    }



}
