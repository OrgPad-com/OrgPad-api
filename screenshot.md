# Screenshot an OrgPage

The screenshot endpoint generates one image synchronously. It requires view permission on the target OrgPage and
returns either WebP or PNG data.

## Contents

- [Endpoint](#endpoint)
- [Choose the Image Format](#choose-the-image-format)
- [Screenshot Map](#screenshot-map)
- [Choose a Screenshot Target](#choose-a-screenshot-target)
- [Dry Run](#dry-run)
- [Errors](#errors)
- [Related Pages](#related-pages)

## Endpoint

```http
POST https://orgpad.info/api/v1/o/{orgpage-id}/screenshot
POST https://orgpad.info/api/v1/s/{short-link}/screenshot
```

The endpoint accepts JSON, EDN, or Transit request bodies. This JSON request generates a dark WebP screenshot of an
implicit fragment identified by a cell's `textId`:

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/screenshot" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  -H "Accept: image/webp, application/json" \
  --data '{
    "resolution": [1200, 800],
    "theme": "dark",
    "fragmentId": "first-cell"
  }' \
  --output screenshot.webp
```

The response body is the image data on success. Save it as a binary file; do not decode it as text.

## Choose the Image Format

Use `Accept: image/webp` for WebP or `Accept: image/png` for PNG. WebP is the default when `Accept` is omitted.

The request body remains a structured format. To receive structured errors while requesting an image, include a normal
API format as an additional accepted value, for example `Accept: image/png, application/json`. A successful request
then returns `image/png`; an error returns JSON. See [binary screenshot responses](formats.md#binary-screenshot-responses).

## Screenshot Map

All fields are optional. An empty map results in all cells closed, unless an initial desktop fragment is set.

| JSON field       | EDN key                        | Description                                                                                                                |
| ---------------- | ------------------------------ |----------------------------------------------------------------------------------------------------------------------------|
| `resolution`     | `:screenshot/resolution`       | Image width and height as `[width, height]`. Each value must be an integer from 300 to 4000. The default is `[1280, 720]`. |
| `theme`          | `:screenshot/theme`            | `light` or `dark`. By default, OrgPad uses the API key owner's configured theme.                                           |
| `fragmentId`     | `:screenshot/fragment-id`      | An explicit fragment ID, an implicit fragment target, or its text ID.                                                      |
| `open`           | `:screenshot/open`             | Use `all` (`:all` in EDN) to open all cells.                                                                               |
| `openedPageIds`  | `:screenshot/opened-page-ids`  | Page IDs or text IDs to open.                                                                                              |
| `focusedBookIds` | `:screenshot/focused-book-ids` | Book IDs or text IDs to focus.                                                                                             |
| `hiddenBookIds`  | `:screenshot/hidden-book-ids`  | Book IDs or text IDs to hide.                                                                                              |

JSON uses arrays for the three ID sets. EDN uses sets. Duplicate IDs do not change the result.

`fragmentId` can refer to an explicit saved fragment. It can also refer to a unit, link, path, or path step and uses
that object's implicit fragment. See [fragments and implicit fragments](orgpage.md#fragments).

The public EDN Malli schema is available as `screenshot-orgpage` in [Malli schemas](schema.cljc).

## Choose a Screenshot Target

Specify at most one of these target forms:

- `fragmentId` for an explicit or implicit fragment;
- `open: "all"` / `:screenshot/open :all` to open all cells;
- one or more of `openedPageIds`, `focusedBookIds`, and `hiddenBookIds` to describe the screenshotted state, without
  explicitly constructing a fragment first.

The three concrete ID sets may be combined with each other, but not with `fragmentId` or `open: all`. Supplying more
than one target form returns `screenshot-target-conflict`.

## Dry Run

Add `dry-run=true` to validate and normalize the request without generating an image:

```http
POST https://orgpad.info/api/v1/o/{orgpage-id}/screenshot?dry-run=true
Accept: image/webp, application/json
```

Dry run returns the resolved screenshot map as normal structured API data, including defaults and the selected output
format. It is useful for checking text IDs and a screenshot target before submitting the real request.

## Errors

The endpoint can return normal authentication, permission, request-body, and ID errors. Its dedicated errors are
`screenshot-target-conflict`, `screenshot-fragment-not-found`, `screenshot-failed`, and `screenshot-timeout`; see
[Screenshot Errors](errors.md#screenshot-errors).

## Related Pages

| Page                                   | When to use it                                            |
| -------------------------------------- | --------------------------------------------------------- |
| [PDF prints of OrgPages](print.md)     | Generate a PDF or print a whole presentation path.        |
| [Input and output formats](formats.md) | Choose image and structured error formats.                |
| [Authentication and API keys](auth.md) | Create a view key and understand sharing-token access.    |
| [OrgPage data](orgpage.md)             | Learn about fragments, units, links, paths, and text IDs. |
| [Malli schemas](schema.cljc)           | Validate EDN request bodies before calling the endpoint.  |
| [Errors](errors.md)                    | Handle screenshot and normal API errors.                  |
