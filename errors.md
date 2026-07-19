# Errors

OrgPad API errors include a stable error code, a human-readable message, a documentation link, and sometimes additional
fields with error-specific context. Read this page when a request fails, when you need to handle errors
programmatically, or when you want to understand the main API failure states.

Use the error `code` for programmatic handling. Do not parse `message`, because its wording may change. Use `docs` and
additional fields to find the next specific check.

## Contents

- [How to Debug an API Error](#how-to-debug-an-api-error)
- [Handling Error Bodies in Scripts](#handling-error-bodies-in-scripts)
- [Error Response Format](#error-response-format)
- [Additional Error Fields](#additional-error-fields)
- [HTTP Status Codes](#http-status-codes)
- [Authentication, Subscription, and Scope Errors](#authentication-subscription-and-scope-errors)
- [Request Format and Body Errors](#request-format-and-body-errors)
- [Screenshot Errors](#screenshot-errors)
- [Print Errors](#print-errors)
- [Route and Object Lookup Errors](#route-and-object-lookup-errors)
- [ID and Operation Errors](#id-and-operation-errors)
- [Unit, Book, Page, Link, and Path Errors](#unit-book-page-link-and-path-errors)
- [Content Format Errors](#content-format-errors)
- [Image, File, Video, and Audio Errors](#image-file-video-and-audio-errors)
- [Math, Embed, YouTube, and OrgPage Embed Errors](#math-embed-youtube-and-orgpage-embed-errors)
- [Sharing Errors](#sharing-errors)
- [Related Pages](#related-pages)

## How to Debug an API Error

Start with the response status, then use the error body to narrow the problem.

1. Check the HTTP status. It tells you whether the failure is about authentication, permissions, request format,
   validation, lookup, upload size, or media type.
2. Check `error.code`. This is the stable value your integration should branch on.
3. Follow `error.docs`. It points to the closest documentation section for the code.
4. Inspect additional fields. Fields such as `requiredPermission`, `schemaError`, `invalidId`, `childUnitIds`,
   `supportedFormats`, `uploadSize`, or `availableSpace` identify the failed input or the failed constraint.
5. For `401` and `403`, check both the user account permission and the API key permission. A request succeeds only when
   both are high enough.
6. For `400` on a request with a body, check `Content-Type`, JSON/EDN/Transit syntax, field names, enum values, and the
   endpoint schema.
7. For operation or content errors, retry with `?dry-run=true` when available. Dry-run output shows the expanded
   operation list without changing the OrgPage.

If an error code appears in this page but the linked page has the detailed rule, treat this page as the index and the
linked page as the source for that endpoint or feature.

## Handling Error Bodies in Scripts

OrgPad API failures return a structured error body with an `error` object. Basic `curl` commands display this
body directly, even when the HTTP status is `400`, `401`, `403`, or another error status.

When you write scripts, your tool or HTTP client must preserve the response body. Some options and libraries handle
non-2xx responses specially: they may throw an exception, suppress the body, or store the body separately from the
status code.

A robust script should keep both:

- the HTTP status code,
- the response body.

Use the HTTP status for the general result. Use `error.code` for programmatic handling.

## Error Response Format

Failed API requests return an object with an `error` field.

JSON format:

```json
{
  "error": {
    "code": "invalid-api-key",
    "message": "Unknown or invalid API key.",
    "docs": "https://…"
  }
}
```

EDN format:

```clojure
{:error {:code    :invalid-api-key
         :message "Unknown or invalid API key."
         :docs    "https://…"}}
```

The `code` field is stable. The `message` field is for people and may change. The `docs` field links to the
documentation page or section that explains the error or the related API feature.

The upload endpoint has a separate partial-failure shape. A successful upload response includes an `errors`
array when at least one file is stored and at least one file fails. Each item in that array uses the same `code`,
`message`, and `docs` fields, but it describes one failed upload part instead of the whole request.

## Additional Error Fields

Some errors include additional fields that make the failed input easier to identify. Treat these fields as optional and
error-specific.

For example, an `unknown-id` error includes the referenced `id` for which the when the error occured:

```json
{
  "error": {
    "code": "unknown-id",
    "message": "The specified id was not found.",
    "docs": "https://…",
    "id": "018f4c8e-2f11-4cc4-a57b-b2b58f98ac9b"
  }
}
```

Common additional fields include:

| Field                                                      | When it appears                         | How to use it                                                                   |
|------------------------------------------------------------|-----------------------------------------|---------------------------------------------------------------------------------|
| `supportedFormats`                                         | Unsupported `Accept` or `Content-Type`. | Choose one listed format.                                                       |
| `schemaError`                                              | `body-schema-error`.                    | Find the request field that does not match the endpoint schema.                 |
| `requiredPermission`, `userPermission`, `apiKeyPermission` | Permission failures.                    | Compare required, user, and key permissions.                                    |
| `orgpageId`, `shortLink`, `id`, `invalidId`                | Scope, route, or ID failures.           | Check whether the referenced object exists and belongs to the expected OrgPage. |
| `childUnitIds`                                             | Ambiguous book content updates.         | Target the intended page unit directly.                                         |
| `uploadSize`, `availableSpace`                             | Upload quota failures.                  | Reduce upload size or free storage.                                             |
| `contentType`, `filename`                                  | Per-file upload failures.               | Check the uploaded file and declared MIME type.                                 |
| `latexError`, `youtubeId`                                  | Math or YouTube processing failures.    | Fix the source formula or referenced video.                                     |

## HTTP Status Codes

HTTP status codes describe the general result of the request.

| Status                       | When it is returned                                                                                                          |
| ---------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| `200 OK`                     | Successful reads and most writes. Upload responses include per-file `errors` entries when some files failed.                 |
| `201 Created`                | OrgPage creation and copy.                                                                                                   |
| `204 No Content`             | Successful delete.                                                                                                           |
| `400 Bad Request`            | Invalid route target, invalid ID, validation failure, unknown object, or operation/content error.                            |
| `401 Unauthorized`           | Missing, malformed, unknown, or invalid bearer API key.                                                                      |
| `403 Forbidden`              | Insufficient user/key permission, single-OrgPage key scope violation, expired API access, or inaccessible attachment target. |
| `404 Not Found`              | The target OrgPage does not exist, or the API user cannot view it.                                                           |
| `406 Not Acceptable`         | Unsupported `Accept` header.                                                                                                 |
| `413 Payload Too Large`      | Upload exceeds the remaining storage quota.                                                                                  |
| `415 Unsupported Media Type` | Unsupported `Content-Type` on a request that requires a body format.                                                         |
| `502 Bad Gateway`            | The screenshot service could not produce an image.                                                                           |
| `504 Gateway Timeout`        | The screenshot service did not respond within the allowed time.                                                              |

The tables below group error codes by the part of request processing that returns them. Each table gives the immediate
meaning of the code and the next documentation page or request field to inspect. For the full list of API routes, see
the [endpoint overview](README.md#endpoint-overview). When the linked page has a more specific rule, use that linked
page as the feature-level reference.

## Authentication, Subscription, and Scope Errors

These errors happen before the endpoint-specific body is processed. They are returned by the authorization and
permission checks around each API handler.

Permission errors compare the endpoint's required permission with two separate permissions: the OrgPad user account's
permission on the target OrgPage and the API key's own permission. Both must satisfy the endpoint requirement.

| Code                     | Status | Likely cause                                                                                                        | Next check                                                                                                     |
| ------------------------ | ------ | ------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| `invalid-authorization`  | `401`  | The `Authorization` header is missing or does not use the bearer token format.                                      | Check [sending the API key](auth.md#send-the-api-key).                                                         |
| `invalid-api-key`        | `401`  | The key ID is unknown, the secret is wrong, or only the key prefix was supplied.                                    | Check the [API key format](auth.md#api-key-format).                                                            |
| `expired-subscription`   | `403`  | The API key owner does not have active API access.                                                                  | Check [subscription requirements](auth.md#subscription-requirements).                                          |
| `orgpage-not-found`      | `404`  | The route target OrgPage does not exist, or the API user cannot view it.                                            | Check the OrgPage ID and whether the user account can view the OrgPage.                                        |
| `user-permission-denied` | `403`  | The user account does not have the permission required by the endpoint.                                             | Compare `requiredPermission` with `userPermission`, then check [permissions](auth.md#permissions-and-scope).   |
| `key-permission-denied`  | `403`  | The API key permission is below the permission required by the endpoint.                                            | Compare `requiredPermission` with `apiKeyPermission`, then check [permissions](auth.md#permissions-and-scope). |
| `key-for-single-orgpage` | `403`  | An OrgPage-specific key was used outside its allowed read scope or on an endpoint that is not allowed for that key. | Check [OrgPage-specific keys](auth.md#orgpage-specific-keys).                                                  |

## Request Format and Body Errors

These errors are about response format negotiation, request content type, body parsing, or schema validation.

Request body errors happen after authentication, when the API parses and validates the declared request format.

| Code                          | Status | Likely cause                                                                    | Next check                                                                              |
| ----------------------------- | ------ | ------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| `unsupported-response-format` | `406`  | The `Accept` header does not allow a response format supported by the endpoint. | Use a supported `Accept` value from [formats](formats.md#request-and-response-headers). |
| `unsupported-content-type`    | `415`  | The request body has an unsupported `Content-Type`.                             | Use `application/json` (default), `application/edn`, or `application/transit+json`.     |
| `invalid-request-body`        | `400`  | The request body could not be parsed as the declared format.                    | Check JSON/EDN/Transit syntax and string escaping.                                      |
| `body-schema-error`           | `400`  | The parsed body does not match the endpoint schema.                             | Inspect `schemaError`, then check the endpoint page or [Malli schemas](schema.cljc).    |

Common `body-schema-error` causes include:

- JSON fields use the wrong casing, such as `content-type` instead of `contentType`.
- EDN fields are missing namespaces, such as `:title` instead of `:orgpage/title`.
- An enum value is unsupported, such as an unknown `color/...`, `permission/...`, or operation name.
- A required field is missing.
- A field has the wrong shape, such as a string where a vector is required.

## Screenshot Errors

These errors are specific to [screenshot generation](screenshot.md).

| Code                            | Status | Likely cause                                                              | Next check                                                            |
| ------------------------------- | ------ | ------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| `screenshot-target-conflict`    | `400`  | More than one screenshot target was supplied.                             | Choose one target in the screenshot map.                              |
| `screenshot-fragment-not-found` | `400`  | The explicit or implicit screenshot fragment target does not exist.       | Check [screenshot targets](screenshot.md#choose-a-screenshot-target). |
| `screenshot-failed`             | `502`  | The screenshot service could not load the OrgPage or produce an image.    | Retry the request or contact support.                                 |
| `screenshot-timeout`            | `504`  | The screenshot service did not produce the image within the allowed time. | Retry with a simpler target or resolution.                            |

## Print Errors

These errors are specific to [PDF generation](print.md).

| Code                       | Status | Likely cause                                                            | Next check                                             |
| -------------------------- | ------ | ----------------------------------------------------------------------- | ------------------------------------------------------ |
| `print-target-conflict`    | `400`  | More than one print target was supplied.                                | Choose one target in the print map.                    |
| `print-fragment-not-found` | `400`  | The explicit or implicit print fragment target does not exist.          | Check [print targets](print.md#choose-a-print-target). |
| `print-failed`             | `502`  | The screenshot service could not load the OrgPage or produce a PDF.     | Retry the request or contact support.                  |
| `print-timeout`            | `504`  | The screenshot service did not produce the PDF within the allowed time. | Retry with a simpler target or presentation.           |

## Route and Object Lookup Errors

These errors mean the API could not resolve the route target or the object requested from an OrgPage.

Route-level errors happen before object-specific validation, because a route parameter does not resolve.

| Code                 | Status | Likely cause                                                 | Next check                                                                     |
| -------------------- | ------ | ------------------------------------------------------------ | ------------------------------------------------------------------------------ |
| `unknown-short-link` | `400`  | The `{short-link}` route target does not exist.              | Check the short link and the [endpoint overview](README.md#endpoint-overview). |
| `unit-not-found`     | `400`  | The unit ID or text ID does not exist in the target OrgPage. | Check the [unit read endpoint](read.md#unit).                                  |
| `link-not-found`     | `400`  | The link ID does not exist in the target OrgPage.            | Check the [link read endpoint](read.md#link).                                  |
| `math-not-found`     | `400`  | The math ID does not exist in the target OrgPage.            | Check the [math read endpoint](read.md#math).                                  |
| `embed-not-found`    | `400`  | The embed ID does not exist in the target OrgPage.           | Check the [embed read endpoint](read.md#embed).                                |
| `path-not-found`     | `400`  | The path ID does not exist in the target OrgPage.            | Check the [path read endpoint](read.md#path).                                  |
| `fragment-not-found` | `400`  | The fragment does not exist in the target OrgPage.           | Check [fragments](orgpage.md#fragments).                                       |

For object references inside operation bodies, also check [IDs and Text IDs](#id-and-operation-errors).

## ID and Operation Errors

These errors happen while parsing operation IDs, resolving text IDs, validating object types, and checking operation
ordering.

| Code                | Status | Likely cause                                                                                          | Next check                                                                                                            |
|---------------------|--------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `invalid-id`        | `400`  | A value is not a UUID, compact UUID, or known text ID where an ID is expected.                        | Check [ID formats](uuid.md#accepted-id-formats) and [text IDs](formats.md#text-ids).                                  |
| `unknown-id`        | `400`  | The ID parses correctly but does not match an existing object or an object created in the same batch. | Check operation order and [general operation errors](ops.md#general-errors).                                          |
| `wrong-id-type`     | `400`  | The ID exists but refers to the wrong object type.                                                    | For example, do not use a page ID where a book ID is required. See [general operation errors](ops.md#general-errors). |
| `id-already-exists` | `400`  | A create operation supplied an ID already used by another object.                                     | Omit the ID or generate a new UUID.                                                                                   |
| `duplicate-text-id` | `400`  | A `textId` is already used by another unit, embed, or fragment in the OrgPage.                        | Choose a unique text ID.                                                                                              |
| `op-for-removed-id` | `400`  | The same operation batch removes an object and also applies another non-removal operation to it.      | Remove the extra operation. You might also be using an incorrect id.                                                  |

## Unit, Book, Page, Link, and Path Errors

These errors describe invalid relationships between units, links, books, pages, paths, and path steps;
see [OrgPage data](orgpage.md) for details. They are returned before the operation batch is applied.

| Code                       | Status | Likely cause                                                                        | Next check                                                                                 |
|----------------------------|--------|-------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| `specify-edited-page`      | `400`  | `content` or `appendedContent` targeted a book with multiple pages.                 | Target the intended page ID or page `textId`; see [updating units](ops.md#updating-units). |
| `missing-parent`           | `400`  | A page references a parent book that does not exist in the batch or OrgPage.        | Check [creating units](ops.md#creating-units).                                             |
| `missing-child-in-parent`  | `400`  | A page points to a book, but the book does not include that page in `childUnitIds`. | Keep the page and book references consistent.                                              |
| `missing-child`            | `400`  | A book references a page that is missing or does not point back to the book.        | Check the book `childUnitIds` and page `parentId`.                                         |
| `parent-mismatch`          | `400`  | A book and page reference each other inconsistently.                                | Make `parentId` and `childUnitIds` agree.                                                  |
| `empty-children`           | `400`  | A book would have no pages.                                                         | Create or keep at least one page in the book.                                              |
| `added-page-out-of-bounds` | `400`  | A page is inserted at an index outside the book page list.                          | Check [adding pages](ops.md#adding-pages).                                                 |
| `moved-page-out-of-bounds` | `400`  | A page is moved to an index outside the book page list.                             | Check [reordering pages](ops.md#reordering-pages).                                         |
| `page-not-in-book`         | `400`  | A page operation targets a page that is not a child of the specified book.          | Check [managing pages in a book](ops.md#managing-pages-in-a-book).                         |
| `same-link-endpoints`      | `400`  | A link would connect a book to itself. Loops are not allowed in OrgPad.             | Use two different book unit IDs; see [creating links](ops.md#creating-links).              |
| `empty-path-steps`         | `400`  | A path would have no steps.                                                         | Create a path with at least one [path step](ops.md#creating-paths).                        |
| `added-step-out-of-bounds` | `400`  | A path step is inserted at an invalid index.                                        | Check [adding path steps](ops.md#adding-path-steps).                                       |
| `moved-step-out-of-bounds` | `400`  | A path step is moved to an invalid index.                                           | Check [reordering path steps](ops.md#reordering-path-steps).                               |

## Content Format Errors

These errors happen while parsing `content`, `appendedContent`, helper tags, or stored content structures. They are
returned before the page content is stored.

| Code                           | Status | Likely cause                                                             | Next check                                                                                        |
|--------------------------------|--------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| `content-and-appended-content` | `400`  | A `unit/update` operation supplied both `content` and `appendedContent`. | Choose one; see [appended content](ops_content.md#appended-content).                              |
| `string-plain-content`         | `400`  | `contentType` is `plain`, but the content is not a string.               | Check [plain text](ops_content.md#plain-text).                                                    |
| `string-markdown-content`      | `400`  | `contentType` is `markdown`, but the content is not a string.            | Check [Markdown](ops_content.md#markdown).                                                        |
| `string-html-content`          | `400`  | `contentType` is `html`, but the content is not a string.                | Check [HTML](ops_content.md#html).                                                                |
| `hiccup-content-schema-error`  | `400`  | Hiccup content does not match the accepted Hiccup structure.             | Check [Hiccup](ops_content.md#hiccup).                                                            |
| `invalid-mark-color`           | `400`  | A `mark` helper uses an unsupported color.                               | Use a supported color from [text highlighting helpers](ops_content.md#text-highlighting-helpers). |
| `unsupported-code-lang`        | `400`  | A code block uses an unsupported language identifier.                    | Use a supported language from [code helpers](ops_content.md#code-helpers).                        |

For stored content tags and attributes, see [Unit content](content.md). For API input helpers and normalization, see
[Unit content in operations](ops_content.md).

## Image, File, Video, and Audio Errors

These errors involve uploaded files, images, media helpers, attachment reuse, or upload limits.

| Code                        | Status               | Likely cause                                                                                         | Next check                                                                                                           |
|-----------------------------|----------------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| `image-not-found`           | `400`                | The image does not exist, is not attached where needed, or cannot be accessed with the key or token. | Check [image helpers](ops_content.md#image-helpers), [image reads](read.md#image) and [attachments](attachments.md). |
| `invalid-image-source`      | `400`                | An image `src` value cannot be parsed as a supported OrgPad image reference.                         | Check accepted image references in [image helpers](ops_content.md#image-helpers).                                    |
| `invalid-image-id-or-token` | `400`                | An image helper or `orgpage/add-image` operation is missing a usable ID/token pair.                  | Supply an image ID, token, or valid OrgPad image URL.                                                                |
| `invalid-image-dm-source`   | `400`                | A dark-mode image source cannot be parsed.                                                           | Check dark-mode image rules in [image helpers](ops_content.md#image-helpers).                                        |
| `dm-image-not-found`        | `400`                | The dark-mode image does not exist or cannot be accessed.                                            | Check the dark-mode image ID or token.                                                                               |
| `dark-mode-image-only`      | `400`                | The helper supplies only a dark-mode image without the main image.                                   | Supply both light and dark image references.                                                                         |
| `dark-mode-different-size`  | `400`                | Light and dark image variants have different dimensions.                                             | Use images with the same width and height.                                                                           |
| `image-missing-in-orgpages` | `403`                | A rename request specified an OrgPage that is not editable or does not contain the image.            | Check `orgpageIds` in [rename images](attachments.md#rename-images).                                                 |
| `file-not-found`            | `400`                | The file does not exist, is not attached where needed, or cannot be accessed with the key or token.  | Check [file helpers](ops_content.md#file-helpers) and [file reads](read.md#file).                                    |
| `invalid-file-source`       | `400`                | A file `src` value cannot be parsed as a supported OrgPad file reference.                            | Check accepted file references in [file helpers](ops_content.md#file-helpers).                                       |
| `invalid-file-id`           | `400`                | A file helper is missing a usable file ID.                                                           | Supply a file ID, token, or valid OrgPad file URL.                                                                   |
| `file-missing-in-orgpages`  | `403`                | A rename request specified an OrgPage that is not editable or does not contain the file.             | Check `orgpageIds` in [rename files](attachments.md#rename-files).                                                   |
| `storage-quota-exceeded`    | `413`                | Uploaded files exceed the remaining storage quota for the OrgPage owner.                             | Reduce upload size or free storage; see [upload files and images](attachments.md#upload-files-and-images).           |
| `video-not-found`           | `400`                | A video helper references a missing or inaccessible file.                                            | Check [video and audio helpers](ops_content.md#video-and-audio-helpers).                                             |
| `invalid-video-source`      | `400`                | A video `src` value cannot be parsed.                                                                | Use a supported OrgPad file reference.                                                                               |
| `invalid-video-id`          | `400`                | A video helper is missing a usable file ID.                                                          | Supply a valid video file ID or URL.                                                                                 |
| `unsupported-video-format`  | `400`                | The referenced file is not a playable video format.                                                  | Use a supported video MIME type from [video helpers](ops_content.md#video-and-audio-helpers).                        |
| `audio-not-found`           | `400`                | An audio helper references a missing or inaccessible file.                                           | Check [video and audio helpers](ops_content.md#video-and-audio-helpers).                                             |
| `invalid-audio-source`      | `400`                | An audio `src` value cannot be parsed.                                                               | Use a supported OrgPad file reference.                                                                               |
| `invalid-audio-id`          | `400`                | An audio helper is missing a usable file ID.                                                         | Supply a valid audio file ID or URL.                                                                                 |
| `unsupported-audio-format`  | `400`                | The referenced file is not a playable audio format.                                                  | Use a supported audio MIME type from [audio helpers](ops_content.md#video-and-audio-helpers).                        |

The upload endpoint reports per-file image failures in a successful `200 OK` response when other files were stored.
These entries appear in the response `errors` array, not in the failed-request `error` envelope.

| Code            | Response shape | Meaning                                                                                            | Next check                                                                                         |
|-----------------|----------------|----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `invalid-image` | `errors` item  | An uploaded file was declared or detected as an image but could not be processed as a valid image. | Check file contents, `contentType`, and [upload behavior](attachments.md#upload-files-and-images). |

## Math, Embed, YouTube, and OrgPage Embed Errors

These errors involve helper tags and operations that create or update math, embeds, YouTube videos, or embedded
OrgPages.

| Code                             | Status | Likely cause                                                                                         | Next check                                                                                      |
|----------------------------------|--------|------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `missing-math-source`            | `400`  | A new math or chemistry helper does not contain source text.                                         | Check [math and chemistry helpers](ops_content.md#math-and-chemistry-helpers).                  |
| `invalid-math-source`            | `400`  | MathJax could not render the LaTeX or chemistry source.                                              | Inspect `latexError` and check [creating math](ops.md#creating-math).                           |
| `duplicate-math-id`              | `400`  | The same math ID appears more than once.                                                             | Keep each math object linked to one page occurrence.                                            |
| `multiple-math-sources`          | `400`  | The same math ID has source in both content and a math operation.                                    | Put the source in one place only.                                                               |
| `missing-math-in-content`        | `400`  | A `math/create` operation creates math that is not referenced from its page content.                 | Check [creating math](ops.md#creating-math).                                                    |
| `removed-math-still-in-content`  | `400`  | A removed math object is still referenced from page content.                                         | Update content or remove the math consistently; see [removing math](ops.md#removing-math).      |
| `math-linked-to-different-page`  | `400`  | A math object is referenced from a page different from the one it belongs to.                        | Check [updating math](ops.md#updating-math).                                                    |
| `invalid-embed-params`           | `400`  | An embed helper is missing required parameters or has an invalid combination.                        | Check [embed helpers](ops_content.md#embed-helpers).                                            |
| `missing-embed-source`           | `400`  | A new embed has no `source` URL and no file ID.                                                      | Supply exactly one embed source.                                                                |
| `embed-source-or-file`           | `400`  | An embed operation supplies both `source` and `fileId`.                                              | Choose a URL embed or a file embed.                                                             |
| `duplicate-embed-id`             | `400`  | The same embed ID appears more than once.                                                            | Keep each embed object linked to one page occurrence.                                           |
| `multiple-embed-sources`         | `400`  | The same embed ID has source in both content and an embed operation.                                 | Put the source in one place only.                                                               |
| `missing-embed-in-content`       | `400`  | An `embed/create` operation creates an embed that is not referenced from page content.               | Check [creating embeds](ops.md#creating-embeds).                                                |
| `removed-embed-still-in-content` | `400`  | A removed embed is still referenced from page content.                                               | Update content or remove the embed consistently; see [removing embeds](ops.md#removing-embeds). |
| `embed-linked-to-different-page` | `400`  | An embed object is referenced from a page different from the one it belongs to.                      | Check [updating embeds](ops.md#updating-embeds).                                                |
| `embed-file-not-found`           | `400`  | A file embed references a missing or inaccessible file.                                              | Check [embed helpers](ops_content.md#embed-helpers).                                            |
| `unsupported-embed-file-format`  | `400`  | The file cannot be embedded.                                                                         | Use PDF, Word, Excel, or PowerPoint; otherwise link the file instead.                           |
| `invalid-orgpage-params`         | `400`  | An OrgPage embed helper is missing required parameters or has an invalid combination.                | Check [OrgPage embed helpers](ops_content.md#orgpage-embed-helpers).                            |
| `short-link-not-found`           | `400`  | An OrgPage embed helper references an unknown short link.                                            | Check the embedded OrgPage short link.                                                          |
| `invalid-orgpage-source`         | `400`  | An OrgPage embed `src` cannot be parsed.                                                             | Use a supported OrgPad OrgPage URL.                                                             |
| `orgpage-not-found`              | `400`  | The embedded OrgPage does not exist or cannot be viewed with the supplied key, token, or short link. | Check access and token values.                                                                  |
| `invalid-youtube-source`         | `400`  | A YouTube helper `src` is not a supported YouTube URL.                                               | Check [YouTube helpers](ops_content.md#youtube-helpers).                                        |
| `invalid-youtube-id`             | `400`  | A YouTube helper uses an invalid video ID.                                                           | Supply an 11-character YouTube video ID.                                                        |
| `missing-youtube-source`         | `400`  | A YouTube helper has no URL or ID.                                                                   | Supply `src` or `id`.                                                                           |
| `youtube-video-unavailable`      | `400`  | The YouTube video cannot be accessed by OrgPad.                                                      | Check `youtubeId`, privacy settings, and video availability.                                    |

## Sharing Errors

These errors happen on the sharing endpoint while validating permission operations.

| Code                             | Status | Likely cause                                                                            | Next check                                                                                |
|----------------------------------|--------|-----------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| `invalid-user-or-usergroup`      | `400`  | The user ID, email, or usergroup ID does not identify a share target.                   | Check [adding a user or usergroup](management.md#add-user-or-usergroup).                  |
| `cannot-change-owner-permission` | `400`  | The operation tries to change or remove the OrgPage owner's permission.                 | The owner always keeps admin permission.                                                  |
| `already-has-permission`         | `400`  | `permission/add-user` targets a user or usergroup that already has explicit permission. | Use `permission/set-user-permission` instead.                                             |
| `permission-not-found`           | `400`  | A set or remove operation targets a user or usergroup without explicit permission.      | Check [set user or usergroup permission](management.md#set-user-or-usergroup-permission). |
| `owner-share-limit-reached`      | `400`  | The OrgPage owner's sharing limit has been reached.                                     | Remove another explicit share or upgrade the owner account.                               |

For the permission operation request format, see [Update sharing state](management.md#update-sharing-state).

## Related Pages

Use these pages to fix common causes of API errors.

| Page                                         | When to use it                                                                                 |
|----------------------------------------------|------------------------------------------------------------------------------------------------|
| [Authentication and API keys](auth.md)       | Fix `401`, `403`, API key, scope, subscription, and permission errors.                         |
| [Input and output formats](formats.md)       | Fix `Accept`, `Content-Type`, JSON, EDN, Transit, and schema errors.                           |
| [Operations](ops.md)                         | Fix operation body, ID, unit, link, math, embed, path, and fragment errors.                    |
| [Unit content in operations](ops_content.md) | Fix `content`, `appendedContent`, content format, and helper tag errors.                       |
| [Attachments](attachments.md)                | Fix upload, quota, file, image, download, rename, and attachment reuse errors.                 |
| [Read endpoints](read.md)                    | Fix object lookup errors for units, links, files, images, maths, embeds, paths, and fragments. |
| [OrgPad UUID formats](uuid.md)               | Fix invalid IDs and understand standard UUIDs, compact UUIDs, and URL-safe IDs.                |
