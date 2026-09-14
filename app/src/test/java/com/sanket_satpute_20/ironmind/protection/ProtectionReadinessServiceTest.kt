package com.sanket_satpute_20.ironmind.protection

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class TestPermissionChecker : PermissionChecker {
    var accessibilityEnabled = true
    var overlayEnabled = true
    var notificationEnabled = true
    var usageStatsEnabled = true

    override fun isAccessibilityEnabled(context: Context) = accessibilityEnabled
    override fun canDrawOverlays(context: Context) = overlayEnabled
    override fun hasNotificationPermission(context: Context) = notificationEnabled
    override fun hasUsageStatsPermission(context: Context) = usageStatsEnabled
}

class ProtectionReadinessServiceTest {

    private lateinit var checker: TestPermissionChecker
    private lateinit var service: ProtectionReadinessService

    @Before
    fun setup() {
        io.mockk.mockkObject(com.sanket_satpute_20.ironmind.core.logging.IronMindLogger)
        io.mockk.every { com.sanket_satpute_20.ironmind.core.logging.IronMindLogger.log(any(), any(), any()) } returns Unit
        io.mockk.every { com.sanket_satpute_20.ironmind.core.logging.IronMindLogger.e(any(), any(), any(), any()) } returns Unit

        checker = TestPermissionChecker()
        val mockContext = io.mockk.mockk<Context>(relaxed = true)
        service = ProtectionReadinessService(mockContext, checker)
    }

    @Test
    fun `evaluate returns Ready when all required capabilities are enabled`() {
        val result = service.evaluate()
        
        assertTrue(result is ProtectionReadiness.Ready)
        val ready = result as ProtectionReadiness.Ready
        assertEquals(true, ready.capabilities[ProtectionCapability.ACCESSIBILITY_SERVICE]?.enabled)
        assertEquals(true, ready.capabilities[ProtectionCapability.SYSTEM_ALERT_WINDOW]?.enabled)
        assertEquals(true, ready.capabilities[ProtectionCapability.POST_NOTIFICATIONS]?.enabled)
    }

    @Test
    fun `evaluate returns NotReady when Accessibility is missing`() {
        checker.accessibilityEnabled = false
        
        val result = service.evaluate()
        
        assertTrue(result is ProtectionReadiness.NotReady)
        val notReady = result as ProtectionReadiness.NotReady
        assertTrue(notReady.missingRequired.contains(ProtectionCapability.ACCESSIBILITY_SERVICE))
    }

    @Test
    fun `evaluate returns NotReady when Overlay is missing`() {
        checker.overlayEnabled = false
        
        val result = service.evaluate()
        
        assertTrue(result is ProtectionReadiness.NotReady)
        val notReady = result as ProtectionReadiness.NotReady
        assertTrue(notReady.missingRequired.contains(ProtectionCapability.SYSTEM_ALERT_WINDOW))
    }

    @Test
    fun `evaluate returns Ready even if optional Notification permission is missing`() {
        checker.notificationEnabled = false
        
        val result = service.evaluate()
        
        assertTrue(result is ProtectionReadiness.Ready)
        val ready = result as ProtectionReadiness.Ready
        assertEquals(false, ready.capabilities[ProtectionCapability.POST_NOTIFICATIONS]?.enabled)
    }
}
