package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.util.messages.Topic

interface CompactUISettingsListener {
    fun settingsChanged()

    companion object {
        @JvmField
        val TOPIC: Topic<CompactUISettingsListener> = Topic.create(
            "Compact UI Settings Changed",
            CompactUISettingsListener::class.java
        )
    }
}
