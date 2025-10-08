package com.github.hovrawl.jetbrainstoggleabletoolwindows.startup

import com.github.hovrawl.jetbrainstoggleabletoolwindows.immersive.ImmersiveTopBarManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class ImmersiveTopBarStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        // Initialize the manager, which will set up listeners
        ImmersiveTopBarManager.getInstance()
    }
}
