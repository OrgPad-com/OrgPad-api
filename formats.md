# Input and Output Formats

The OrgPad API supports JSON (default), EDN, and Transit for request and response bodies. Use `Content-Type` to describe
non-JSON request bodies and `Accept` to choose non-JSON response formats. JSON is the primary format used by API and in
these docs. EDN and Transit are especially useful for Clojure and ClojureScript clients.

## Contents

- [Request and Response Headers](#request-and-response-headers)
- [JSON and EDN Naming Rules](#json-and-edn-naming-rules)
- [IDs and UUIDs](#ids-and-uuids)
- [Text IDs](#text-ids)
- [Related Pages](#related-pages)

## Request and Response Headers

The API supports these request and response formats:

| Format  | Content type               |
|---------|----------------------------|
| JSON    | `application/json`         |
| EDN     | `application/edn`          |
| Transit | `application/transit+json` |

When no format is specified, the API uses JSON for both input and output.

Set `Content-Type` when the request body is EDN or Transit. For JSON requests, `Content-Type: application/json` is
optional but often useful for readability.

Set `Accept` to choose the response format.

```http
Content-Type: application/json
Accept: application/json
```

Unsupported `Accept` values return `406` with `unsupported-response-format` error.

Unsupported `Content-Type` values return `415` with `unsupported-content-type` error.

If the request body cannot be parsed, the API returns `400` with `invalid-request-body` error. If the parsed body does
not match the endpoint schema, the API returns `400` with `body-schema-error`.

For request format errors, see [Request format and body errors](errors.md#request-format-and-body-errors). For the
full error envelope, see [Error response format](errors.md#error-response-format).

## JSON and EDN Naming Rules

JSON object keys are plain strings.

- Single-word keys use the simple field name, such as `title`.
- Multi-word keys use camelCase, such as `creationTime`, `contentType`, or `childUnitIds`.

Enum values and operation names are strings with a prefix separated by `/`, such as `color/orchid`, `permission/edit`,
`unit/book`, or `unit/create`.

Datetime values are UTC ISO 8601 strings, such as `2026-05-14T13:46:12.863945Z`.

Example JSON body:

```json
{
  "title": "Title",
  "description": "Description",
  "tags": [
    "api",
    "docs"
  ],
  "color": "color/orange"
}
```

In EDN, object keys, enum values, and operation names are namespaced keywords. Sets are used intead of vectors when
order of elements does not matter. The JSON body above corresponds to this EDN body:

```clojure
{:orgpage/title       "Title"
 :orgpage/description "Description"
 :orgpage/tags        #{"api" "docs"}
 :orgpage/color       :color/orange}
```

[Transit](https://github.com/cognitect/transit-clj) encodes EDN data into JSON without losing type information and can
be more efficient when sending large messages. The EDN example above looks like this in Transit:

```json
[
  "^ ",
  "~:orgpage/title",
  "Title",
  "~:orgpage/description",
  "Description",
  "~:orgpage/tags",
  [
    "~#set",
    [
      "api",
      "docs"
    ]
  ],
  "~:orgpage/color",
  "~:color/orange"
]
```

API supports Transit for convenience and it will not be discussed further in this documentation.

## IDs and UUIDs

OrgPad object IDs are UUIDv4 values.

JSON response bodies return object IDs as standard UUID strings:

```json
{
  "id": "0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd"
}
```

EDN response bodies use native UUID literals:

```clojure
{:orgpage/id #uuid "0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd"}
```

OrgPad also supports compact UUID strings encoded in URL-safe base64. For example, the above UUID can be represented as
`"AHE9K_jnBB4ouByMo-9KjN"`. For conversion details, see [OrgPad UUID Formats](uuid.md).

API routes and request bodies accept standard UUID strings, EDN native UUID literals, and OrgPad compact UUID strings.
ID fields in API responses use standard UUID strings in JSON and native UUID literals in EDN. OrgPad URLs embedded in
responses usually use compact IDs, so you may see compact IDs in image URLs, file links, embedded OrgPage URLs, or other
OrgPad-generated links.

## Text IDs

OrgPad allows adding a user-defined identifier called `textId` to a unit, embed, or fragment. Text IDs are stored on the
object and must be unique within one OrgPage.

For example, you can add `pythagorean-theorem` to a unit and then use this `textId` instead of its ID in request bodies
and routes. This makes API changes more readable and keeps references stable without having to deal with long UUIDs.

Text IDs can be used in supported ID fields in the same request that creates them and in later requests. They must match
`^[A-Za-z0-9_-]+$`.

## Related Pages

Use these pages when formats connect to request bodies, IDs, content, or error handling.

| Page                                               | When to use it                                                               |
|----------------------------------------------------|------------------------------------------------------------------------------|
| [Operations](ops.md)                               | Learn how operation request bodies are structured.                           |
| [Read endpoints](read.md)                          | See how response formats affect returned OrgPage data.                       |
| [Attachments](attachments.md)                      | Upload files and images with multipart requests.                             |
| [Unit content](content.md)                         | Understand stored unit content tags, attributes, and examples.               |
| [Unit content in operations](ops_content.md)       | Send HTML, Hiccup, Markdown, helper tags, files, embeds, and math.           |
| [OrgPage data](orgpage.md)                         | Understand full OrgPage response data and object fields.                     |
| [OrgPad UUID formats](uuid.md)                     | Understand standard UUIDs, compact IDs, and ID conversion.                   |
| [Malli schemas](schema.cljc)                       | Validate EDN request bodies with [Malli](https://github.com/metosin/malli).  |
| [Errors](errors.md#request-format-and-body-errors) | Fix `Accept`, `Content-Type`, parsing, and schema errors.                    |
| [API overview](README.md)                          | Start from the quick start and endpoint map before choosing request formats. |
