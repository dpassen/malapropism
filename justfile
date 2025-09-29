[default, private]
commands:
    @just --list

# Run unit tests
test:
    @clojure -X:test

# Watch source code and run tests accordingly
watch-test:
    @clojure -X:test:watcher

# Lint source code
[parallel]
lint: lint-kondo lint-splint

# Lint source code using clj-kondo
[private]
lint-kondo:
    @rg -tclojure -tedn --files | xargs clj-kondo --parallel --lint

# Lint source code using splint
[private]
lint-splint:
    @clojure -M:splint

# Check for reflection using clj-check
check:
    @clojure -M:check

# Check for editorconfig violations using editorconfig-checker
editorconfig:
    @editorconfig-checker

# Install locally
install:
    @clojure -T:build install

# Deploy to Clojars
publish:
    @clojure -T:build deploy
