package edu.sfsu.csc413.chess.model;

/**
 * A single move: which piece went where, and what happened as a result.
 */
public record Move(Position from, Position to, Piece moved, Piece captured, PieceType promotesTo) {

    /** A move to an empty square. */
    public static Move quiet(Position from, Position to, Piece moved) {
        return new Move(from, to, moved, null, null);
    }

    /** A move that removes an enemy piece from the destination square. */
    public static Move capture(Position from, Position to, Piece moved, Piece captured) {
        return new Move(from, to, moved, captured, null);
    }

    /** A pawn reaching the far rank and becoming {@code promotesTo}. */
    public static Move promotion(Position from, Position to, Piece moved, Piece captured, PieceType promotesTo) {
        return new Move(from, to, moved, captured, promotesTo);
    }

    public boolean isCapture() {
        return captured != null;
    }

    public boolean isPromotion() {
        return promotesTo != null;
    }

    /** Long algebraic notation, e.g. "e2e4", or "e7e8q" for a promotion. */
    @Override
    public String toString() {
        String text = from.toString() + to.toString();
        if (isPromotion()) {
            text += Character.toLowerCase(promotesTo.symbol());
        }
        return text;
    }
}
