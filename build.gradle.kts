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
    withJavadocJar()
    withSourcesJar()
}

// Strict javadoc — catches stale @link, malformed HTML, missing params, etc.
// Internal packages must be on the source path (public classes reference internal types
// via their signatures), but we disable doclint for the jextract-generated .internal/
// bindings so they don't trip warnings.
tasks.withType<Javadoc>().configureEach {
    val opts = options as org.gradle.external.javadoc.CoreJavadocOptions
    opts.addBooleanOption("Xdoclint:all,-missing", true)
    opts.addBooleanOption("Xdoclint/package:-mil.army.usace.hec.dss.internal.*", true)
    opts.addStringOption("Xwerror", "-quiet")
}

// Make `check` fail the build if javadoc is broken.
tasks.named("check") { dependsOn("javadoc") }

// -------- Public API snapshot ----------
// `apiSnapshot`        regenerates api/public-api.txt from the compiled classes.
// `generateApiSnapshotTmp` writes the current snapshot to build/tmp/public-api.txt.
// `checkApiSnapshot`   fails if the committed snapshot differs from the current code.
//                      Wired into `check` so PRs that change the public API surface
//                      show up as a file diff in review.
val apiSnapshotFile = layout.projectDirectory.file("api/public-api.txt")
val apiSnapshotTmpFile = layout.buildDirectory.file("tmp/public-api.txt")

val apiSnapshot by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Regenerate api/public-api.txt from compiled classes."
    dependsOn("compileTestJava", "classes")
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("mil.army.usace.hec.dss.tools.ApiSnapshot")
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            sourceSets["main"].output.classesDirs.singleFile.absolutePath,
            apiSnapshotFile.asFile.absolutePath
        )
    })
}

val generateApiSnapshotTmp by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Write the current public API to build/tmp/public-api.txt for diff."
    dependsOn("compileTestJava", "classes")
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("mil.army.usace.hec.dss.tools.ApiSnapshot")
    doFirst { apiSnapshotTmpFile.get().asFile.parentFile.mkdirs() }
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            sourceSets["main"].output.classesDirs.singleFile.absolutePath,
            apiSnapshotTmpFile.get().asFile.absolutePath
        )
    })
}

val checkApiSnapshot by tasks.registering {
    group = "verification"
    description = "Fail if the committed api/public-api.txt is out of date."
    dependsOn(generateApiSnapshotTmp)
    doLast {
        val committed = apiSnapshotFile.asFile
        val current = apiSnapshotTmpFile.get().asFile
        if (!committed.exists()) {
            throw GradleException(
                "api/public-api.txt is missing. Run `./gradlew apiSnapshot` and commit the result."
            )
        }
        if (committed.readText() != current.readText()) {
            val diff = ProcessBuilder("diff", "-u", committed.absolutePath, current.absolutePath)
                .redirectErrorStream(true).start()
            val out = diff.inputStream.bufferedReader().readText()
            diff.waitFor()
            throw GradleException(
                "Public API has changed — api/public-api.txt is out of date.\n" +
                "Run `./gradlew apiSnapshot` and commit the updated file.\n\n" +
                out
            )
        }
    }
}

tasks.named("check") { dependsOn(checkApiSnapshot) }

// -------- Client-perspective compile check ----------
// A separate named module that `requires mil.army.usace.hec.dss;` — so JPMS
// enforces the module boundary. If anything in `.internal` leaks into a public
// signature, or a rename/deletion breaks a client call site, this source set
// fails to compile. The code is never run; compilation alone is the test.
sourceSets {
    create("clientTest") {
        java.srcDir("src/clientTest/java")
    }
}

tasks.named<JavaCompile>("compileClientTestJava") {
    dependsOn("jar")
    // Put the built jar on the module path and compile clientTest as its own module.
    // classpath stays empty so nothing is pulled in except what mil.army.usace.hec.dss
    // explicitly exports — this is what lets the compile catch .internal leaks.
    val jarTask = tasks.named<Jar>("jar")
    inputs.files(jarTask)
    doFirst {
        options.compilerArgs = listOf(
            "--module-path", jarTask.get().archiveFile.get().asFile.absolutePath
        )
    }
    classpath = files()
    destinationDirectory.set(layout.buildDirectory.dir("classes/java/clientTest"))
}

tasks.named("check") { dependsOn("compileClientTestJava") }

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
// The hecdss.h header is pinned to a specific commit in the hec-dss repo.
// To update, change hecDssGitRef below. Run `./gradlew downloadHeader` to see a clickable link.

val jextractGroup = "code generation"
val hecDssRepo = "HydrologicEngineeringCenter/hec-dss"
val hecDssHeaderPath = "heclib/hecdss/hecdss.h"
val hecDssGitRef = project.findProperty("hecdss.gitRef")?.toString() ?: "65801a2291ae832596657ee9766eebd8863f0c42"
val headerFile = layout.buildDirectory.file("native/hecdss.h").get().asFile
val generatedSourceDir = file("src/main/java")
val bindingsPackage = "mil.army.usace.hec.dss.internal"

tasks.register("downloadHeader") {
    group = jextractGroup
    description = "Download hecdss.h from the hec-dss GitHub repository."

    inputs.property("gitRef", hecDssGitRef)
    outputs.file(headerFile)

    doLast {
        val uri = uri("https://raw.githubusercontent.com/$hecDssRepo/$hecDssGitRef/$hecDssHeaderPath")
        headerFile.parentFile.mkdirs()
        uri.toURL().openStream().use { input ->
            headerFile.outputStream().use { output -> input.copyTo(output) }
        }
        println("Downloaded $hecDssHeaderPath @ ${hecDssGitRef.take(12)}")
        println("  https://github.com/$hecDssRepo/blob/$hecDssGitRef/$hecDssHeaderPath")
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
