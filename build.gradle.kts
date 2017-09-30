import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.champeau.gradle.JMHPlugin
import me.champeau.gradle.JMHPluginExtension
import net.minecraftforge.gradle.tasks.DeobfuscateJar
import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.ReobfTaskFactory
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import net.minecraftforge.gradle.user.patcherUser.forge.ForgePlugin
import nl.javadude.gradle.plugins.license.LicenseExtension
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.DescribeOp
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.script.lang.kotlin.*
import org.spongepowered.asm.gradle.plugins.MixinExtension
import org.spongepowered.asm.gradle.plugins.MixinGradlePlugin
import kotlin.apply

// Gradle repositories and dependencies
buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            setUrl("http://files.minecraftforge.net/maven")
        }
        maven {
            setUrl("http://repo.spongepowered.org/maven")
        }
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.ajoberstar:grgit:2.0.0-milestone.1")
        classpath("org.spongepowered:mixingradle:0.4-SNAPSHOT")
        classpath("com.github.jengelman.gradle.plugins:shadow:1.2.3")
        classpath("gradle.plugin.nl.javadude.gradle.plugins:license-gradle-plugin:0.13.1")
        classpath("me.champeau.gradle:jmh-gradle-plugin:0.3.1")
        classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
    }
}

plugins {
    java
    idea
    eclipse
}

apply {
    plugin<ForgePlugin>()
    plugin<ShadowPlugin>()
    plugin<MixinGradlePlugin>()
    plugin<LicensePlugin>()
    plugin<JMHPlugin>()
    from("build.gradle.groovy")
}

// tasks
val build by tasks
val jar: Jar by tasks
val shadowJar: ShadowJar by tasks
val test: Test by tasks
val processResources: ProcessResources by tasks
val deobfMcSRG: DeobfuscateJar by tasks
val deobfMcMCP: DeobfuscateJar by tasks

defaultTasks = listOf("licenseFormat", "build")

//it can't be named forgeVersion because ForgeExtension has property named forgeVersion
val theForgeVersion by project
val theMappingsVersion by project
val malisisCoreVersion by project
val malisisCoreMinVersion by project

val licenseYear by project
val projectName by project

val versionSuffix by project
val versionMinorFreeze by project

val sourceSets = the<JavaPluginConvention>().sourceSets
val mainSourceSet = sourceSets["main"]
val minecraft = the<ForgeExtension>()

version = getModVersion()
group = "cubichunks"

(mainSourceSet as ExtensionAware).extra["refMap"] = "cubicchunks.mixins.refmap.json"

configure<IdeaModel> {
    module.apply {
        inheritOutputDirs = true
    }
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}

configure<BasePluginConvention> {
    archivesBaseName = "CubicChunks"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<MixinExtension> {
    token("MC_FORGE", extractForgeMinorVersion())
}

configure<ForgeExtension> {
    version = theForgeVersion as String
    runDir = "run"
    mappings = theMappingsVersion as String

    isUseDepAts = true

    replace("@@VERSION@@", project.version)
    replace("/*@@DEPS_PLACEHOLDER@@*/",
            ",dependencies = \"after:forge@[13.20.1.2454,);after:malisiscore@[$malisisCoreMinVersion,)\"")
    replace("@@MALISIS_VERSION@@", malisisCoreMinVersion)
    replaceIn("cubicchunks/CubicChunks.java")

    val args = listOf(
            "-Dfml.coreMods.load=cubicchunks.asm.CubicChunksCoreMod", //the core mod class, needed for mixins
            "-Dmixin.env.compatLevel=JAVA_8", //needed to use java 8 when using mixins
            "-Dmixin.debug.verbose=true", //verbose mixin output for easier debugging of mixins
            "-Dmixin.debug.export=true", //export classes from mixin to runDirectory/.mixin.out
            "-Dcubicchunks.debug=true", //various debug options of cubic chunks mod. Adds items that are not normally there!
            "-XX:-OmitStackTraceInFastThrow", //without this sometimes you end up with exception with empty stacktrace
            "-Dmixin.checks.interfaces=true", //check if all interface methods are overriden in mixin
            "-Dfml.noGrab=false", //change to disable Minecraft taking control over mouse
            "-ea", //enable assertions
            "-da:io.netty..." //disable netty assertions because they sometimes fail
    )

    clientJvmArgs.addAll(args)
    serverJvmArgs.addAll(args)
}

configure<LicenseExtension> {
    val ext = (this as HasConvention).convention.extraProperties
    ext["project"] = projectName
    ext["year"] = licenseYear
    exclude("**/*.info")
    exclude("**/package-info.java")
    exclude("**/*.json")
    exclude("**/*.xml")
    exclude("assets/*")
    exclude("cubicchunks/server/chunkio/async/forge/*") // Taken from forge
    header = file("HEADER.txt")
    ignoreFailures = false
    strictCheck = true
    mapping(mapOf("java" to "SLASHSTAR_STYLE"))
}

configure<NamedDomainObjectContainer<ReobfTaskFactory.ReobfTaskWrapper>> {
    create("shadowJar").apply {
        mappingType = ReobfMappingType.SEARGE
    }
}
// temporary until malisiscore updates
deobfMcSRG.apply {
    isFailOnAtError = false
}
deobfMcMCP.apply{
    isFailOnAtError = false
}
build.dependsOn("reobfShadowJar")

configure<JMHPluginExtension> {
    iterations = 10
    benchmarkMode = listOf("thrpt")
    batchSize = 16
    timeOnIteration = "1000ms"
    fork = 1
    threads = 1
    timeUnit = "ms"
    verbosity = "NORMAL"
    warmup = "1000ms"
    warmupBatchSize = 16
    warmupForks = 1
    warmupIterations = 10
    profilers = listOf("perfasm")
    jmhVersion = "1.17.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven {
        setUrl("https://oss.sonatype.org/content/repositories/public/")
    }
    maven {
        setUrl("http://repo.spongepowered.org/maven")
    }
}

