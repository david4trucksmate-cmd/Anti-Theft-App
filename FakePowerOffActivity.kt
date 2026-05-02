class FakePowerOffActivity : AppCompatActivity() {
    companion object {
        private const val PIN_CODE = "1234"
        private var failedAttempts = 0
        private val cameraHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreenOverlay()
        showShutdownAnimation()
    }

    private fun setFullScreenOverlay() {
        // Make it SYSTEM overlay - impossible to dismiss
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        
        // Black background theme
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }

    private fun showShutdownAnimation() {
        setContentView(R.layout.activity_fake_poweroff)
        
        // Shutdown animation sequence
        val shutdownText = findViewById<TextView>(R.id.shutdown_text)
        val progressBar = findViewById<ProgressBar>(R.id.shutdown_progress)
        
        // Animate "Shutting Down..." with dots
        var dotCount = 0
        val animator = object : CountDownTimer(5000, 500) {
            override fun onTick(millisUntilFinished: Long) {
                dotCount = (dotCount + 1) % 4
                shutdownText.text = "Shutting Down${".".repeat(dotCount)}"
                progressBar.progress = (100 - millisUntilFinished / 50).toInt()
            }
            
            override fun onFinish() {
                // Go FULL BLACK - CPU still running!
                window.decorView.setBackgroundColor(Color.BLACK)
                findViewById<View>(R.id.shutdown_container).visibility = View.GONE
                
                // Show PIN prompt after 3 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    showPinPrompt()
                }, 3000)
            }
        }
        animator.start()
    }

    private fun showPinPrompt() {
        setContentView(R.layout.pin_prompt_layout)
        val pinEditText = findViewById<EditText>(R.id.pin_input)
        val unlockButton = findViewById<Button>(R.id.unlock_button)

        pinEditText.inputType = InputType.TYPE_CLASS_NUMBER
        pinEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4) unlockButton.performClick()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        unlockButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()
            if (enteredPin == PIN_CODE) {
                // Correct PIN - exit lockdown
                failedAttempts = 0
                finish()
            } else {
                failedAttempts++
                Toast.makeText(this, "Wrong PIN! Attempts: $failedAttempts/3", Toast.LENGTH_SHORT).show()
                
                if (failedAttempts >= 3) {
                    takeIntruderPhoto()
                } else {
                    pinEditText.text.clear()
                }
            }
        }
    }

    private fun takeIntruderPhoto() {
        val cameraIntent = Intent(this, CameraCaptureActivity::class.java)
        startActivity(cameraIntent)
        finish()
    }
}
