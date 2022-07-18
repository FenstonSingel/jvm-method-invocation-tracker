import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.fennmata.fcbp.java"
version = "0.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    implementation("org.ow2.asm:asm:9.3")
    implementation("org.ow2.asm:asm-commons:9.3")
    implementation("org.ow2.asm:asm-util:9.3")
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        manifest {
            attributes["Premain-Class"] = "net.fennmata.fcbp.java.invoketracker.PremainClass"
            attributes["Can-Retransform-Classes"] = "true"
        }
    }

    shadowJar {
        relocate("org.objectweb.asm", "net.fennmata.fcbp.java.invoketracker.asm")
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

}
