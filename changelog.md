# API Changelog

Changes to the experimental OrgPad API are listed here in reverse chronological order. For complete request and
response details, follow the links in each entry.

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
