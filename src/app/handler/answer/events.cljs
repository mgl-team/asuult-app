(ns app.handler.answer.events
  (:require
   [re-frame.core :as re-frame]
   [ajax.core :as ajax]
   [cljs-bean.core :as bean]
   [app.api :as api]
   [app.db :as db]))


(re-frame/reg-event-fx
 :get-answers
 (fn [{:keys [db]} [_ id]]
   {:db    (assoc-in db [:loading :get-answers] true)
    :http-xhrio {:method                 :get
                 :uri                    (api/endpoint "questions" (str id) "answers")
                 :headers                (api/auth-header db)
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:get-answers-success]
                 :on-failure             [:api-request-error :get-answers]}}))

(re-frame/reg-event-fx
 :get-answers-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body]
     {:db (-> db
              (assoc :answers data)
              (assoc-in [:loading :get-answers] false))})))

;; -----------------------------------------------------
(re-frame/reg-event-fx
 :get-answer
 (fn [{:keys [db]} [_ pid id]]
   {:db    (assoc-in db [:loading :get-answer] true)
    :http-xhrio {:method                 :get
                 :uri                    (api/endpoint "questions" (str pid) "answers" (str id))
                 :headers                (api/auth-header db)
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:get-answer-success]
                 :on-failure             [:api-request-error :get-answer]}}))

(re-frame/reg-event-fx
 :get-answer-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body
         answer (:answer db)]
     {:db (-> db
              (assoc :answer (merge answer (select-keys body :content)))
              (assoc-in [:loading :get-answer] false))})))
;; -----------------------------------------------------
(re-frame/reg-event-fx
 :set-answer
 (fn [{db :db} [_ params]]
   {:db             (assoc db :answer params)}))
;; -----------------------------------------------------
(re-frame/reg-event-fx
 :question-focus-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body
         focus (get-in db [:question :user_focus])
         focus-count (get-in db [:question :focus_count])
         focus-count (if (= 0 focus) (inc focus-count) (dec focus-count))
         id (get-in db [:question :id])
         questions (get db :questions)
         questions (map #(if (= id (:id %))
                           (assoc % :focus_count focus-count)
                           %)
                        questions)]
     {:db (-> db
              (assoc-in [:question :user_focus] (if (= 1 focus) 0 1))
              (assoc-in [:question :focus_count] focus-count)
              (assoc :questions questions)
              (assoc-in [:loading :question-focus] false))})))

;; -----------------------------------------------------
(re-frame/reg-event-fx
 :create-answer
 (fn [{:keys [db]} [_ id params]]
   {:db    (assoc-in db [:loading :create-answer] true)
    :http-xhrio {:method                 :post
                 :uri                    (api/endpoint "questions" (str id) "answers")
                 :headers                (api/auth-header db)
                 :params                 params
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:create-answer-success]
                 :on-failure             [:api-request-error :create-answer]}}))

(re-frame/reg-event-fx
 :create-answer-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body
         id (get-in db [:question :id])
         questions (map #(if (= id (:id %))
                           (assoc % :answer_count (inc (:answer_count %)))
                           %)
                        (:questions db))
         question (:question db)
         question (assoc question :answer_count (inc (:answer_count question)))]
     {:db (-> db
              (assoc-in [:loading :create-answer] false)
              (assoc :questions questions)
              (assoc :question question))
      :dispatch-n [[:navigate-to :question-detail]
                   [:get-answers (get-in db [:question :id])]]})))

;; -----------------------------------------------------
(re-frame/reg-event-fx
 :answer-delete
 (fn [{:keys [db]} [_ pid id]]
   {:db    (assoc-in db [:loading :answer-delete] true)
    :http-xhrio {:method                 :delete
                 :uri                    (api/endpoint "questions" (str pid) "answers" (str id))
                 :headers                (api/auth-header db)
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:answer-delete-success]
                 :on-failure             [:api-request-error :answer-delete]}}))

