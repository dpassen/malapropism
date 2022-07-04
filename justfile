default:
    @just --list

test:
    @clojure -X:test

watch-test:
    @clojure -X:test:watcher

lint:
    @rg -tclojure -tedn --files | xargs clj-kondo --parallel --lint

check:
    @clojure -M:check

editorconfig:
    @git ls-files | xargs editorconfig-checker

install:
    @clojure -T:build install

publish:
    @clojure -T:build deploy
