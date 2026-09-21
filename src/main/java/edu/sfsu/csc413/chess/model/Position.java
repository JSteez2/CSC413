package edu.sfsu.csc413.chess.model;

public record Position(int file, int rank) {

    public static final int BOARD_SIZE = 8;

    public Position {
	if (file < 0 || file >= BOARD_SIZE || rank < 0 || rank >= BOARD_SIZE) {
	    throw new IllegalArgumentException("Position if off the board");
	}
    }

    public static Position parse(String square) {
	if (square == null || square.length() != 2) {
	    throw new IllegalArgumentException("Invalid position");
	}

	int file = square.charAt(0) - 'a';
	int rank = square.charAt(1) - '1';

	return new Position(file, rank);
    }

    public Position offsetOrNull(int fileOffset, int rankOffset) {
	int newFile = file + fileOffset;
	int newRank = rank + rankOffset;

	if (newFile < 0 || newFile > 7 || newRank < 0 || newRank > 7) {
	    return null;
	}

	return new Position(newFile, newRank);
  
    }

    @Override
    public String toString() {
	char fileLetter = (char) ('a' + file);
	char rankNumber = (char) ('1' + rank);

	return "" + fileLetter + rankNumber;
    }
}