dependencies {
    // configurations, some of them aren't necessary but added for consistency when specifying "extendsFrom"
    val jmh by configurations
    val forgeGradleMc by configurations
    val forgeGradleMcDeps by configurations
    val forgeGradleGradleStart by configurations
    val compile by configurations
    val testCompile by configurations
    val deobfCompile by configurations

    compile("com.flowpowered:flow-noise:1.0.1-SNAPSHOT")
    testCompile("junit:junit:4.11")
    testCompile("org.hamcrest:hamcrest-junit:2.0.0.0")
    testCompile("it.ozimov:java7-hamcrest-matchers:0.7.0")
    testCompile("org.mockito:mockito-core:2.1.0-RC.2")
    testCompile("org.spongepowered:launchwrappertestsuite:1.0-SNAPSHOT")

    compile("org.spongepowered:mixin:0.7.4-SNAPSHOT") {
        isTransitive = false
    }

    compile(project(":RegionLib"))

    // use deobfProvided for now to avoid crash with malisiscore asm, but still have it compiling
    deobfCompile("net.malisis:malisiscore:$malisisCoreVersion") {
        isTransitive = false
    }

    jmh.extendsFrom(compile)
    jmh.extendsFrom(forgeGradleMc)
    jmh.extendsFrom(forgeGradleMcDeps)
    testCompile.extendsFrom(forgeGradleGradleStart)
    testCompile.extendsFrom(forgeGradleMcDeps)
}

// this is needed because it.ozimov:java7-hamcrest-matchers:0.7.0 depends on guava 19, while MC needs guava 21
configurations.all { resolutionStrategy { force("com.google.guava:guava:21.0") } }

jar.apply {
    jarConfig()
}

task<Jar>("jarDev") {
    from(mainSourceSet.output)
    jarConfig()
    classifier = "dev"
    tasks["assemble"].dependsOn(this)
}

fun Jar.jarConfig(): Jar {
    exclude("LICENSE.txt", "log4j2.xml")
    manifest.attributes["FMLAT"] = "cubicchunks_at.cfg"
    manifest.attributes["FMLCorePlugin"] = "cubicchunks.asm.CubicChunksCoreMod"
    manifest.attributes["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
    manifest.attributes["TweakOrder"] = "0"
    manifest.attributes["ForceLoadAsMod"] = "true"
    return this
}

shadowJar.apply {
    relocate("com.flowpowered", "cubicchunks.com.flowpowered")
    exclude("log4j2.xml")
    classifier = ""
}

test.apply {
    systemProperty("lwts.tweaker", "cubicchunks.tweaker.MixinTweakerServer")
}

processResources.apply {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.version)
    inputs.property("mcversion", minecraft.version)

    // replace stuff in mcmod.info, nothing else
    from(mainSourceSet.resources.srcDirs) {
        include("mcmod.info")

        // replace version and mcversion
        expand(mapOf("version" to project.version, "mcversion" to minecraft.version))
    }

    // copy everything else, thats not the mcmod.info
    from(mainSourceSet.resources.srcDirs) {
        exclude("mcmod.info")
    }
}

