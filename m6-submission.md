# M6 Submission

## 1. Who decides, who shows

Checkmate is detected in `MoveGenerator.status()`, which `Game.status()` calls. It goes there because of the first question, "Is it a rule?": being in check with no legal move is a rule of chess, so it belongs in the model next to `isInCheck()` and `legalMoves()`. It is announced in `GameController.announceResult()`, which runs at the end of `handle()` and tells the view with `showMessage()`. That is the third question, "Does it take a request, run the rule, and tell the view?", so it is controller code, and `ConsoleView` just prints the message it is given.

## 2. The dependency rule

Only `Main` imports `edu.sfsu.csc413.chess.view`, because it builds the `BoardView` and the `GameController`. `GameController`, `ConsoleView` and `EmojiView` are already inside the `view` package, so they don't need the import. Nothing in `engine`, `model` or `factory` imports it, and nothing there should: the rules must not depend on how the game is shown, or adding a new view would mean editing `Game` or `MoveGenerator`. The arrows only point one way, from the view to the engine.

## 3. Test 2 on your engine

I expect the diff to be one new file, a `SwingView` class in the `view` package that implements `BoardView`, plus one line in `Main` so it can choose the Swing view. `GameController` and everything in `engine` should not change. The controller only talks to the `BoardView` interface, a button click can call `handle()` with an action like `new PlayerAction.Undo()`, and the Swing view can override `promotionChoice()` to show a dialog without the controller knowing.
