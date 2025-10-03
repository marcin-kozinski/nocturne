package nocturne

plugins { id("org.danilopianini.gradle-pre-commit-git-hooks") }

gitHooks {
    preCommit { from { "scripts/pre-commit.sh" } }
    createHooks(overwriteExisting = true)
}
