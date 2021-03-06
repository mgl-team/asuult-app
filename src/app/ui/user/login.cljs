(ns app.ui.user.login
  (:require
   [app.ui.nativebase :as nbase]
   [app.ui.components :as ui]
   [app.ui.text :as text]
   [steroid.rn.core :as rn]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [cljs-bean.core :as bean]
   ["native-base" :refer [ArrowForwardIcon]]
   ["react-native-vector-icons/Ionicons" :default Ionicons]
   ["react-native-vector-icons/MaterialCommunityIcons" :default MaterialCommunityIcons]))

(defn view []
  (let [show (reagent/atom true)
        mobile (reagent/atom "")
        props {:fontSize 18 :fontFamily "MongolianBaiZheng"}]
    (fn []
      [nbase/box {:h "100%" :safeArea true}
       [nbase/flex {:mt 20 :mx 10 :h "80%" :justifyContent "space-between"}
        [nbase/vstack {:space 4}
         [nbase/hstack {}
          [text/measured-text props "ᠤᠳᠠᠰᠤᠨ"]
          [text/measured-text props " ᠳ᠋ᠤᠭᠠᠷ"]]
         [nbase/input {:keyboardType "number-pad"
                       :placeholder "Input Mobile"
                       :onFocus #(reset! show false)
                       :onBlur #(reset! show true)
                       :on-change-text #(reset! mobile %)}]
         [nbase/flex {:flexDirection "row" :justifyContent "space-between"}
          [nbase/hstack {:space 2}
           [nbase/pressable
            {:onPress #(js/console.log "aaa")}
            [text/measured-text {:color "#005db4"} "Login in with name"]]
           [nbase/pressable
            {:onPress #(js/console.log "bbb")}
            [text/measured-text {:color "#005db4"} "Login in with email"]]]
          [nbase/icon-button {:w 20 :h 20 :borderRadius "full" :variant "solid" :colorScheme "indigo"
                              :justifyContent "center" :alignSelf "center" :alignItems "center"
                              :icon (reagent/as-element [nbase/icon {:as Ionicons :name "arrow-forward"}])
                              :on-press #(do
                                           (js/console.log ">>>> " @mobile)
                                           (re-frame/dispatch [:user-check-mobile @mobile]))}]]]

        (if @show
          [nbase/center
           [nbase/hstack {:space 3}
            [nbase/icon-button {:size "sm" :borderRadius "full" :variant "solid" :colorScheme "muted"
                                :icon (reagent/as-element [nbase/icon {:as Ionicons :name "logo-apple"}])}]
            [nbase/icon-button {:size "sm" :borderRadius "full" :variant "solid" :colorScheme "muted"
                                :icon (reagent/as-element [nbase/icon {:as MaterialCommunityIcons :name "wechat"}])}]]])]])))
