package edu.sfsu.csc413.chess;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.view.BoardView;
import edu.sfsu.csc413.chess.view.BoxedView;
import edu.sfsu.csc413.chess.view.ConsoleView;
import edu.sfsu.csc413.chess.view.EmojiView;
import edu.sfsu.csc413.chess.view.GameController;
import edu.sfsu.csc413.chess.view.SwingView;

/** Entry point: builds a game, a view, and a controller, then starts playing. */
public final class Main {

    public static void main(String[] args) {
        Game game = new Game();

        BoardView view;
        if (args.length > 0 && args[0].equals("emoji")) {
            view = new EmojiView();
        } else if (args.length > 0 && args[0].equals("swing")) {
            view = new SwingView();
        } else if (args.length > 0 && args[0].equals("boxed")) {
            view = new BoxedView();
        } else {
            view = new ConsoleView();
        }

        GameController controller = new GameController(game, view);
        controller.start();
    }

    private Main() {
    }
}
