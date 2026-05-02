class MainActivity : AppCompatActivity() {
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)
        
        setupDeviceAdmin()
        startAntiTheftService()
    }

    private fun setupDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable for anti-theft protection")
            }
            startActivity(intent)
        }
    }

    private fun startAntiTheftService() {
        val serviceIntent = Intent(this, AntiTheftService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
