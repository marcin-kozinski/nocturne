package nocturne

plugins { id("org.danilopianini.gradle-pre-commit-git-hooks") }

gitHooks {
    preCommit { from { "scripts/pre-commit.sh" } }
    hook("pre-push") { from { "scripts/pre-push.sh" } }
    createHooks(overwriteExisting = true)
}
