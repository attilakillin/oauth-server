import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

group = "com.bme.jnsbbk"
version = "21-09-29"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry:1.3.1")
    implementation("org.springframework:spring-aspects:5.3.10")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    //implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    //implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
    //implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")

    implementation("mysql:mysql-connector-java:8.0.26")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.withType<BootJar> {
    archiveFileName.set(archiveBaseName.get() + "." + archiveExtension.get())
    destinationDirectory.set(file(project.buildDir))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
