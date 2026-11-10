package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.PieceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Connects the player's decisions to the game: carry out what was asked, then
 * ask the view to redraw.
 *
 * <p>This is the C in MVC. It is the only class that knows about both the
 * {@link Game} and the {@link BoardView}. It holds no game state of its own:
 * the model owns the position, the view owns the display, and the controller
 * owns only the conversation between them.
 *
 * <p>When you are done, this class mentions no {@code Scanner}, no
 * {@code System.out}, no window, and none of the words "undo", "moves", or
 * "quit". Those belong to the view and to {@link PlayerAction#parse}.
 */
public class GameController {

    private final Game game;
    private final BoardView view;
    private boolean playerQuit = false;

    public GameController(Game game, BoardView view) {
        this.game = game;
        this.view = view;
    }

    /** The game being played, so a view can read it in order to draw it. */
    public Game game() {
        return game;
    }

    /**
     * Greets the player, draws the opening position, and hands control to the
     * view with {@link BoardView#start}.
     */
    public void start() {
        view.showMessage("Welcome to chess.");
        view.render(game);
        view.start(this);
    }

    /**
     * The ask-and-handle loop, for views that can wait for input.
     *
     * <p>Asks the view for the next action and handles it, until the player
     * quits or the game is over. A console view calls this from its
     * {@code start}; an event-driven view never calls it.
     */
    public void runLoop() {
        while (!playerQuit && !game.isGameOver()) {
            PlayerAction nextAction = view.nextAction(game);
            handle(nextAction);
        }
    }

    /**
     * Carries out one player action and redraws.
     *
     * <p>Every route into the game goes through here: {@link #runLoop()} calls
     * it, and so will a button's click handler in M7. The handout lists what
     * each of the four actions does and what the player is told.
     */
    public void handle(PlayerAction action) {
        switch (action) {
            case PlayerAction.Move move -> playMove(move.notation());
            case PlayerAction.Undo undo -> undoLastMove();
            case PlayerAction.ShowMoves showMoves -> showLegalMoves();
            case PlayerAction.Quit quit -> {
                playerQuit = true;
                view.showMessage("Goodbye.");
                view.close();
                return;
            }
        }

        view.render(game);

        if (game.isGameOver()) {
            announceResult();
            view.close();
        }
    }

    /** Plays the move the player typed, or tells them it is not legal. */
    private void playMove(String notation) {
        Optional<Move> foundMove = game.findLegalMove(notation);

        if (foundMove.isEmpty() && notation.length() == 4) {
            PieceType promotionPiece = view.promotionChoice(game);
            char promotionLetter = Character.toLowerCase(promotionPiece.symbol());
            foundMove = game.findLegalMove(notation + promotionLetter);
        }

        if (foundMove.isPresent()) {
            game.play(foundMove.get());
        } else {
            view.showMessage("Not a legal move: '" + notation + "'.");
        }
    }

    /** Takes back the last move, or says there is nothing to take back. */
    private void undoLastMove() {
        Optional<Move> undoneMove = game.undoLastMove();

        if (undoneMove.isEmpty()) {
            view.showMessage("Nothing to undo.");
        }
    }

    /** Shows every legal move, sorted, for example "20 legal moves: a2a3 a2a4 ...". */
    private void showLegalMoves() {
        List<String> moveNames = new ArrayList<>();
        for (Move legalMove : game.legalMoves()) {
            moveNames.add(legalMove.toString());
        }
        Collections.sort(moveNames);

        String message = moveNames.size() + " legal moves:";
        for (String moveName : moveNames) {
            message = message + " " + moveName;
        }
        view.showMessage(message);
    }

    /** Says who won, or that it is a draw. */
    private void announceResult() {
        if (game.status() == GameStatus.CHECKMATE) {
            Color winner = game.sideToMove().opposite();
            view.showMessage("Checkmate. " + winner + " wins.");
        } else {
            view.showMessage("Stalemate. The game is a draw.");
        }
    }
}
