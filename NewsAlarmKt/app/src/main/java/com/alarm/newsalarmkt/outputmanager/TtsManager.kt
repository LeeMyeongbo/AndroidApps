package com.alarm.newsalarmkt.outputmanager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.alarm.newsalarmkt.database.AlarmData
import com.alarm.newsalarmkt.utils.LogUtil
import java.util.Locale
import java.util.function.Consumer

class TtsManager(context: Context, data: AlarmData?, private val initListener: OnInitListener?) {

    enum class Mode {
        DEFAULT, ADD_SILENCE
    }

    fun interface OnInitListener {
        fun onInitialized()
    }

    fun interface OnTtsAllDoneListener {
        fun onSpeechAllDone()
    }

    private lateinit var manager: AudioManager
    private lateinit var attr: AudioAttributes
    private lateinit var tts: TextToSpeech

    private val speakList = ArrayList<Runnable>()
    private val voiceList = ArrayList<Voice>()
    private var doneListener: OnTtsAllDoneListener? = null
    private var isTtsInitialized = false
    private var originalVolume = 0
    private var pendingSpeechCnt = 0

    init {
        storeOriginalVolume(context)
        initAudioAttributes()
        initTts(
            context,
            data?.voiceIdx ?: 0,
            data?.volumeSize ?: 4,
            data?.tempo ?: 1.0f,
            data?.pitch ?: 1.0f
        )
    }

    private fun storeOriginalVolume(context: Context) {
        manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        originalVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM)
    }

    private fun initAudioAttributes() {
        attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    }

    private fun initTts(context: Context?, idx: Int, volumeSize: Int, tempo: Float, pitch: Float) {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener { status: Int ->
            if (status == TextToSpeech.ERROR) {
                LogUtil.logE(
                    CLASS_NAME, "OnInitListener.onInit",
                    "tts initialization failed - status $status"
                )
                return@OnInitListener
            }
            setTts(idx, volumeSize, tempo, pitch)
            initListener?.onInitialized()
            speakList.forEach(Consumer { obj: Runnable -> obj.run() })
            isTtsInitialized = true
            LogUtil.logI(CLASS_NAME, "OnInitListener.onInit", "tts initialized successfully!")
        })
    }

    private fun setTts(idx: Int, volumeSize: Int, tempo: Float, pitch: Float) {
        prepareVoices()
        setSpecificVoice(idx)
        setVolumeSize(volumeSize)
        setVoicePitch(pitch)
        setVoiceTempo(tempo)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            private var speechCnt = 0

            override fun onStart(s: String?) {
            }

            override fun onDone(s: String?) {
                speechCnt++
                if (speechCnt == pendingSpeechCnt) {
                    LogUtil.logD(CLASS_NAME, "onDone", "all speech done")
                    doneListener?.onSpeechAllDone()
                }
            }

            @Deprecated("Deprecated")
            override fun onError(s: String?) {
            }
        })
    }

    private fun prepareVoices() {
        tts.language = Locale.KOREAN
        tts.setAudioAttributes(attr)
        for (voice in tts.voices) {
            val name = voice.name.lowercase()
            if (name.contains("ko-kr") && name.contains("local")) {
                voiceList.add(voice)
                LogUtil.logD(CLASS_NAME, "prepareVoices", "found voice : ${voice.name}")
            }
        }
        if (voiceList.isEmpty()) {
            tts.voice = tts.defaultVoice
            LogUtil.logD(
                CLASS_NAME,
                "prepareVoices",
                "no local voice found - add default voice ${tts.defaultVoice.name}"
            )
        }
    }

    fun setSpecificVoice(idx: Int) {
        if (idx < 0 || idx >= voiceList.size) {
            LogUtil.logE(CLASS_NAME, "setSpecificVoice", "invalid voice index : $idx")
            tts.voice = tts.defaultVoice
            return
        }

        val voice = voiceList[idx]
        tts.voice = voice
        LogUtil.logD(CLASS_NAME, "setSpecificVoice", "voice index : $idx")
        LogUtil.logD(CLASS_NAME, "setSpecificVoice", "voice info : $voice")
    }

    fun setVolumeSize(volumeSize: Int) {
        manager.setStreamVolume(AudioManager.STREAM_ALARM, volumeSize, 0)
        LogUtil.logD(CLASS_NAME, "setVolumeSize", "set volume : $volumeSize")
    }

    fun setVoicePitch(pitch: Float) {
        tts.setPitch(pitch)
        LogUtil.logD(CLASS_NAME, "setVoicePitch", "set pitch : $pitch")
    }

    fun setVoiceTempo(tempo: Float) {
        tts.setSpeechRate(tempo)
        LogUtil.logD(CLASS_NAME, "setVoiceTempo", "set tempo : $tempo")
    }

    val availableVoiceNum: Int = voiceList.size

    fun setTtsDoneListener(listener: OnTtsAllDoneListener?) {
        doneListener = listener
    }

    fun speakArticles(titleList: ArrayList<String>, bodyList: ArrayList<String>) {
        speak("안녕하세요? 오늘의 뉴스를 알려드리겠습니다.", Mode.ADD_SILENCE)
        for (i in titleList.indices) {
            speak("${(i + 1)}번 뉴스입니다.", Mode.ADD_SILENCE)
            speak(titleList[i], Mode.ADD_SILENCE)
            speak("기사 내용입니다.", Mode.ADD_SILENCE)
            speakArticleBody(bodyList, i)
        }
    }

    private fun speakArticleBody(bodyList: ArrayList<String>, cur: Int) {
        val body = bodyList[cur]
        val maxLenPerSpeech = TextToSpeech.getMaxSpeechInputLength() / 4
        val num = body.length / maxLenPerSpeech
        for (i in 0..num) {
            val l = Integer.min(maxLenPerSpeech * i + maxLenPerSpeech, body.length)
            speak(body.substring(i * maxLenPerSpeech, l), Mode.DEFAULT)
        }
    }

    @JvmOverloads
    fun speak(txt: String, mode: Mode, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        if (isTtsInitialized) {
            speakWithTts(txt, mode, queueMode)
        } else {
            speakList.add(Runnable { speakWithTts(txt, mode, queueMode) })
        }
    }

    private fun speakWithTts(txt: String?, mode: Mode, queueMode: Int) {
        if (mode == Mode.ADD_SILENCE) {
            tts.playSilentUtterance(800L, queueMode, "silence")
            pendingSpeechCnt++
        }
        tts.speak(txt, queueMode, null, "speak")
        pendingSpeechCnt++
    }

    fun release() {
        if (!::tts.isInitialized) return
        manager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
        tts.stop()
        tts.shutdown()
        LogUtil.logI(CLASS_NAME, "release", "tts released completely!")
    }

    companion object {
        private const val CLASS_NAME = "TtsManager"
    }
}
