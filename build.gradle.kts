plugins {
    java
    `maven-publish`
}

group = "mil.army.usace.hec"
version = "0.0.4"

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
// Only the x86_64 macOS native is bundled; Apple Silicon hosts run it under Rosetta. The arm64
// darwin dylib is deferred because it carries a code signature that the rpath repair below
// (patchMacosNativeRpath) would invalidate, and re-signing it would require running on macOS.
val macosX64Natives by configurations.creating

// Define Version Numbers
val hecDssVersion = "7-JA-7"
val nativeLibLoaderVersion = "2.5.0"
val junitVersion = "5.10.0"

dependencies {
    // HEC-DSS Binaries
    windowsNatives("mil.army.usace.hec:hecdss:$hecDssVersion-win-x86_64@zip")
    linuxNatives("mil.army.usace.hec:hecdss:$hecDssVersion-linux-x86_64@zip")
    macosX64Natives("mil.army.usace.hec:hecdss:$hecDssVersion-darwin-x86_64@zip")
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
    description = "Extract all supported OS native libraries from the HEC-DSS zip files."
    dependsOn(
        tasks.named("extractWindowsNatives"),
        tasks.named("extractLinuxNatives"),
        patchMacosNativeRpath,
    )
}

// All three bundled platforms publish a 7-JA-7 native, so a missing artifact fails the build.
registerNativeTask("Windows", windowsNatives, "windows_64")
registerNativeTask("Linux", linuxNatives, "linux_64")
registerNativeTask("MacosX64", macosX64Natives, "osx_64")

fun registerNativeTask(name: String, sources: Configuration, platform: String) {
    tasks.register<Copy>("extract${name}Natives") {
        group = nativeLibrariesGroup
        description = "Extract $platform native libraries from the HEC-DSS zip file."
        from(provider { sources.files.map { zipTree(it) } })
        // 7-JA-7+ zips bundle hecdss.h next to the library — keep the header out of the jar.
        exclude("**/*.h")
        into(layout.buildDirectory.dir("resources/main/natives/${platform}"))
        inputs.files(sources)
        outputs.dir(layout.buildDirectory.dir("resources/main/natives/${platform}"))
    }
}

// WHY: the published darwin libhecdss.dylib resolves zlib via `@rpath/libz.1.dylib`, but its only
// LC_RPATH is the hec-dss build machine's path (.../build/_deps/zlib-build), which exists nowhere
// else — so dyld cannot find zlib and the load fails on every other Mac (UnsatisfiedLinkError
// surfaced as "Cannot load native library 'hecdss'"). Rewrite that rpath in place to /usr/lib so
// `@rpath/libz.1.dylib` resolves to the system zlib every Mac ships. The x86_64 dylib is unsigned,
// so the byte edit needs no re-signing and is safe to perform on any build OS.
val patchMacosNativeRpath by tasks.registering {
    group = nativeLibrariesGroup
    description = "Repoint the macOS native's @rpath to /usr/lib so it can resolve the system zlib."
    dependsOn("extractMacosX64Natives")
    val dylib = layout.buildDirectory.file("resources/main/natives/osx_64/libhecdss.dylib")
    inputs.files(tasks.named("extractMacosX64Natives"))
    outputs.file(dylib)
    doLast { rewriteMachoRpathsToUsrLib(dylib.get().asFile) }
}

// Rewrite every LC_RPATH in a 64-bit little-endian Mach-O to /usr/lib, in place. /usr/lib (8 bytes)
// always fits in the original rpath's null-padded string slot, so command sizes are unchanged and
// the file stays structurally identical.
fun rewriteMachoRpathsToUsrLib(file: java.io.File) {
    val data = file.readBytes()
    fun u32(o: Int): Int =
        (data[o].toInt() and 0xff) or
            ((data[o + 1].toInt() and 0xff) shl 8) or
            ((data[o + 2].toInt() and 0xff) shl 16) or
            ((data[o + 3].toInt() and 0xff) shl 24)
    require(u32(0) == 0xfeedfacf.toInt()) { "Not a 64-bit little-endian Mach-O: ${file.name}" }
    val newPath = "/usr/lib".toByteArray(Charsets.UTF_8)
    val lcRpath = 0x8000001c.toInt()
    val ncmds = u32(16)
    var p = 32 // mach_header_64 size
    var patched = 0
    repeat(ncmds) {
        val cmd = u32(p)
        val size = u32(p + 4)
        if (cmd == lcRpath) {
            val strStart = p + u32(p + 8)
            val region = p + size - strStart
            require(newPath.size + 1 <= region) { "rpath slot too small in ${file.name}" }
            for (j in 0 until region) data[strStart + j] = if (j < newPath.size) newPath[j] else 0
            patched++
        }
        p += size
    }
    require(patched > 0) { "no LC_RPATH found in ${file.name}" }
    file.writeBytes(data)
    logger.lifecycle("Patched $patched rpath(s) -> /usr/lib in ${file.name}")
}

// -------------- jextract: Generate FFM Bindings -----------------------
// hecdss.h ships inside the HEC-DSS native zip (7-JA-7+), so the bindings are generated from the
// exact header that matches the bundled binary. This replaces the previous GitHub raw download,
// which pinned a separate commit that could drift from the library ABI.

val jextractGroup = "code generation"
val headerFile = layout.buildDirectory.file("native/hecdss.h").get().asFile
val generatedSourceDir = file("src/main/java")
val bindingsPackage = "mil.army.usace.hec.dss.internal"

val extractHeader by tasks.registering(Copy::class) {
    group = jextractGroup
    description = "Extract hecdss.h from the HEC-DSS native zip for jextract."
    from(provider { linuxNatives.files.map { zipTree(it) } }) { include("**/*.h") }
    into(headerFile.parentFile)
    inputs.files(linuxNatives)
    outputs.file(headerFile)
}

tasks.register<Exec>("generateBindings") {
    group = jextractGroup
    description = "Generate Java FFM bindings from hecdss.h using jextract."
    dependsOn(extractHeader)

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
// Credentials live in ~/.gradle/gradle.properties as nexusUser / nexusPassword. mavenUser /
// mavenPassword are still honoured as a fallback so existing CI overrides keep working.
val nexusUser: String? by project
val nexusPassword: String? by project
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
                username = nexusUser ?: mavenUser
                password = nexusPassword ?: mavenPassword
            }
            val releasesRepoUrl = uri("https://www.hec.usace.army.mil/nexus/repository/maven-releases/")
            val snapshotsRepoUrl = uri("https://www.hec.usace.army.mil/nexus/repository/maven-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

tasks.named("publish") { dependsOn("jar") }
