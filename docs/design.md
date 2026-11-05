# Design

Two diagrams of the engine as it stands after M5. Both are drawn from the code
in `src/main/java`; if the two disagree, the code is right and this file is stale.

## Class diagram: `engine` and `model`

The six piece subclasses are drawn without their methods: each one has the same
public constructor and overrides `pseudoLegalMoves`, and `Pawn` also overrides
`attacks`.

```mermaid
classDiagram
    direction TB

    namespace engine {
        class Game {
            -Board board
            -List~Move~ history
            -Color sideToMove
            +Game()
            +Game(Board, Color)
            +board() Board
            +sideToMove() Color
            +history() List~Move~
            +legalMoves() List~Move~
            +findLegalMove(String) Optional~Move~
            +play(Move) void
            +undoLastMove() Optional~Move~
        }
        class MoveGenerator {
            <<final>>
            -MoveGenerator()
            +legalMoves(Board, Color)$ List~Move~
            ~pseudoLegalMoves(Board, Color)$ List~Move~
            +isInCheck(Board, Color)$ boolean
            +isAttacked(Board, Position, Color)$ boolean
        }
    }

    namespace model {
        class Board {
            -Piece[][] squares
            +Board()
            +pieceAt(Position) Piece
            +isEmpty(Position) boolean
            +place(Position, Piece) void
            +positionsOf(Color) List~Position~
            +kingPosition(Color) Position
            +apply(Move) void
            +undo(Move) void
            +toString() String
        }
        class Piece {
            <<abstract>>
            -Color color
            -PieceType type
            #Piece(Color, PieceType)
            +color() Color
            +type() PieceType
            +symbol() char
            +pseudoLegalMoves(Board, Position)* List~Move~
            +attacks(Board, Position, Position) boolean
            #slidingMoves(Board, Position, int[][]) List~Move~
            #steppingMoves(Board, Position, int[][]) List~Move~
            +toString() String
        }
        class Pawn
        class Knight
        class Bishop
        class Rook
        class Queen
        class King
        class Move {
            <<record>>
            +Position from
            +Position to
            +Piece moved
            +Piece captured
            +PieceType promotesTo
            +quiet(Position, Position, Piece)$ Move
            +capture(Position, Position, Piece, Piece)$ Move
            +promotion(Position, Position, Piece, Piece, PieceType)$ Move
            +isCapture() boolean
            +isPromotion() boolean
            +toString() String
        }
        class Position {
            <<record>>
            +int file
            +int rank
            +parse(String)$ Position
            +offsetOrNull(int, int) Position
            +toString() String
        }
        class Color {
            <<enumeration>>
            WHITE
            BLACK
            +opposite() Color
            +pawnDirection() int
            +pawnStartRank() int
            +promotionRank() int
        }
        class PieceType {
            <<enumeration>>
            PAWN
            KNIGHT
            BISHOP
            ROOK
            QUEEN
            KING
            +symbol() char
            +fromSymbol(char)$ PieceType
        }
    }

    %% Inheritance: the six pieces are kinds of Piece
    Piece <|-- Pawn
    Piece <|-- Knight
    Piece <|-- Bishop
    Piece <|-- Rook
    Piece <|-- Queen
    Piece <|-- King

    %% What Game holds (fields) and what it only uses
    Game *-- "1" Board : board
    Game --> "0..*" Move : history
    Game --> "1" Color : sideToMove
    Game ..> MoveGenerator : calls legalMoves

    %% MoveGenerator holds nothing, so every arrow out of it is a dependency
    MoveGenerator ..> Board
    MoveGenerator ..> Piece
    MoveGenerator ..> Move
    MoveGenerator ..> Position
    MoveGenerator ..> Color

    %% The board holds pieces that outlive it (a captured piece lives on in Move)
    Board o-- "0..64" Piece : squares
    Board ..> Move : apply / undo
    Board ..> Position

    %% What a Move refers to
    Move --> "2" Position : from, to
    Move --> "1..2" Piece : moved, captured
    Move --> "0..1" PieceType : promotesTo

    %% What a Piece holds and uses
    Piece --> "1" Color : color
    Piece --> "1" PieceType : type
    Piece ..> Board
    Piece ..> Move : creates
```

## Sequence diagram: `game.legalMoves()` after M5

One `Piece` lifeline stands for every piece asked. The first loop is the M4
generation loop, unchanged; the second is the M5 filter. Every candidate is
undone before the next one is tried, so the board ends exactly as it started.

```mermaid
sequenceDiagram
    participant caller
    participant Game
    participant MoveGenerator
    participant Board
    participant Piece

    caller->>+Game: legalMoves()
    Game->>+MoveGenerator: legalMoves(board, sideToMove)

    MoveGenerator->>MoveGenerator: pseudoLegalMoves(board, color)
    MoveGenerator->>+Board: positionsOf(color)
    Board-->>-MoveGenerator: squares
    loop each square from
        MoveGenerator->>+Board: pieceAt(from)
        Board-->>-MoveGenerator: piece
        MoveGenerator->>+Piece: pseudoLegalMoves(board, from)
        Piece-->>-MoveGenerator: moves
    end

    loop each triedMove
        MoveGenerator->>+Board: apply(triedMove)
        Board-->>-MoveGenerator: 

        MoveGenerator->>MoveGenerator: isInCheck(board, color)
        MoveGenerator->>+Board: kingPosition(color)
        Board-->>-MoveGenerator: whereKingStands
        alt whereKingStands is null
            Note over MoveGenerator: no king, so not in check
        else king found
            MoveGenerator->>MoveGenerator: isAttacked(board, whereKingStands, color.opposite())
            MoveGenerator->>+Board: positionsOf(color.opposite())
            Board-->>-MoveGenerator: enemySquares
            loop each enemySquare, until one attacks
                MoveGenerator->>+Board: pieceAt(enemySquare)
                Board-->>-MoveGenerator: enemyPiece
                MoveGenerator->>+Piece: attacks(board, enemySquare, whereKingStands)
                Piece-->>-MoveGenerator: true / false
            end
        end

        MoveGenerator->>+Board: undo(triedMove)
        Board-->>-MoveGenerator: 

        alt king is not in check
            Note over MoveGenerator: keep: safeMoves.add(triedMove)
        else king is in check
            Note over MoveGenerator: drop triedMove
        end
    end

    MoveGenerator-->>-Game: safeMoves
    Game-->>-caller: safeMoves
```
