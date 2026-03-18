// 1. 构建脚本配置（插件依赖）
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20") 
    }
}

// 2. 项目仓库配置
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// 3. 应用插件
plugins {
    id("com.android.application")
    kotlin("android")
}

// 4. Android模块配置
android {
    compileSdk = 34
    namespace = "org.somebody.hcode"

    defaultConfig {
        applicationId = "org.somebody.hcode"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    lintOptions {
        checkReleaseBuilds = false
        abortOnError = false
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    // 新增：启用ViewBinding（按模块配置）
    buildFeatures {
        viewBinding = true
    }
}

// 5. 依赖配置
dependencies {
    // 基础依赖
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Kotlin标准库
    implementation(kotlin("stdlib-jdk25"))
}
