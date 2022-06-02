default:
    @just --list

test:
    @clojure -X:test

lint:
    @clj-kondo --parallel --lint src test deps.edn build.edn

install:
    @clojure -T:build install

publish:
    @clojure -T:build deploy
