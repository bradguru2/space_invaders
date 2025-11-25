package org.game.invaders

/**
 * Constants - define common constant values for project.
 */
object Constants {
    // Playfield
    const val DIVIDE_LINE_RATIO = 0.10f

    // HUD
    const val HUD_HEIGHT_RATIO: Float = 0.05f
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

    const val BALL_COLOR_R: Float = 0.0f
    const val BALL_COLOR_G: Float = 0.25f
    const val BALL_COLOR_B: Float = 1.00f

    // Paddle Size Ratios
    const val NORMAL_PADDLE_RATIO: Float = 0.15f   // 15% of window width
    const val SMALL_PADDLE_RATIO: Float = 0.075f    // 7.5% of window width
    const val PADDLE_HEIGHT_RATIO = 0.03f     // 2% of window height
    const val PADDLE_MARGIN = 100f              // Margin from bottom of window in pixels

    // Frame Size Rations
    const val TOP_FRAME_RATIO = 0.030f // 3.0 % of window height
    const val SIDE_FRAME_RATIO = 0.05f // 5 % of window width
    const val BOTTOM_FRAME_RATIO = 0.10f // 10 % of window height

    // Brick Size Ratio
    const val BRICK_HEIGHT_RATIO = PADDLE_HEIGHT_RATIO
    const val BRICK_WIDTH_RATIO = 2 * SIDE_FRAME_RATIO // A brick is twice as wide as a side-frame
    const val BRICK_MARGIN_RATIO = 0.15f // In local space
    const val BRICK_COLUMN_COUNT = 9 // 9 * 0.10 = 0.90 = 1 - 2 * 0.05
    const val BRICK_ROW_COUNT = 8 // Standard break number of rows
    const val BRICK_START_RATIO = 0.55f

    // Ball Constants
    const val BALL_START_RATIO = 0.50f
    const val BALL_HEIGHT_RATIO = 0.03f

    // Shader Resource Paths
    const val HUD_VERTEX_SHADER_PATH = "/shaders/hud_vertex.glsl"
    const val HUD_FRAGMENT_SHADER_PATH = "/shaders/hud_fragment.glsl"
    const val FRAME_VERTEX_SHADER_PATH = "/shaders/frame_vertex.glsl"
    const val FRAME_FRAGMENT_SHADER_PATH = "/shaders/frame_fragment.glsl"
    const val PADDLE_VERTEX_SHADER_PATH = "/shaders/player_vertex.glsl"
    const val PADDLE_FRAGMENT_SHADER_PATH = "/shaders/player_fragment.glsl"
    const val BALL_VERTEX_SHADER_PATH = "/shaders/ball_vertex.glsl"
    const val BALL_FRAGMENT_SHADER_PATH = "/shaders/ball_fragment.glsl"
    const val BRICK_VERTEX_SHADER_PATH = "/shaders/brick_vertex.glsl"
    const val BRICK_FRAGMENT_SHADER_PATH = "/shaders/brick_fragment.glsl"
}
