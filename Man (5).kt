import pt.isel.canvas.*


// Speed of man in pixels per frame, in horizontal and vertical directions
const val GRAVITY = CELL_HEIGHT/SPRITE_HEIGHT
const val JUMP_SPEED = -CELL_HEIGHT/2
const val MOVE_SPEED = CELL_WIDTH / 6
const val CLIMBING_SPEED = CELL_HEIGHT / 4
//Canvas limits
const val MAX_X = (GRID_WIDTH - 1) * CELL_WIDTH
const val MAX_Y = (GRID_HEIGHT - 1) * CELL_HEIGHT


/**
 * Represents the Man in the game.
 * @property pos is the position in the board.
 * @property faced the direction the man is facing
 * @property speed velocidade do man
 * @property climbing boolean para indicar se o Man está a subir/descer as escadas
 */
data class Man(
    val pos: Point,
    val faced: Direction,
    val speed: Speed,
    val manWillMove: Boolean,
    val climbing: Boolean,
    val manWillJump: Boolean,
    val manWillClimb:Boolean,
    val animationState: animations
)

/**
 * Creates the Man in the cell
 */
fun createMan(cell: Cell) = Man(
    pos = cell.toPoint(),
    faced = Direction.LEFT,
    speed = Speed(0, 0),
    manWillMove = false,
    climbing = false,
    manWillJump = false,
    manWillClimb = false,
    animationState = animations.MOVE1,
)

/**
 * Changes the horizontal speed (dx) of the Man.
 * @param newdx the new horizontal speed value.
 * @return a new Speed object with the updated horizontal speed and the original vertical speed.
 */
fun Game.changeSpeedX(newdx: Int): Speed = Speed(newdx, man.speed.dy)

/**
 * Changes the vertical speed (dy) of the Man.
 * @param newdy the new speed vertical value.
 * @return a new Speed object with the updated vertical speed and the original horizontal speed.
 */
fun Game.changeSpeedY(newdy: Int): Speed = Speed(man.speed.dx, newdy)

/**
 * used to update Man's position.
 * @return the man with the updated position limited to the canvas area.
 */
fun Game.updateManPosition(): Point {
    return man.pos + Speed(man.speed.dx, man.speed.dy)
}


/** sets the jumping speed dy and dx(to do the arc jump)
 * if man's is on the borders dx becomes 0 and dy is the same
 * @return Speed dy and dx
 */
fun Man.jump(): Speed {
    val newdx = MOVE_SPEED * faced.dCol
    val newdy = JUMP_SPEED
    return when {
        (this.pos.x == 0 || this.pos.x == MAX_X) -> Speed(0, newdy)
        else -> Speed(newdx, newdy)
    }
}

/**
 * fun that adds gravity to dy making the man fall when he isn't on the floor
 * @return Speed making gravity work if there is no floor else it keeps dx and dy becomes 0
 * */

fun Game.manFall(): Speed {
    //Calculates the speed with gravity and limits it
    val updatedManSpeed = man.speed.copy(dy = man.speed.dy + GRAVITY).fallingSpeedLimit()

    // Calculates the next position and transforms it to cell
    val futureCell = (man.pos + updatedManSpeed ).toCell()

    // Checks if man will be on (or inside) the floor
    val checkFutureFloor = floor.any { (futureCell.row +1 == it.row) && (futureCell.col == it.col) }

    // ensures that the Man won't be inside the floor by subtracting the Man's y and the next Cell y, setting it to his
    // next speed (this will only happen if the next cell is a floor cell and Man's dy>0 to make sure that he is falling,
    // this way we make sure that he won't jump and clip to the floor upwards)
    return when{
        ((checkFutureFloor) && man.speed.dy>0) -> {
            man.speed.copy(dy = futureCell.toPoint().y - man.pos.y)}
        else ->updatedManSpeed
    }


}

/**
 * limits the falling velocity
 * @return the limited speed when the man is falling
 */
fun Speed.fallingSpeedLimit(): Speed {
    val maxFallingSpeed = -JUMP_SPEED
    return if (dy >= maxFallingSpeed) Speed(dx, maxFallingSpeed) else this
}

