# SaleOkase-REST [![Build Status](https://travis-ci.org/ergl/clojure-rest.svg?branch=master)](https://travis-ci.org/ergl/clojure-rest)

REST api for [saleokase.github.io](http://saleokase.github.io) (WIP, not implemented yet).

### Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0.0 or above installed.

### Getting it up and running

To start a web server for the application, run `lein run` inside the project folder. Then point your browser to `http://localhost:5000`

You may run `lein test` first to check that all tests pass ( they should! ).


### Developing

First, in [handler.clj](https://github.com/ergl/clojure-rest/blob/6a8ec80128ddb11fbbcbdc16c2787d656b6d97c1/src/clojure_rest/handler.clj#L114), you should switch

```clojure
(def app (prod-handler))
```

to this

```clojure
(def app (dev-handler))
```

That way, all server requests will be printed to `stdout`.

Then, run the application with `lein ring server` instead of `lein run`.

That should automatically open your browser pointing to `http://localhost:5000`.

After this, any other change you make to any of the application files should be automatically changed in the browser. No need to restart the server!

If you want your changes to automatically reload the browser tab, you may switch

```clojure
:ring {:handler clojure-rest.handler/app
         :port 5000
         :auto-reload? true
         :auto-refresh? false}
```

to this

```clojure
:ring {:handler clojure-rest.handler/app
         :port 5000
         :auto-reload? true
         :auto-refresh? true}
```

in [project.clj](https://github.com/ergl/clojure-rest/blob/6a8ec80128ddb11fbbcbdc16c2787d656b6d97c1/project.clj#L23)



## Licensing

This project is distributed under the GPLv3 license. For more information, please check the [License](./LICENSE).

All additional libraries used belong to their respective authors.
