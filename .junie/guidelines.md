JetBrains-Toggleable-Tool-Windows – Development Guidelines

Scope
- This document captures project-specific knowledge to speed up work on this IntelliJ Platform plugin. It assumes familiarity with Gradle, Kotlin, and IntelliJ Platform Plugin development.

Plugin Purpose & Behavior
- Goal: Provide toggle actions per tool-window stripe (left, right, bottom) that remember the last active tool window on that stripe.
- Expected behavior per stripe action (Left/Right/Bottom):
  1) When invoked and a tool window on that stripe is active/focused, hide it and remember it as the active window for that stripe.
  2) When invoked again and no tool window is active on that stripe, re-open (activate) the remembered window for that stripe.
  3) If another tool window on the same stripe was activated since, update the memory to that one upon hide.
- Scope per stripe:
  - Left: Project, Structure, Services, etc.
  - Right: Commit, TODO, Problems, etc.
  - Bottom: Run, Debug, Terminal, Problems, Build, etc.
- Actions & bindings:
  - Provide three actions: ToggleLeftStripeToolWindow, ToggleRightStripeToolWindow, ToggleBottomStripeToolWindow.
  - Users can bind shortcuts via Settings/Preferences > Keymap (search by action id or name). Suggested examples: Alt+1 (Left toggle), Alt+2 (Right toggle), Alt+3 (Bottom toggle) — adjust to avoid conflicts.
- State storage strategy:
  - Store per-project last-remembered tool window id for each stripe in a ProjectService (persistent or in-memory; persistent preferred using @State + PersistentStateComponent).
  - Clear or validate ids on project open/close; if the remembered id no longer exists, pick the next available (e.g., most recently active on that stripe) or do nothing.
- Edge cases to handle:
  - No active tool window on the stripe when first invoked: do nothing or activate the remembered one if available.
  - Multiple active/focused components: derive the stripe from ToolWindowManager.getActiveToolWindowId() or focus owner, then map to stripe.
  - Tool window temporarily unavailable (e.g., disabled by IDE): detect via ToolWindowManager.getToolWindow(id) == null and skip.
  - Floating or detached windows: treat as belonging to their original stripe; hide/activate accordingly.
- Implementation pointers (when implementing):
  - ToolWindow APIs: com.intellij.openapi.wm.ToolWindowManager, ToolWindow, ToolWindowId.
  - Stripe classification: ToolWindow.anchor (ToolWindowAnchor.LEFT/RIGHT/BOTTOM) or layout info from ToolWindowManager.
  - Hiding/Showing: toolWindow.hide(null) and toolWindow.activate(null, true) or show(null) depending on UX; use ApplicationManager.invokeLater if needed.
  - Tracking activity: ToolWindowManager.getActiveToolWindowId() and/or AnAction event context (PlatformDataKeys.TOOL_WINDOW) to find current.
  - Register actions in plugin.xml with stable action ids and presentation; optionally add a ToggleToolWindowAction subclass for shared logic.

Build and Configuration
- Java toolchain: The project builds against Java 21 (see build.gradle.kts → kotlin.jvmToolchain(21)).
  - Local dev: Install JDK 21 (Temurin, Oracle, or JetBrains Runtime 21) and ensure JAVA_HOME points to it. On Windows PowerShell:
    - [System] Set-Item -Path Env:JAVA_HOME -Value "C:\Program Files\Java\jdk-21"
    - [Session] $Env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
  - Alternatively, let IntelliJ IDEA run Gradle with its bundled JBR (Settings > Build Tools > Gradle > Gradle JVM = Use Project JDK/JetBrains Runtime 21).
- Gradle Wrapper: Always use the wrapper provided by the repo.
  - Windows: .\gradlew.bat tasks
  - macOS/Linux: ./gradlew tasks
- IntelliJ Platform configuration is managed via the IntelliJ Platform Gradle Plugin (see build.gradle.kts intellijPlatform { ... }). Key points:
  - The target IDE, bundled plugins/modules, and test framework are driven by gradle.properties (platformType, platformVersion, platformBundledPlugins, platformBundledModules, platformPlugins).
  - Plugin description is extracted from the README between <!-- Plugin description --> and <!-- Plugin description end --> at build time. Do not remove those markers.
  - Changelog notes for the plugin manifest are sourced from CHANGELOG.md via the gradle-changelog-plugin.
- Running the IDE for manual testing:
  - Start a sandbox IDE with the plugin: .\gradlew.bat runIde
  - For UI tests sandbox (if needed): .\gradlew.bat runIdeForUiTests (registered under intellijPlatformTesting.runIde).
- Signing/Publishing: Controlled via environment variables (CERTIFICATE_CHAIN, PRIVATE_KEY, PRIVATE_KEY_PASSWORD, PUBLISH_TOKEN). Not needed for local builds.

