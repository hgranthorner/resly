(ns org.resly.linkedin
  (:require
   [org.resly.rake :as rake]
   [org.resly.utils :as utils]))

(comment
  (-> (utils/read-edn-file "resources/test_data.edn")
      (select-keys [:first-name
                    :last-name
                    :accomplishment-projects
                    :accomplishment-organizations
                    :experiences
                    :education])
      :experiences
      (->>
       (mapv #(assoc % :rake (rake/apply-rake (:title %)
                                              (or (:description %) ""))))))
  ;; Interesting keys:
  ;; :first-name, :last-name
  ;; :accomplishment-projects
  ;; :accomplishment-organizations
  ;; :experiences
  ;; :education
  )
