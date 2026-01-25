package org.game.invaders

import org.game.invaders.utilities.MusicUtilities
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10
import java.nio.IntBuffer

import java.nio.ShortBuffer
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class RetroSynth(
    sourceCount: Int = 16   // Number of simultaneous sounds allowed
) {
    private val sources = IntArray(sourceCount)
    private val buffersInUse = mutableListOf<Int>()

    init {
        MusicUtilities.initializeOpenAL() // Ensure OpenAL shared context is initialized

        // Create a pool of reusable sources
        for (i in 0 until sourceCount) {
            sources[i] = AL10.alGenSources()
        }
    }

    // ------------------------------
    // Cleanup
    // ------------------------------
    fun cleanup() {
        // Delete all buffers still hanging around
        for (b in buffersInUse) {
            AL10.alDeleteBuffers(b)
        }

        // Delete sources
        for (s in sources) {
            AL10.alDeleteSources(s)
        }
    }

    // ------------------------------
    // Utility: find a free source
    // ------------------------------
    private fun acquireSource(): Int {
        // First reclaim any finished sources and delete their buffers
        reclaimFinishedSources()

        for (s in sources) {
            val state = AL10.alGetSourcei(s, AL10.AL_SOURCE_STATE)
            if (state != AL10.AL_PLAYING) {
                // Make sure previous buffer is unhooked
                AL10.alSourcei(s, AL10.AL_BUFFER, 0)
                return s
            }
        }

        // No free source → just stop the first one
        val s = sources[0]
        AL10.alSourceStop(s)
        AL10.alSourcei(s, AL10.AL_BUFFER, 0)
        return s
    }

    private fun reclaimFinishedSources() {
        val iterator = buffersInUse.iterator()
        while (iterator.hasNext()) {
            val buf = iterator.next()

            // OpenAL does NOT provide direct "is buffer still used",
            // but we can check all sources.
            var stillUsed = false

            for (s in sources) {
                val bound = AL10.alGetSourcei(s, AL10.AL_BUFFER)
                if (bound == buf) {
                    val state = AL10.alGetSourcei(s, AL10.AL_SOURCE_STATE)
                    if (state == AL10.AL_PLAYING) stillUsed = true
                }
            }

            if (!stillUsed) {
                AL10.alDeleteBuffers(buf)
                iterator.remove()
            }
        }
    }

    // ------------------------------
    // Play helper
    // ------------------------------
    private fun playBuffer(buffer: Int) {
        val source = acquireSource()
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer)
        AL10.alSourcePlay(source)

        buffersInUse.add(buffer)
    }

    // ------------------------------
    // Square Wave Beep
    // ------------------------------
    fun playSquareBeep(freq: Float, durationMs: Int) {
        val sampleRate = 44100
        val samples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = AL10.alGenBuffers()

        val data: ShortBuffer = BufferUtils.createShortBuffer(samples)

        var phase = 0f
        val step = freq / sampleRate

        for (i in 0 until samples) {
            val value = if (phase < 0.5f) 12000 else -12000
            val env = 1f - (i / samples.toFloat())
            data.put((value * env).toInt().toShort())
            phase = (phase + step) % 1f
        }

        data.flip()
        AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, data, sampleRate)

        playBuffer(buffer)
    }

    // ------------------------------
    // Atari/NES style noise burst
    // ------------------------------
    fun playNoiseBurst(durationMs: Int) {
        val sampleRate = 44100
        val samples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = AL10.alGenBuffers()

        val data = BufferUtils.createShortBuffer(samples)

        var lfsr = 0xACE1 // seed
        for (i in 0 until samples) {
            val bit = ((lfsr shr 0) xor (lfsr shr 2) xor (lfsr shr 3) xor (lfsr shr 5)) and 1
            lfsr = (lfsr shr 1) or (bit shl 15)

            // random noise bit → audio sample
            var sample = if ((lfsr and 1) == 1) 12000 else -12000

            // fade-out
            val env = 1f - (i / samples.toFloat())
            sample = (sample * env).toInt()

            data.put(sample.toShort())
        }

        data.flip()
        AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, data, sampleRate)

        playBuffer(buffer)
    }

    // ------------------------------
    // UFO Sound (warble effect)
    // ------------------------------
    fun playUfoSound(durationMs: Int) {
        val sampleRate = 44100
        val samples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = AL10.alGenBuffers()

        val data: ShortBuffer = BufferUtils.createShortBuffer(samples)

        // UFO sound: square wave with vibrato (frequency modulation)
        val baseFreq = 440f // Base frequency (A4)
        val vibratoFreq = 6f // Vibrato speed (Hz)
        val vibratoDepth = 60f // Vibrato depth (Hz)

        var phase = 0f
        for (i in 0 until samples) {
            val t = i / sampleRate.toFloat()
            val freq = baseFreq + sin(2 * Math.PI * vibratoFreq * t).toFloat() * vibratoDepth
            val step = freq / sampleRate
            val value = if (phase < 0.5f) 12000 else -12000
            data.put(value.toShort())
            phase = (phase + step) % 1f
        }

        data.flip()
        AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, data, sampleRate)

        playBuffer(buffer)
    }

    // ------------------------------
    // Invader Step Sound (short blip or drum-like)
    // durationMs: duration of the step in milliseconds
    // type: "classic" (square wave) or "drum" (noise burst or decaying sine)
    fun playInvaderStep(durationMs: Int, type: String = "classic") {
        val sampleRate = 44100
        val samples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = AL10.alGenBuffers()
        val data: ShortBuffer = BufferUtils.createShortBuffer(samples)

        when (type) {
            "drum" -> {
                // Drum-like: decaying sine wave (kick) + noise burst (snare)
                val freq = 120f // Kick drum frequency
                for (i in 0 until samples) {
                    val t = i / sampleRate.toFloat()
                    // Exponential decay envelope
                    val env = exp((-6 * t).toDouble()).toFloat()
                    // Sine wave for kick
                    val sine = (sin(2 * Math.PI * freq * t) * 12000 * env).toInt()
                    // Add a bit of noise for snare effect
                    val noise = ((Random.nextDouble() * 2 - 1) * 3000 * env).toInt()
                    data.put((sine + noise).toShort())
                }
            }
            else -> {
                // Classic: square wave blip
                val freq = 220f
                var phase = 0f
                val step = freq / sampleRate
                for (i in 0 until samples) {
                    val value = if (phase < 0.5f) 12000 else -12000
                    val env = 1f - (i / samples.toFloat())
                    data.put((value * env).toInt().toShort())
                    phase = (phase + step) % 1f
                }
            }
        }
        data.flip()
        AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, data, sampleRate)
        playBuffer(buffer)
    }
}
