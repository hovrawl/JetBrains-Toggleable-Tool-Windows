package com.github.hovrawl.jetbrainstoggleabletoolwindows.startup

import com.github.hovrawl.jetbrainstoggleabletoolwindows.controller.CompactUIController
import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class CompactUIStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (CompactUISettings.getInstance().state.enabled) {
            // Initialize the controller so hover detection is installed at startup
            CompactUIController.getInstance(project)
        }
    }
}