/**
 * checks if the man should fall
 * @return true if the man should fall
 */
fun Game.checkFall(): Boolean {
    return !floorCheck() && !man.climbing && !checkStairFloor()
}

/**
 * Decides which speed function to use depending on the current state of the game
 * @return returns one of the speed functions
 */
fun Game.updateSpeed(): Speed {
    return when {
        checkFall() -> manFall() //man isn't on a platform or stair, so he'll fall
        man.manWillJump -> man.speed //man is on the platform, about to jump
        man.manWillMove -> Speed(man.speed.dx, 0) //man is on the platform, about to move
        man.manWillClimb -> Speed(0, man.speed.dy) //man is on the stairs, about to climb
        man.climbing -> man.speed.stopIfInCell(man.pos) //man is climbing
        else -> Speed(this.man.speed.dx, 0).stopIfInCell(man.pos) //man is on the platform
    }
}

/**
 * Verifies if man is on a stair
 * @return true if man is on a stair
 */
fun Game.stairCheck(pos:Point): Boolean {
    return stairs.contains(pos.toCell())
}

/**
 * checks if there is ground below Man
 * @return true if there is ground below
 */
fun Game.floorCheck(): Boolean {
    return (floor.any { (man.pos.toCell().row + 1== it.row) && (man.pos.toCell().col == it.col)})
}
/**
 * checks if man is on stair cell where he is not supposed to fall
 * @return true if he isn't supposed to fall
 */
fun Game.checkStairFloor(): Boolean {
    return floor.any { man.pos.toCell().row + 1 == it.row && stairCheck(man.pos) }
}

/**
 * Removes the food that man took
 * @return a list without the food that man took
 */
fun Game.checkFood(): List<Cell> {
    return food.filter { it != man.pos.toCell() }
}
/**
 * Removes the eggs that man took
 * @return a list without the eggs that man took
 */
fun Game.checkEgg(): List<Cell> {
    return egg.filter { it != man.pos.toCell() }
}
/**
 * Shows the Win/loss message on the screen
 * @return if man won, "you won", if man lose "you loose"
 */
fun Canvas.endGame(game: Game){
    when {
        (game.egg.isEmpty()) -> drawText(194,380,"YOU WON", GREEN, 110)
        (game.time <= 0 || game.checkHenCollision()) -> drawText(194,380,"YOU LOSE", RED, 110)
        }
    }

/**
 * Checks the state of the game
 * @return if man won, Winner, if man lose TIMEOUT, if man is playing PLAYING
 */
fun Game.currentGameState():GameState{
    return when{
        (egg.isEmpty()) -> GameState.Winner
        (time <= 0) -> GameState.Timeout
        (checkHenCollision()) -> GameState.Loser
        else -> GameState.Playing
    }

}

/**
 * checks if the position of the man is the same as any food
 * @return boolean
 */
fun Game.checkScoreFood(): Boolean {
    for (i in this.food) {
        if (i == man.pos.toCell()) {
            return true
        }
    }
    return false
}

/**
 * checks if the position of the man is the same as an egg
 * @return boolean
 */
fun Game.checkScoreEgg(): Boolean {
    for (i in this.egg) {
        if (i == man.pos.toCell()) {
            return true
        }
    }
    return false
}

/**
 * updates man's score depending on what type of collectible he took
 * @return man's score
 */
fun Game.score(): Int {
    return when {
        checkScoreFood() -> score + 50
        checkScoreEgg() -> score + 100
        else -> score
    }
}

/**
 * shows the score on the canvas window
 * @return shows the score
 */
fun Canvas.showScore(game: Game) {
    drawText(47, 27, "Score: ${game.score}", YELLOW, CELL_HEIGHT)
}

/**
 * stops the time if Man wins or loses
 * @return stopped time
 */
fun Game.time(): Int {
    return  if (time == 0 || egg.isEmpty() || checkHenCollision()){
        time
    }else {
        time - 1
    }

}

/**
 * shows the time on the canvas window
 * @return shows the time
 */
fun Canvas.showTime(game: Game) {
    drawText(750, 27, "Time:${game.time}", YELLOW, CELL_HEIGHT)
}


