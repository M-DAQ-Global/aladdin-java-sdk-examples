plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.m-daq:mdaq-aladdin-java-sdk:0.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}