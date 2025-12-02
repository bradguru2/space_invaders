package org.game.invaders

/**
 * Constants - define common constant values for project.
 */
object Constants {
    // Playfield
    const val DIVIDE_LINE_RATIO = 0.10f
    const val FONT_SCALE = 1.00f // Scale to Glyph windows and reserve space to avoid clipping

    // HUD
    const val HUD_HEIGHT_RATIO: Float = 0.10f
    const val RIGHT_PADDING_PX: Float = 20f

    // Colors
    const val TEXT_COLOR_R: Float = 1f
    const val TEXT_COLOR_G: Float = 0.647f
    const val TEXT_COLOR_B: Float = 0f

    const val FRAME_COLOR_R: Float = 0.5f
    const val FRAME_COLOR_G: Float = 0.5f
    const val FRAME_COLOR_B: Float = 0.0f

    const val PADDLE_COLOR_R: Float = 0.863f
    const val PADDLE_COLOR_G: Float = 0.816f
    const val PADDLE_COLOR_B: Float = 0.314f

    const val MISSILE_COLOR_R: Float = 0.2f
    const val MISSILE_COLOR_G: Float = 0.2f
    const val MISSILE_COLOR_B: Float = 0.2f

    // Paddle Size Ratios
    const val NORMAL_PLAYER_RATIO: Float = 0.15f   // 15% of window width
    const val SMALL_PLAYER_RATIO: Float = 0.075f    // 7.5% of window width
    const val PLAYER_HEIGHT_RATIO = 0.07f     // 2% of window height

    // Frame Size Rations
    const val TOP_FRAME_RATIO = 0.030f // 3.0 % of window height
    const val SIDE_FRAME_RATIO = 0.05f // 5 % of window width
    const val BOTTOM_FRAME_RATIO = 0.10f // 10 % of window height

    // Brick Size Ratio
    const val ENEMY_HEIGHT_RATIO = 0.07f
    const val ENEMY_WIDTH_RATIO = 1.21 * SIDE_FRAME_RATIO
    const val ENEMY_MARGIN_RATIO = 0.15f // In local space
    const val ENEMY_COLUMN_COUNT = 6
    const val ENEMY_ROW_COUNT = 6
    const val ENEMY_START_RATIO = 0.375f

    // Missile Constants
    const val BALL_START_RATIO = 0.50f
    const val MISSILE_HEIGHT_RATIO = 0.06f
    const val MISSILE_WIDTH_RATIO = 0.01f

    // Shader Resource Paths
    const val HUD_VERTEX_SHADER_PATH = "/shaders/hud_vertex.glsl"
    const val HUD_FRAGMENT_SHADER_PATH = "/shaders/hud_fragment.glsl"
    const val FRAME_VERTEX_SHADER_PATH = "/shaders/frame_vertex.glsl"
    const val FRAME_FRAGMENT_SHADER_PATH = "/shaders/frame_fragment.glsl"
    const val PADDLE_VERTEX_SHADER_PATH = "/shaders/player_vertex.glsl"
    const val PADDLE_FRAGMENT_SHADER_PATH = "/shaders/player_fragment.glsl"
    const val MISSILE_VERTEX_SHADER_PATH = "/shaders/missile_vertex.glsl"
    const val MISSILE_FRAGMENT_SHADER_PATH = "/shaders/missile_fragment.glsl"
    const val ENEMY_VERTEX_SHADER_PATH = "/shaders/enemy_vertex.glsl"
    const val ENEMY_FRAGMENT_SHADER_PATH = "/shaders/enemy_fragment.glsl"
}
