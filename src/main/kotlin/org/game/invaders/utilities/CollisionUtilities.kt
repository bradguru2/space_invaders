package org.game.invaders.utilities

import org.game.invaders.Constants

object CollisionUtilities {

    @JvmStatic
    fun aabbOverlap(
        ax: Float,
        ay: Float,
        aw: Float,
        ah: Float,
        bx: Float,
        by: Float,
        bw: Float,
        bh: Float
    ): Boolean {
        return ax < bx + bw &&
                ax + aw > bx &&
                ay < by + bh &&
                ay + ah > by
    }

    @JvmStatic
    fun missileHitsEnemy(
        missileX: Float,
        missileY: Float,
        missileWidth: Float,
        missileHeight: Float,
        enemyX: Float,
        enemyY: Float,
        enemyW: Float,
        enemyH: Float,
    ): Boolean {
        return aabbOverlap(
            missileX,
            missileY,
            missileWidth,
            missileHeight,
            enemyX,
            enemyY,
            enemyW,
            enemyH
        )
    }

    @JvmStatic
    fun missileHitsPlayer(
        missileX: Float,
        missileY: Float,
        missileWidth: Float,
        missileHeight: Float,
        playerX: Float,
        playerY: Float,
        playerWidth: Float,
        playerHeight: Float,
    ) : Boolean {
        return aabbOverlap(
            missileX,
            missileY,
            missileWidth,
            missileHeight,
            playerX,
            playerY,
            playerWidth,
            playerHeight
        )
    }
}