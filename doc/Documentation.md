# SaleOkase Documentation

## Dependencies

### Java

From the [official site](http://clojure.org):

> Clojure is a dynamic programming language that targets the Java Virtual Machine.

If you don't have Java installed, you'll need to do so. Clojure requires [Java](http://java.sun.com/javase/downloads/index.jsp) 1.6 or greater.

### Leiningen

Leiningen is a package manager and build tool for Clojure.

To start working on the project, you'll need to install [Leiningen](http://leiningen.org) 2.0.0 or above. It is available for Windows, Linux and OSX.

### H2

We use [H2](http://www.h2database.com) as our database.

Go ahead and download the `.jar` file and put it on the main project folder (at the root level).

## Installation

First, pick a spot for your project folder.

Run `git clone https://github.com/ergl/saleokase.git`. This command should clone all the source code and assets into the `saleokase` directory.

If you haven't installed Leiningen yet, you should do it now, as we're going to need it for the rest of the tutorial.

## Configuration

#### Google Maps API

We should configure the *Google Maps API* token. In `resources/public/index.html`:

```html
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDu2jTpsKWwFMvxPz77ZJHMhXWPYsNZueY"></script>
```

This access token is only valid for requests coming from the old static site, so you'll need a valid one for local development. You can get one at the [google developers console](https://console.developers.google.com).


#### Database settings

Go ahead and do `java -jar h2-X.X.XXX.jar` from the console - this should open a browser window with the H2 Console.

![](./assets/h2console.png)

Choose a filename, username and password - then remember to put those in `profiles.clj`:

```clojure
{:dev {:env {...
             :h2-user "your-db-username"
             :h2-password "your-db-password"
             :h2-type "your-db-filepath"
             :h2-script ""}}
 ;; The db settings under the :test settings don't need to be changed
 :test {:env {...
              :h2-user ""
              :h2-password ""
              :h2-type "mem:documents"
              :h2-script "INIT=RUNSCRIPT FROM './schema.sql'"}}}
```

This tells the program to use our database file when running, but skip it and use an in-memory database for the tests - this way our test can't change real data.

Once this is done, click on the *Connect* button to get to the main console.

![](./assets/h2console-1.png)

Copy the contents of the `schema.sql` file and put them on the text area of the console - then click the *Run* button. Once this is done you can disconnect from the console (by clicking the top left button). You can now stop the database process by pressing `ctrl+C` on the terminal where you ran `java -jar ....`.

Please note that if you forget to stop the process, the program won't run, as the database only accepts a single connection at the same time.

#### Server secret key

To sign our own client access tokens, we use a secret key. It's important that this key remains secret, but bear in mind that the key we use in production **will** be different from the one we use during development. This means all access tokens that we issue during development will be invalid when we go live.

First, you should pick a good, random, secret key. I use a 32-byte, random base64 string, but any good random sequence generator will do.

Once you have one, add it to the `profiles.clj` file:

```clojure
{:dev {:env {:secret-key "your-secret-key"
             ...}}
 :test {:env {:secret-key "your-secret-key"
              ...}}}
```

Please note that without providing a secret key, the server will crash.


#### Server settings and port

You may want to change the server settings or port. To do that, you'll have to change the ring settins in the `project.clj` file:

```clojure
:ring {:handler clojure-rest.handler/app
       :port 5000
       :auto-reload? true
       :auto-refresh? false}
```
Where:

- `:handler` - Program entry point
- `:port` - Local listening port
- `:auto-reload?` - Tells *ring* to hotload any changes that we make to our files.
- `:auto-refresh?` - Tells *ring* to reload the browser page on server update

If you want to set up local logging, you can do so in the `handler.clj` file. Just change

```clojure
(def app (prod-handler))
```

to this

```clojure
(def app (dev-handler))
```

Please note that changing this will make ring write to `stdout` any incoming requests, including the ones generated during testing. I find this annoying, so I almost never turn logging on while developing.

#### Profiles

After going through all these steps, yout `profiles.clj` file should look like this:

```clojure
{:dev {:env {:secret-key "your-secret-key"
             :h2-user "your-db-username"
             :h2-password "your-db-password"
             :h2-type "your-db-filepath"
             :h2-script ""}}
 :test {:env {:secret-key "your-secret-key"
              :h2-user ""
              :h2-password ""
              :h2-type "mem:documents"
              :h2-script "INIT=RUNSCRIPT FROM './schema.sql'"}}}
```

Go ahead and add `:dependencies [[ring/ring-mock "0.2.0"]]` before the `:env` section of `:dev`:

```clojure
{:dev {:dependencies [[ring/ring-mock "0.2.0"]]
       :env {:secret-key "your-secret-key"
             :h2-user "your-db-username"
             :h2-password "your-db-password"
             :h2-type "your-db-filepath"
             :h2-script ""}}
 :test {:env {:secret-key "your-secret-key"
              :h2-user ""
              :h2-password ""
              :h2-type "mem:documents"
              :h2-script "INIT=RUNSCRIPT FROM './schema.sql'"}}}

```

Now you're all set.


## Developing

Point your terminal to  the `saleokase` folder, and run `lein deps`. This command will download all the dependencies and libraries that we use for the project. You only need to do this once (actually, you don't have to - leiningen will download them as you need them when running the server - but this way you'll only need to wait once)

Then, run `lein test` to check that the project does build and runs correctly. If you receive any error, please check the Configuration section. If that does not help, please send me a message or open an issue.

With your terminal opened in the project folder, run `lein ring server-headless`. This command will automatically start the backend server.

This command will also reload any changes you make to any clojure file without restarting the server. Occasionally, you will change something and the server won't be able to reload it (such as changes to the schema of a function). In those rare cases, restarting the server will do.
