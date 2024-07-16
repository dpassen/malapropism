# malapropism

[![test](https://github.com/dpassen/malapropism/actions/workflows/test.yaml/badge.svg)](https://github.com/dpassen/malapropism/actions/workflows/test.yaml)
[![lint](https://github.com/dpassen/malapropism/actions/workflows/lint.yaml/badge.svg)](https://github.com/dpassen/malapropism/actions/workflows/lint.yaml)
[![check](https://github.com/dpassen/malapropism/actions/workflows/check.yaml/badge.svg)](https://github.com/dpassen/malapropism/actions/workflows/check.yaml)
[![editorconfig](https://github.com/dpassen/malapropism/actions/workflows/editorconfig.yaml/badge.svg)](https://github.com/dpassen/malapropism/actions/workflows/editorconfig.yaml)
[![downloads](https://img.shields.io/clojars/dt/org.passen/malapropism.svg?color=opal)](https://clojars.org/org.passen/malapropism)

An [omniconf](https://github.com/grammarly/omniconf) inspired config library
using [malli](https://github.com/metosin/malli)

## Latest version

malapropism is deployed to [Clojars](https://clojars.org)

[![Clojars Project](https://img.shields.io/clojars/v/org.passen/malapropism.svg)](https://clojars.org/org.passen/malapropism/)

## Rationale

Configuration is one of the peripheries of our systems.
It's a good place to ensure correctness.
Enter [malli](https://github.com/metosin/malli), a library that I've really
grown to appreciate.

Configuration should be flexible. Most solutions in this space assume too much.

With malapropism,

- Stick it in an [atom](https://clojuredocs.org/clojure.core/atom)? Yeah!
- Put it in a [delay](https://clojuredocs.org/clojure.core/delay)? Okay
- Keep it behind [core.cache](https://github.com/clojure/core.cache) or
  [core.memoize](https://github.com/clojure/core.memoize)? Sure, why not?
- Deliver it to a [promise](https://clojuredocs.org/clojure.core/promise)?
  I could see it
- Re-compute it each and every time? I wouldn’t
- Have a single configuration? Yeah, that makes sense
- Have more than one? No one's going to stop you

## Usage

```clojure
(require '[org.passen.malapropism.core :as malapropism])

(def config-schema
  [:map
   [:env-key :keyword]
   [:scm-rev :string]
   [:port :int]
   [:prefix
    {:default "/api"}
    :string]])

(-> (malapropism/with-schema config-schema)
    (malapropism/with-values-from-env)
    (malapropism/with-values-from-system)
    (malapropism/with-values-from-map
      {:env-key :dev
       :scm-rev "923345"
       :port    8080
       :prefix  "/web"})
    (malapropism/verify! :verbose? true))
```

## Consumers

These companies use malapropism. Feel free to add your company to the list.

- [Splash Financial](https://www.splashfinancial.com/)

## License

Copyright © 2022-2023 Derek Passen

Released under an MIT license.
