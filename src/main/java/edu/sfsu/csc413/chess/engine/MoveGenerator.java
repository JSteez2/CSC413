package edu.sfsu.csc413.chess.engine;

import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the moves a side may play in a given position.
 *
 * <p>This class exists because of a question no single piece can answer. A
 * piece knows how it moves; it does not know the rule that applies to every
 * piece at once: <strong>you may not leave your own king attacked.</strong>
 * That rule needs the whole board, and it arrives in M5. This week the class
 * is where the generation loop lives, moved out of {@code Game}, so that when
 * the rule arrives it has a home.
 *
 * <p>Notice what this class is not. It holds no state: no board, no side to
 * move, no history. Everything it needs arrives as a parameter, so its methods
 * are {@code static} and it cannot be instantiated. Ask it the same question
 * about the same board twice and you get the same answer.
 *
 * <p>Nothing outside the {@code engine} package needs this class. The view and
 * {@code Main} talk to {@link Game}, which decides what "legal" means. That is
 * why one of the two methods below has no access modifier.
 */
public final class MoveGenerator {

    private MoveGenerator() {
    }

    /**
     * Every move {@code color} may legally play in the given position.
     *
     * <p>A pseudo-legal move is kept only if it does not leave {@code color}'s
     * own king in check. The only way to find out is to try it: each candidate
     * is applied to the board, checked, and undone before the next one is
     * tried, so the caller's board is exactly as it was when this returns.
     * {@code Game.legalMoves()} calls this and did not change when it tightened.
     */
    public static List<Move> legalMoves(Board board, Color color) {
        List<Move> safeMoves = new ArrayList<>();
        // An empty list to collect the moves that pass the king-safety test.
        for (Move triedMove : pseudoLegalMoves(board, color)) {
            // Try every move the pieces say they can make.
            board.apply(triedMove);
            // Play the move on the board for a moment.
            boolean kingLeftExposed = isInCheck(board, color);
            // After playing it, is our own king attacked?
            board.undo(triedMove);
            // Take the move back right away, before deciding anything.
            if (!kingLeftExposed) {
                // The king is safe after this move...
                safeMoves.add(triedMove);
                // ...so it is a legal move: keep it.
            }
        }
        return safeMoves;
        // Every move we kept; the board is back the way we found it.
    }

    /**
     * How the game stands for the side to move: still going, in check,
     * checkmate, or stalemate.
     */
    public static GameStatus status(Board board, Color sideToMove) {
        boolean isInCheck = isInCheck(board, sideToMove);
        boolean hasLegalMove = !legalMoves(board, sideToMove).isEmpty();

        if (isInCheck && hasLegalMove) {
            return GameStatus.CHECK;
        } else if (isInCheck && !hasLegalMove) {
            return GameStatus.CHECKMATE;
        } else if (!isInCheck && !hasLegalMove) {
            return GameStatus.STALEMATE;
        } else {
            return GameStatus.IN_PROGRESS;
        }
    }

    /**
     * True if {@code color}'s king is attacked by the other colour.
     *
     * <p>A side with no king on the board is never in check. Tests build
     * positions without kings all the time, so that is a normal case.
     */
    public static boolean isInCheck(Board board, Color color) {
        Position whereKingStands = board.kingPosition(color);
        // Find where this colour's king is standing.
        if (whereKingStands == null) {
            // No king on the board at all...
            return false;
            // ...so nothing can be checking it.
        }
        Color opponentColor = color.opposite();
        // The side that could be attacking our king.
        return isAttacked(board, whereKingStands, opponentColor);
        // In check means: the enemy attacks the king's square.
    }

    /**
     * True if any piece of colour {@code attacker} attacks {@code target}.
     *
     * <p>"Attacks", not "could move there": a pawn moves straight ahead but
     * attacks only diagonally. Each piece is asked through
     * {@code Piece.attacks}, which {@code Pawn} overrides, so this method never
     * asks what kind of piece it is looking at.
     */
    public static boolean isAttacked(Board board, Position target, Color attacker) {
        for (Position enemySquare : board.positionsOf(attacker)) {
            // Look at every square that holds one of the attacker's pieces.
            Piece enemyPiece = board.pieceAt(enemySquare);
            // Get the piece standing there.
            if (enemyPiece.attacks(board, enemySquare, target)) {
                // Ask the piece itself whether it attacks the target square.
                return true;
                // One attacker is enough.
            }
        }
        return false;
        // No piece of that colour attacks the target.
    }

    /**
     * Every geometrically possible move for {@code color}, king safety aside.
     *
     * <p>This is the loop from session 6 §3 and M3's {@code Game.legalMoves()},
     * moved here unchanged. Note how little it knows: it never asks what kind
     * of piece it is looking at. It asks each piece for its own moves.
     *
     * <p>Package-private on purpose. Only {@code Game} and this class's tests,
     * both in {@code engine}, may call it. A view that wants the moves asks
     * {@code Game}.
     */
    static List<Move> pseudoLegalMoves(Board board, Color color) {
        List<Move> moves = new ArrayList<>();
        // An empty list to collect every move we find.
        for (Position from : board.positionsOf(color)) {
            // Each square that holds one of this colour's pieces.
            moves.addAll(board.pieceAt(from).pseudoLegalMoves(board, from));
            // Ask that piece for its own moves, and add them all.
        }
        return moves;
        // Hand back everything we collected.
    }
}
