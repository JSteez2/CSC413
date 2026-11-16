package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A text board with a border around every square, drawn from the side to
 * move: when it is White's turn, rank 1 is at the bottom; when it is Black's
 * turn, the board turns around and rank 8 is at the bottom.
 *
 * <p>It reads moves from the keyboard like {@link ConsoleView}, so its
 * {@link #start} lets the controller drive with {@code runLoop()}. It only
 * reads the game; every move goes to the controller as a {@link PlayerAction}.
 */
public class BoxedView implements BoardView {

    private static final String BORDER_LINE = "  +---+---+---+---+---+---+---+---+";

    private final PrintStream out;
    private final Scanner in;

    /** A view reading from the keyboard and printing to the screen. */
    public BoxedView() {
        this(System.out, new Scanner(System.in));
    }

    /** A view with its own input and output, so a test can type and read. */
    public BoxedView(PrintStream out, Scanner in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void render(Game game) {
        boolean whiteAtBottom = game.sideToMove() == Color.WHITE;

        out.println();
        out.println(fileLetters(whiteAtBottom));
        out.println(BORDER_LINE);
        for (int row = 0; row < 8; row++) {
            int rank;
            if (whiteAtBottom) {
                rank = 7 - row;
            } else {
                rank = row;
            }

            String line = (rank + 1) + " |";
            for (int column = 0; column < 8; column++) {
                int file;
                if (whiteAtBottom) {
                    file = column;
                } else {
                    file = 7 - column;
                }
                Piece piece = game.board().pieceAt(new Position(file, rank));
                if (piece == null) {
                    line = line + "   |";
                } else {
                    line = line + " " + piece.symbol() + " |";
                }
            }
            out.println(line + " " + (rank + 1));
            out.println(BORDER_LINE);
        }
        out.println(fileLetters(whiteAtBottom));
        out.println();

        String statusLine = game.sideToMove() + " to move";
        if (game.status() != GameStatus.IN_PROGRESS) {
            statusLine = statusLine + "  |  " + game.status();
        }
        out.println(statusLine);
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    /** A keyboard can wait for the next move, so the controller drives the game. */
    @Override
    public void start(GameController controller) {
        controller.runLoop();
    }

    @Override
    public PlayerAction nextAction(Game game) {
        out.print("Your move (e.g. e2e4, 'undo', 'moves', 'quit'): ");
        out.flush();
        if (in.hasNextLine()) {
            return PlayerAction.parse(in.nextLine());
        }
        return PlayerAction.parse("quit");
    }

    /** The letters a to h, in the order the columns are drawn. */
    private static String fileLetters(boolean whiteAtBottom) {
        String letters = "   ";
        for (int column = 0; column < 8; column++) {
            char letter;
            if (whiteAtBottom) {
                letter = (char) ('a' + column);
            } else {
                letter = (char) ('h' - column);
            }
            letters = letters + " " + letter + "  ";
        }
        return letters;
    }
}
