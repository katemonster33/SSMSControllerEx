import org.gradle.internal.jvm.Jvm

plugins {
    `cpp-library`
}

library {
    binaries.configureEach {
        if(targetPlatform.targetMachine.operatingSystemFamily.isWindows) {
            val compileTask = compileTask.get()
            compileTask.includes.from("${Jvm.current().javaHome}/include")
            compileTask.includes.from("${Jvm.current().javaHome}/include/win32")

            compileTask.source.from(fileTree("src") {
                include("**/*.c")
            })

            if (toolChain is VisualCpp) {
                compileTask.compilerArgs.addAll("/TC")
            } else if (toolChain is GccCompatibleToolChain) {
                compileTask.compilerArgs.addAll("-x", "c", "-std=c11")
            }
        }
    }
}