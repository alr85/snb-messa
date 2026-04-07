// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
val defaultVersionCode by extra(1)
val defaultVersionName by extra("1.0.6")
val defaultVersionCode1 by extra(6)
