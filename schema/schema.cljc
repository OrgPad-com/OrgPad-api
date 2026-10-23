(ns orgpad.api.schema
  "Public Malli schemas for OrgPad API EDN request and response bodies.")

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
    [:orgpage/title {:optional true} [:string {:min 1 :max max-title-length}]]
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
     [:orgpage/title {:optional true} [:string {:min 1 :max max-title-length}]]
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
        [:unit/title {:optional true} [:string {:min 1}]]
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
        [:unit/title {:optional true} [:string {:min 1}]]
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
     [:unit/title {:optional true} [:maybe [:string {:min 1}]]]
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

   [:math/set-macros
    [:tuple
     [:= :math/set-macros]
     [:map {:closed true}
      [:math/macros [:maybe [:string {:min 1}]]]]]]

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

;; Response body schemas
;;
;; The API serializes these EDN-shaped values to JSON when application/json is
;; requested. JSON field names are documented by the corresponding TypeScript
;; types and api.schema.json. No schema is provided for multipart requests,
;; binary successes, or successful 204 responses.

(def no-body
  "Routes without a request body."
  [:maybe :nil])

(def api-error
  "All API error responses have these fields. Error-code-specific detail keys
  are intentionally open; see errors.types.ts for their discriminated shapes."
  [:map
   [:error
    [:map
     [:code keyword?]
     [:message :string]
     [:docs :string]]]])

(defn api-response
  "The union of a successful EDN response body and the shared error envelope."
  [success]
  [:or success api-error])

(def utc-timestamp :string)

(def user
  [:map {:closed true}
   [:user/id uuid-or-string?]
   [:user/first-name {:optional true} :string]
   [:user/last-name {:optional true} :string]])

(def init-fragments
  [:map {:closed true}
   [:init-fragments/default {:optional true} uuid-or-string?]
   [:init-fragments/small-screen {:optional true} uuid-or-string?]
   [:init-fragments/screenshot {:optional true} uuid-or-string?]])

(def orgpage-metadata
  [:map {:closed true}
   [:orgpage/id uuid-or-string?]
   [:orgpage/title {:optional true} :string]
   [:orgpage/description {:optional true} :string]
   [:orgpage/tags {:optional true} [:set :string]]
   [:orgpage/color spectrum-colors-enum]
   [:orgpage/owner uuid-or-string?]
   [:orgpage/creation-time utc-timestamp]
   [:orgpage/last-load-time {:optional true} utc-timestamp]
   [:orgpage/last-edit-time {:optional true} utc-timestamp]
   [:orgpage/math-macros {:optional true} :string]
   [:orgpage/init-fragments {:optional true} init-fragments]])

(def response-unit-props
  [:map {:closed true}
   [:props/color all-colors-enum]
   [:props/title-size title-sizes]])

(def book
  [:map {:closed true}
   [:unit/id uuid-or-string?]
   [:unit/text-id {:optional true} string-uuid-or-text-id]
   [:unit/type [:= :unit/book]]
   [:unit/title {:optional true} :string]
   [:unit/pos canvas-pos]
   [:unit/child-unit-ids [:vector uuid-or-string?]]
   [:unit/props response-unit-props]])

(def page
  [:map {:closed true}
   [:unit/id uuid-or-string?]
   [:unit/text-id {:optional true} string-uuid-or-text-id]
   [:unit/type [:= :unit/page]]
   [:unit/parent-id uuid-or-string?]
   [:unit/content {:optional true} :any]])

(def unit
  [:or book page])

(def response-link-props
  [:map {:closed true}
   [:props/color all-colors-enum]
   [:props/weight {:optional true} link-weights]
   [:props/arrowhead link-arrowheads]])

(def link
  [:map {:closed true}
   [:link/id uuid-or-string?]
   [:link/endpoint-ids [:tuple uuid-or-string? uuid-or-string?]]
   [:link/props {:optional true} response-link-props]])

(def math
  [:map {:closed true}
   [:math/id uuid-or-string?]
   [:math/page-id uuid-or-string?]
   [:math/type [:enum :math/math :math/chemistry]]
   [:math/source :string]
   [:math/block {:optional true} :boolean]
   [:math/width finite-number]
   [:math/height finite-number]
   [:math/vertical-align finite-number]
   [:math/svg :string]])

