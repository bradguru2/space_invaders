package org.game.invaders.utilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.openal.AL10.*
import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.IntBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

object MusicUtilities {

    @Volatile
    private var stop = false

    @JvmStatic
    fun stopPlayback() {
        stop = true
    }

    @JvmStatic
    fun playAsync(scope: CoroutineScope, resource: String): Job =
        scope.launch(Dispatchers.IO) {
            stop = false
            playLoopingAudio(resource) // your blocking loop uses `stop` to exit
        }

    @JvmStatic
    private suspend fun playLoopingAudio(path: String) {
        // Init OpenAL
        val device = alcOpenDevice(null as ByteBuffer?)
        val context = alcCreateContext(device, null as IntBuffer?)
        alcMakeContextCurrent(context)
        AL.createCapabilities(ALC.createCapabilities(device))

        val src = alGenSources()

        fun openPcmStream(): AudioInputStream {
            val rawStream = javaClass.getResourceAsStream(path)
                ?: error("Missing resource: $path")
            val buffered = BufferedInputStream(rawStream) // adds mark/reset support
            val audioStream = AudioSystem.getAudioInputStream(buffered)
            val pcm = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                audioStream.format.sampleRate,
                16,
                audioStream.format.channels,
                audioStream.format.channels * 2,
                audioStream.format.sampleRate,
                false
            )
            return AudioSystem.getAudioInputStream(pcm, audioStream) // converts on the fly
        }

        var stream = openPcmStream()
        val pcmFormat = stream.format
        val format = if (pcmFormat.channels == 1) AL_FORMAT_MONO16 else AL_FORMAT_STEREO16

        val buffers = IntArray(4) { alGenBuffers() }
        val bufSize = 4096 * pcmFormat.frameSize
        val tmp = ByteArray(bufSize)
        val pcmBuf = BufferUtils.createByteBuffer(bufSize)

        fun fill(bufId: Int): Boolean {
            var read = stream.read(tmp, 0, tmp.size)
            if (read <= 0) {
                stream.close()
                stream = openPcmStream()
                read = stream.read(tmp, 0, tmp.size)
                if (read <= 0) return false
            }
            pcmBuf.clear()
            pcmBuf.put(tmp, 0, read).flip() // set limit = read
            alBufferData(bufId, format, pcmBuf, pcmFormat.sampleRate.toInt())
            return true
        }

        // Prime queue
        var active = 0
        for (b in buffers) {
            if (fill(b)) {
                alSourceQueueBuffers(src, b)
                active++
            }
        }
        alSourcePlay(src)
        var count = active
        // Stream/loop
        while (currentCoroutineContext().isActive && !stop && count > 0) {
            val processed = alGetSourcei(src, AL_BUFFERS_PROCESSED)
            repeat(processed) {
                val b = alSourceUnqueueBuffers(src)
                if (fill(b)) {
                    alSourceQueueBuffers(src, b)
                } else {
                    count--
                }
            }
            if (alGetSourcei(src, AL_SOURCE_STATE) != AL_PLAYING && count > 0) {
                alSourcePlay(src) // restart if starved
            }
            delay(10)
            if (count == 0) count = active
        }

        // Cleanup
        alDeleteSources(src)
        buffers.forEach { alDeleteBuffers(it) }
        alcDestroyContext(context)
        alcCloseDevice(device)
    }
}