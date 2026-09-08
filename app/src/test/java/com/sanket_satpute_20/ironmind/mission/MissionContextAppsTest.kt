package com.sanket_satpute_20.ironmind.mission

import android.content.SharedPreferences
import com.sanket_satpute_20.ironmind.data.PrefManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionContextAppsTest {

    @Test
    fun `resolveRecoveryContextApps restores exact multi package set`() {
        val savedContext = setOf("com.youtube", "com.chrome")
        val legacy = "com.legacy"
        val result = resolveRecoveryContextApps(legacyPackageName = legacy, savedContext = savedContext)
        assertEquals(savedContext, result)
    }

    @Test
    fun `resolveRecoveryContextApps does NOT silently replace with emptySet`() {
        val savedContext = setOf("com.youtube")
        val result = resolveRecoveryContextApps(legacyPackageName = null, savedContext = savedContext)
        assertEquals(savedContext, result)
    }

    @Test
    fun `legacy task with no saved context falls back to Task packageName`() {
        val legacy = "com.legacy.app"
        val result = resolveRecoveryContextApps(legacyPackageName = legacy, savedContext = null)
        assertEquals(setOf(legacy), result)
    }

    @Test
    fun `legacy task with neither source gets emptySet safely`() {
        val result = resolveRecoveryContextApps(legacyPackageName = null, savedContext = null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `saveMissionContextApps and getMissionContextApps round trip`() {
        val fakePrefs = FakeSharedPreferences()
        val prefManager = PrefManager(fakePrefs)
        val taskId = 42
        val contextApps = setOf("app1", "app2")

        prefManager.saveMissionContextApps(taskId, contextApps)
        assertEquals(contextApps, prefManager.getMissionContextApps(taskId))
    }

    @Test
    fun `clearMissionContextApps removes durable context`() {
        val fakePrefs = FakeSharedPreferences()
        val prefManager = PrefManager(fakePrefs)
        val taskId = 99
        val contextApps = setOf("app1", "app2")

        prefManager.saveMissionContextApps(taskId, contextApps)
        prefManager.clearMissionContextApps(taskId)
        assertNull(prefManager.getMissionContextApps(taskId))
    }
}

class FakeSharedPreferences : SharedPreferences {
    val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map.toMutableMap()

    override fun getString(key: String, defValue: String?): String? = map[key] as? String ?: defValue

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        val set = map[key] as? Set<String> ?: return defValues
        return set.toMutableSet()
    }

    override fun getInt(key: String, defValue: Int): Int = map[key] as? Int ?: defValue

    override fun getLong(key: String, defValue: Long): Long = map[key] as? Long ?: defValue

    override fun getFloat(key: String, defValue: Float): Float = map[key] as? Float ?: defValue

    override fun getBoolean(key: String, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue

    override fun contains(key: String): Boolean = map.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor(this)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
}

class FakeEditor(private val prefs: FakeSharedPreferences) : SharedPreferences.Editor {
    private val changes = mutableMapOf<String, Any?>()
    private var clear = false

    override fun putString(key: String, value: String?): SharedPreferences.Editor = apply { changes[key] = value }

    override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = apply { changes[key] = values?.toSet() }

    override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply { changes[key] = value }

    override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply { changes[key] = value }

    override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply { changes[key] = value }

    override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply { changes[key] = value }

    override fun remove(key: String): SharedPreferences.Editor = apply { changes[key] = null }

    override fun clear(): SharedPreferences.Editor = apply { clear = true }

    override fun commit(): Boolean {
        if (clear) prefs.map.clear()
        for ((k, v) in changes) {
            if (v == null) prefs.map.remove(k) else prefs.map[k] = v
        }
        return true
    }

    override fun apply() {
        commit()
    }
}
