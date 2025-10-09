package com.github.hovrawl.jetbrainstoggleabletoolwindows.startup

import com.github.hovrawl.jetbrainstoggleabletoolwindows.immersive.ImmersiveTopBarManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ImmersiveTopBarStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Initialize the manager for this project frame
        ImmersiveTopBarManager.getInstance().initializeForProject(project)
    }
}