val writeModVersion by tasks.creating {
    dependsOn("build")
    file("VERSION").writeText("VERSION=" + version)
}

fun getMcVersion(): String {
    if (minecraft.version == null) {
        return (theForgeVersion as String).split("-")[0]
    }
    return minecraft.version
}

//returns version string according to this: http://mcforge.readthedocs.org/en/latest/conventions/versioning/
//format: MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH(-final/rcX/betaX)
//rcX and betaX are not implemented yet
fun getModVersion(): String {
    return try {
        val git = Grgit.open()
        val describe = DescribeOp(git.repository).call()
        val branch = getGitBranch(git)
        getModVersion_do(describe, branch);
    } catch(ex: RuntimeException) {
        logger.error("Unknown error when accessing git repository! Are you sure the git repository exists?", ex)
        String.format("%s-%s.%s.%s%s%s", getMcVersion(), "9999", "9999", "9999", "", "NOVERSION")
    }
}

fun getGitBranch(git: Grgit): String {
    var branch: String = git.branch.current.name
    if (branch.equals("HEAD")) {
        branch = when {
            System.getenv("TRAVIS_BRANCH")?.isEmpty() == false -> // travis
                System.getenv("TRAVIS_BRANCH")
            System.getenv("GIT_BRANCH")?.isEmpty() == false -> // jenkins
                System.getenv("GIT_BRANCH")
            System.getenv("BRANCH_NAME")?.isEmpty() == false -> // ??? another jenkins alternative?
                System.getenv("BRANCH_NAME")
            else -> throw RuntimeException("Found HEAD branch! This is most likely caused by detached head state! Will assume unknown version!")
        }
    }

    if (branch.startsWith("origin/")) {
        branch = branch.substring("origin/".length)
    }
    return branch
}

fun getModVersion_do(describe: String, branch: String): String {
    if (branch.startsWith("MC_")) {
        val branchMcVersion = branch.substring("MC_".length)
        if (branchMcVersion != getMcVersion()) {
            logger.warn("Branch version different than project MC version! MC version: " +
                    getMcVersion() + ", branch: " + branch + ", branch version: " + branchMcVersion)
        }
    }

    //branches "master" and "MC_something" are not appended to version sreing, everything else is
    //only builds from "master" and "MC_version" branches will actually use the correct versioning
    //but it allows to distinguish between builds from different branches even if version number is the same
    val branchSuffix = if (branch == "master" || branch.startsWith("MC_")) "" else ("-" + branch.replace("[^a-zA-Z0-9.-]", "_"))

    val baseVersionRegex = "v[0-9]+\\.[0-9]+"
    val unknownVersion = String.format("%s-UNKNOWN_VERSION%s%s", getMcVersion(), versionSuffix, branchSuffix)
    if (!describe.contains('-')) {
        //is it the "vX.Y" format?
        if (describe.matches(Regex(baseVersionRegex))) {
            return String.format("%s-%s.0.0%s%s", getMcVersion(), describe, versionSuffix, branchSuffix)
        }
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    //Describe format: vX.Y-build-hash
    val parts = describe.split("-")
    if (!parts[0].matches(Regex(baseVersionRegex))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    if (!parts[1].matches(Regex("[0-9]+"))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    val mcVersion = getMcVersion()
    val modAndApiVersion = parts[0].substring(1)
    //next we have commit-since-tag
    val commitSinceTag = Integer.parseInt(parts[1])

    val minorFreeze = if ((versionMinorFreeze as String).isEmpty()) -1 else Integer.parseInt(versionMinorFreeze as String)

    val minor = if (minorFreeze < 0) commitSinceTag else minorFreeze
    val patch = if (minorFreeze < 0) 0 else (commitSinceTag - minorFreeze)

    val version = String.format("%s-%s.%d.%d%s%s", mcVersion, modAndApiVersion, minor, patch, versionSuffix, branchSuffix)
    return version
}

fun extractForgeMinorVersion(): String {
    // version format: MC_VERSION-MAJOR.MINOR.?.BUILD
    return (theForgeVersion as String).split(Regex("-")).getOrNull(1)?.split(Regex("\\."))?.getOrNull(1) ?:
            throw RuntimeException("Invalid forge version format: " + theForgeVersion)
}
