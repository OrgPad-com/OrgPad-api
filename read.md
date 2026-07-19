# Read Endpoints

Read endpoints load OrgPages and their objects with API GET requests. These endpoints require view permission. Reading
the OrgPage permission map requires admin permission and is described in
[Managing OrgPages](management.md#read-sharing-state).

## Contents

- [OrgPage Listing](#orgpage-listing)
- [Public OrgPage Listing](#public-orgpage-listing)
- [Read Full OrgPage](#read-full-orgpage)
- [Read Part of an OrgPage](#read-part-of-an-orgpage)
    - [Metadata](#metadata)
    - [Unit](#unit)
    - [Link](#link)
    - [File](#file)
    - [Image](#image)
    - [Math](#math)
    - [Embed](#embed)
    - [Path](#path)
    - [Fragment](#fragment)
- [Related Pages](#related-pages)

## OrgPage Listing

This endpoint retrieves metadata for all OrgPages accessible to the API key owner, as displayed in the
[OrgPage list](https://orgpad.info/list). This includes:

- OrgPages owned by the key owner,
- OrgPages shared directly with the key owner,
- OrgPages shared with a usergroup (called a team in OrgPad app) that includes the key owner.

This endpoint requires a general API key with at least view permission. OrgPage-specific API keys cannot use this
endpoint because it is not scoped to one OrgPage.

```http
GET https://orgpad.info/api/v1/o
```

In JSON format:

```json
{
  "orgpages": [
    {
      "id": "24c7c368-bbee-4d30-bed4-d0472d84a417",
      "title": "Todolist",
      "description": "My todolist with tasks I have to finish.",
      "tags": [
        "tasks",
        "work",
        "todolist"
      ],
      "color": "color/orchid",
      "owner": "3fc2ab1b-3dbf-4678-8caa-3c24a06ae8f2",
      "viewToken": "f965a13f-21d1-41ca-9442-941cfc66f7dc",
      "creationTime": "2026-05-14T08:38:29.442021Z",
      "permission": "permission/admin"
    },
    {
      "id": "09ea7fb2-dda3-4200-bb31-cba88a1bb7b5",
      "title": "OrgPad API",
      "description": "This document deals with new OrgPad API: endpoints, data format, examples.",
      "tags": [
        "development",
        "API",
        "OrgPad"
      ],
      "color": "color/blue",
      "owner": "50475d55-4f6d-401f-9b08-33927a04897f",
      "viewToken": "1cab3eae-1406-4b72-a9d2-dcce8af42899",
      "creationTime": "2026-05-14T07:46:01.349191Z",
      "lastEditTime": "2026-05-14T07:47:11.859554Z",
      "permission": "permission/edit"
    },
    {
      "id": "919b45f1-93ff-4307-af7b-00ac2b5b6d42",
      "title": "OrgPad changelog",
      "description": "Changes and updates of OrgPad.",
      "tags": [
        "development",
        "updates",
        "OrgPad"
      ],
      "color": "color/red",
      "owner": "50475d55-4f6d-401f-9b08-33927a04897f",
      "viewToken": "fd3a199c-4091-47ae-b78b-b0ed4144b6d4",
      "creationTime": "2026-05-14T08:00:57.393763Z",
      "lastEditTime": "2026-05-14T08:01:47.976037Z",
      "permission": "permission/edit"
    }
  ],
  "owners": [
    {
      "id": "50475d55-4f6d-401f-9b08-33927a04897f",
      "firstName": "Pavel",
      "lastName": "Klavík"
    },
    {
      "id": "3fc2ab1b-3dbf-4678-8caa-3c24a06ae8f2",
      "firstName": "Test",
      "lastName": "User"
    }
  ],
  "usergroups": [
    {
      "id": "8e46bbd3-ea7b-4af4-8a75-626e30e6e177",
      "name": "Planning projects",
      "orgpageIds": [
        "24c7c368-bbee-4d30-bed4-d0472d84a417",
        "09ea7fb2-dda3-4200-bb31-cba88a1bb7b5"
      ]
    }
  ],
  "sharedOrgpageIds": [
    "919b45f1-93ff-4307-af7b-00ac2b5b6d42"
  ]
}
```

In EDN format:

```clojure
{:orgpages           [#:orgpage{:id            #uuid"24c7c368-bbee-4d30-bed4-d0472d84a417"
                                :title         "Todolist"
                                :description   "My todolist with tasks I have to finish."
                                :tags          #{"tasks" "work" "todolist"}
                                :color         :color/orchid
                                :owner         #uuid"3fc2ab1b-3dbf-4678-8caa-3c24a06ae8f2"
                                :view-token    #uuid"f965a13f-21d1-41ca-9442-941cfc66f7dc"
                                :creation-time "2026-05-14T08:38:29.442021Z"
                                :permission    :permission/admin}
                      #:orgpage{:id             #uuid"09ea7fb2-dda3-4200-bb31-cba88a1bb7b5"
                                :title          "OrgPad API"
                                :description    "This document deals with new OrgPad API: endpoints, data format, examples."
                                :tags           #{"development" "API" "OrgPad"}
                                :color          :color/blue
                                :owner          #uuid"50475d55-4f6d-401f-9b08-33927a04897f"
                                :view-token     #uuid"1cab3eae-1406-4b72-a9d2-dcce8af42899"
                                :creation-time  "2026-05-14T07:46:01.349191Z"
                                :last-edit-time "2026-05-14T07:47:11.859554Z"
                                :permission     :permission/edit}
                      #:orgpage{:id             #uuid"919b45f1-93ff-4307-af7b-00ac2b5b6d42"
                                :title          "OrgPad changelog"
                                :description    "Changes and updates of OrgPad."
                                :tags           #{"development" "updates" "OrgPad"}
                                :color          :color/red
                                :owner          #uuid"50475d55-4f6d-401f-9b08-33927a04897f"
                                :view-token     #uuid"fd3a199c-4091-47ae-b78b-b0ed4144b6d4"
                                :creation-time  "2026-05-14T08:00:57.393763Z"
                                :last-edit-time "2026-05-14T08:01:47.976037Z"
                                :permission     :permission/edit}]
 :owners             [#:user{:id         #uuid"50475d55-4f6d-401f-9b08-33927a04897f"
                             :first-name "Pavel"
                             :last-name  "Klavík"}
                      #:user{:id         #uuid"3fc2ab1b-3dbf-4678-8caa-3c24a06ae8f2"
                             :first-name "Test"
                             :last-name  "User"}]
 :usergroups         [#:usergroup{:id          #uuid"8e46bbd3-ea7b-4af4-8a75-626e30e6e177"
                                  :name        "Planning projects"
                                  :orgpage-ids #{#uuid"24c7c368-bbee-4d30-bed4-d0472d84a417"
                                                 #uuid"09ea7fb2-dda3-4200-bb31-cba88a1bb7b5"}}]
 :shared-orgpage-ids #{#uuid"919b45f1-93ff-4307-af7b-00ac2b5b6d42"}}
```

Response fields:

- `orgpages`: metadata for the returned OrgPages, see below.
- `owners`: public owner information for the returned OrgPages. Each owner contains `id`, optional `firstName`, and
  optional `lastName`.
- `usergroups`: teams that the API key owner belongs to. This field is omitted when the key owner does not belong to any
  usergroup. Each usergroup contains `id`, `name`, and, when non-empty, IDs of OrgPages shared with this usergroup in
  `orgpageIds`.
- `sharedOrgpageIds`: IDs of returned OrgPages shared directly with the API key owner. This field is omitted when no
  returned OrgPages are shared directly.

OrgPage fields:

- `id`: OrgPage ID.
- `title`: OrgPage title.
- `description`: OrgPage description.
- `tags`: OrgPage tags.
- `color`: OrgPage color.
- `permission`: the maximum permission of the API key owner on this OrgPage: `permission/view`, `permission/comment`,
  `permission/edit`, or `permission/admin`.
- `owner`: ID of the OrgPage owner. The owner details are included in `owners`.
- `viewToken`: view token that grants view access to this OrgPage when added as the `token` query parameter.
- `creationTime`: time when the OrgPage was created, as a UTC ISO datetime string.
- `lastEditTime`: time of the last edit, as a UTC ISO datetime string. This field is omitted when the OrgPage has not
  been edited after creation.

## Public OrgPage Listing

This endpoint retrieves metadata for [public OrgPages](https://orgpad.info/public). Public OrgPages are OrgPages
published with `permission/view`. The response contains only `orgpages` and `owners`, using the same field shapes as
the [accessible OrgPage listing](#orgpage-listing).

This endpoint requires an API key with at least view permission. Both general and OrgPage-specific API keys can use it,
because it only lists public OrgPages.

```http
GET https://orgpad.info/api/v1/public
```

## Read Full OrgPage

This endpoint retrieves the full data structure for one OrgPage. It includes all units, links, files, images, and other
objects. For the response structure and object field descriptions, see [OrgPage data](orgpage.md). It
requires an API key with view permission. An OrgPage-specific key can read its selected OrgPage, a public OrgPage, or
an OrgPage authorized by a sharing token.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}
GET https://orgpad.info/api/v1/s/{short-link}
```

For this [example OrgPage](https://orgpad.info/s/orgpage-data-example), it returns
this [JSON response](example-orgpage.json) and this [EDN response](example-orgpage.edn).

For JSON responses, unit content is returned as HTML. For EDN and Transit responses, unit content remains as Hiccup
data. IDs in response fields are standard UUID hex strings. URLs inside rendered content may contain compact base64 IDs
because OrgPad uses that format in URLs.

## Read Part of an OrgPage

These endpoints read only part of an OrgPage. This is faster and returns a smaller payload than reading the full OrgPage
and parsing the required data from it. The format is the same as in the full OrgPage response, but only relevant objects
are included. For object field descriptions, see [OrgPage data](orgpage.md). These endpoints require an API
key with view permission, either general or scoped to the target OrgPage.

### Metadata

Retrieves only OrgPage metadata.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/meta
GET https://orgpad.info/api/v1/s/{short-link}/meta
```

In JSON format:

```json
{
  "id": "e8efc539-e16f-4c28-9923-65022103bee1",
  "title": "OrgPage data example",
  "description": "A simple OrgPad created to showcase what OrgPad data looks like.",
  "tags": [
    "example",
    "API",
    "OrgPad",
    "data"
  ],
  "color": "color/purple",
  "owner": "50475d55-4f6d-401f-9b08-33927a04897f",
  "creationTime": "2026-05-13T13:46:23.139749Z",
  "lastLoadTime": "2026-05-14T00:22:40.201665Z",
  "lastEditTime": "2026-05-13T17:18:03.840382Z"
}
```

In EDN format:

```clojure
{:orgpage/id             #uuid "e8efc539-e16f-4c28-9923-65022103bee1"
 :orgpage/title          "OrgPage data example"
 :orgpage/description    "A simple OrgPad created to showcase what OrgPad data looks like."
 :orgpage/tags           #{"example" "API" "OrgPad" "data"}
 :orgpage/color          :color/purple
 :orgpage/owner          #uuid "50475d55-4f6d-401f-9b08-33927a04897f"
 :orgpage/creation-time  "2026-05-13T13:46:23.139749Z"
 :orgpage/last-load-time "2026-05-14T00:22:40.201665Z"
 :orgpage/last-edit-time "2026-05-13T17:18:03.840382Z"}
```

For field descriptions, see the metadata fields in [OrgPage data](orgpage.md).

### Unit

Retrieves the requested unit together with related objects.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/unit/{unit-id-or-text-id}
GET https://orgpad.info/api/v1/s/{short-link}/unit/{unit-id-or-text-id}
```

The response contains:

- The unit itself.
- When the unit is a page, its parent book.
- When the unit is a book, all its pages.
- When the unit is a book, all incident links connected to it.
- Math and embed objects whose page is included.
- Files and images used by the returned unit content or embeds.

Missing units return `unit-not-found` error. See [Route and object lookup errors](errors.md#route-and-object-lookup-errors).

In JSON format:

```json
{
  "units": [
    {
      "id": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "textId": "binomial-book",
      "type": "unit/book",
      "childUnitIds": [
        "da5b1957-7d24-44f9-869e-9cebe27f7e67",
        "06fcdc12-0d52-4847-9b88-c685d72f364e"
      ],
      "pos": [
        -678.4950106793899,
        -290.60864427844024
      ],
      "title": "Binomial theorem",
      "props": {
        "titleSize": "props/h2",
        "color": "color/teal"
      }
    },
    {
      "id": "da5b1957-7d24-44f9-869e-9cebe27f7e67",
      "textId": "binomial-page1",
      "type": "unit/page",
      "parentId": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "content": "<p><a href=\"https://en.wikipedia.org/wiki/Binomial_theorem\">Binomial theorem</a> states for every <math id=\"bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294\"></math> and <math id=\"bd17c7be-c295-436a-9ac7-525581a641d1\"></math> that</p><p><math id=\"488d2221-c45e-4a4a-9845-ed2ff00f5ed3\"></math></p><p><a href=\"/file/A1wzY1upVBw5SBwTeHWt8k\"><img height=\"24\" src=\"/static/img/files/pdf.svg\" style=\"margin-bottom:6;margin-right:4;vertical-align:text-bottom;\" width=\"24\" />Binomial_theorem.pdf</a></p>"
    },
    {
      "id": "06fcdc12-0d52-4847-9b88-c685d72f364e",
      "textId": "binomial-page2",
      "type": "unit/page",
      "parentId": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "content": "<p><embed id=\"6b921cb9-a9d9-4e43-b4a5-468ed99174b4\" width=\"700\" height=\"450\"/></p>"
    }
  ],
  "links": [
    {
      "id": "61b08d4a-d24c-44dd-8652-f9405ed85ebd",
      "endpointIds": [
        "a1506e9e-eb3c-4507-9fba-195d5e6e493c",
        "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
      ],
      "props": {
        "weight": "props/strong",
        "arrowhead": "props/none",
        "color": "color/blue"
      }
    },
    {
      "id": "0af5d31c-57f3-4a6f-b801-221f2c28ee15",
      "endpointIds": [
        "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5",
        "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
      ],
      "props": {
        "weight": "props/strong",
        "arrowhead": "props/single",
        "color": "color/orange"
      }
    }
  ],
  "maths": [
    {
      "id": "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294",
      "pageId": "da5b1957-7d24-44f9-869e-9cebe27f7e67",
      "type": "math/math",
      "source": "x,y \\in \\mathbb R",
      "width": 8.019,
      "height": 2.509,
      "verticalAlign": -0.671,
      "svg": "<svg>…</svg>"
    },
    {
      "id": "bd17c7be-c295-436a-9ac7-525581a641d1",
      "pageId": "da5b1957-7d24-44f9-869e-9cebe27f7e67",
      "type": "math/math",
      "source": "n \\in \\mathbb N",
      "width": 5.66,
      "height": 2.176,
      "verticalAlign": -0.338,
      "svg": "<svg>…</svg>"
    },
    {
      "id": "488d2221-c45e-4a4a-9845-ed2ff00f5ed3",
      "pageId": "da5b1957-7d24-44f9-869e-9cebe27f7e67",
      "type": "math/math",
      "source": "(x+y)^n = \\sum_{k=0}^n {n \\choose k} x^ky^{n-k}.",
      "width": 25.875,
      "height": 7.176,
      "block": true,
      "verticalAlign": -3.171,
      "svg": "<svg>…</svg>"
    }
  ],
  "embeds": [
    {
      "id": "6b921cb9-a9d9-4e43-b4a5-468ed99174b4",
      "pageId": "06fcdc12-0d52-4847-9b88-c685d72f364e",
      "fileId": "35c33635-ba95-41c3-9481-c137875adf24"
    }
  ],
  "files": [
    {
      "id": "35c33635-ba95-41c3-9481-c137875adf24",
      "token": "2498ced3-60be-48b7-b86a-9b5b3a79316e",
      "filename": "Binomial_theorem.pdf",
      "contentType": "application/pdf",
      "size": 947386,
      "lastModified": "2026-05-13T12:52:48.415900369Z"
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/units  [{:unit/id             #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                   :unit/text-id        "binomial-book"
                   :unit/type           :unit/book
                   :unit/child-unit-ids [#uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                                         #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"]
                   :unit/pos            [-678.4950106793899 -290.60864427844024]
                   :unit/title          "Binomial theorem"
                   :unit/props          {:props/title-size :props/h1
                                         :props/color      :color/teal}}
                  {:unit/id        #uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                   :unit/text-id   "binomial-page1"
                   :unit/type      :unit/page
                   :unit/parent-id #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                   :unit/content   [[:p [:a {:href "https://en.wikipedia.org/wiki/Binomial_theorem"}
                                         "Binomial theorem"]
                                     " states for every "
                                     [:math {:math/id #uuid "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294"}]
                                     " and "
                                     [:math {:math/id #uuid "bd17c7be-c295-436a-9ac7-525581a641d1"}]
                                     " that"]
                                    [:p [:math {:math/id #uuid "488d2221-c45e-4a4a-9845-ed2ff00f5ed3"}]]
                                    [:p
                                     [:a {:href "/file/A1wzY1upVBw5SBwTeHWt8k"}
                                      [:img {:src    "/static/img/files/pdf.svg"
                                             :width  24
                                             :height 24
                                             :style  {:vertical-align "text-bottom"
                                                      :margin-right   4
                                                      :margin-bottom  6}}]
                                      "Binomial_theorem.pdf"]]]}
                  {:unit/id        #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"
                   :unit/text-id   "binomial-page2"
                   :unit/type      :unit/page
                   :unit/parent-id #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                   :unit/content   [[:p [:embed {:embed/id     #uuid "6b921cb9-a9d9-4e43-b4a5-468ed99174b4"
                                                 :embed/width  700
                                                 :embed/height 450}]]]}]
 :orgpage/links  [{:link/id           #uuid "61b08d4a-d24c-44dd-8652-f9405ed85ebd"
                   :link/endpoint-ids [#uuid "a1506e9e-eb3c-4507-9fba-195d5e6e493c"
                                       #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"]
                   :link/props        {:props/weight    :props/strong
                                       :props/arrowhead :props/none
                                       :props/color     :color/blue}}
                  {:link/id           #uuid "0af5d31c-57f3-4a6f-b801-221f2c28ee15"
                   :link/endpoint-ids [#uuid "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5"
                                       #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"]
                   :link/props        {:props/weight    :props/strong
                                       :props/arrowhead :props/single
                                       :props/color     :color/orange}}]
 :orgpage/maths  [{:math/id             #uuid "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294"
                   :math/page-id        #uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                   :math/type           :math/math
                   :math/source         "x,y \\in \\mathbb R"
                   :math/width          8.019
                   :math/height         2.509
                   :math/vertical-align -0.671
                   :math/svg            "<svg>…</svg>"}
                  {:math/id             #uuid "bd17c7be-c295-436a-9ac7-525581a641d1"
                   :math/page-id        #uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                   :math/type           :math/math
                   :math/source         "n \\in \\mathbb N"
                   :math/width          5.66
                   :math/height         2.176
                   :math/vertical-align -0.338
                   :math/svg            "<svg>…</svg>"}
                  {:math/id             #uuid "488d2221-c45e-4a4a-9845-ed2ff00f5ed3"
                   :math/page-id        #uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                   :math/type           :math/math
                   :math/source         "(x+y)^n = \\sum_{k=0}^n {n \\choose k} x^ky^{n-k}."
                   :math/width          25.875
                   :math/height         7.176
                   :math/block          true
                   :math/vertical-align -3.171
                   :math/svg            "<svg>…</svg>"}]
 :orgpage/embeds [{:embed/id      #uuid "6b921cb9-a9d9-4e43-b4a5-468ed99174b4"
                   :embed/page-id #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"
                   :embed/file-id #uuid "35c33635-ba95-41c3-9481-c137875adf24"}]
 :orgpage/files  [{:file/id            #uuid "35c33635-ba95-41c3-9481-c137875adf24"
                   :file/token         #uuid "2498ced3-60be-48b7-b86a-9b5b3a79316e"
                   :file/filename      "Binomial_theorem.pdf"
                   :file/content-type  "application/pdf"
                   :file/size          947386
                   :file/last-modified "2026-05-13T12:52:48.415900369Z"}]}
```

### Link

Retrieves the requested link and its endpoint book units.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/link/{link-id}
GET https://orgpad.info/api/v1/s/{short-link}/link/{link-id}
```

In JSON format:

```json
{
  "links": [
    {
      "id": "0af5d31c-57f3-4a6f-b801-221f2c28ee15",
      "endpointIds": [
        "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5",
        "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
      ],
      "props": {
        "weight": "props/strong",
        "arrowhead": "props/single",
        "color": "color/orange"
      }
    }
  ],
  "units": [
    {
      "id": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "textId": "binomial-book",
      "type": "unit/book",
      "childUnitIds": [
        "da5b1957-7d24-44f9-869e-9cebe27f7e67",
        "06fcdc12-0d52-4847-9b88-c685d72f364e"
      ],
      "pos": [
        -678.4950106793899,
        -290.60864427844024
      ],
      "title": "Binomial theorem",
      "props": {
        "titleSize": "props/h2",
        "color": "color/teal"
      }
    },
    {
      "id": "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5",
      "type": "unit/book",
      "childUnitIds": [
        "843fe53a-a75e-4d11-9dd4-018d758e2beb"
      ],
      "pos": [
        -75.54478863069679,
        46.913746590527474
      ],
      "title": "Computing binomial coefficients",
      "props": {
        "titleSize": "props/h2",
        "color": "color/blueberry"
      }
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/links [{:link/id           #uuid "0af5d31c-57f3-4a6f-b801-221f2c28ee15"
                  :link/endpoint-ids [#uuid "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5"
                                      #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"]
                  :link/props        {:props/weight    :props/strong
                                      :props/arrowhead :props/single
                                      :props/color     :color/orange}}]
 :orgpage/units [{:unit/id             #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                  :unit/text-id        "binomial-book"
                  :unit/type           :unit/book
                  :unit/child-unit-ids [#uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                                        #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"]
                  :unit/pos            [-678.4950106793899 -290.60864427844024]
                  :unit/title          "Binomial theorem"
                  :unit/props          {:props/title-size :props/h1
                                        :props/color      :color/teal}}
                 {:unit/id             #uuid "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5"
                  :unit/type           :unit/book
                  :unit/child-unit-ids [#uuid "843fe53a-a75e-4d11-9dd4-018d758e2beb"]
                  :unit/pos            [-75.54478863069679 46.913746590527474]
                  :unit/title          "Computing binomial coefficients"
                  :unit/props          {:props/title-size :props/h2
                                        :props/color      :color/blueberry}}]}
```

Missing links return `link-not-found` error. See [Route and object lookup errors](errors.md#route-and-object-lookup-errors).

### File

Retrieves the referenced file together with all pages and embeds using it.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/file/{file-id}
GET https://orgpad.info/api/v1/s/{short-link}/file/{file-id}
```

In JSON format:

```json
{
  "files": [
    {
      "id": "35c33635-ba95-41c3-9481-c137875adf24",
      "token": "2498ced3-60be-48b7-b86a-9b5b3a79316e",
      "filename": "Binomial_theorem.pdf",
      "contentType": "application/pdf",
      "size": 947386,
      "lastModified": "2026-05-13T12:52:48.415900369Z"
    }
  ],
  "units": [
    {
      "id": "da5b1957-7d24-44f9-869e-9cebe27f7e67",
      "textId": "binomial-page1",
      "type": "unit/page",
      "parentId": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "content": "<p><a href=\"https://en.wikipedia.org/wiki/Binomial_theorem\">Binomial theorem</a> states for every <math id=\"bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294\"></math> and <math id=\"bd17c7be-c295-436a-9ac7-525581a641d1\"></math> that</p><p><math id=\"488d2221-c45e-4a4a-9845-ed2ff00f5ed3\"></math></p><p><a href=\"/file/A1wzY1upVBw5SBwTeHWt8k\"><img height=\"24\" src=\"/static/img/files/pdf.svg\" style=\"margin-bottom:6;margin-right:4;vertical-align:text-bottom;\" width=\"24\" />Binomial_theorem.pdf</a></p>"
    },
    {
      "id": "06fcdc12-0d52-4847-9b88-c685d72f364e",
      "textId": "binomial-page2",
      "type": "unit/page",
      "parentId": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "content": "<p><embed id=\"6b921cb9-a9d9-4e43-b4a5-468ed99174b4\" width=\"700\" height=\"450\"/></p>"
    }
  ],
  "embeds": [
    {
      "id": "6b921cb9-a9d9-4e43-b4a5-468ed99174b4",
      "pageId": "06fcdc12-0d52-4847-9b88-c685d72f364e",
      "fileId": "35c33635-ba95-41c3-9481-c137875adf24"
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/files  [{:file/id            #uuid "35c33635-ba95-41c3-9481-c137875adf24"
                   :file/token         #uuid "2498ced3-60be-48b7-b86a-9b5b3a79316e"
                   :file/filename      "Binomial_theorem.pdf"
                   :file/content-type  "application/pdf"
                   :file/size          947386
                   :file/last-modified "2026-05-13T12:52:48.415900369Z"}]
 :orgpage/units  [{:unit/id        #uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                   :unit/text-id   "binomial-page1"
                   :unit/type      :unit/page
                   :unit/parent-id #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                   :unit/content   [[:p [:a {:href "https://en.wikipedia.org/wiki/Binomial_theorem"}
                                         "Binomial theorem"]
                                     " states for every "
                                     [:math {:math/id #uuid "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294"}]
                                     " and "
                                     [:math {:math/id #uuid "bd17c7be-c295-436a-9ac7-525581a641d1"}]
                                     " that"]
                                    [:p [:math {:math/id #uuid "488d2221-c45e-4a4a-9845-ed2ff00f5ed3"}]]
                                    [:p
                                     [:a {:href "/file/A1wzY1upVBw5SBwTeHWt8k"}
                                      [:img {:src    "/static/img/files/pdf.svg"
                                             :width  24
                                             :height 24
                                             :style  {:vertical-align "text-bottom"
                                                      :margin-right   4
                                                      :margin-bottom  6}}]
                                      "Binomial_theorem.pdf"]]]}
                  {:unit/id        #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"
                   :unit/text-id   "binomial-page2"
                   :unit/type      :unit/page
                   :unit/parent-id #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                   :unit/content   [[:p [:embed {:embed/id     #uuid "6b921cb9-a9d9-4e43-b4a5-468ed99174b4"
                                                 :embed/width  700
                                                 :embed/height 450}]]]}]
 :orgpage/embeds [{:embed/id      #uuid "6b921cb9-a9d9-4e43-b4a5-468ed99174b4"
                   :embed/page-id #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"
                   :embed/file-id #uuid "35c33635-ba95-41c3-9481-c137875adf24"}]}
```

Missing files return `file-not-found` error. See [Image, file, video, and audio errors](errors.md#image-file-video-and-audio-errors).
To read file metadata and find accessible OrgPages that contain the file, see
[Attachment metadata](attachments.md#attachment-metadata).

### Image

Retrieves the referenced image together with all pages using it.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/image/{image-id}
GET https://orgpad.info/api/v1/s/{short-link}/image/{image-id}
```

In JSON format:

```json
{
  "images": [
    {
      "id": "c40700c0-7b5b-4cf6-8983-31b4483f531b",
      "token": "8b52d311-9b92-4d27-9777-477787b9efe2",
      "filename": "pascal.png",
      "width": 540,
      "height": 540,
      "format": "png",
      "thumbnailFormat": "png",
      "size": 10792,
      "lastModified": "2026-05-13T10:21:36.156948376Z",
      "url": "https://dr282zn36sxxg.cloudfront.net/…",
      "thumbnailSizes": [
        {
          "width": 540,
          "height": 540,
          "size": 10792
        },
        {
          "width": 500,
          "height": 500,
          "size": 46379
        },
        {
          "width": 250,
          "height": 250,
          "size": 18619
        },
        {
          "width": 125,
          "height": 125,
          "size": 7498
        }
      ]
    }
  ],
  "units": [
    {
      "id": "c5f5584e-ca21-4a58-9827-70a859dd76f9",
      "type": "unit/page",
      "parentId": "a1506e9e-eb3c-4507-9fba-195d5e6e493c",
      "content": "<p><img height=\"500\" src=\"/img/DEBwDAe1tM9omDMbRIP1Mb\" width=\"500\" /></p>"
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/images [{:image/id               #uuid "c40700c0-7b5b-4cf6-8983-31b4483f531b"
                   :image/token            #uuid "8b52d311-9b92-4d27-9777-477787b9efe2"
                   :image/filename         "pascal.png"
                   :image/width            540
                   :image/height           540
                   :image/format           "png"
                   :image/thumbnail-format "png"
                   :image/size             10792
                   :image/last-modified    "2026-05-13T10:21:36.156948376Z"
                   :image/url              "https://dr282zn36sxxg.cloudfront.net/…"
                   :image/thumbnail-sizes  [{:thumbnail/width  540
                                             :thumbnail/height 540
                                             :thumbnail/size   10792}
                                            {:thumbnail/width  500
                                             :thumbnail/height 500
                                             :thumbnail/size   46379}
                                            {:thumbnail/width  250
                                             :thumbnail/height 250
                                             :thumbnail/size   18619}
                                            {:thumbnail/width  125
                                             :thumbnail/height 125
                                             :thumbnail/size   7498}]}]
 :orgpage/units  [{:unit/id        #uuid "c5f5584e-ca21-4a58-9827-70a859dd76f9"
                   :unit/type      :unit/page
                   :unit/parent-id #uuid "a1506e9e-eb3c-4507-9fba-195d5e6e493c"
                   :unit/content   [[:p [:img {:src    "/img/DEBwDAe1tM9omDMbRIP1Mb"
                                               :width  500
                                               :height 500}]]]}]}
```

Missing images return `image-not-found` error. See [Image, file, video, and audio errors](errors.md#image-file-video-and-audio-errors).
To read image metadata and find accessible OrgPages that contain the image, see
[Attachment metadata](attachments.md#attachment-metadata).

### Math

Retrieves the referenced math and its page.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/math/{math-id}
GET https://orgpad.info/api/v1/s/{short-link}/math/{math-id}
```

In JSON format:

```json
{
  "maths": [
    {
      "id": "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294",
      "pageId": "da5b1957-7d24-44f9-869e-9cebe27f7e67",
      "type": "math/math",
      "source": "x,y \\in \\mathbb R",
      "width": 8.019,
      "height": 2.509,
      "verticalAlign": -0.671,
      "svg": "<svg>…</svg>"
    }
  ],
  "units": [
    {
      "id": "da5b1957-7d24-44f9-869e-9cebe27f7e67",
      "textId": "binomial-page1",
      "type": "unit/page",
      "parentId": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "content": "<p><a href=\"https://en.wikipedia.org/wiki/Binomial_theorem\">Binomial theorem</a> states for every <math id=\"bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294\"></math> and <math id=\"bd17c7be-c295-436a-9ac7-525581a641d1\"></math> that</p><p><math id=\"488d2221-c45e-4a4a-9845-ed2ff00f5ed3\"></math></p><p><a href=\"/file/A1wzY1upVBw5SBwTeHWt8k\"><img height=\"24\" src=\"/static/img/files/pdf.svg\" style=\"margin-bottom:6;margin-right:4;vertical-align:text-bottom;\" width=\"24\" />Binomial_theorem.pdf</a></p>"
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/maths [{:math/id             #uuid "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294"
                  :math/page-id        #uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                  :math/type           :math/math
                  :math/source         "x,y \\in \\mathbb R"
                  :math/width          8.019
                  :math/height         2.509
                  :math/vertical-align -0.671
                  :math/svg            "<svg>…</svg>"}]
 :orgpage/units [{:unit/id        #uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"
                  :unit/text-id   "binomial-page1"
                  :unit/type      :unit/page
                  :unit/parent-id #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                  :unit/content   [[:p [:a {:href "https://en.wikipedia.org/wiki/Binomial_theorem"}
                                        "Binomial theorem"]
                                    " states for every "
                                    [:math {:math/id #uuid "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294"}]
                                    " and "
                                    [:math {:math/id #uuid "bd17c7be-c295-436a-9ac7-525581a641d1"}]
                                    " that"]
                                   [:p [:math {:math/id #uuid "488d2221-c45e-4a4a-9845-ed2ff00f5ed3"}]]
                                   [:p
                                    [:a {:href "/file/A1wzY1upVBw5SBwTeHWt8k"}
                                     [:img {:src    "/static/img/files/pdf.svg"
                                            :width  24
                                            :height 24
                                            :style  {:vertical-align "text-bottom"
                                                     :margin-right   4
                                                     :margin-bottom  6}}]
                                     "Binomial_theorem.pdf"]]]}]}
```

Missing math objects return `math-not-found` error. See [Route and object lookup errors](errors.md#route-and-object-lookup-errors).

### Embed

Retrieves the referenced embed and its page.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/embed/{embed-id-or-text-id}
GET https://orgpad.info/api/v1/s/{short-link}/embed/{embed-id-or-text-id}
```

In JSON format:

```json
{
  "embeds": [
    {
      "id": "6b921cb9-a9d9-4e43-b4a5-468ed99174b4",
      "pageId": "06fcdc12-0d52-4847-9b88-c685d72f364e",
      "fileId": "35c33635-ba95-41c3-9481-c137875adf24"
    }
  ],
  "units": [
    {
      "id": "06fcdc12-0d52-4847-9b88-c685d72f364e",
      "textId": "binomial-page2",
      "type": "unit/page",
      "parentId": "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
      "content": "<p><embed id=\"6b921cb9-a9d9-4e43-b4a5-468ed99174b4\" width=\"700\" height=\"450\"/></p>"
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/embeds [{:embed/id      #uuid "6b921cb9-a9d9-4e43-b4a5-468ed99174b4"
                   :embed/page-id #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"
                   :embed/file-id #uuid "35c33635-ba95-41c3-9481-c137875adf24"}]
 :orgpage/units  [{:unit/id        #uuid "06fcdc12-0d52-4847-9b88-c685d72f364e"
                   :unit/text-id   "binomial-page2"
                   :unit/type      :unit/page
                   :unit/parent-id #uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                   :unit/content   [[:p [:embed {:embed/id     #uuid "6b921cb9-a9d9-4e43-b4a5-468ed99174b4"
                                                 :embed/width  700
                                                 :embed/height 450}]]]}]}
```

Missing embeds return `embed-not-found` error. See [Route and object lookup errors](errors.md#route-and-object-lookup-errors).

### Path

Retrieves the referenced path and all its path steps.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/path/{path-id}
GET https://orgpad.info/api/v1/s/{short-link}/path/{path-id}
```

In JSON format:

```json
{
  "paths": [
    {
      "id": "6d12c87d-1804-472b-8cc7-b2259ef2ba27",
      "title": "Example presentation",
      "stepIds": [
        "5911ae1b-1b23-4c95-845c-813e12d131e1",
        "04f7bb75-7b6f-4b3c-a55c-ec553c7d5361",
        "aa15e4c0-d1a0-4697-a38a-6eb013a26b74",
        "cc472b3b-7df7-40ee-b9cb-6668c49f31df",
        "488cee41-0f98-423e-8b92-ef5df91e2903",
        "4bf18c56-55e5-46dd-b308-0a9cb44c72f6"
      ]
    }
  ],
  "pathSteps": [
    {
      "id": "5911ae1b-1b23-4c95-845c-813e12d131e1",
      "focusedBookIds": [
        "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
        "b47226d3-226b-4c79-91a3-095a2bddbdd3",
        "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5",
        "d9c8360b-0811-4808-97ed-f1f65f208fed",
        "a1506e9e-eb3c-4507-9fba-195d5e6e493c"
      ]
    },
    {
      "id": "04f7bb75-7b6f-4b3c-a55c-ec553c7d5361",
      "focusedBookIds": [
        "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
      ],
      "openedPageIds": [
        "da5b1957-7d24-44f9-869e-9cebe27f7e67"
      ]
    },
    {
      "id": "aa15e4c0-d1a0-4697-a38a-6eb013a26b74",
      "focusedBookIds": [
        "a1506e9e-eb3c-4507-9fba-195d5e6e493c"
      ],
      "openedPageIds": [
        "c5f5584e-ca21-4a58-9827-70a859dd76f9"
      ]
    },
    {
      "id": "cc472b3b-7df7-40ee-b9cb-6668c49f31df",
      "focusedBookIds": [
        "b47226d3-226b-4c79-91a3-095a2bddbdd3",
        "d9c8360b-0811-4808-97ed-f1f65f208fed"
      ],
      "openedPageIds": [
        "ce6be8c1-8de4-462c-8b51-094412bf42ea"
      ]
    },
    {
      "id": "488cee41-0f98-423e-8b92-ef5df91e2903",
      "focusedBookIds": [
        "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5"
      ],
      "openedPageIds": [
        "843fe53a-a75e-4d11-9dd4-018d758e2beb"
      ]
    },
    {
      "id": "4bf18c56-55e5-46dd-b308-0a9cb44c72f6",
      "focusedBookIds": [
        "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd",
        "b47226d3-226b-4c79-91a3-095a2bddbdd3",
        "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5",
        "d9c8360b-0811-4808-97ed-f1f65f208fed",
        "a1506e9e-eb3c-4507-9fba-195d5e6e493c"
      ]
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/paths      [{:path/id       #uuid "6d12c87d-1804-472b-8cc7-b2259ef2ba27"
                       :path/title    "Example presentation"
                       :path/step-ids [#uuid "5911ae1b-1b23-4c95-845c-813e12d131e1"
                                       #uuid "04f7bb75-7b6f-4b3c-a55c-ec553c7d5361"
                                       #uuid "aa15e4c0-d1a0-4697-a38a-6eb013a26b74"
                                       #uuid "cc472b3b-7df7-40ee-b9cb-6668c49f31df"
                                       #uuid "488cee41-0f98-423e-8b92-ef5df91e2903"
                                       #uuid "4bf18c56-55e5-46dd-b308-0a9cb44c72f6"]}]
 :orgpage/path-steps [{:step/id               #uuid "5911ae1b-1b23-4c95-845c-813e12d131e1"
                       :step/focused-book-ids #{#uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                                                #uuid "b47226d3-226b-4c79-91a3-095a2bddbdd3"
                                                #uuid "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5"
                                                #uuid "d9c8360b-0811-4808-97ed-f1f65f208fed"
                                                #uuid "a1506e9e-eb3c-4507-9fba-195d5e6e493c"}}
                      {:step/id               #uuid "04f7bb75-7b6f-4b3c-a55c-ec553c7d5361"
                       :step/focused-book-ids #{#uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"}
                       :step/opened-page-ids  #{#uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"}}
                      {:step/id               #uuid "aa15e4c0-d1a0-4697-a38a-6eb013a26b74"
                       :step/focused-book-ids #{#uuid "a1506e9e-eb3c-4507-9fba-195d5e6e493c"}
                       :step/opened-page-ids  #{#uuid "c5f5584e-ca21-4a58-9827-70a859dd76f9"}}
                      {:step/id               #uuid "cc472b3b-7df7-40ee-b9cb-6668c49f31df"
                       :step/focused-book-ids #{#uuid "b47226d3-226b-4c79-91a3-095a2bddbdd3"
                                                #uuid "d9c8360b-0811-4808-97ed-f1f65f208fed"}
                       :step/opened-page-ids  #{#uuid "ce6be8c1-8de4-462c-8b51-094412bf42ea"}}
                      {:step/id               #uuid "488cee41-0f98-423e-8b92-ef5df91e2903"
                       :step/focused-book-ids #{#uuid "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5"}
                       :step/opened-page-ids  #{#uuid "843fe53a-a75e-4d11-9dd4-018d758e2beb"}}
                      {:step/id               #uuid "4bf18c56-55e5-46dd-b308-0a9cb44c72f6"
                       :step/focused-book-ids #{#uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
                                                #uuid "b47226d3-226b-4c79-91a3-095a2bddbdd3"
                                                #uuid "f71d5f0e-7863-4ab6-8f24-1e1dea820bb5"
                                                #uuid "d9c8360b-0811-4808-97ed-f1f65f208fed"
                                                #uuid "a1506e9e-eb3c-4507-9fba-195d5e6e493c"}}]}
```

Missing paths return `path-not-found` error. See [Route and object lookup errors](errors.md#route-and-object-lookup-errors).

### Fragment

Retrieves the referenced fragment.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/fragment/{fragment-id-or-text-id}
GET https://orgpad.info/api/v1/s/{short-link}/fragment/{fragment-id-or-text-id}
```

In JSON format:

```json
{
  "fragments": [
    {
      "id": "361e0937-4876-4600-8815-39cae4bdbc96",
      "textId": "bin-theorem",
      "title": "Binomial Theorem",
      "openedPageIds": [
        "da5b1957-7d24-44f9-869e-9cebe27f7e67"
      ],
      "focusedBookIds": [
        "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"
      ]
    }
  ]
}
```

In EDN format:

```clojure
{:orgpage/fragments [{:fragment/id               #uuid "361e0937-4876-4600-8815-39cae4bdbc96"
                      :fragment/text-id          "bin-theorem"
                      :fragment/title            "Binomial Theorem"
                      :fragment/opened-page-ids  #{#uuid "da5b1957-7d24-44f9-869e-9cebe27f7e67"}
                      :fragment/focused-book-ids #{#uuid "490b560f-a9ee-43b8-b6a2-cfaa6b88d0bd"}}]}
```

Missing fragments return `fragment-not-found` error. See [Route and object lookup errors](errors.md#route-and-object-lookup-errors).

## Related Pages

Use these pages when read endpoints connect to response data, formats, permissions, or editing.

| Page                                               | When to use it                                    |
|----------------------------------------------------|---------------------------------------------------|
| [OrgPage data](orgpage.md)                         | Understand the fields returned by read endpoints. |
| [Authentication and API keys](auth.md)             | Understand view permissions and API key scope.    |
| [Input and output formats](formats.md)             | Understand JSON, EDN, and Transit responses.      |
| [Attachments](attachments.md#attachment-metadata)   | Read attachment metadata across accessible OrgPages. |
| [Operations](ops.md)                               | Modify the objects returned by read endpoints.    |
| [Errors](errors.md#route-and-object-lookup-errors) | Understand missing-object and permission errors.  |
