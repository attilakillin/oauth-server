plugins {
    id("org.springframework.boot") version "2.5.6" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false

    id("org.jmailen.kotlinter") version "3.6.0" apply false

    kotlin("jvm") version "1.5.30" apply false
    kotlin("kapt") version "1.5.30" apply false
    kotlin("plugin.spring") version "1.5.30" apply false
    kotlin("plugin.jpa") version "1.5.30" apply false
}
