plugins {
    id("ecbuild.java-conventions")
    id("ecbuild.copy-conventions")
}

extra.set("copyTo", "{server}/plugins")

dependencies {
    compileOnly("cn.nukkit:nukkit")
    compileOnly(project(":ECCommons"))
}

group = "me.onebone"
description = "Actaeon"
