# Authentication and API Keys

Every OrgPad API request requires a bearer API token. Create API keys in OrgPad settings
under [API](https://orgpad.info/settings/api). OrgPad shows the full token only once, so copy it immediately and store
it securely.

## Contents

- [Subscription Requirements](#subscription-requirements)
- [API Key Format](#api-key-format)
- [Send the API Key](#send-the-api-key)
- [Inspect the Current API Key](#inspect-the-current-api-key)
- [Create and Manage API Keys](#create-and-manage-api-keys)
- [Choose an API Key](#choose-an-api-key)
- [Permissions and Scope](#permissions-and-scope)
- [Security Notes](#security-notes)
- [Related Pages](#related-pages)

## Subscription Requirements

API access requires an active Professional subscription or an active organization subscription, such as School or
Enterprise.

If the API key owner's subscription does not include API access or is no longer active, requests return `403`
with `expired-subscription` [error](errors.md).

## API Key Format

An API key has this format:

```text
orgpad_<id>_<secret>
```

Example:

```text
orgpad_IYwC5kruKvQ_iyGEz107I3nsPd620yi9sKel3AC65We9
```

In this example:

- `orgpad_` is the fixed prefix for OrgPad API keys.
- `IYwC5kruKvQ` is the key ID. OrgPad uses it to find the API key record.
- `iyGEz107I3nsPd620yi9sKel3AC65We9` is the secret. It authenticates the request and must be kept private.

The key ID by itself is not enough to authenticate. The full token, including the secret, is required in
the `Authorization` header.

OrgPad never stores the secret itself. Only the SHA-256 hash of the secret is stored. Incoming requests are verified by
hashing the provided secret and comparing it with the stored hash. For this reason, the full token cannot be shown again
after creation.

## Send the API Key

Send the API key in the `Authorization` header using the bearer token scheme:

```http
Authorization: Bearer orgpad_…
```

For example:

```bash
curl "https://orgpad.info/api/v1/o" \
  -H "Authorization: Bearer orgpad_IYwC5kruKvQ_iyGEz107I3nsPd620yi9sKel3AC65We9" \
  -H "Accept: application/json"
```

A missing or malformed `Authorization` header returns `invalid-authorization` error.
An unknown API key or invalid secret returns `invalid-api-key` error. See the authentication
[error](errors.md#authentication-subscription-and-scope-errors) section for the full list.

## Inspect the Current API Key

Use this endpoint to check the non-secret metadata of the API key that authenticated the request:

```http
GET https://orgpad.info/api/v1/info
```

For example:

```bash
curl "https://orgpad.info/api/v1/info" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Accept: application/json"
```

The response includes the key's `id`, `title`, `userId`, `permission`, and `creationTime`. It also includes
`orgpageId` for an OrgPage-specific key and `lastUsed` after the key has been used. It never returns the key secret
or its SHA-256 hash.

JSON example response:

```json
{
  "id": "fHbdk95-3Vg",
  "title": "Reporting reader",
  "userId": "50475d55-4f6d-401f-9b08-33927a04897f",
  "orgpageId": "e7ff2b14-1894-4aad-aae0-cf5b08f9875e",
  "permission": "permission/view",
  "creationTime": "2026-07-18T11:21:46.931973Z",
  "lastUsed": "2026-07-19T16:16:50.195098Z"
}
```

EDN example response:

```clojure
{:api-key/id            "fHbdk95-3Vg"
 :api-key/title         "Reporting reader"
 :api-key/user-id       #uuid "50475d55-4f6d-401f-9b08-33927a04897f"
 :api-key/orgpage-id    #uuid "e7ff2b14-1894-4aad-aae0-cf5b08f9875e"
 :api-key/permission    :permission/view
 :api-key/creation-time "2026-07-18T11:21:46.931973Z"
 :api-key/last-used     "2026-07-19T16:16:50.195098Z"}
```

## Create and Manage API Keys

Create and manage API keys in OrgPad settings under [API](https://orgpad.info/settings/api).

When creating a key, choose:

- **Name**: a required label used to identify the key later.
- **Target OrgPage**: all accessible OrgPages, one selected OrgPage, or *For a new OrgPage*.
- **Permission**: view, edit, or admin.

Choose *For a new OrgPage* when an integration needs its own isolated document. OrgPad creates an empty OrgPage and
an OrgPage-specific key for it in one step.

![API key settings](img/api-key.png)

After creating a key, OrgPad shows the full token once. Copy it immediately and store it securely.

![Immediately copy the full API token](img/copy-full-token.png)

Later, the API settings list shows only the key title and token prefix. The prefix contains `orgpad_` and the key ID,
but not the secret. It is useful for identifying the key, but it cannot be used to call the API.

The API settings list also shows when a key was created, when it was last used, and the target OrgPage for
OrgPage-specific keys. Existing keys can be renamed or deleted. Delete a key to revoke it immediately.

![List of all API tokens](img/token-list.png)

## Choose an API Key

Choose the narrowest key that can do the work your integration needs.

| Use case                         | Recommended key            |
|----------------------------------|----------------------------|
| Read one OrgPage                 | OrgPage-specific view key  |
| Read many accessible OrgPages    | General view key           |
| Edit one OrgPage                 | OrgPage-specific edit key  |
| Edit many accessible OrgPages    | General edit key           |
| Delete OrgPage or manage sharing | OrgPage-specific admin key |
| Create or copy OrgPages          | General admin key          |

Use a general key only when the integration needs to work across multiple OrgPages or create new OrgPages.

Use an OrgPage-specific key when an integration should be limited to one OrgPage. OrgPage-specific keys cannot create
new OrgPages, copy OrgPages, or use endpoints that are not scoped to that OrgPage.

## Permissions and Scope

API keys have one of these permission levels:

- `permission/view`
- `permission/edit`
- `permission/admin`

OrgPad has a separate `permission/comment` level for OrgPage sharing, but API keys cannot be created with comment
permission.

In JSON request and response bodies, permissions are strings such as `permission/edit`. In EDN request and response
bodies, permissions are keywords such as `:permission/edit`.

A request succeeds only when both the user account and the API key have enough permission for the endpoint.

For example, an edit API key cannot delete an OrgPage since the endpoint requires admin permission. Similarly, an admin
API key cannot edit an OrgPage when the user account itself only has view permission on that OrgPage.

### General keys

A general key can access every OrgPage where the key owner has enough permission for the requested API action.

For example, a general edit key can edit OrgPages where the key owner has edit or admin permission. It cannot edit
OrgPages where the key owner has only view permission.

### OrgPage-specific keys

An OrgPage-specific key is normally limited to one selected OrgPage.

For OrgPage-specific keys, the available permission choices depend on the key owner's permission on that OrgPage:

- View is available when the OrgPage can be selected.
- Edit is available only when the key owner can edit the OrgPage.
- Admin is available only when the key owner is an admin of the OrgPage.

An OrgPage-specific key can also read another OrgPage when that OrgPage is public or when a sharing token in the route
authorizes view access. It can use the public OrgPage listing for the same reason. These exceptions are read-only: a
sharing token does not let the key edit or manage another OrgPage.

It cannot access your OrgPage listing or any other non-public OrgPage without a sharing token. Accessing these endpoints
returns `403` with `key-for-single-orgpage` [error](errors.md#authentication-subscription-and-scope-errors).

### Permission errors

Permission failures use different error codes depending on what failed:

| Code                     | Status | What to check                                                                               |
| ------------------------ | ------ | ------------------------------------------------------------------------------------------- |
| `orgpage-not-found`      | `404`  | The OrgPage does not exist, or the key owner cannot view it.                                |
| `user-permission-denied` | `403`  | The key owner does not have enough permission on the OrgPage.                               |
| `key-permission-denied`  | `403`  | The API key does not have the required permission level.                                    |
| `key-for-single-orgpage` | `403`  | An OrgPage-specific key was used outside its allowed read scope or on an unscoped endpoint. |
| `expired-subscription`   | `403`  | The key owner does not have active API access.                                              |

For troubleshooting details, see [Authentication, subscription, and scope errors](errors.md#authentication-subscription-and-scope-errors).

## Security Notes

Treat API keys as secrets. Anyone with the full token can access OrgPad data allowed by that key.

Follow these rules:

- Store API keys in environment variables, secret managers, or secure server-side configuration.
- Do not put API keys in client-side code, public repositories, screenshots, logs, or shared OrgPages.
- Use OrgPage-specific keys when possible.
- Delete keys that are no longer used.
- Delete and recreate a key if it may have been exposed.

After an API key is created, OrgPad sends a confirmation email to the account owner. If you receive this email but did
not create the key, delete the key immediately in OrgPad settings and
contact [support@orgpad.info](mailto:support@orgpad.info).

## Related Pages

Use these pages when authentication connects to formats, endpoints, or errors.

| Page                                                             | When to use it                                                             |
|------------------------------------------------------------------|----------------------------------------------------------------------------|
| [Managing OrgPages](management.md)                               | Create, delete, copy, and share OrgPages.                                  |
| [Read endpoints](read.md)                                        | Read OrgPages and OrgPage objects with view permission.                    |
| [Operations](ops.md)                                             | Edit OrgPage content with an API key that has edit permission.             |
| [Input and output formats](formats.md)                           | Understand `Accept`, `Content-Type`, JSON, EDN, and Transit.               |
| [Errors](errors.md#authentication-subscription-and-scope-errors) | Fix `401`, `403`, API key, subscription, scope, and permission errors.     |
| [API overview](README.md)                                        | Start from the quick start and endpoint map before applying API key rules. |
