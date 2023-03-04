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

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}