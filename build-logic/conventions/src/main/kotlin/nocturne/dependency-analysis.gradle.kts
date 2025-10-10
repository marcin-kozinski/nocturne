package nocturne

plugins { id("com.autonomousapps.dependency-analysis") }

dependencyAnalysis {
    reporting { printBuildHealth(true) }
    issues { all { onAny { severity("fail") } } }
}
