# API Changelog

Changes to the experimental OrgPad API are listed here in reverse chronological order. For complete request and
response details, follow the links in each entry.

## 2026-10-22

- Added the `math/set-macros` operation for setting OrgPage-wide LaTeX macro definitions. Updating macros re-renders
  affected formulas and rejects the request if the new macros make any formula invalid. OrgPage API responses now
  include the optional `mathMacros` field. See [Math Operations](ops.md#setting-math-macros) and
  [OrgPage metadata](orgpage.md#orgpage-metadata).
- An operations request may contain at most one `math/create` or `math/update` operation per math ID. Combine changes
  into one operation to avoid the `multiple-math-operations` error. See [Math Operations](ops.md#math-operations).

## 2026-08-24

- Image thumbnails are always encoded as AVIF. Image metadata no longer includes
  `:image/thumbnail-format`.

## 2026-08-21

- Empty strings are no longer accepted as OrgPage or unit titles. Omit an OrgPage title to create an untitled OrgPage;
  use `null` in JSON or `nil` in EDN to remove a unit title. See [Managing OrgPages](management.md#create-orgpage) and
  [unit operations](ops.md#unit-operations).

## 2026-08-20

- Image uploads also recognize AVIF, HEIC, HEIF, BMP, ICO, and JPEG XL (JXL) as image attachments. See
  [Upload Files and Images](attachments.md#upload-files-and-images).

## 2026-08-16

- Video uploads now generate a poster image from the first visible frame. When `orgpage/add-file` adds a video, the
  API automatically adds its poster image to the OrgPage; removing the video removes the poster as well. A generated
  poster cannot be removed while its video remains attached and returns the `video-poster` error. See
  [adding attached files](ops.md#adding-attached-files), [removing attached files](ops.md#removing-attached-files),
  and [removing attached images](ops.md#removing-attached-images).

## 2026-07-28

- Text `mark` highlights now also support `color/light-gray` and `color/gray`. See
  [Text highlighting](ops_content.md#text-highlighting-helpers).

## 2026-07-20

- Added `GET /api/v1/info`, which returns non-secret metadata for the API key used by the request. See
  [Authentication and API keys](auth.md#inspect-the-current-api-key).
- OrgPage-specific view keys can now read public OrgPages and OrgPages authorized by a sharing token or short link.
  They can also use `GET /api/v1/public`. This does not grant edit or management access outside their assigned
  OrgPage. See [OrgPage-specific keys](auth.md#orgpage-specific-keys).
- The API-key dialog can create an empty OrgPage and an OrgPage-specific key for it in one step by selecting
  **For a new OrgPage**.
- OrgPages created through `POST /api/v1/o` now appear in open owner dashboards without a refresh. API deletion also
  notifies open dashboards and disconnects clients viewing the deleted OrgPage. See
  [Managing OrgPages](management.md).
- Added synchronous `POST /api/v1/o/{orgpage-id}/screenshot` and
  `POST /api/v1/s/{short-link}/screenshot`. They return one WebP or PNG image, support dry runs and explicit or
  implicit fragment targets, and report dedicated screenshot errors. See [Screenshot an OrgPage](screenshot.md).
- Added synchronous `POST /api/v1/o/{orgpage-id}/print` and `POST /api/v1/s/{short-link}/print`. They return one
  PDF, support dry runs and the same fragment targets as screenshots, and can print every step of a presentation path
  into one document. See [Print an OrgPage](print.md).
- Added the public `screenshot-orgpage` and `print-orgpage` Malli request-body schemas. See
  [Malli schemas](schema.cljc).
