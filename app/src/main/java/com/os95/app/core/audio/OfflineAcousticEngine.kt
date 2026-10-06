package com.os95.app.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class AcousticMode(val displayName: String, val description: String) {
    OFF("Mute", "Silent focus"),
    BROWNIAN("Brown Noise", "Deep ambient masking for intense problem solving"),
    PINK("Pink Noise", "1/f cognitive frequency for memory retention"),
    CLOCK_TICK("Exam Clock", "1 Hz rhythmic pulse simulating an exam hall")
}

class OfflineAcousticEngine {

    private val isPlaying = AtomicBoolean(false)
    @Volatile private var currentMode: AcousticMode = AcousticMode.OFF
    @Volatile private var currentVolume: Float = 0.6f

    private var audioTrack: AudioTrack? = null
    private var synthesisThread: Thread? = null

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE_SAMPLES = 2048
    }

    fun setMode(mode: AcousticMode) {
        currentMode = mode
        if (mode == AcousticMode.OFF) {
            stop()
        } else if (!isPlaying.get()) {
            start()
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0.0f, 1.0f)
    }

    fun getMode(): AcousticMode = currentMode
    fun getVolume(): Float = currentVolume
    fun isRunning(): Boolean = isPlaying.get()

    @Synchronized
    fun start() {
        if (isPlaying.get() || currentMode == AcousticMode.OFF) return

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, BUFFER_SIZE_SAMPLES * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isPlaying.set(true)

            synthesisThread = Thread({ runSynthesisLoop() }, "OS95-AcousticEngine").apply {
                isDaemon = true
                priority = Thread.NORM_PRIORITY
                start()
            }
        } catch (_: Exception) {
            isPlaying.set(false)
            audioTrack = null
        }
    }

    @Synchronized
    fun stop() {
        isPlaying.set(false)
        try {
            synthesisThread?.interrupt()
            synthesisThread = null
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {
            audioTrack = null
        }
    }

    private fun runSynthesisLoop() {
        val random = Random()
        val buffer = ShortArray(BUFFER_SIZE_SAMPLES)

        // State variables for procedural generators
        var brownState = 0.0f

        // Pink noise filter state (Kellet 6-pole)
        var b0 = 0.0f
        var b1 = 0.0f
        var b2 = 0.0f
        var b3 = 0.0f
        var b4 = 0.0f
        var b5 = 0.0f
        var b6 = 0.0f

        // Clock tick sample counter
        var sampleCounter = 0

        while (isPlaying.get() && !Thread.currentThread().isInterrupted) {
            val mode = currentMode
            val vol = currentVolume

            if (mode == AcousticMode.OFF) {
                // Sleep briefly if muted while thread is active
                try {
                    Thread.sleep(50)
                } catch (_: InterruptedException) {
                    break
                }
                continue
            }

            for (i in 0 until BUFFER_SIZE_SAMPLES) {
                var sample = 0.0f

                when (mode) {
                    AcousticMode.BROWNIAN -> {
                        val white = (random.nextFloat() * 2.0f - 1.0f)
                        brownState = (brownState + (0.02f * white)) / 1.02f
                        sample = (brownState * 3.5f).coerceIn(-1.0f, 1.0f)
                    }

                    AcousticMode.PINK -> {
                        val white = random.nextFloat() * 2.0f - 1.0f
                        b0 = 0.99886f * b0 + white * 0.0555179f
                        b1 = 0.99332f * b1 + white * 0.0750759f
                        b2 = 0.96900f * b2 + white * 0.1538520f
                        b3 = 0.86650f * b3 + white * 0.3104856f
                        b4 = 0.55000f * b4 + white * 0.5329522f
                        b5 = -0.7616f * b5 - white * 0.0168980f
                        sample = ((b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362f) * 0.11f).coerceIn(-1.0f, 1.0f)
                        b6 = white * 0.115926f
                    }

                    AcousticMode.CLOCK_TICK -> {
                        val posInSecond = sampleCounter % SAMPLE_RATE
                        if (posInSecond < 1200) {
                            // Synthesize a sharp mechanical click
                            val t = posInSecond.toDouble() / SAMPLE_RATE
                            val env = exp(-t * 220.0)
                            val osc = sin(2.0 * PI * 1800.0 * t) * 0.7 + sin(2.0 * PI * 900.0 * t) * 0.3
                            sample = (osc * env).toFloat().coerceIn(-1.0f, 1.0f)
                        } else {
                            sample = 0.0f
                        }
                        sampleCounter = (sampleCounter + 1) % SAMPLE_RATE
                    }

                    AcousticMode.OFF -> {
                        sample = 0.0f
                    }
                }

                val pcm = (sample * vol * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                buffer[i] = pcm
            }

            val written = audioTrack?.write(buffer, 0, BUFFER_SIZE_SAMPLES) ?: -1
            if (written <= 0) {
                break
            }
        }
    }
}