(def embed
  [:or
   [:map {:closed true}
    [:embed/id uuid-or-string?]
    [:embed/page-id uuid-or-string?]
    [:embed/text-id {:optional true} string-uuid-or-text-id]
    [:embed/source :string]]
   [:map {:closed true}
    [:embed/id uuid-or-string?]
    [:embed/page-id uuid-or-string?]
    [:embed/text-id {:optional true} string-uuid-or-text-id]
    [:embed/file-id uuid-or-string?]]])

(def file-attachment
  [:map {:closed true}
   [:file/id uuid-or-string?]
   [:file/token uuid-or-string?]
   [:file/filename :string]
   [:file/content-type :string]
   [:file/size finite-number]
   [:file/last-modified utc-timestamp]])

(def thumbnail-size
  [:map {:closed true}
   [:thumbnail/width finite-number]
   [:thumbnail/height finite-number]
   [:thumbnail/size finite-number]])

(def image-attachment
  [:map {:closed true}
   [:image/id uuid-or-string?]
   [:image/token uuid-or-string?]
   [:image/filename :string]
   [:image/width finite-number]
   [:image/height finite-number]
   [:image/format :string]
   [:image/thumbnail-format {:optional true} :string]
   [:image/size finite-number]
   [:image/last-modified utc-timestamp]
   [:image/url {:optional true} :string]
   [:image/thumbnail-sizes [:vector thumbnail-size]]])

(def fragment
  [:map {:closed true}
   [:fragment/id uuid-or-string?]
   [:fragment/text-id {:optional true} string-uuid-or-text-id]
   [:fragment/title {:optional true} :string]
   [:fragment/opened-page-ids {:optional true} [:set uuid-or-string?]]
   [:fragment/closed-book-ids {:optional true} [:set uuid-or-string?]]
   [:fragment/focused-book-ids {:optional true} [:set uuid-or-string?]]
   [:fragment/shown-book-ids {:optional true} [:set uuid-or-string?]]
   [:fragment/hidden-book-ids {:optional true} [:set uuid-or-string?]]])

(def path
  [:map {:closed true}
   [:path/id uuid-or-string?]
   [:path/title {:optional true} :string]
   [:path/step-ids [:vector uuid-or-string?]]])

(def path-step
  [:map {:closed true}
   [:step/id uuid-or-string?]
   [:step/audio-id {:optional true} uuid-or-string?]
   [:step/opened-page-ids {:optional true} [:set uuid-or-string?]]
   [:step/closed-book-ids {:optional true} [:set uuid-or-string?]]
   [:step/focused-book-ids {:optional true} [:set uuid-or-string?]]
   [:step/shown-book-ids {:optional true} [:set uuid-or-string?]]
   [:step/hidden-book-ids {:optional true} [:set uuid-or-string?]]])

(def orgpage-objects
  [:map {:closed true}
   [:orgpage/units {:optional true} [:vector unit]]
   [:orgpage/links {:optional true} [:vector link]]
   [:orgpage/maths {:optional true} [:vector math]]
   [:orgpage/embeds {:optional true} [:vector embed]]
   [:orgpage/files {:optional true} [:vector file-attachment]]
   [:orgpage/images {:optional true} [:vector image-attachment]]
   [:orgpage/fragments {:optional true} [:vector fragment]]
   [:orgpage/paths {:optional true} [:vector path]]
   [:orgpage/path-steps {:optional true} [:vector path-step]]])

(def orgpage
  [:map {:closed true}
   [:orgpage/id uuid-or-string?]
   [:orgpage/title {:optional true} :string]
   [:orgpage/description {:optional true} :string]
   [:orgpage/tags {:optional true} [:set :string]]
   [:orgpage/color spectrum-colors-enum]
   [:orgpage/owner uuid-or-string?]
   [:orgpage/creation-time utc-timestamp]
   [:orgpage/last-load-time {:optional true} utc-timestamp]
   [:orgpage/last-edit-time {:optional true} utc-timestamp]
   [:orgpage/math-macros {:optional true} :string]
   [:orgpage/init-fragments {:optional true} init-fragments]
   [:orgpage/units {:optional true} [:vector unit]]
   [:orgpage/links {:optional true} [:vector link]]
   [:orgpage/maths {:optional true} [:vector math]]
   [:orgpage/embeds {:optional true} [:vector embed]]
   [:orgpage/files {:optional true} [:vector file-attachment]]
   [:orgpage/images {:optional true} [:vector image-attachment]]
   [:orgpage/fragments {:optional true} [:vector fragment]]
   [:orgpage/paths {:optional true} [:vector path]]
   [:orgpage/path-steps {:optional true} [:vector path-step]]])

