plugins {
    java
    `maven-publish`
}

group = "mil.army.usace.hec"
version = "1.0-SNAPSHOT"

// ------ Java Configuration -----------------------
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
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
val hecDssVersion = "7-JA-6"
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

// -------------- jextract: Generate FFM Bindings -----------------------
//
// Currently, the hecdss native zips (e.g. hecdss:7-JA-6-linux-x86_64@zip) only contain the
// shared library (.so/.dll) without the header file. The downloadHeader task works around this
// by fetching hecdss.h from the hec-dss GitHub repo's main branch.
//
// TODO: When the hec-dss build pipeline is updated to bundle hecdss.h inside the native zips,
//       switch downloadHeader to extract the header from the native artifact instead:
//
//   val headersConfig by configurations.creating
//   dependencies {
//       headersConfig("mil.army.usace.hec:hecdss:$hecDssVersion-headers@zip")
//       // or extract from one of the platform zips if the header is included there:
//       // headersConfig("mil.army.usace.hec:hecdss:$hecDssVersion-linux-x86_64@zip")
//   }
//
//   tasks.register<Copy>("downloadHeader") {
//       from(provider { headersConfig.files.map { zipTree(it) } }) {
//           include("*.h")
//       }
//       into(headerFile.parentFile)
//   }
//
//   This would guarantee the header always matches the binary version, eliminating the risk
//   of main-branch header drift. The hec-dss CMakeLists.txt or CI would need to include
//   hecdss.h in the published zip or as a separate -headers classifier artifact.

val jextractGroup = "code generation"
val headerFile = file("src/main/native/hecdss.h")
val generatedSourceDir = file("src/main/java")
val bindingsPackage = "mil.army.usace.hec.dss.internal"
val hecDssGitRef = project.findProperty("hecdss.gitRef")?.toString() ?: "main"

tasks.register("downloadHeader") {
    group = jextractGroup
    description = "Download hecdss.h from the hec-dss GitHub repository."

    outputs.file(headerFile)

    doLast {
        val uri = uri("https://raw.githubusercontent.com/HydrologicEngineeringCenter/hec-dss/$hecDssGitRef/heclib/hecdss/hecdss.h")
        headerFile.parentFile.mkdirs()
        uri.toURL().openStream().use { input ->
            headerFile.outputStream().use { output -> input.copyTo(output) }
        }
        println("Downloaded hecdss.h from ref '$hecDssGitRef' to ${headerFile.path}")
    }
}

tasks.register<Exec>("generateBindings") {
    group = jextractGroup
    description = "Generate Java FFM bindings from hecdss.h using jextract."
    dependsOn("downloadHeader")

    val jextractBin = project.findProperty("jextract.path")?.toString() ?: "jextract"

    inputs.file(headerFile)
    outputs.dir(generatedSourceDir.resolve(bindingsPackage.replace('.', '/')))

    commandLine(
        jextractBin,
        "--target-package", bindingsPackage,
        "--output", generatedSourceDir.absolutePath,
        headerFile.absolutePath
    )
}

// -------------- Publishing -----------------------
val mavenUser: String? by project
val mavenPassword: String? by project

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
