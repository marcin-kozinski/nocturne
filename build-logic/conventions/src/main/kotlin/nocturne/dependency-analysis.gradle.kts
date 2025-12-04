package nocturne

plugins { id("com.autonomousapps.dependency-analysis") }

dependencyAnalysis {
    issues { all { onAny { severity("fail") } } }
    structure {
        bundle("androidx-compose-foundation") {
            primary("androidx.compose.foundation:foundation")
            includeDependency("androidx.compose.foundation:foundation-layout")
        }
        bundle("androidx-compose-ui") {
            primary("androidx.compose.ui:ui")
            includeDependency("androidx.compose.ui:ui-text")
        }
        bundle("androidx-compose-ui-test") {
            primary("androidx.compose.ui:ui-test-junit4")
            includeDependency("androidx.compose.ui:ui-test")
        }
    }
    reporting { printBuildHealth(true) }
}
