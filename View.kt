import pt.isel.canvas.*

// Dimensions of the sprites in the images files
// Floor, Egg, Food and Stair are 1x1 ; Man is 1x2 ; Hen is 1x3 or 2x2
const val SPRITE_WIDTH = 24  // [pixels in image file]
const val SPRITE_HEIGHT = 16 // [pixels in image file]

// Dimensions of the Arena grid
const val GRID_WIDTH = 20
const val GRID_HEIGHT = 24

// Dimensions of each cell of the Arena grid
const val VIEW_FACTOR = 2 // each cell is VIEW_FACTOR x sprite
const val CELL_WIDTH = VIEW_FACTOR * SPRITE_WIDTH   // [pixels]
const val CELL_HEIGHT = VIEW_FACTOR * SPRITE_HEIGHT  // [pixels]



/**
 * Creates a canvas with the dimensions of the arena.
 */
fun createCanvas() = Canvas(GRID_WIDTH * CELL_WIDTH, GRID_HEIGHT * CELL_HEIGHT, BLACK)

/**
 * Draw horizontal and vertical lines of the grid in arena.
 */
fun Canvas.drawGridLines() {
    (0 ..< width step CELL_WIDTH).forEach { x -> drawLine(x, 0, x, height, WHITE, 1) }
    (0 ..< height step CELL_HEIGHT).forEach { y -> drawLine(0, y, width, y, WHITE, 1) }
}

/**
 * Represents a sprite in the image.
 * Example: Sprite(2,3,1,1) is the man facing left.
 * @property row the row of the sprite in the image. (in sprites)
 * @property col the column of the sprite in the image. (in sprites)
 * @property height the height of the sprite in the image. (in sprites)
 * @property width the width of the sprite in the image. (in sprites)
 */
data class Sprite(val row: Int, val col: Int, val height: Int = 1, val width: Int = 1)

/**
 * Draw a sprite in a position of the canvas.
 * @param pos the position in the canvas (top-left of base cell).
 * @param spriteRow the row of the sprite in the image.
 * @param spriteCol the column of the sprite in the image.
 * @param spriteHeight the height of the sprite in the image.
 * @param spriteWidth the width of the sprite in the image.
 */
fun Canvas.drawSprite(pos: Point, s: Sprite) {
    val x = s.col * SPRITE_WIDTH + s.col + 1  // in pixels
    val y = s.row * SPRITE_HEIGHT + s.row + s.height
    val h = s.height * SPRITE_HEIGHT
    val w = s.width * SPRITE_WIDTH
    drawImage(
        fileName = "chuckieEgg|$x,$y,$w,$h",
        xLeft = pos.x,
        yTop = pos.y - (s.height-1) * CELL_HEIGHT,
        width = CELL_WIDTH * s.width,
        height = CELL_HEIGHT * s.height
    )
}

/**
 * Draw all the elements of the game.
 */
fun Canvas.drawGame(game: Game) {
    erase()
    //drawGridLines()
    game.floor.forEach { drawSprite(it.toPoint(), Sprite(0,0)) }
    game.stairs.forEach { drawSprite(it.toPoint(), Sprite(0,1)) }
    game.food.forEach { drawSprite(it.toPoint(), Sprite(1,0)) }
    game.egg.forEach { drawSprite(it.toPoint(), Sprite(1,1)) }
    game.hen.forEach { drawSprite(it.toPoint(), Sprite(0,5,3,1)) }
    showTime(game)
    showScore(game)
    drawMan(game.man)
    endGame(game)
}

/**
 * Gets the sprite of the man
 * @return the current sprite when man is walking to the Left
 */
private fun getLeftSprite(state: animations): Sprite {
    return when (state) {
        animations.MOVE1 -> Sprite(2, 2, 2)
        animations.MOVE2 -> Sprite(2, 4, 2)
        animations.MOVE3 -> Sprite(2, 3, 2)
    }
}
/**
 * Gets the sprite of the man
 * @return the current sprite when man is walking to the right
 */
private fun getRightSprite(state: animations): Sprite {
    return when (state) {
        animations.MOVE1 -> Sprite(0, 2, 2)
        animations.MOVE2 -> Sprite(0, 4, 2)
        animations.MOVE3 -> Sprite(0, 3, 2)
    }
}

/**
 * Gets the sprite of the man
 *  * @return the current sprite when man is going up the stairs
 */
private fun getUpSprite(state: animations): Sprite {
    return when (state) {
        animations.MOVE1 -> Sprite(4, 1, 2)
        animations.MOVE2 -> Sprite(4, 2, 2)
        animations.MOVE3 -> Sprite(4, 0, 2)

    }
}
/**
 * Changes the state of the man
 * @return the new state
 */
fun Game.changeState(): animations {
    return when {
        (man.climbing&&man.pos.x % 8 == 0) && man.animationState != animations.MOVE2-> animations.MOVE2
        (man.pos.x % 4 == 0 && man.animationState != animations.MOVE3 && man.animationState != animations.MOVE2 && !checkFall()) -> animations.MOVE3
        else -> animations.MOVE1
    }//|| stairCheck(man.pos)&& man.pos.y % 8 == 0     ||(stairCheck(man.pos)
}
/**
 * Draws the sprite of the man
 * @param receives the current man
 */
fun Canvas.drawMan(m: Man) {
    val sprite = when (m.faced) {
        Direction.LEFT -> if (m.speed.dx == 0) Sprite(2, 3, 2) else getLeftSprite(m.animationState)
        Direction.RIGHT -> if (m.speed.dx == 0) Sprite(0, 3, 2) else getRightSprite(m.animationState)
        Direction.UP -> if (m.speed.dy == 0) Sprite(4, 0, 2) else getUpSprite(m.animationState)
        Direction.DOWN -> if (m.speed.dy == 0) Sprite(4, 0, 2) else getUpSprite(m.animationState)
    }
    drawSprite(m.pos, sprite)
}



