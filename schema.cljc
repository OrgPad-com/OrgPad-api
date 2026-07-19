(ns orgpad.api.schema
  "Public Malli schemas for OrgPad API EDN request bodies.")

(def max-title-length 200)
(def max-description-length 2000)
(def max-tags 60)
(def max-tag-length 30)

(def string-uuid-or-text-id
  [:re #"^[A-Za-z0-9_-]+$"])

(def uuid-or-string?
  [:or uuid? string-uuid-or-text-id])

(def id-or-email
  [:or uuid? string?])

(def non-negative-index
  [:and :int [:>= 0]])

(def max-canvas-coordinate 1000000)

(defn finite-number?
  [num]
  (and (number? num)
       #?(:clj  (Double/isFinite (double num))
          :cljs (js/Number.isFinite num))))

(def finite-number
  [:fn finite-number?])

(def canvas-coordinate
  [:and finite-number
   [:>= (- max-canvas-coordinate)]
   [:<= max-canvas-coordinate]])

(def canvas-pos
  [:tuple canvas-coordinate canvas-coordinate])

(def spectrum-colors
  [:color/red
   :color/orange
   :color/yellow
   :color/lime
   :color/green
   :color/mint
   :color/teal
   :color/blue
   :color/blueberry
   :color/purple
   :color/orchid
   :color/pink])

(def grayscale-colors
  [:color/white
   :color/light-gray
   :color/gray])

(def all-colors
  (vec (concat grayscale-colors spectrum-colors)))

(def spectrum-colors-enum
  (into [:enum] spectrum-colors))

(def all-colors-enum
  (into [:enum] all-colors))

(def unit-content-type
  [:enum :hiccup :html :plain :markdown])

(def title-sizes
  [:enum :props/h1 :props/h2 :props/h3])

(def link-weights
  [:enum :props/none :props/strong])

(def link-arrowheads
  [:enum :props/none :props/single :props/double])

(def user-permissions
  [:enum :permission/view :permission/comment :permission/edit :permission/admin])

;; Request body schemas
;;
;; These vars are the public entry points for validating API EDN request bodies:
;;
;; - create-orgpage: POST /api/v1/o
;; - op: one operation inside the operations endpoint body
;; - ops: POST /api/v1/o/{orgpage-id}/ops
;; - file-rename: POST /api/v1/file/{file-id}/rename
;; - image-rename: POST /api/v1/img/{image-id}/rename
;; - permission-op: POST /api/v1/o/{orgpage-id}/share
;; - screenshot-orgpage: POST /api/v1/o/{orgpage-id}/screenshot
;; - print-orgpage: POST /api/v1/o/{orgpage-id}/print

(def create-orgpage
  [:maybe
   [:map {:closed true}
    [:orgpage/title {:optional true} [:string {:max max-title-length}]]
    [:orgpage/description {:optional true} [:string {:max max-description-length}]]
    [:orgpage/tags {:optional true} [:set {:max max-tags}
                                     [:string {:max max-tag-length}]]]
    [:orgpage/color {:optional true} spectrum-colors-enum]]])

(def op
  [:multi {:dispatch first}
   [:orgpage/update-meta
    [:tuple
     [:= :orgpage/update-meta]
     [:map {:closed true}
     [:orgpage/title {:optional true} [:string {:max max-title-length}]]
     [:orgpage/description {:optional true} [:string {:max max-description-length}]]
     [:orgpage/tags {:optional true} [:set {:max max-tags}
                                      [:string {:max max-tag-length}]]]
     [:orgpage/color {:optional true} spectrum-colors-enum]
     [:orgpage/init-fragments {:optional true}
      [:map {:closed true}
       [:init-fragments/default {:optional true} [:maybe uuid-or-string?]]
       [:init-fragments/small-screen {:optional true} [:maybe uuid-or-string?]]
       [:init-fragments/screenshot {:optional true} [:maybe uuid-or-string?]]]]]]]

   [:orgpage/remove-image
    [:tuple
     [:= :orgpage/remove-image]
     [:map {:closed true}
      [:image/id uuid-or-string?]]]]

   [:orgpage/remove-file
    [:tuple
     [:= :orgpage/remove-file]
     [:map {:closed true}
      [:file/id uuid-or-string?]]]]

   [:orgpage/add-image
    [:tuple
     [:= :orgpage/add-image]
     [:map {:closed true}
      [:image/id uuid-or-string?]
      [:image/token {:optional true} uuid-or-string?]]]]

   [:orgpage/add-file
    [:tuple
     [:= :orgpage/add-file]
     [:map {:closed true}
      [:file/id uuid-or-string?]
      [:file/token {:optional true} uuid-or-string?]]]]

   [:unit/create
    [:tuple
     [:= :unit/create]
     [:and
      [:map
       [:unit/type {:optional true} [:enum :unit/book :unit/page]]]
      [:multi {:dispatch :unit/type}
      [:unit/book
       [:map {:closed true}
        [:unit/id {:optional true} uuid-or-string?]
        [:unit/type [:= :unit/book]]
        [:unit/pos canvas-pos]
        [:unit/child-unit-ids [:vector uuid-or-string?]]
        [:unit/title {:optional true} :string]
        [:unit/props {:optional true}
         [:map {:closed true}
          [:props/color {:optional true} all-colors-enum]
          [:props/title-size {:optional true} title-sizes]]]
        [:unit/text-id {:optional true} string-uuid-or-text-id]]]
      [:unit/page
       [:map {:closed true}
        [:unit/id {:optional true} uuid-or-string?]
        [:unit/type [:= :unit/page]]
        [:unit/parent-id uuid-or-string?]
        [:unit/content {:optional true} :any]
        [:unit/content-type {:optional true} unit-content-type]
        [:unit/text-id {:optional true} string-uuid-or-text-id]]]
      [:malli.core/default                                      ;; Shorthand form.
       [:map {:closed true}
        [:unit/id {:optional true} uuid-or-string?]
        [:unit/page-id {:optional true} uuid-or-string?]
        [:unit/pos canvas-pos]
        [:unit/title {:optional true} :string]
        [:unit/content {:optional true} :any]
        [:unit/content-type {:optional true} unit-content-type]
        [:unit/props {:optional true}
         [:map {:closed true}
          [:props/color {:optional true} all-colors-enum]
          [:props/title-size {:optional true} title-sizes]]]
        [:unit/text-id {:optional true} string-uuid-or-text-id]
        [:unit/page-text-id {:optional true} string-uuid-or-text-id]]]]]]]

   [:unit/update
    [:tuple
     [:= :unit/update]
     [:map {:closed true}
     [:unit/id {:optional true} uuid-or-string?]
     [:unit/title {:optional true} [:maybe :string]]
     [:unit/content {:optional true} [:maybe :any]]
     [:unit/appended-content {:optional true} :any]
     [:unit/content-type {:optional true} unit-content-type]
     [:unit/text-id {:optional true} [:maybe string-uuid-or-text-id]]]]]

   [:unit/remove
    [:tuple
     [:= :unit/remove]
     [:map {:closed true}
      [:unit/id uuid-or-string?]]]]

   [:unit/move
    [:tuple
     [:= :unit/move]
     [:map {:closed true}
      [:unit/id uuid-or-string?]
      [:unit/pos canvas-pos]]]]

   [:unit/change-props
    [:tuple
     [:= :unit/change-props]
     [:map {:closed true}
      [:unit/id uuid-or-string?]
      [:unit/props
       [:map {:closed true}
        [:props/color {:optional true} all-colors-enum]
        [:props/title-size {:optional true} title-sizes]]]]]]

   [:unit/add-child-unit
    [:tuple
     [:= :unit/add-child-unit]
     [:map {:closed true}
      [:unit/id uuid-or-string?]
      [:unit/child-id uuid-or-string?]
      [:unit/index {:optional true} non-negative-index]]]]

   [:unit/remove-child-unit
    [:tuple
     [:= :unit/remove-child-unit]
     [:map {:closed true}
      [:unit/id uuid-or-string?]
      [:unit/child-id uuid-or-string?]]]]

   [:unit/move-child-unit
    [:tuple
     [:= :unit/move-child-unit]
     [:map {:closed true}
      [:unit/id uuid-or-string?]
      [:unit/child-id uuid-or-string?]
      [:unit/new-index non-negative-index]]]]

   [:link/create
    [:tuple
     [:= :link/create]
     [:map {:closed true}
      [:link/id {:optional true} uuid-or-string?]
      [:link/endpoint-ids [:tuple uuid-or-string? uuid-or-string?]]
      [:link/props {:optional true}
       [:map {:closed true}
        [:props/color {:optional true} all-colors-enum]
        [:props/weight {:optional true} link-weights]
        [:props/arrowhead {:optional true} link-arrowheads]]]]]]

   [:link/remove
    [:tuple
     [:= :link/remove]
     [:map {:closed true}
      [:link/id uuid-or-string?]]]]

   [:link/straighten
    [:tuple
     [:= :link/straighten]
     [:map {:closed true}
      [:link/id uuid-or-string?]]]]

   [:link/flip
    [:tuple
     [:= :link/flip]
     [:map {:closed true}
      [:link/id uuid-or-string?]]]]

   [:link/change-props
    [:tuple
     [:= :link/change-props]
     [:map {:closed true}
      [:link/id uuid-or-string?]
      [:link/props
       [:map {:closed true}
        [:props/color {:optional true} all-colors-enum]
        [:props/weight {:optional true} link-weights]
        [:props/arrowhead {:optional true} link-arrowheads]]]]]]

   [:math/create
    [:tuple
     [:= :math/create]
     [:map {:closed true}
      [:math/id {:optional true} uuid-or-string?]
      [:math/page-id uuid-or-string?]
      [:math/source :string]
      [:math/block {:optional true} [:maybe :boolean]]
      [:math/type [:enum :math/math :math/chemistry]]]]]

   [:math/update
    [:tuple
     [:= :math/update]
     [:map {:closed true}
      [:math/id uuid-or-string?]
      [:math/source {:optional true} :string]
      [:math/block {:optional true} [:maybe :boolean]]
      [:math/type {:optional true} [:enum :math/math :math/chemistry]]]]]

   [:math/remove
    [:tuple
     [:= :math/remove]
     [:map {:closed true}
      [:math/id uuid-or-string?]]]]

   [:embed/create
    [:tuple
     [:= :embed/create]
     [:map {:closed true}
      [:embed/id {:optional true} uuid-or-string?]
      [:embed/page-id uuid-or-string?]
      [:embed/source {:optional true} :string]
      [:embed/file-id {:optional true} uuid-or-string?]
      [:embed/file-token {:optional true} uuid-or-string?]
      [:embed/text-id {:optional true} string-uuid-or-text-id]]]]

   [:embed/update
    [:tuple
     [:= :embed/update]
     [:map {:closed true}
      [:embed/id uuid-or-string?]
      [:embed/source {:optional true} :string]
      [:embed/file-id {:optional true} uuid-or-string?]
      [:embed/file-token {:optional true} uuid-or-string?]
      [:embed/text-id {:optional true} [:maybe string-uuid-or-text-id]]]]]

   [:embed/remove
    [:tuple
     [:= :embed/remove]
     [:map {:closed true}
      [:embed/id uuid-or-string?]]]]

   [:path/create
    [:tuple
     [:= :path/create]
     [:map {:closed true}
      [:path/id {:optional true} uuid-or-string?]
      [:path/title {:optional true} :string]]]]

   [:path/update-title
    [:tuple
     [:= :path/update-title]
     [:map {:closed true}
      [:path/id uuid-or-string?]
      [:path/title [:maybe :string]]]]]

   [:path/remove
    [:tuple
     [:= :path/remove]
     [:map {:closed true}
      [:path/id uuid-or-string?]]]]

   [:path/add-step
    [:tuple
     [:= :path/add-step]
     [:map {:closed true}
      [:path/id uuid-or-string?]
      [:step/id {:optional true} uuid-or-string?]
      [:step/index {:optional true} non-negative-index]
      [:step/audio-id {:optional true} uuid-or-string?]
      [:step/opened-page-ids {:optional true} [:set uuid-or-string?]]
      [:step/closed-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/focused-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/shown-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/hidden-book-ids {:optional true} [:set uuid-or-string?]]]]]

   [:path/remove-step
    [:tuple
     [:= :path/remove-step]
     [:map {:closed true}
      [:step/id uuid-or-string?]]]]

   [:path/update-units
    [:tuple
     [:= :path/update-units]
     [:map {:closed true}
      [:step/id uuid-or-string?]
      [:step/add-opened-page-ids {:optional true} [:set uuid-or-string?]]
      [:step/remove-opened-page-ids {:optional true} [:set uuid-or-string?]]
      [:step/add-closed-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/remove-closed-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/add-focused-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/remove-focused-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/add-shown-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/remove-shown-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/add-hidden-book-ids {:optional true} [:set uuid-or-string?]]
      [:step/remove-hidden-book-ids {:optional true} [:set uuid-or-string?]]]]]

   [:path/set-audio
    [:tuple
     [:= :path/set-audio]
     [:map {:closed true}
      [:step/id uuid-or-string?]
      [:step/audio-id [:maybe uuid-or-string?]]]]]

   [:path/move-step
    [:tuple
     [:= :path/move-step]
     [:map {:closed true}
      [:step/id uuid-or-string?]
      [:step/new-index non-negative-index]]]]

   [:fragment/create
    [:tuple
     [:= :fragment/create]
     [:map {:closed true}
      [:fragment/id {:optional true} uuid-or-string?]
      [:fragment/text-id string-uuid-or-text-id]
      [:fragment/title {:optional true} :string]
      [:fragment/opened-page-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/closed-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/focused-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/shown-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/hidden-book-ids {:optional true} [:set uuid-or-string?]]]]]

   [:fragment/update
    [:tuple
     [:= :fragment/update]
     [:map {:closed true}
      [:fragment/id uuid-or-string?]
      [:fragment/title {:optional true} [:maybe :string]]
      [:fragment/text-id {:optional true} string-uuid-or-text-id]]]]

   [:fragment/update-units
    [:tuple
     [:= :fragment/update-units]
     [:map {:closed true}
      [:fragment/id uuid-or-string?]
      [:fragment/add-opened-page-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/remove-opened-page-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/add-closed-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/remove-closed-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/add-focused-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/remove-focused-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/add-shown-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/remove-shown-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/add-hidden-book-ids {:optional true} [:set uuid-or-string?]]
      [:fragment/remove-hidden-book-ids {:optional true} [:set uuid-or-string?]]]]]

   [:fragment/remove
    [:tuple
     [:= :fragment/remove]
     [:map {:closed true}
      [:fragment/id uuid-or-string?]]]]])

(def ops
  [:vector op])

(def file-rename
  [:map {:closed true}
   [:file/filename :string]
   [:file/orgpage-ids {:optional true} [:set uuid-or-string?]]])

(def image-rename
  [:map {:closed true}
   [:image/filename :string]
   [:image/orgpage-ids {:optional true} [:set uuid-or-string?]]])

(def permission-op
  [:multi {:dispatch first}
   [:permission/set-public-permission
    [:tuple
     [:= :permission/set-public-permission]
     [:enum :permission/none :permission/view]]]
   [:permission/reset-tokens
    [:tuple
     [:= :permission/reset-tokens]]]
   [:permission/add-user
    [:tuple
     [:= :permission/add-user]
     id-or-email
     user-permissions]]
   [:permission/set-user-permission
    [:tuple
     [:= :permission/set-user-permission]
     id-or-email
     user-permissions]]
   [:permission/remove-user
    [:tuple
     [:= :permission/remove-user]
     id-or-email]]])

(def screenshot-orgpage
  [:maybe
   [:map {:closed true}
    [:screenshot/resolution {:optional true}
     [:tuple [:int {:min 300 :max 4000}]
      [:int {:min 300 :max 4000}]]]
    [:screenshot/theme {:optional true} [:enum :light :dark]]
    [:screenshot/fragment-id {:optional true} uuid-or-string?]
    [:screenshot/open {:optional true} [:enum :all]]
    [:screenshot/opened-page-ids {:optional true} [:set uuid-or-string?]]
    [:screenshot/focused-book-ids {:optional true} [:set uuid-or-string?]]
    [:screenshot/hidden-book-ids {:optional true} [:set uuid-or-string?]]]])

(def print-orgpage
  [:maybe
   [:map {:closed true}
    [:print/color {:optional true} [:enum :white :gray :dm]]
    [:print/size {:optional true} [:enum :a4 :letter :ratio-4-to-3 :ratio-16-to-9]]
    [:print/orientation {:optional true} [:enum :landscape :portrait]]
    [:print/padding {:optional true} [:or [:int {:min 0 :max 500}]
                                      [:tuple [:int {:min 0 :max 500}]
                                       [:int {:min 0 :max 500}]]]]
    [:print/path-id {:optional true} uuid-or-string?]
    [:print/fragment-id {:optional true} uuid-or-string?]
    [:print/open {:optional true} [:enum :all]]
    [:print/opened-page-ids {:optional true} [:set uuid-or-string?]]
    [:print/focused-book-ids {:optional true} [:set uuid-or-string?]]
    [:print/hidden-book-ids {:optional true} [:set uuid-or-string?]]]])
