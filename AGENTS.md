# Instructions for coding agents

This is a chess engine built with MVC. The model (`model/`, `engine/`,
`factory/`) and the controller (`view/GameController.java`) are written by
hand and are finished. Your job is views only.

## Do

- Add views as new classes in `src/main/java/edu/sfsu/csc413/chess/view/`.
- Implement the `BoardView` interface. Read it, `ConsoleView`,
  `GameController`, and `PlayerAction` before writing anything.
- Read the game only through `Game`: `board()`, `sideToMove()`,
  `legalMoves()`, `status()`, `history()`.
- Send every player action to the controller as a `PlayerAction`:
  `controller.handle(new PlayerAction.Move("e2e4"))`.
- In `Main.main`, add one `else if` for the new view's command-line word,
  next to the existing `emoji` check. Change nothing else in `Main`.
- Write it simply, the way a student would: one class unless a second is
  clearly needed, plain loops and `if`/`else`, and descriptive camelCase
  names.

## Don't

- Don't change anything in `model/`, `engine/`, or `factory/`.
- Don't change `BoardView`, `PlayerAction`, `GameController`,
  `ConsoleView`, or `EmojiView`.
- Don't call `game.play`, `game.undoLastMove`, or anything that changes
  the game. Only the controller changes the game.
- Don't work out a chess rule in the view. Legal moves come from
  `game.legalMoves()`; check and checkmate come from `game.status()`.
- Don't wait for the next move inside the window: a window's `start`
  builds the window and returns; it never calls `controller.runLoop()`.
  A modal dialog that asks one question, such as a promotion piece, is
  fine.
- Don't add dependencies to `pom.xml`. Swing is part of the JDK.
- Don't change or delete any test.

## Check your work

- `./mvnw test` passes, with no test changed or deleted.
- `git diff --stat submit-m6` lists only new files under `view/` (and new
  tests under `src/test/java/edu/sfsu/csc413/chess/view/`), `Main.java`,
  and the M7 files at the top of the repository.
- Open the window and play Fool's Mate (`f2f3`, `e7e5`, `g2g4`, `d8h4`),
  one illegal move, and an Undo. If you could not open it, say so.

## Stop and ask

If you think a file under "Don't" has to change, stop and explain why
instead of changing it.