(def id-response
  [:map {:closed true}
   [:orgpage/id uuid-or-string?]])

(def api-key-info-response
  (api-response
   [:map {:closed true}
    [:api-key/id :string]
    [:api-key/title :string]
    [:api-key/user-id uuid-or-string?]
    [:api-key/orgpage-id {:optional true} uuid-or-string?]
    [:api-key/permission user-permissions]
    [:api-key/creation-time utc-timestamp]
    [:api-key/last-used {:optional true} utc-timestamp]]))

(def read-orgpage-response (api-response orgpage))
(def read-orgpage-metadata-response (api-response orgpage-metadata))
(def read-orgpage-objects-response (api-response orgpage-objects))
(def read-unit-response read-orgpage-objects-response)
(def read-link-response read-orgpage-objects-response)
(def read-file-response read-orgpage-objects-response)
(def read-image-response read-orgpage-objects-response)
(def read-math-response read-orgpage-objects-response)
(def read-embed-response read-orgpage-objects-response)
(def read-path-response read-orgpage-objects-response)
(def read-fragment-response read-orgpage-objects-response)
(def create-orgpage-response (api-response id-response))
(def copy-orgpage-response (api-response id-response))

(def listed-orgpage
  [:map {:closed true}
   [:orgpage/id uuid-or-string?]
   [:orgpage/title {:optional true} :string]
   [:orgpage/description {:optional true} :string]
   [:orgpage/tags {:optional true} [:set :string]]
   [:orgpage/color spectrum-colors-enum]
   [:orgpage/owner uuid-or-string?]
   [:orgpage/creation-time utc-timestamp]
   [:orgpage/last-edit-time {:optional true} utc-timestamp]
   [:orgpage/view-token uuid-or-string?]
   [:orgpage/permission user-permissions]])

(def orgpage-list-response
  (api-response
   [:map {:closed true}
    [:orgpages [:vector listed-orgpage]]
    [:owners [:vector user]]
    [:usergroups {:optional true} [:vector [:map]]]
    [:shared-orgpage-ids {:optional true} [:set uuid-or-string?]]]))

(def public-orgpage-list-response
  (api-response
   [:map {:closed true}
    [:orgpages [:vector listed-orgpage]]
    [:owners [:vector user]]]))

(def returned-operation
  "Server-normalized operation. Its full discriminated form is in ops.types.ts."
  [:tuple keyword? [:map]])

(def operations-response
  (api-response
   [:map {:closed true}
    [:ops/op-id uuid-or-string?]
    [:ops/list [:vector returned-operation]]
    [:ops/dry-run {:optional true} [:= true]]]))

(def sharing-response-body
  [:map
   [:permissions/owner user]
   [:permissions/users {:optional true} [:vector [:map]]]
   [:permissions/public [:enum :permission/none :permission/view]]
   [:permissions/view-token uuid-or-string?]
   [:permissions/comment-token uuid-or-string?]
   [:permissions/edit-token uuid-or-string?]
   [:permissions/view-link :string]
   [:permissions/comment-link :string]
   [:permissions/edit-link :string]
   [:permissions/fragments {:optional true} [:vector [:map]]]
   [:permissions/paths {:optional true} [:vector [:map]]]])

(def read-sharing-response (api-response sharing-response-body))
(def update-sharing-response read-sharing-response)

(def file-metadata-response
  (api-response
   [:map
    [:file/id uuid-or-string?]
    [:file/token uuid-or-string?]
    [:file/filename :string]
    [:file/content-type :string]
    [:file/size finite-number]
    [:file/last-modified utc-timestamp]
    [:file/orgpage-ids [:set uuid-or-string?]]]))

