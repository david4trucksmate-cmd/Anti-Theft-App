class SimChangeReceiver : BroadcastReceiver() {
    companion object {
        private const val PIN_CODE = "1234" // Change this to your secret PIN
        private const val STOLEN_STATE_KEY = "device_stolen"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TelephonyManager.ACTION_SIM_STATE_CHANGED -> {
                val simState = intent.getStringExtra(TelephonyManager.EXTRA_SIM_STATE)
                val isLocked = KeyguardManager(context).isKeyguardLocked
                
                // SIM Removed while device locked = STOLEN!
                if (simState == TelephonyManager.SIM_STATE_ABSENT && isLocked) {
                    Log.d("SimChangeReceiver", "SIM REMOVED while LOCKED - TRIGGERING LOCKDOWN!")
                    
                    // Mark device as stolen
                    val prefs = context.getSharedPreferences("antitheft", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(STOLEN_STATE_KEY, true).apply()
                    
                    // Launch stolen overlay IMMEDIATELY
                    val stolenIntent = Intent(context, StolenDeviceActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                    context.startActivity(stolenIntent)
                    
                    // Play loud alarm (ignores silent mode)
                    playLoudAlarm(context)
                }
            }
        }
    }
    
    private fun playLoudAlarm(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.isMusicActive = true // Force audio stream
            
            val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound) // Add alarm_sound.mp3 to res/raw
            mediaPlayer.isLooping = true
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
            mediaPlayer.setVolume(1.0f, 1.0f)
            mediaPlayer.start()
            
            // Keep service alive to maintain alarm
            val serviceIntent = Intent(context, AntiTheftService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            Log.e("SimChangeReceiver", "Alarm failed", e)
        }
    }
}
