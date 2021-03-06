(ns app.handler.question.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :question-list
 (fn [db _]
   (get db :questions)))

(re-frame/reg-sub
 :question
 (fn [db _]
   (get db :question)))

(re-frame/reg-sub
 :question-my
 (fn [db _]
   (get db :question-my)))
