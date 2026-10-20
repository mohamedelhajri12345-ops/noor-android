package com.elhajri.noor.mychild

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object MyChildStore {
    private const val PREFS_NAME = "noor_mychild_prefs"
    private const val KEY_PROFILES = "profiles_json"
    private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    private const val KEY_PARENT_PIN = "parent_pin"
    private const val KEY_SAFE_MODE_ACTIVE = "safe_mode_active"
    private const val KEY_MEMORIZATION_PREFIX = "memo_"
    private const val KEY_REWARDS_PREFIX = "rewards_"
    private const val KEY_SCREEN_TIME_PREFIX = "screen_time_"
    private const val KEY_LAST_SCREEN_TIME_DATE = "screen_time_date_"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Parent PIN & Safe Mode ---
    fun getParentPin(context: Context): String {
        return prefs(context).getString(KEY_PARENT_PIN, "1234") ?: "1234"
    }

    fun setParentPin(context: Context, newPin: String) {
        prefs(context).edit().putString(KEY_PARENT_PIN, newPin).apply()
    }

    fun verifyParentPin(context: Context, enteredPin: String): Boolean {
        return getParentPin(context) == enteredPin
    }

    fun isKidsSafeModeActive(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_SAFE_MODE_ACTIVE, false)
    }

    fun setKidsSafeModeActive(context: Context, active: Boolean) {
        prefs(context).edit().putBoolean(KEY_SAFE_MODE_ACTIVE, active).apply()
    }

    // --- Profiles ---
    fun getProfiles(context: Context): List<ChildProfile> {
        val jsonStr = prefs(context).getString(KEY_PROFILES, null) ?: return defaultInitialProfiles(context)
        val list = mutableListOf<ChildProfile>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChildProfile(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        age = obj.optInt("age", 7),
                        avatar = obj.optString("avatar", "🦁"),
                        bedtimeHour = obj.optInt("bedtimeHour", 20),
                        bedtimeMinute = obj.optInt("bedtimeMinute", 0),
                        bedtimeNotificationEnabled = obj.optBoolean("bedtimeNotificationEnabled", true),
                        totalStarsEarned = obj.optInt("totalStarsEarned", 0),
                        screenTimeMinutesToday = obj.optInt("screenTimeMinutesToday", 0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (list.isEmpty()) defaultInitialProfiles(context) else list
    }

    private fun defaultInitialProfiles(context: Context): List<ChildProfile> {
        val defaultProfile = ChildProfile(
            id = "default_child_1",
            name = "طفلي المبدع",
            age = 7,
            avatar = "🦁",
            bedtimeHour = 20,
            bedtimeMinute = 0,
            totalStarsEarned = 15
        )
        saveProfiles(context, listOf(defaultProfile))
        setActiveProfileId(context, defaultProfile.id)
        return listOf(defaultProfile)
    }

    private fun saveProfiles(context: Context, list: List<ChildProfile>) {
        val array = JSONArray()
        for (p in list) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("age", p.age)
            obj.put("avatar", p.avatar)
            obj.put("bedtimeHour", p.bedtimeHour)
            obj.put("bedtimeMinute", p.bedtimeMinute)
            obj.put("bedtimeNotificationEnabled", p.bedtimeNotificationEnabled)
            obj.put("totalStarsEarned", p.totalStarsEarned)
            obj.put("screenTimeMinutesToday", p.screenTimeMinutesToday)
            obj.put("createdAt", p.createdAt)
            array.put(obj)
        }
        prefs(context).edit().putString(KEY_PROFILES, array.toString()).apply()
    }

    fun getActiveProfile(context: Context): ChildProfile {
        val profiles = getProfiles(context)
        val activeId = prefs(context).getString(KEY_ACTIVE_PROFILE_ID, null)
        return profiles.find { it.id == activeId } ?: profiles.firstOrNull() ?: ChildProfile("id_1", "طفلي", 7)
    }

    fun setActiveProfileId(context: Context, id: String) {
        prefs(context).edit().putString(KEY_ACTIVE_PROFILE_ID, id).apply()
    }

    fun saveChildProfile(context: Context, profile: ChildProfile) {
        val current = getProfiles(context).toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile
        } else {
            current.add(profile)
        }
        saveProfiles(context, current)
        if (getActiveProfile(context).id == profile.id || current.size == 1) {
            setActiveProfileId(context, profile.id)
        }
    }

    fun deleteChildProfile(context: Context, id: String) {
        val current = getProfiles(context).filter { it.id != id }
        saveProfiles(context, current)
        if (current.isNotEmpty()) {
            setActiveProfileId(context, current.first().id)
        }
    }

    fun addStarsToChild(context: Context, childId: String, amount: Int) {
        val profiles = getProfiles(context).toMutableList()
        val index = profiles.indexOfFirst { it.id == childId }
        if (index >= 0) {
            val old = profiles[index]
            val updated = old.copy(totalStarsEarned = old.totalStarsEarned + amount)
            profiles[index] = updated
            saveProfiles(context, profiles)
        }
    }

    // --- Screen Time ---
    fun addScreenTime(context: Context, childId: String, minutes: Int = 1) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val lastDate = prefs(context).getString(KEY_LAST_SCREEN_TIME_DATE + childId, "")
        var currentMin = if (lastDate == today) {
            prefs(context).getInt(KEY_SCREEN_TIME_PREFIX + childId, 0)
        } else {
            0
        }
        currentMin += minutes
        prefs(context).edit()
            .putString(KEY_LAST_SCREEN_TIME_DATE + childId, today)
            .putInt(KEY_SCREEN_TIME_PREFIX + childId, currentMin)
            .apply()

        // Also update child profile's screenTimeMinutesToday
        val profiles = getProfiles(context).toMutableList()
        val idx = profiles.indexOfFirst { it.id == childId }
        if (idx >= 0) {
            profiles[idx] = profiles[idx].copy(screenTimeMinutesToday = currentMin)
            saveProfiles(context, profiles)
        }
    }

    fun getTodayScreenTimeMinutes(context: Context, childId: String): Int {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val lastDate = prefs(context).getString(KEY_LAST_SCREEN_TIME_DATE + childId, "")
        return if (lastDate == today) {
            prefs(context).getInt(KEY_SCREEN_TIME_PREFIX + childId, 0)
        } else 0
    }

    // --- Surah Memorization Tracker ---
    fun getMemorizationMap(context: Context, childId: String): Map<Int, Int> {
        val jsonStr = prefs(context).getString(KEY_MEMORIZATION_PREFIX + childId, null) ?: return emptyMap()
        val result = mutableMapOf<Int, Int>()
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val surahNum = key.toIntOrNull() ?: continue
                val stars = obj.getInt(key)
                result[surahNum] = stars
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun setSurahMemorizationStars(context: Context, childId: String, surahNumber: Int, starsCount: Int) {
        val map = getMemorizationMap(context, childId).toMutableMap()
        map[surahNumber] = starsCount
        val obj = JSONObject()
        for ((k, v) in map) {
            obj.put(k.toString(), v)
        }
        prefs(context).edit().putString(KEY_MEMORIZATION_PREFIX + childId, obj.toString()).apply()

        // Award stars to child if 3 stars reached
        if (starsCount == 3) {
            addStarsToChild(context, childId, 3)
        }
    }

    // --- Parent Rewards / Motivation Cards ---
    fun getParentRewards(context: Context, childId: String): List<ParentReward> {
        val jsonStr = prefs(context).getString(KEY_REWARDS_PREFIX + childId, null)
            ?: return defaultRewards()
        val list = mutableListOf<ParentReward>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    ParentReward(
                        id = item.getString("id"),
                        titleAr = item.getString("titleAr"),
                        starCost = item.getInt("starCost"),
                        iconEmoji = item.optString("iconEmoji", "🎁"),
                        isRedeemed = item.optBoolean("isRedeemed", false),
                        redeemedAt = if (item.has("redeemedAt")) item.getLong("redeemedAt") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (list.isEmpty()) defaultRewards() else list
    }

    private fun defaultRewards(): List<ParentReward> {
        return listOf(
            ParentReward("r_1", "ساعة آيباد أو ألعاب", 20, "📱"),
            ParentReward("r_2", "رحلة إلى الحديقة", 35, "🎡"),
            ParentReward("r_3", "قصة جديدة من المكتبة", 15, "📚"),
            ParentReward("r_4", "هدية خاصة من الوالدين", 50, "🎁")
        )
    }

    fun saveParentRewards(context: Context, childId: String, list: List<ParentReward>) {
        val array = JSONArray()
        for (r in list) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("titleAr", r.titleAr)
            obj.put("starCost", r.starCost)
            obj.put("iconEmoji", r.iconEmoji)
            obj.put("isRedeemed", r.isRedeemed)
            r.redeemedAt?.let { obj.put("redeemedAt", it) }
            array.put(obj)
        }
        prefs(context).edit().putString(KEY_REWARDS_PREFIX + childId, array.toString()).apply()
    }

    fun redeemReward(context: Context, childId: String, rewardId: String): Boolean {
        val rewards = getParentRewards(context, childId).toMutableList()
        val idx = rewards.indexOfFirst { it.id == rewardId }
        if (idx < 0) return false

        val reward = rewards[idx]
        val activeProfile = getActiveProfile(context)
        if (activeProfile.totalStarsEarned < reward.starCost) return false

        // Deduct stars & mark redeemed
        addStarsToChild(context, childId, -reward.starCost)
        rewards[idx] = reward.copy(isRedeemed = true, redeemedAt = System.currentTimeMillis())
        saveParentRewards(context, childId, rewards)
        return true
    }

    fun addParentReward(context: Context, childId: String, titleAr: String, cost: Int, emoji: String) {
        val rewards = getParentRewards(context, childId).toMutableList()
        val newReward = ParentReward(
            id = UUID.randomUUID().toString(),
            titleAr = titleAr,
            starCost = cost,
            iconEmoji = emoji
        )
        rewards.add(newReward)
        saveParentRewards(context, childId, rewards)
    }
}
