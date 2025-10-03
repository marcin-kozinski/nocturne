#!/usr/bin/env kotlin
val mode =
    when (args[0]) {
        "--fix" -> Mode.Fix
        "--dry-run" -> Mode.Check
        else -> throw IllegalArgumentException("You have to specify either --fix or --dry-run")
    }
val versionCatalog = java.io.File(args[1])

val source = versionCatalog.readText()
val formatted =
    source
        .lines()
        .filter { it.isNotBlank() }
        .splitWhen { it.startsWith('[') }
        .joinToString(separator = "\n\n", postfix = "\n") { it.sorted().joinToString("\n") }

when (mode) {
    Mode.Fix -> {
        // Overwrite the version catalog with the sorted version.
        versionCatalog.writeText(formatted)
        println("Done formatting ${versionCatalog.path}")
    }
    Mode.Check -> {
        if (formatted == source) {
            kotlin.system.exitProcess(0)
        } else {
            println(versionCatalog.path)
            kotlin.system.exitProcess(1)
        }
    }
}

// Utils:

enum class Mode {
    Fix,
    Check,
}

fun <Item> List<Item>.splitWhen(predicate: (Item) -> Boolean): List<List<Item>> {
    return fold(mutableListOf<MutableList<Item>>()) { splits, item ->
        if (predicate(item)) {
            splits.apply { add(mutableListOf(item)) }
        } else {
            splits.apply { lastOrNull()?.add(item) }
        }
    }
}
