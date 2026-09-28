package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

	private final Color color;
	private final PieceType type;

	protected Piece(Color color, PieceType type) {
		this.color = color;
		this.type = type;
	}

	public Color color() {
		return color;
	}

	public PieceType type() {
		return type;
	}


