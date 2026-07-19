# Print an OrgPage

The print endpoint generates one PDF synchronously. It requires view permission on the target OrgPage and returns PDF
data. Supplying a presentation path produces one PDF page for each path step, merged into a single document.

## Contents

- [Endpoint](#endpoint)
- [Choose the PDF Format](#choose-the-pdf-format)
- [Print Map](#print-map)
- [Choose a Print Target](#choose-a-print-target)
- [Dry Run](#dry-run)
- [Errors](#errors)
- [Related Pages](#related-pages)

## Endpoint

```http
POST https://orgpad.info/api/v1/o/{orgpage-id}/print
POST https://orgpad.info/api/v1/s/{short-link}/print
```

The endpoint accepts JSON, EDN, or Transit request bodies. This JSON request generates a portrait Letter PDF of an
implicit fragment identified by a cell's `textId`:

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/print" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  -H "Accept: application/pdf, application/json" \
  --data '{
    "color": "gray",
    "size": "letter",
    "orientation": "portrait",
    "fragmentId": "first-cell"
  }' \
  --output orgpage.pdf
```

The response body is PDF data on success. Save it as a binary file; do not decode it as text.

## Choose the PDF Format

The only successful response format is `application/pdf`, and it is the default when `Accept` is omitted.

The request body remains a structured format. To receive structured errors while requesting a PDF, include a normal
API format as an additional accepted value, for example `Accept: application/pdf, application/json`. A successful
request then returns `application/pdf`; an error returns JSON. See [binary PDF responses](formats.md#binary-pdf-responses).

## Print Map

All fields are optional. Defaults are white, A4, and landscape. An empty map prints all cells closed, unless an initial
desktop fragment is set.

| JSON field       | EDN key                   | Description                                                              |
| ---------------- | ------------------------- | ------------------------------------------------------------------------ |
| `color`          | `:print/color`            | `white`, `gray`, or `dm`. The default is `white`.                        |
| `size`           | `:print/size`             | `a4`, `letter`, `ratio-4-to-3`, or `ratio-16-to-9`. The default is `a4`. |
| `orientation`    | `:print/orientation`      | `landscape` or `portrait`. The default is `landscape`.                   |
| `padding`        | `:print/padding`          | A uniform padding number or `[horizontal, vertical]`, from 0 to 500.     |
| `pathId`         | `:print/path-id`          | A path ID or text ID. Prints every path step into one merged PDF.        |
| `fragmentId`     | `:print/fragment-id`      | An explicit fragment ID, an implicit fragment target, or its text ID.    |
| `open`           | `:print/open`             | Use `all` (`:all` in EDN) to open all cells.                             |
| `openedPageIds`  | `:print/opened-page-ids`  | Page IDs or text IDs to open.                                            |
| `focusedBookIds` | `:print/focused-book-ids` | Book IDs or text IDs to focus.                                           |
| `hiddenBookIds`  | `:print/hidden-book-ids`  | Book IDs or text IDs to hide.                                            |

JSON uses arrays for the three ID sets. EDN uses sets. Duplicate IDs do not change the result.

`fragmentId` can refer to an explicit saved fragment. It can also refer to a unit, link, path, or path step and uses
that object's implicit fragment. See [fragments and implicit fragments](orgpage.md#fragments).

`pathId` uses the path's ordered steps. It cannot be combined with another target form.

The public EDN Malli schema is available as `print-orgpage` in [Malli schemas](schema.cljc).

## Choose a Print Target

Specify at most one of these target forms:

- `pathId` to print every step in a presentation path into one PDF;
- `fragmentId` for an explicit or implicit fragment;
- `open: "all"` / `:print/open :all` to open all cells;
- one or more of `openedPageIds`, `focusedBookIds`, and `hiddenBookIds` to describe the printed state, without
  explicitly constructing a fragment first.

The three concrete ID sets may be combined with each other, but not with `pathId`, `fragmentId`, or `open: all`.
Supplying more than one target form returns `print-target-conflict`.

## Dry Run

Add `dry-run=true` to validate and normalize the request without generating a PDF:

```http
POST https://orgpad.info/api/v1/o/{orgpage-id}/print?dry-run=true
Accept: application/pdf, application/json
```

Dry run returns the resolved print map as normal structured API data, including defaults and the selected target. It is
useful for checking text IDs and a print target before submitting the real request.

## Errors

The endpoint can return normal authentication, permission, request-body, and ID errors. Its dedicated errors are
`print-target-conflict`, `print-fragment-not-found`, `print-failed`, and `print-timeout`; see
[Print Errors](errors.md#print-errors).

## Related Pages

| Page                                   | When to use it                                            |
| -------------------------------------- | --------------------------------------------------------- |
| [Screenshot an OrgPage](screenshot.md) | Generate a PNG or WebP image from an OrgPage.             |
| [Input and output formats](formats.md) | Choose PDF and structured error formats.                  |
| [Authentication and API keys](auth.md) | Create a view key and understand sharing-token access.    |
| [OrgPage data](orgpage.md)             | Learn about fragments, units, links, paths, and text IDs. |
| [Malli schemas](schema.cljc)           | Validate EDN request bodies before calling the endpoint.  |
| [Errors](errors.md)                    | Handle print and normal API errors.                       |
