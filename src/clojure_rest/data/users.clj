(ns clojure-rest.data.users
  (:use ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [buddy.hashers :as bhash]
            [clojure-rest.util.utils :refer :all]
            [clojure.walk :refer [keywordize-keys]]
            [clojure-rest.db :as db]
            [clojure-rest.util.http :as h]
            [clojure-rest.sanitize.user-sanitize :as us]
            [clojure-rest.validation.user-validate :as uv]
            [clojure-rest.util.error :refer [bind-error
                                             apply-if-present
                                             bind-to
                                             err-not-found
                                             status-deleted
                                             err-server-error]]))


(def ^:private brief-query
  (str "select username, name from users where username = ?"))


(def ^:private match-query
  (str "select username, profileimage from users "
       "where username like '%' || ? || '%' order by username limit 5"))


(def ^:private deleted-query
  (str "select username, name from users where deleted = false"))


(def ^:private contact-query
  (str "select F.username, F.profileimage "
       "from users U "
       "left outer join (users_users UU inner join users F on (UU.friendid = F.usersid)) "
       "on (U.usersid = UU.usersid) "
       "where U.username = ?"))


;; String -> String
;; Hashes the given password with bcrypt + sha512, 12 iterations
(defn- hash-pass [pass]
  (bhash/encrypt pass))


;; String -> Either<{}|nil>
;; Gets the UUID of the given user
(defn get-user-id [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               ["select usersId from users where username = ?" username]
                                               (when-not (empty? results)
                                                 (first results)))))


;; String -> Either<{}|nil>
;; Returns a response with the contents of the specified username
(defn- user-brief-extract! [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               [brief-query username]
                                               (cond (empty? results) nil
                                                     :else (first results)))))


;; [{}?, Error?] -> [{}?, Error?]
(defn- bind-user-brief-extract [params]
  (bind-error #(let [res (user-brief-extract! %)]
                 (if (nil? res)
                   [nil err-not-found]
                   [res nil])) params))


;; {} -> Either<{}|nil>
;; Creates a new user with the provided content, then returns said user
(defn- user-insert! [content]
  (let [id (db/uuid)]
    (sql/with-connection (db/db-connection)
                         (let [user (assoc content :usersId id :password (hash-pass (content :password)))]
                           (sql/insert-record :users user)))
    (user-brief-extract! (content :username))))


;; [{}?, Error?] -> [{}?, Error?]
(defn- bind-user-insert [params]
  (bind-error #(let [res (user-insert! %)]
                 (if (nil? res)
                   [nil err-server-error]
                   [res nil])) params))


;; String, {} -> Either<{}|nil>
;; Updates the specified user with the provided content, then returns said user
(defn- user-update! [username content]
  (let [user (merge (uv/get-user-table username) content)]
    (sql/with-connection (db/db-connection)
                         (sql/update-values :users ["username = ?" username] user))
    (user-brief-extract! (user :username))))


;; String, [{}?, Error?] -> [{}?, Error?]
;; Binds the user-update! call to an error tuple
(defn- bind-user-update [username [val err]]
  (if (nil? err)
    (let [res (user-update! username val)]
      (if (nil? res) [nil err-server-error] [res nil]))
    [nil err]))


;; String -> ()
;; Deletes the given user from the table.
;; Returns nothing
(defn- user-delete! [username]
  (let [user-id ((get-user-id username) :usersid)]
    (sql/with-connection (db/db-connection)
                         (sql/update-values :users ["username = ?" username] {:deleted true})
                         (sql/delete-rows :sessions ["usersId = ?" user-id]))))


