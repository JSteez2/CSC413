package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The pawn: moves forward, captures diagonally, may move two squares from its
 * starting rank, and promotes on the last rank. En passant is not handled here.
 */
public class Pawn extends Piece {

    /** What a pawn may become on reaching the far rank. */
    private static final PieceType[] PROMOTION_CHOICES = { PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT };

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public List<Move> pseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        int direction = color().pawnDirection();

        // Forward one, onto an empty square only.
        Position oneStep = from.offsetOrNull(0, direction);
        if (oneStep != null && board.pieceAt(oneStep) == null) {
            addAdvance(moves, from, oneStep, null);

            // Forward two from the starting rank, only if both squares are empty.
            if (from.rank() == color().pawnStartRank()) {
                Position twoStep = from.offsetOrNull(0, 2 * direction);
                if (twoStep != null && board.pieceAt(twoStep) == null) {
                    moves.add(Move.quiet(from, twoStep, this));
                }
            }
        }

        // Diagonal captures, onto an enemy only.
        for (int fileOffset : new int[] { -1, 1 }) {
            Position target = from.offsetOrNull(fileOffset, direction);
            if (target == null) {
                continue;
            }
            Piece occupant = board.pieceAt(target);
            if (occupant != null && occupant.color() != color()) {
                addAdvance(moves, from, target, occupant);
            }
        }
        return moves;
    }

    /** Adds a move to {@code to}; on the last rank it becomes four promotions. */
    private void addAdvance(List<Move> moves, Position from, Position to, Piece captured) {
        if (to.rank() == color().promotionRank()) {
            for (PieceType choice : PROMOTION_CHOICES) {
                moves.add(Move.promotion(from, to, this, captured, choice));
            }
        } else if (captured == null) {
            moves.add(Move.quiet(from, to, this));
        } else {
            moves.add(Move.capture(from, to, this, captured));
        }
    }

    /**
     * A pawn attacks the two squares diagonally ahead of it, whether or not
     * anything stands there. It does not attack the square straight ahead.
     */
    @Override
    public boolean attacks(Board board, Position from, Position target) {
        int direction = color().pawnDirection();
        for (int fileOffset : new int[] { -1, 1 }) {
            Position diagonal = from.offsetOrNull(fileOffset, direction);
            if (target.equals(diagonal)) {
                return true;
            }
        }
        return false;
    }
}
