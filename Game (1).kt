/**
 * Represents the game action.
 */
enum class Action { WALK_LEFT, WALK_RIGHT, UP_STAIRS, DOWN_STAIRS, JUMP }
/**
 * Represents the game state.
 */
enum class GameState { Playing, Winner, Timeout, Loser}
/**
 * Represents the man animation.
 */
enum class animations {
    MOVE1, MOVE2,MOVE3
}

/**
 * Represents all game information.
 * @property man information about man
 * @property floor positions of floor cells
 * @property stairs positions of stairs cells
 */
data class Game(
    val man: Man,
    val floor: List<Cell>,
    val stairs: List<Cell>,
    val food : List<Cell>,
    val egg : List<Cell>,
    val score: Int = 0,
    val time: Int = 2666,
    val hen : List<Cell>,
    val currentGameState: GameState

)

/**
 * Loads a game from a file.
 * @param fileName the name of the file with the game information.
 * @return the game loaded.
 */

fun loadGame(fileName: String) :Game {
    val cells: List<CellContent> = loadLevel(fileName)

    return Game(
        man = createMan( cells.first { it.type==CellType.MAN }.cell ),
        floor = cells.ofType(CellType.FLOOR),
        stairs = cells.ofType(CellType.STAIR),
        egg = cells.ofType(CellType.EGG),
        food = cells.ofType(CellType.FOOD),
        hen = cells.ofType(CellType.HEN),
        currentGameState = GameState.Playing
    )
}

/**
 * Performs an action to the game.
 * If the action is null, returns current game.
 * @param action the action to perform.
 * @receiver the current game.
 * @return the game after the action performed.
 */
fun Game.doAction(action: Action?): Game {
    val updatedMan = when (action) {
        Action.WALK_LEFT ->
            if (floor.any {man.pos.toCell().col - 1 == it.col && man.pos.toCell().row == it.row }
                || egg.isEmpty() || time == 0 || checkHenCollision())
            {
            man
        } else {
            if (floorCheck() || stairCheck(man.pos)) {
                man.copy(
                    climbing = false,
                    faced = Direction.LEFT,
                    speed = changeSpeedX(-MOVE_SPEED),
                    manWillMove = true

                )
            } else {
                man
            }
        }

        Action.WALK_RIGHT ->
            if (floor.any {man.pos.toCell().col + 1 == it.col && man.pos.toCell().row == it.row }
                || egg.isEmpty() || time == 0|| checkHenCollision()) {
            man
        } else {
            if (floorCheck() || stairCheck(man.pos)) {   //println("K")
                man.copy(
                    climbing = false,
                    faced = Direction.RIGHT,
                    speed = changeSpeedX(MOVE_SPEED),
                    manWillMove = true

                )
            } else {
                man
            }
        }

        Action.UP_STAIRS ->
            if (egg.isEmpty() || time==0 || checkHenCollision()) {
                man
            } else {
                if (stairCheck(man.pos + Speed(0, -2*CELL_HEIGHT)) && !checkFall() &&man.pos.x%48==0) {
                    man.copy(
                        climbing = true,
                        faced = Direction.UP,
                        speed = changeSpeedY(-CLIMBING_SPEED),
                        manWillClimb = true
                    )
                } else {
                    man
                }
            }

        Action.DOWN_STAIRS ->
            if (egg.isEmpty() || time == 0 || checkHenCollision()) {
                man
            } else {
                if (stairCheck(man.pos + Speed(0,CELL_HEIGHT)) && !checkFall()&&man.pos.x%48==0) {
                    man.copy(
                        climbing = true,
                        faced = Direction.DOWN,
                        speed = changeSpeedY(CLIMBING_SPEED) ,
                        manWillClimb = true,

                        )
                } else {
                    man
                }
            }

        Action.JUMP ->
            if (egg.isEmpty() || time == 0 || checkHenCollision()) {
                man
            } else {
                if ((floorCheck()||checkStairFloor())&&!man.climbing) {
                    man.copy(
                        climbing = false,
                        speed = man.jump(),
                        manWillJump = true
                    )
                } else {
                    man
                }
            }
        else -> man.copy(speed =Speed(0,0))

    }
    return this.copy(man = updatedMan)
}

/**
 * It automatically updates the parameters in this function
 * @return Game that contains man.copy that will allow the change of certain parameters
 */
fun Game.stepFrame(): Game {
    // Creates a copy of man and updates the speed
    val newGame1 = copy(
        man = man.copy(
            speed = updateSpeed(),
        ),
    )
    return newGame1.copy(
        man = man.copy(
            pos =  newGame1.updateManPosition().limitToArea(MAX_X,MAX_Y), //Updates Mans position
            speed = newGame1.man.speed, //Takes the updated speed calculated in newGame1
            manWillJump = false,
            manWillMove = false,
            manWillClimb = false,
            animationState = changeState()
        ),
        floor = floor,
        stairs = stairs,
        food = checkFood(), //Updates Games food
        egg = checkEgg(),//Updates Games eggs
        score = score(),//Updates Games score
        time = time(),//Updates Games time
        hen = hen, //Updates Games hen
        currentGameState = currentGameState()
    )
}