;; String -> Natural
;; Binds the deletion of the given user to 204 no content (resource deleted)
(defn- bind-user-delete [user]
  (->> (-!>> user user-delete!)
       (#(when (= user %) status-deleted))))


;; String, String -> ()
;; Establishes a contact relationship between the two usernames
;; TODO: Add notification table to be able to confirm a friend request
(defn- user-add! [friend-name own-name]
  (let [own-id ((get-user-id own-name) :usersid)
        friend-id ((get-user-id friend-name) :usersid)]
    (sql/with-connection (db/db-connection)
                         (sql/insert-record :users_users {:usersid own-id :friendid friend-id})
                         (sql/insert-record :users_users {:usersid friend-id :friendid own-id}))))


;; String, {} -> String
(defn- bind-user-add [friend-name own-name]
  (user-add! friend-name (own-name :username))
  own-name)


;; String -> Either<[{}]|[]>
(defn- user-contacts-extract! [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               [contact-query username]
                                               (->> (vec results)
                                                    (#(if (= ((first %) :username) nil) [] %))))))

;; String -> [{}?]
;; Returns a list of 5 users that match the supplied username
(defn- match-users [username]
  (sql/with-connection (db/db-connection)
                       (sql/with-query-results results
                                               [match-query username]
                                               (into [] results))))


;; String -> [[{}?], nil]
(defn- bind-match-users [username]
  (bind-to (match-users username)))


;; () -> Response[:body []?]
;; Returns a response with the contents of all the users in the database
(defn get-all-users []
  (response
    (sql/with-connection (db/db-connection)
                         (sql/with-query-results results
                                                 [deleted-query]
                                                 (when-not (empty? results)
                                                   (into [] results))))))


;; {"email" "username" "password"} -> Response[:body val? :status Either<200|400|500>]
;; Returns a response with either the contents of the created user, a 400 bad request, or a 500 server error
(defn create-new-user [content]
  (->> content
       us/sanitize-signup
       uv/validate-signup
       bind-user-insert
       h/wrap-response))


;; String -> Response[:body val? :status Either<200|404>]
;; Returns a response with either the contents of the given user or a 404 not found (if error)
(defn get-user [username]
  (->> username
       clojure.string/trim
       (#(if (uv/user-exists? %) [% nil] [nil err-not-found]))
       bind-user-brief-extract
       h/wrap-response))


;; String, {} -> Response[:body val? :status Either<200|404|500>]
;; Returns a response with either the contents of the updated user, a 404 not found, or a 500 server error
(defn update-user [username content]
  (if (uv/user-exists? username)
    (do
      (->> content
           us/sanitize-update
           uv/validate-update
           (apply-if-present #(bind-to (assoc % :password (hash-pass (% :password)))) :password)
           (bind-user-update username)
           h/wrap-response))
    {:status err-not-found}))


;; String -> Response[:status Either<204|404>]
;; Returns a response with either 204 no content (user deleted) or 404 (user not found)
(defn delete-user [username]
  (if (uv/user-exists? username)
    (do
      (->> username
           bind-user-delete
           h/empty-response-with-code))
    {:status err-not-found}))


;; String -> Response[:status Either<200|400|404>]
;; Adds the supplied contact username to the list username's contact list
(defn add-contact [username content]
  (if (uv/user-exists? username)
    (do
      (->> content
           us/sanitize-add
           uv/validate-add
           (bind-error #(bind-to (bind-user-add username %)))
           h/wrap-response))
    {:status 404}))


;; String -> Response[:body [{}?] :status Either<200|404>]
;; Gets all the user contacts in a vector, returns 404 if the given username does not exist
(defn get-user-contacts [username]
  (if (uv/user-exists? username)
    (response (user-contacts-extract! username))
    {:status 404}))


;; String -> Response[:body [{}?] :status 200]
;; Returns a response with either the matches of the supplied username, or 404 not found
(defn search-users [username]
  (->> username
       clojure.string/trim
       bind-match-users
       h/wrap-response))


;; String, String -> Boolean
;; Check if the supplied password matches with the hashed password of the given username
(defn pass-matches? [username password]
  (if (uv/user-exists? username)
    (sql/with-connection (db/db-connection)
                         (->> (sql/with-query-results results
                                                      ["select password from users where username = ?" username]
                                                      (into {} results))
                              (:password)
                              (bhash/check password)))))
