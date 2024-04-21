import java.util.UUID

plugins {
    id("com.android.library")
}

android {
    namespace = "org.bobrteam.models"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }

    buildFeatures {
        buildConfig = false
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("$buildDir/generated/assets")
        }
    }
}

val genUUID = tasks.register("genUUID") {
    val uuid = UUID.randomUUID().toString()
    val odir = file("$buildDir/generated/assets/vosk-model-small-ru-0.22")
    val ofile = file("$odir/uuid")

    doLast {
        mkdir(odir)
        ofile.writeText(uuid)
    }
}

tasks.getByName("preBuild") {
    dependsOn(genUUID)
}
