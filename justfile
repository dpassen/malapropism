[default, private]
commands:
    @just --list

# Run unit tests
[group('test')]
test:
    @clojure -X:test

# Watch source code and run tests accordingly
[group('test')]
watch-test:
    @clojure -X:test:watcher

# Lint source code
[parallel, group('dev')]
lint: lint-kondo lint-splint

# Lint source code using clj-kondo
[private, group('dev')]
lint-kondo:
    @rg -tclojure -tedn --files | xargs clj-kondo --parallel --lint

# Lint source code using splint
[private, group('dev')]
lint-splint:
    @clojure -M:splint

# Check for reflection using clj-check
[group('dev')]
check:
    @clojure -M:check

# Check for editorconfig violations using editorconfig-checker
[group('dev')]
editorconfig:
    @editorconfig-checker

# Install locally
[group('release')]
install:
    @clojure -T:build install

# Deploy to Clojars
[group('release')]
publish:
    @clojure -T:build deploy
