// Version information for dependencies

object Versions {
    val kotlin = "1.5.32"
    val hamkrest = "1.7.0.0"
    val hamcrest = "2.1"
    val junit = "5.4.2"
    val targetJvm = "1.8"
}

object Groups {
    val kotlin = "org.jetbrains.kotlin"
    val hamkrest = "com.natpryce"
    val hamcrest = "org.hamcrest"
    val junit = "org.junit.jupiter"
}

object Deps {
    val junitApi = "${Groups.junit}:junit-jupiter-api:${Versions.junit}"
    val junitParams = "${Groups.junit}:junit-jupiter-params:${Versions.junit}"
    val junitEngine = "${Groups.junit}:junit-jupiter-engine:${Versions.junit}"
    val hamkrest = "${Groups.hamkrest}:hamkrest:${Versions.hamkrest}"
    val hamcrest = "${Groups.hamcrest}:hamcrest:${Versions.hamcrest}"
    val kotlinReflection = "${Groups.kotlin}:kotlin-reflect:${Versions.kotlin}"
}
