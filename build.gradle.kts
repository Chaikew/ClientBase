ext["mcClientVersion"] = "1.8.9"
ext["mcpZip"] = "mcp.zip"

plugins {
    id("java")
    id("idea")
}

apply<fr.chaikew.build.MClientPlugin>()

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val bundled: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {}

tasks.withType<Jar> {
    from(bundled.map {
        if (it.isDirectory)
            it
        else
            zipTree(it).matching {
                exclude("META-INF/**")
            }
    })
}