(ns iwaswhere-web.misc-utils-test
  "Here, we test some helpter functions. These tests are written in cljc and
   can also run on the JVM, as we only have pure punctions in the target
   namespace."
  (:require #?(:clj  [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :refer-macros [deftest testing is]])
            [iwaswhere-web.utils.misc :as u]))

(deftest duration-string-test
  (testing "test output for some different durations"
    (is (= (u/duration-string 0) ""))
    (is (= (u/duration-string 11) "11s"))
    (is (= (u/duration-string 111) "1m 51s"))
    (is (= (u/duration-string 1111) "18m 31s"))
    (is (= (u/duration-string 11111) "3h 5m 11s"))
    (is (= (u/duration-string 111111) "30h 51m 51s"))
    (is (= (u/duration-string 7931.999999999999) "2h 12m 11s"))))

(def test-entry
  {:mentions       #{}
   :tags           #{}
   :timezone       "Europe/Berlin"
   :utc-offset     -120
   :timestamp      1465059173965
   :md             ""})

(def test-entry2
  {:mentions       #{}
   :tags           #{"#cljc"}
   :timezone       "Europe/Berlin"
   :utc-offset     -120
   :timestamp      1465059173966
   :md             "Moving to #cljc"})

(def pvt-entry
  {:mentions       #{}
   :tags           #{"#pvt"}
   :timezone       "Europe/Berlin"
   :utc-offset     -120
   :timestamp      1465059173965
   :md             "Some #pvt entry"})

(def pvt-entry2
  {:mentions       #{}
   :tags           #{"#private"}
   :timezone       "Europe/Berlin"
   :utc-offset     -120
   :timestamp      1465059173965
   :md             "Some #private entry"})

(def pvt-entry3
  {:mentions       #{}
   :tags           #{"#nsfw"}
   :timezone       "Europe/Berlin"
   :utc-offset     -120
   :timestamp      1465059173965
   :md             "Something #nsfw"})

(def pvt-test-conf {:pvt-hashtags #{"#pvt" "#private" "#nsfw" "#consumption"}})

(deftest pvt-filter-test
  (testing "properly detects privacy status of entries"
    (let [pvt-filter-fn (u/pvt-filter pvt-test-conf {})]
      (is (pvt-filter-fn test-entry))
      (is (pvt-filter-fn test-entry2))
      (is (not (pvt-filter-fn pvt-entry)))
      (is (not (pvt-filter-fn pvt-entry2)))
      (is (not (pvt-filter-fn pvt-entry3))))))

(deftest double-ts-to-long-test
  (testing "correctly converts number"
    (is (= 100000 (u/double-ts-to-long 100))))
  (testing "converted number is of correct type"
    (is (= (type (u/double-ts-to-long 100)) #?(:clj  java.lang.Long
                                             :cljs js/Number))))
  (testing "calling with other than number results in nil"
    (is (nil? (u/double-ts-to-long nil)))
    (is (nil? (u/double-ts-to-long "123")))))

(def completed-entry
  {:arrival-date        "2016-08-16 11:29:41 +0000"
   :departure-date      "2016-08-16 16:33:19 +0000"
   :tags                #{"#visit" "#import"}
   :departure-timestamp 1.471365199000049E9
   :arrival-timestamp   1.471346981391931E9
   :horizontal-accuracy 29.1
   :type                "visit"
   :longitude           10.0
   :latitude            53.0
   :device              "iPhone"
   :timestamp           1471346981391
   :md                  "Duration: 303.6m #visit"})

(deftest visit-timestamps-test
  (testing "entry with completed visit parsed correctly"
    (is (= {:arrival-ts   1471346981391
            :departure-ts 1471365199000}
           (u/visit-timestamps completed-entry))))
  (testing "entry without visit parsed correctly"
    (is (= {:arrival-ts   nil
            :departure-ts nil}
           (u/visit-timestamps (-> completed-entry
                                   (dissoc :arrival-timestamp)
                                   (dissoc :departure-timestamp))))))
  (testing "entry with incomplete visit parsed correctly"
    (is (= {:arrival-ts   1471346981391
            :departure-ts nil}
           (u/visit-timestamps (merge completed-entry
                                      {:departure-timestamp 64092211200}))))))

(deftest find-missing-test
  (let [entries-map (atom {1465059173965 test-entry
                           1465059173966 test-entry2})]
    (testing "properly maps existing entry"
      (is (= (let [mapper-fn (u/find-missing-entry entries-map (fn [_msg]))]
               (mapv mapper-fn [1465059173965 1465059173966]))
             [test-entry test-entry2])))
    (testing "calls put-fn for missing entry"
      (let [sent-msg (atom [])
            fake-put-fn (fn [msg] (reset! sent-msg msg))
            mapper-fn (u/find-missing-entry entries-map fake-put-fn)
            res (mapv mapper-fn [1465059173965 1465059173966 1465059173967])]
        (is (= res [test-entry test-entry2 {:timestamp 1465059173967}]))
        (is (= @sent-msg [:entry/find {:timestamp 1465059173967}]))))))

(deftest deep-merge-test
  (testing "maps are merged properly"
    (is (= {:a {:b {:c 2
                    :d 2}}
            :b 2}
           (u/deep-merge {:a {:b {:c 1
                                  :d 2}}
                          :b 1}
                         {:b 2}
                         {:a {:b {:c 2}}}))))
  (testing "handles nil properly"
    (is (= (u/deep-merge nil nil nil)
           nil))
    (is (= (u/deep-merge nil {:a 1})
           {:a 1}))
    (is (= (u/deep-merge {:a 1} nil nil)
           {:a 1}))))

(deftest count-words-test
  (testing "counts words properly"
    (is (= 69
           (u/count-words {:md "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum  "}))))
  (testing "counts words properly when word at beginning of line"
    (is (= 29
           (u/count-words {:md "Alabama \nArizona\nCalifornia\nColorado\nConnecticut\nDelaware\nDistrictOfColumbia\nFlorida\nIllinois\nLouisiana\nMaine\nMaryland\nMassachussets\nMississippi \nNevada\nNew Hampshire\nNew Jersey\nNew York\nOregon\nPennsylvania\nRhode Island\nUtah\nVermont\nVirgina\nWashington\n\n"})))))
