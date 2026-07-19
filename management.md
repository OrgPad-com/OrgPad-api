# Managing OrgPages

OrgPage management endpoints create, delete, copy, and share OrgPages through the API. These endpoints require an API
key with admin permission.

## Contents

- [Create OrgPage](#create-orgpage)
- [Delete OrgPage](#delete-orgpage)
- [Copy OrgPage](#copy-orgpage)
- [Read Sharing State](#read-sharing-state)
- [Update Sharing State](#update-sharing-state)
- [Related Pages](#related-pages)

## Create OrgPage

Creates a new OrgPage and returns its ID. Requires an admin API key that is not limited to a single OrgPage.

```http
POST https://orgpad.info/api/v1/o
```

JSON body:

```json
{
  "title": "New OrgPage",
  "description": "Created through API",
  "tags": [
    "api",
    "test"
  ],
  "color": "color/orchid"
}
```

EDN body:

```clojure
{:orgpage/title       "New OrgPage"
 :orgpage/description "Created through API"
 :orgpage/tags        #{"api" "test"}
 :orgpage/color       :color/orchid}
```

All four input fields are optional. To create an OrgPage with default metadata, omit the request body or send JSON
`null`. In EDN, send `nil`. If `color` is omitted, OrgPad uses `color/blue`.

JSON example response:

```json
{
  "id": "136a1f33-0113-41ac-aff6-f50542a3228a"
}
```

EDN example response:

```clojure
{:orgpage/id #uuid "136a1f33-0113-41ac-aff6-f50542a3228a"}
```

If the owner has an OrgPad dashboard open, the new OrgPage appears there without a manual refresh.

If the body does not match the expected schema, the API returns `body-schema-error`. See
[Request format and body errors](errors.md#request-format-and-body-errors).

## Delete OrgPage

Deletes the target OrgPage. Requires admin permission from both the user account and the API key. Successful deletion
returns `204 No Content`. Open dashboards are notified and clients currently viewing the deleted OrgPage are
disconnected.

```http
DELETE https://orgpad.info/api/v1/o/{orgpage-id}
DELETE https://orgpad.info/api/v1/s/{short-link}
```

## Copy OrgPage

Copies the target OrgPage into a new OrgPage owned by the API key owner. Units, links, maths, embeds, paths, path steps,
and fragments are copied with newly generated IDs. Files and images are shared with the new OrgPage.

Copy requires the user to be able to view the source OrgPage, and the API key itself must have `permission/admin`.
Single-OrgPage API keys cannot copy.

```http
POST https://orgpad.info/api/v1/o/{orgpage-id}/copy
POST https://orgpad.info/api/v1/s/{short-link}/copy
```

The request does not need a body. The response returns the new OrgPage ID.

JSON example response:

```json
{
  "id": "6dab909c-86ea-4eaf-8366-af51a025fc01"
}
```

EDN example response:

```clojure
{:orgpage/id #uuid "6dab909c-86ea-4eaf-8366-af51a025fc01"}
```

## Read Sharing State

Retrieves the permission map for the target OrgPage. It contains the owner, users, and usergroups with explicit access,
public sharing state, and tokens and short links for view, comment, and edit access. Requires admin permission on the
OrgPage.

```http
GET https://orgpad.info/api/v1/o/{orgpage-id}/share
GET https://orgpad.info/api/v1/s/{short-link}/share
```

JSON example response:

```json
{
  "owner": {
    "id": "50475d55-4f6d-401f-9b08-33927a04897f",
    "firstName": "Pavel",
    "lastName": "Klavík"
  },
  "users": [
    {
      "id": "d74c2dc2-4303-479e-b2b7-fe588f8491e4",
      "firstName": "Kamila",
      "lastName": "Klavíková",
      "permission": "permission/admin"
    },
    {
      "id": "3dcdf6bf-6d44-4889-bb9e-ecd984f4b3f8",
      "name": "OrgPad development",
      "permission": "permission/edit"
    }
  ],
  "public": "permission/none",
  "viewToken": "d712ed43-944b-40f8-a11c-e0fd918944ff",
  "commentToken": "225d5737-a21e-4dcc-86e5-3e8026551188",
  "editToken": "61921cb1-9cda-4d28-bc40-7dea7e50d2a3",
  "viewLink": "B0i41gxAIeK",
  "commentLink": "whHqNe7Gt1Z",
  "editLink": "8od8fh2HJUn",
  "fragments": [
    {
      "id": "361e0937-4876-4600-8815-39cae4bdbc96",
      "textId": "bin-theorem",
      "title": "Binomial Theorem"
    },
    {
      "id": "1a23f4b4-d1ca-49e3-9741-d4811f899f8d",
      "textId": "pascal-video",
      "title": "Pascal video"
    }
  ],
  "paths": [
    {
      "id": "6d12c87d-1804-472b-8cc7-b2259ef2ba27",
      "title": "Example presentation",
      "viewLink": "Yc7QIOzSwvt",
      "commentLink": "Fml10j-DTsU",
      "editLink": "2px5WB-XbyO",
      "numSteps": 6
    }
  ]
}
```

EDN example response:

```clojure
{:permissions/owner         {:user/id         #uuid "50475d55-4f6d-401f-9b08-33927a04897f"
                             :user/first-name "Pavel"
                             :user/last-name  "Klavík"}
 :permissions/users         [{:user/id         #uuid "d74c2dc2-4303-479e-b2b7-fe588f8491e4"
                              :user/first-name "Kamila"
                              :user/last-name  "Klavíková"
                              :user/permission :permission/admin}
                             {:usergroup/id         #uuid "3dcdf6bf-6d44-4889-bb9e-ecd984f4b3f8"
                              :usergroup/name       "OrgPad development"
                              :usergroup/permission :permission/edit}]
 :permissions/public        :permission/none
 :permissions/view-token    #uuid "d712ed43-944b-40f8-a11c-e0fd918944ff"
 :permissions/comment-token #uuid "225d5737-a21e-4dcc-86e5-3e8026551188"
 :permissions/edit-token    #uuid "61921cb1-9cda-4d28-bc40-7dea7e50d2a3"
 :permissions/view-link     "B0i41gxAIeK"
 :permissions/comment-link  "whHqNe7Gt1Z"
 :permissions/edit-link     "8od8fh2HJUn"
 :permissions/fragments     [{:fragment/id      #uuid "361e0937-4876-4600-8815-39cae4bdbc96"
                              :fragment/text-id "bin-theorem"
                              :fragment/title   "Binomial Theorem"}
                             {:fragment/id      #uuid "1a23f4b4-d1ca-49e3-9741-d4811f899f8d"
                              :fragment/text-id "pascal-video"
                              :fragment/title   "Pascal video"}]
 :permissions/paths         [{:path/id           #uuid "6d12c87d-1804-472b-8cc7-b2259ef2ba27"
                              :path/title        "Example presentation"
                              :path/view-link    "Yc7QIOzSwvt"
                              :path/comment-link "Fml10j-DTsU"
                              :path/edit-link    "2px5WB-XbyO"
                              :path/num-steps    6}]}
```

The example refers to a real OrgPage, but the tokens and short links shown here are example values and do not grant
access. Treat tokens and short links from your own OrgPages as sensitive access credentials.

Response fields:

- `owner`: owner of the OrgPage. Contains `id`, and optionally `firstName` and `lastName`.
- `users`: users and usergroups (teams) with explicit access to the OrgPage. User entries contain `id`,
  optional `firstName`, optional `lastName`, and `permission`. Usergroup entries contain `id`, `name`, and `permission`.
- `public`: public access level for the OrgPage. Possible values are `permission/none` and `permission/view`. With
  `permission/view`, anyone can read the OrgPage, and it appears in the list of public OrgPages.
- `viewToken`, `commentToken`, `editToken`: permission tokens for view, comment, and edit access. A token grants the
  corresponding access level when used in an OrgPad share URL.
- `viewLink`, `commentLink`, `editLink`: short links for view, comment, and edit access. A full OrgPad URL is
  `https://orgpad.info/s/{short-link}`. The same short link can be used with API short-link routes, for example
  `https://orgpad.info/api/v1/s/{short-link}`.
- `fragments`: named locations in the OrgPage. Each entry contains `id`, optional `textId`, and `title`. To open
  the OrgPage on a fragment, add `#id` or `#textId` to the URL.
- `paths`: presentations in the OrgPage. Each entry contains `id`, `title`, permission short links, and `numSteps`.
  To open a presentation on a particular step, use the `step` query parameter.

## Update Sharing State

Applies a single permission operation to the target OrgPage. Requires admin permission on the OrgPage.

```http
POST https://orgpad.info/api/v1/o/{orgpage-id}/share
POST https://orgpad.info/api/v1/s/{short-link}/share
```

In JSON, the request body is an array with the operation name followed by zero to two parameters:
`[operation-id, param1, param2]`. In EDN, the same shape is written as a vector. The response returns the modified
permission map in the same format as the read endpoint.

To find a usergroup ID, call the [OrgPage listing](read.md#orgpage-listing) endpoint with a general API key. The
response includes a `usergroups` field for teams that the API key owner belongs to. Each usergroup contains its `id`
and `name`. Or share an OrgPage with this usergroup manually and [read the OrgPage sharing state](#read-sharing-state).

The following permission operations are supported:

### Set public permission

Changes the public permission for the OrgPage. Possible values are `permission/none` and `permission/view`.

JSON body:

```json
[
  "permission/set-public-permission",
  "permission/view"
]
```

EDN body:

```clojure
[:permission/set-public-permission :permission/view]
```

### Reset tokens

Invalidates all permission tokens and short links for the OrgPage and all of its paths. The response contains newly
generated tokens and short links. Use this when existing tokens or short links were shared by accident.

JSON body:

```json
[
  "permission/reset-tokens"
]
```

EDN body:

```clojure
[:permission/reset-tokens]
```

### Add user or usergroup

Shares the OrgPage with the specified user or usergroup. The first parameter identifies the user or usergroup by ID, or
the user by email. The permission is one of `permission/view`, `permission/comment`, `permission/edit`, and
`permission/admin`.

JSON body:

```json
[
  "permission/add-user",
  "support@orgpad.info",
  "permission/view"
]
```

EDN body:

```clojure
[:permission/add-user "support@orgpad.info" :permission/view]
```

[Errors](errors.md#sharing-errors):

- `invalid-user-or-usergroup`: the user or usergroup reference could not be resolved.
- `already-has-permission`: the user or usergroup already has explicit permission on the OrgPage.
- `cannot-change-owner-permission`: the target user is the OrgPage owner.
- `owner-share-limit-reached`: adding this permission would exceed the owner's sharing limit. Sharing limit applies only
  when the owner is in free plan.

### Set user or usergroup permission

Changes permission for the specified user or usergroup. The first parameter identifies the user or usergroup by ID, or
the user by email. The permission is one of `permission/view`, `permission/comment`, `permission/edit`, and
`permission/admin`.

JSON body:

```json
[
  "permission/set-user-permission",
  "support@orgpad.info",
  "permission/edit"
]
```

EDN body:

```clojure
[:permission/set-user-permission "support@orgpad.info" :permission/edit]
```

[Errors](errors.md#sharing-errors):

- `invalid-user-or-usergroup`: the user or usergroup reference could not be resolved.
- `permission-not-found`: the user or usergroup does not have explicit permission on the OrgPage.
- `cannot-change-owner-permission`: the target user is the OrgPage owner.

### Remove user or usergroup permission

Removes permission for the specified user or usergroup. The first parameter identifies the user or usergroup by ID, or
the user by email.

JSON body:

```json
[
  "permission/remove-user",
  "support@orgpad.info"
]
```

EDN body:

```clojure
[:permission/remove-user "support@orgpad.info"]
```

[Errors](errors.md#sharing-errors):

- `invalid-user-or-usergroup`: the user or usergroup reference could not be resolved.
- `permission-not-found`: the user or usergroup does not have explicit permission on the OrgPage.
- `cannot-change-owner-permission`: the target user is the OrgPage owner.

## Related Pages

Use these pages when OrgPage management connects to authentication, editing, or errors.

| Page                                   | When to use it                                                |
|----------------------------------------|---------------------------------------------------------------|
| [Operations](ops.md)                   | Edit OrgPage content after creation.                          |
| [Read endpoints](read.md)              | Read OrgPages and sharing state returned by the API.          |
| [API cookbook](cookbook.md)            | See create, copy, and delete examples with `curl`.            |
| [Authentication and API keys](auth.md) | Understand API key permissions and subscription requirements. |
| [Errors](errors.md#sharing-errors)     | Understand common management and sharing errors.              |
