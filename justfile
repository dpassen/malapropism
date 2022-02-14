default:
	@just --list

test:
	@clojure -X:test

lint:
	@clj-kondo --parallel --lint src test
