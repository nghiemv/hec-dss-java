plugins {
    java
    `maven-publish`
}

group = "mil.army.usace.hec"
version = "1.0-SNAPSHOT"

// ------ Java Configuration -----------------------
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

// ------- Dependencies -----------------------
repositories {
    mavenCentral()
    maven { url = uri("https://www.hec.usace.army.mil/nexus/repository/maven-public/") }
}

// Define platform-specific configurations
val windowsNatives by configurations.creating
val linuxNatives by configurations.creating

// Define Version Numbers
val hecDssVersion = "7-IV-1"
val nativeLibLoaderVersion = "2.5.0"
val junitVersion = "5.10.0"

dependencies {
    // HEC-DSS Binaries
    windowsNatives("mil.army.usace.hec:hecdss:$hecDssVersion-win-x86_64@zip")
    linuxNatives("mil.army.usace.hec:hecdss:$hecDssVersion-linux-x86_64@zip")
    // NativeLibLoader to load the native libraries seamlessly
    implementation("org.scijava:native-lib-loader:$nativeLibLoaderVersion")
    // JUnit Testing Framework
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// -------------- Testing -----------------------

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// -------------- Natives -----------------------
tasks.named<ProcessResources>("processResources") {
    dependsOn(extractAllNatives)
}

val nativeLibrariesGroup = "native libraries"

val extractAllNatives by tasks.registering {
    group = nativeLibrariesGroup
    description = "Extract all supported OS native libraries from the HEC-DSS zip file."
    dependsOn(tasks.named("extractWindowsNatives"), tasks.named("extractLinuxNatives"))
}

registerNativeTask("Windows", windowsNatives, "windows_64")
registerNativeTask("Linux", linuxNatives, "linux_64")

fun registerNativeTask(name: String, sources: FileCollection, platform: String) {
    tasks.register<Copy>("extract${name}Natives") {
        group = nativeLibrariesGroup
        description = "Extract $platform native libraries from the HEC-DSS zip file."
        from(provider { sources.files.map { zipTree(it) } })
        into(layout.buildDirectory.dir("resources/main/natives/${platform}"))
        inputs.files(sources)
        outputs.dir(layout.buildDirectory.dir("resources/main/natives/${platform}"))
    }
}

// -------------- Publishing -----------------------
val mavenUser: String by project
val mavenPassword: String by project

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "mil.army.usace.hec"
            artifactId = "hec-dss-java"

            from(components["java"])
        }
    }
    repositories {
        maven {
            credentials {
                username = mavenUser
                password = mavenPassword
            }
            val releasesRepoUrl = uri("https://www.hec.usace.army.mil/nexus/repository/maven-releases/")
            val snapshotsRepoUrl = uri("https://www.hec.usace.army.mil/nexus/repository/maven-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

tasks.named("publish") { dependsOn("jar") }