Testing
- Frameworks:
  - JUnit (via libs.junit, libs.opentest4j in build.gradle.kts)
  - IntelliJ Platform test framework (TestFrameworkType.Platform) for BasePlatformTestCase-based tests.
- Headless execution prerequisites:
  - Java 21 available to Gradle (JAVA_HOME or Gradle JVM set in IDE). Without a Java runtime you’ll get: "ERROR: JAVA_HOME is not set".
  - On CI, ensure a JDK 21 is available or configure Gradle Toolchains/Gradle JVM accordingly.
- Run all tests:
  - Windows: .\gradlew.bat test
  - macOS/Linux: ./gradlew test
- Run a single class or method (Gradle test filtering):
  - Single class: .\gradlew.bat test --tests "com.github.hovrawl.jetbrainstoggleabletoolwindows.MyPluginTest"
  - Single method: .\gradlew.bat test --tests "com.github.hovrawl.jetbrainstoggleabletoolwindows.MyPluginTest.testXMLFile"
- IntelliJ-specific fixtures:
  - BasePlatformTestCase requires the IntelliJ test framework configured by the plugin. Do not convert these to plain JUnit tests unless you replace platform fixtures accordingly.
- Test data location:
  - See src/test/testData (e.g., rename fixtures used by MyPluginTest.testRename). The test class is annotated with @TestDataPath("$CONTENT_ROOT/src/test/testData").

Demonstration – Using Existing Tests
- This repository already contains a working example test suite at src/test/kotlin/com/github/hovrawl/jetbrainstoggleabletoolwindows/MyPluginTest.kt demonstrating:
  - Parsing and validating XML PSI (testXMLFile)
  - Rename refactoring using test data (testRename)
  - Accessing a project-level service (testProjectService)
- To demonstrate the process end-to-end (after ensuring JDK 21 is available):
  1) .\gradlew.bat clean test
  2) Run a single method to iterate quickly:
     .\gradlew.bat test --tests "com.github.hovrawl.jetbrainstoggleabletoolwindows.MyPluginTest.testProjectService"
  3) Inspect reports at build\reports\tests\test\index.html

Adding New Tests
- Choose the right base:
  - For plugin behavior interacting with the IDE (PSI, VFS, actions), prefer BasePlatformTestCase or LightPlatformTestCase.
  - For pure logic/utilities independent of the IntelliJ runtime, use plain JUnit 5 tests to keep them fast.
- Test data:
  - Place under src/test/testData/<feature>/<case> and reference with @TestDataPath("$CONTENT_ROOT/src/test/testData") plus myFixture.test* helpers.
- Patterns for common tests:
  - PSI parsing: myFixture.configureByText(FileType.INSTANCE, "...") and assert on Psi tree.
  - Rename/move: myFixture.testRename("before.ext", "after.ext", "newName").
  - Action execution: myFixture.testAction(ActionManager.getInstance().getAction("actionId")).
- Running only new tests: Use --tests filter as shown above to shorten feedback loop.

Code Style and Project Conventions
- Kotlin: Follow Kotlin official style. Enable “Reformat Code” and “Optimize Imports” on save.
- Nullability: Prefer explicit types and immutable vals. Avoid platform types escaping public APIs.
- Logging/diagnostics: For plugin internals, prefer thisLogger() or IntelliJ’s diagnostic log facilities over println.
- Messages and resources: Keep user-facing strings in src/main/resources/messages/*.properties; use the generated MyBundle for retrieval to support localization.
- Plugin XML:
  - src/main/resources/META-INF/plugin.xml defines plugin id, name, vendor, and extensions. Keep this in sync with gradle.properties (pluginGroup, pluginName).
  - Do not edit descriptor fields that are computed from README/CHANGELOG unless you update build.gradle.kts accordingly.

Troubleshooting
- Gradle can’t start due to missing Java:
  - Symptom: ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
  - Fix: Install JDK 21 and set JAVA_HOME, or configure Gradle JVM in IntelliJ to JetBrains Runtime 21.
- Plugin description extraction fails:
  - Ensure README still contains the <!-- Plugin description --> and <!-- Plugin description end --> markers and that they wrap valid Markdown.
- Tests hang or fail on startup:
  - Ensure no custom IDE instance is running with locks in the Gradle sandbox; run with --info to see which IDE/version is provisioned.

Useful Gradle Tasks
- test – run unit and platform tests
- runIde – start a sandbox IDE with the plugin installed
- buildPlugin – assemble the distributable zip
- verifyPlugin – run the Marketplace Plugin Verifier against recommended IDEs

Notes
- Version catalogs are in gradle/libs.versions.toml; upgrade platform/java/kotlin/testing versions centrally there.
- CI configs: qodana.yml and codecov.yml exist but require tokens/secrets to be effective.
