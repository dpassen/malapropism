default:
    @just --list

test:
    @clojure -X:test

watch-test:
    @clojure -X:test:watcher

[parallel]
lint: lint-kondo lint-splint

lint-kondo:
    @rg -tclojure -tedn --files | xargs clj-kondo --parallel --lint

lint-splint:
    @clojure -M:splint

check:
    @clojure -M:check

editorconfig:
    @editorconfig-checker

install:
    @clojure -T:build install

publish:
    @clojure -T:build deploy
