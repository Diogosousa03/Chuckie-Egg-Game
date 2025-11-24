import pt.isel.canvas.*

data class Hen(
    val cell: Cell,
    val faced: Direction,
    val speed: Speed,

    )

fun Game.checkHenCollision():Boolean {
    for (i in this.hen) {
        if (i == man.pos.toCell()) {
            return true
        }
    }
    return false
}

