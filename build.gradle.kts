import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt)
}

val detektFormattingDependency = libs.detekt.formatting

tasks.register<Detekt>("detektAll") {
    group = "verification"
    description = "Run Detekt on all modules"
    detektClasspath.setFrom(configurations.named("detekt"))

    parallel = true
    ignoreFailures = false
    autoCorrect = false
    buildUponDefaultConfig = true

    config.setFrom(files(rootProject.file("detekt/config.yml")))
    basePath = rootProject.projectDir.absolutePath

    setSource(files(rootProject.projectDir))
    include("**/*.kt", "**/*.kts")
    exclude(
        "**/build/**",
        "**/.gradle/**",
        "**/generated/**",
        "**/src/**/generated/**"
    )

    // Console output is what Android Studio linkifies
    reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

// Optional, but fine to keep module detekt tasks consistent
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        add("detektPlugins", detektFormattingDependency)
    }

    // Configure test logging to show results for all test tasks
    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    plugins.withId("io.gitlab.arturbosch.detekt") {
        extensions.configure<DetektExtension>("detekt") {
            config.setFrom(rootProject.files("detekt/config.yml"))
            buildUponDefaultConfig = true
            ignoreFailures = false
            autoCorrect = false
            basePath = rootProject.projectDir.absolutePath
        }

        tasks.withType<Detekt>().configureEach {
            ignoreFailures = false
            basePath = rootProject.projectDir.absolutePath
            reports {
                html.required.set(false)
                xml.required.set(false)
                txt.required.set(false)
                sarif.required.set(false)
                md.required.set(false)
            }
        }
    }
}