(re-frame/reg-event-fx
 :answer-delete-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body
         id (get-in db [:question :id])
         questions (map #(if (= id (:id %))
                           (assoc % :answer_count (dec (:answer_count %)))
                           %)
                        (:questions db))
         question (:question db)
         question (assoc question :answer_count (dec (:answer_count question)))]
     {:db (-> db
              (assoc-in [:loading :answer-delete] false)
              (assoc :questions questions)
              (assoc :question question))
      :dispatch-n [[:navigate-to :question-detail]
                   [:get-answers (get-in db [:question :id])]]})))
;; -----------------------------------------------------
(re-frame/reg-event-fx
 :answer-comment-create
 (fn [{:keys [db]} [_ id params]]
   {:db    (assoc-in db [:loading :answer-comment-create] true)
    :http-xhrio {:method                 :post
                 :uri                    (api/endpoint "answers" (str id) "comments")
                 :headers                (api/auth-header db)
                 :params                 params
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:answer-comment-create-success]
                 :on-failure             [:api-request-error :answer-comment-create]}}))

(re-frame/reg-event-fx
 :answer-comment-create-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body]
     {:db (-> db
              (assoc-in [:loading :answer-comment-create] false))
      :dispatch-n [[:navigate-to :question-detail]
                   [:answer-comments (get-in db [:question :id])]]})))

;; -----------------------------------------------------
(re-frame/reg-event-fx
 :answer-comment-update
 (fn [{:keys [db]} [_ pid id params]]
   {:db    (assoc-in db [:loading :answer-comment-update] true)
    :http-xhrio {:method                 :put
                 :uri                    (api/endpoint "answers" (str pid) "comments" (str id))
                 :headers                (api/auth-header db)
                 :params                 params
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:answer-comment-update-success]
                 :on-failure             [:api-request-error :answer-comment-update]}}))

(re-frame/reg-event-fx
 :answer-comment-update-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body]
     {:db (-> db
              (assoc-in [:loading :answer-comment-update] false))
      :dispatch-n [[:navigate-to :answer-detail]
                   [:answer-comments (get-in db [:answer :id])]]})))
;; -----------------------------------------------------
(re-frame/reg-event-fx
 :answer-comment-delete
 (fn [{:keys [db]} [_ pid id]]
   {:db    (assoc-in db [:loading :answer-comment-delete] true)
    :http-xhrio {:method                 :delete
                 :uri                    (api/endpoint "answers" (str pid) "comments" (str id))
                 :headers                (api/auth-header db)
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:answer-comment-delete-success]
                 :on-failure             [:api-request-error :answer-comment-delete]}}))

(re-frame/reg-event-fx
 :answer-comment-delete-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body]
     {:db (-> db
              (assoc-in [:loading :answer-comment-delete] false))
      :dispatch-n [[:navigate-to :question-detail]
                   [:get-answers (get-in db [:question :id])]]})))

;; -----------------------------------------------------
(re-frame/reg-event-fx
 :answer-comments
 (fn [{:keys [db]} [_ id]]
   {:db    (assoc-in db [:loading :answer-comments] true)
    :http-xhrio {:method                 :get
                 :uri                    (api/endpoint "answers" (str id) "comments")
                 :headers                (api/auth-header db)
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:answer-comments-success]
                 :on-failure             [:api-request-error :answer-comments]}}))

(re-frame/reg-event-fx
 :answer-comments-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body]
     {:db (-> db
              (assoc-in [:loading :answer-comments] false)
              (assoc :answer-comments data))})))

;; -----------------------------------------------------
(re-frame/reg-event-fx
 :answer-report
 (fn [{:keys [db]} [_ id params]]
   {:db    (assoc-in db [:loading :answer-report] true)
    :http-xhrio {:method                 :post
                 :uri                    (api/endpoint "answers" (str id) "report")
                 :headers                (api/auth-header db)
                 :params                 params
                 :format                 (ajax/json-request-format)
                 :response-format        (ajax/json-response-format {:keywords? true})
                 :on-success             [:answer-report-success]
                 :on-failure             [:api-request-error :answer-report]}}))

(re-frame/reg-event-fx
 :answer-report-success
 (fn [{db :db} [_ body]]
   (let [{data :data} body]
     {:db (-> db
              (assoc-in [:loading :answer-report] false)
              (assoc :answer-report data))})))
