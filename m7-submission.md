# M7 Submission

## 1. Group

Alone.

## 2. Tools

Claude (Anthropic), model Claude Opus 5.5, used as a coding agent on my
repository. It read `AGENTS.md` (through the one-line `CLAUDE.md`) and
wrote the views on the run branches.

## 3. Other view

`boxed`: `java -cp target/classes edu.sfsu.csc413.chess.Main boxed`
(`BoxedView.java`, chosen in `Main.java:22`).

## 4. Runs

Each run is its own branch starting from `main`. All four were done in one
conversation with the agent rather than four fresh sessions; `AGENTS.md`
was the only thing I changed between the Swing runs.

| Branch | Request | Change from the previous run |
|---|---|---|
| `m7-run-1` | "Add a graphical 2D view of the board using Swing. I want to play by clicking a piece and then clicking the square to move it to. Show whose turn it is and messages like check or illegal moves, with an Undo button. Follow AGENTS.md." | first run: `AGENTS.md` as committed in `f8c108a` |
| `m7-run-2` | the same request | `AGENTS.md:18-20`: "Write it simply, the way a student would: one class unless a second is clearly needed, plain loops and `if`/`else`, and descriptive camelCase names." |
| `m7-run-3` | the same request | `AGENTS.md:21-22`: "When a piece is selected, mark the squares it can move to. Get them from `game.legalMoves()`; don't work them out." |
| `m7-run-4` | "Add a second view: a text board with a border, drawn from the side to move, chosen with the word boxed on the command line. Use only the JDK. Follow AGENTS.md." | a different view; `AGENTS.md` unchanged from `main` |

## 5. `AGENTS.md`

I added the rule at `AGENTS.md:18-20` ("Write it simply... one class unless
a second is clearly needed"). Run 1 built the board out of 64 `SquarePanel`
objects, an inner class (`m7-run-1:SwingView.java:232`), plus three more
panels just for the coordinates. It worked, but it was more code than the
job needs and more than I want to explain at the defense. With the rule,
run 2 drew the whole board in one `paintComponent` loop in one class.

I also used the corrected modal-dialog rule from session 17 §5
(`AGENTS.md:33-36`), so that a promotion dialog is allowed while
`controller.runLoop()` still is not.

## 6. Review

All line numbers are in `src/main/java/edu/sfsu/csc413/chess/view/SwingView.java` on `main`.

1. **`start`, lines 105-112.** Stores the controller, then uses
   `SwingUtilities.invokeLater` to draw the board and show the window, and
   returns. It never calls `controller.runLoop()`. *Checked:* searched the
   file for `runLoop` (no matches), and played Fool's Mate in the window
   without the `UnsupportedOperationException` from session 18. *Kept.*
2. **Turning a click into a square, lines 156-157.** `file = x / SQUARE_SIZE`
   and `rank = 7 - y / SQUARE_SIZE`. Screen rows count down and ranks count
   up, which is the bug from session 18 §5. *Checked:* clicked e2 then e4;
   the top label changed to `BLACK to move` and the pawn was on e4, not e5
   or e7. *Kept.*
3. **The legal-move dots, lines 188-198 and 219.**
   `squaresSelectedPieceCanReach` keeps the `to()` square of each move in
   `game.legalMoves()` whose `from()` is the selected square; the view never
   works out a move itself. *Checked:* after 1. e4 e5 I selected the f1
   bishop and the dots were on e2, d3, c4, b5 and a6, its five legal
   squares; searched the file for `game.play` and `undoLastMove` (no
   matches). *Kept.*
4. **Promotion, lines 121-141.** The controller asks `promotionChoice` for
   any four-letter move it can't find (`GameController.java:100`), including
   an illegal `e2e5`. This method opens the dialog only if one of
   `game.legalMoves()` is a promotion from `moveFromSquare` to
   `moveToSquare`; otherwise it answers queen and the controller reports the
   illegal move. *Checked:* `e2e5` showed `Not a legal move: 'e2e5'.` with
   no dialog. Then I played e2e4, d7d5, e4d5, c7c6, d5c6, g8f6, c6b7, b8d7
   and moved the pawn from b7 to a8: the dialog opened with Queen, Rook,
   Bishop and Knight, and choosing Rook put a white rook on a8. *Kept* (it
   came from run 1, see 8).
5. **`close`, lines 144-149.** The controller calls `close()` when the game
   ends. Instead of closing the window, it stops taking clicks and disables
   Undo, so the final position and `Checkmate. BLACK wins.` stay on screen.
   *Checked:* after Fool's Mate the window stayed open, the board ignored
   clicks, and Undo was greyed out. *Kept.*
6. **Line 262.** `piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE`
   uses the full package name because `java.awt.Color` is imported for
   drawing. It is long but correct, and it compares the enum, not a string.
   *Kept.*

## 7. Comparison

I compared my three Swing runs (`m7-run-1`, `-2`, `-3`).

- **Where the half-finished move lives.** In all three it is a field in the
  view, never in the model or controller: `selected` in run 1
  (`m7-run-1:SwingView.java:57`), `selectedSquare` in run 2
  (`m7-run-2:SwingView.java:51`) and run 3 (`m7-run-3:SwingView.java:57`).
  Run 1's 64 `SquarePanel`s each read that one field when they repaint
  (`m7-run-1:SwingView.java:251`).
- **How each finds legal moves.** Run 2 doesn't look: it sends the move to
  the controller (`m7-run-2:SwingView.java:140`) and shows the controller's
  "Not a legal move" message. Run 1 reads `game.legalMoves()` only to decide
  whether to show the promotion dialog (`m7-run-1:SwingView.java:145`). Run 3
  reads `game.legalMoves()` to draw the dots
  (`m7-run-3:SwingView.java:153-163`).
- **Promotion.** Run 1 overrides `promotionChoice` with a four-button dialog,
  shown only for real promotions (`m7-run-1:SwingView.java:143-162`). Runs 2
  and 3 don't override it, so they get `BoardView`'s default
  (`BoardView.java:70`): always a queen.
- **Easier to change, and which I kept.** Run 3. One class and one paint
  loop (`SwingView.java:215-249` on `main`), so the colours, the dots, or the
  side the board is drawn from are all changed in one place. In run 1 the
  drawing is in the inner class and the board's order is set in the
  constructor's grid loop (`m7-run-1:SwingView.java:65-72`), so a change like
  drawing from black's side touches both. Run 2 is as simple as run 3 but
  gives no sign of where a piece can go.

## 8. Applied

From `m7-run-1`, the promotion dialog that only appears for a real
promotion. In my code: `promotionChoice` at `SwingView.java:114-141`, and
the two fields it reads, `moveFromSquare` and `moveToSquare`, set at lines
177-178 just before the move is sent. Commit `47193fa`.

## 9. Debugger evidence

[Fill in after doing it in IntelliJ. I found no real bug in the views, so
this follows one move through the click handler.]

Breakpoint on `SwingView.java:163` (`boolean clickedOwnPiece = ...`), then
Debug `Main` with the program argument `swing`, and click e2.

- I predicted: `file` = ___, `rank` = ___, `clickedSquare` = ___,
  `clickedOwnPiece` = ___, and that `selectedSquare` would become e2.
- I saw: `x` = ___, `y` = ___, `file` = ___, `rank` = ___,
  `clickedSquare` = ___, `clickedOwnPiece` = ___.
- Then I resumed and clicked e4. At line 182 `moveNotation` was ___.

## 10. By hand

[Fill in after your own commit.] Commit `_______` ("M7 by hand: ___").
What I changed: ___. To make it, I read ___.