(def image-metadata-response
  (api-response
   [:map
    [:image/id uuid-or-string?]
    [:image/token uuid-or-string?]
    [:image/filename :string]
    [:image/width finite-number]
    [:image/height finite-number]
    [:image/format :string]
    [:image/thumbnail-sizes [:vector thumbnail-size]]
    [:image/size finite-number]
    [:image/last-modified utc-timestamp]
    [:image/orgpage-ids [:set uuid-or-string?]]]))

(def upload-response
  (api-response
   [:map {:closed true}
    [:files {:optional true} [:vector file-attachment]]
    [:images {:optional true} [:vector image-attachment]]
    [:errors {:optional true} [:vector [:map]]]]))

(def rename-file-response
  (api-response
   [:map
    [:old-file-id {:optional true} uuid-or-string?]
    [:old-poster-id {:optional true} uuid-or-string?]
    [:file-ops [:vector returned-operation]]
    [:orgpage-ops [:map-of uuid-or-string? [:vector returned-operation]]]
    [:dry-run {:optional true} [:= true]]
    [:file file-attachment]
    [:poster {:optional true} image-attachment]]))

(def rename-image-response
  (api-response
   [:map
    [:image image-attachment]
    [:old-image-id {:optional true} uuid-or-string?]
    [:image-ops [:vector returned-operation]]
    [:orgpage-ops [:map-of uuid-or-string? [:vector returned-operation]]]
    [:dry-run {:optional true} [:= true]]]))

(def screenshot-dry-run-response
  (api-response [:map [:dry-run [:= true]] [:format :string]]))

(def print-dry-run-response
  (api-response [:map [:dry-run [:= true]]]))

;; All public route body schemas. Request entries describe EDN request bodies;
;; response entries include the successful EDN body and the shared error body.
(def request-schemas
  {:api/get-info no-body
   :api/list-orgpages no-body
   :api/list-public-orgpages no-body
   :api/create-orgpage create-orgpage
   :api/copy-orgpage no-body
   :api/delete-orgpage no-body
   :api/read-orgpage no-body
   :api/read-orgpage-metadata no-body
   :api/read-orgpage-unit no-body
   :api/read-orgpage-link no-body
   :api/read-orgpage-file no-body
   :api/read-orgpage-image no-body
   :api/read-orgpage-math no-body
   :api/read-orgpage-embed no-body
   :api/read-orgpage-path no-body
   :api/read-orgpage-fragment no-body
   :api/apply-orgpage-ops ops
   :api/read-orgpage-share no-body
   :api/update-orgpage-share permission-op
   :api/read-file-metadata no-body
   :api/read-image-metadata no-body
   ;; Upload uses multipart/form-data rather than an EDN request body.
   :api/rename-file file-rename
   :api/rename-image image-rename
   :api/screenshot screenshot-orgpage
   :api/print print-orgpage})

(def non-edn-request-routes
  "Routes whose request body is not EDN."
  #{:api/upload})

(def response-schemas
  {:api/get-info api-key-info-response
   :api/list-orgpages orgpage-list-response
   :api/list-public-orgpages public-orgpage-list-response
   :api/create-orgpage create-orgpage-response
   :api/copy-orgpage copy-orgpage-response
   :api/read-orgpage read-orgpage-response
   :api/read-orgpage-metadata read-orgpage-metadata-response
   :api/read-orgpage-unit read-unit-response
   :api/read-orgpage-link read-link-response
   :api/read-orgpage-file read-file-response
   :api/read-orgpage-image read-image-response
   :api/read-orgpage-math read-math-response
   :api/read-orgpage-embed read-embed-response
   :api/read-orgpage-path read-path-response
   :api/read-orgpage-fragment read-fragment-response
   :api/apply-orgpage-ops operations-response
   :api/read-orgpage-share read-sharing-response
   :api/update-orgpage-share update-sharing-response
   :api/read-file-metadata file-metadata-response
   :api/read-image-metadata image-metadata-response
   :api/upload upload-response
   :api/rename-file rename-file-response
   :api/rename-image rename-image-response
   ;; Non-dry-run screenshot and print successes are binary.
   :api/screenshot screenshot-dry-run-response
   :api/print print-dry-run-response})

(def non-edn-success-routes
  "Routes with no successful EDN response body (binary or HTTP 204)."
  #{:api/delete-orgpage :api/download-file :api/download-image
    :api/screenshot :api/print})
