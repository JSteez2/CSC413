package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.factory.BoardFactory;
import edu.sfsu.csc413.chess.model.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the bordered text view, with typed input and no terminal. */
class BoxedViewTest {

    @Test
    @DisplayName("plays Fool's Mate to the end through the controller")
    void playsFoolsMate() {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        Scanner typed = new Scanner("f2f3\ne7e5\ng2g4\nd8h4\n");
        BoxedView view = new BoxedView(new PrintStream(printed), typed);

        new GameController(new Game(), view).start();

        String output = printed.toString();
        assertTrue(output.contains("Checkmate. BLACK wins."), output);
    }

    @Test
    @DisplayName("draws the board from the side to move")
    void drawsFromTheSideToMove() {
        ByteArrayOutputStream printedForWhite = new ByteArrayOutputStream();
        new BoxedView(new PrintStream(printedForWhite), new Scanner(""))
                .render(new Game(BoardFactory.standard(), Color.WHITE));
        String whiteBoard = printedForWhite.toString();

        ByteArrayOutputStream printedForBlack = new ByteArrayOutputStream();
        new BoxedView(new PrintStream(printedForBlack), new Scanner(""))
                .render(new Game(BoardFactory.standard(), Color.BLACK));
        String blackBoard = printedForBlack.toString();

        // White sees rank 8 at the top; Black sees rank 1 at the top.
        assertTrue(whiteBoard.indexOf("8 |") < whiteBoard.indexOf("1 |"), whiteBoard);
        assertTrue(blackBoard.indexOf("1 |") < blackBoard.indexOf("8 |"), blackBoard);
    }
}
