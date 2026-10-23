# Schemas and TypeScript Types

OrgPad provides request schemas and TypeScript types alongside the API reference. Choose the representation that matches
your request format and tools.

## Contents

- [TypeScript Types](#typescript-types)
- [JSON Schema](#json-schema)
- [Malli Schemas](#malli-schemas)
- [Scope and Limitations](#scope-and-limitations)
- [Related Pages](#related-pages)

## TypeScript Types

The TypeScript files describe JSON requests and responses as [TypeScript types](types). Import endpoint types from
[api.types.ts](types/api.types.ts), shared response objects from [data.types.ts](types/data.types.ts), and individual
operations or content types from [ops.types.ts](types/ops.types.ts).

For example, the following code creates two cells and connects them:

```ts
import type { OperationsRequest } from "./types/api.types";

const operations: OperationsRequest = [
  ["unit/create", {
    id: "hello-book",
    pageId: "hello-page",
    pos: [0, 0],
    title: "Hello"
  }],
  ["unit/create", {
    id: "world-book",
    pageId: "world-page",
    pos: [320, 0],
    title: "World"
  }],
  ["link/create", {
    endpointIds: ["hello-book", "world-book"]
  }]
];
```

The import paths above assume the files are in a `types` directory next to your source; adjust them to wherever you
place the files.

TypeScript catches structural mistakes before a request is sent. For example, a link needs exactly two endpoints:

```ts
const invalidOperations: OperationsRequest = [
  // @ts-expect-error A link connects exactly two book IDs.
  ["link/create", { endpointIds: ["hello-book", "world-book", "extra-book"] }]
];
```

It cannot verify semantic constraints such as whether these IDs exist in the target OrgPage; the API validates those.

### Request and response bodies

The endpoint request and response types describe bodies, not path parameters, query parameters, or headers. For those,
consult the API documentation. A request type of `undefined` means the route has no request body. Route comments use the
OrgPage-ID form where an equivalent short-link route is also available. JSON page content in responses is HTML. Uploads
use multipart form data, while successful screenshots, prints, and attachment downloads are binary data.

Endpoint response types include both the successful body and a structured API error using `ApiResponse<T>`
from `api.types.ts`. Only the body shape is described, not HTTP status codes; check the status to determine which
response you received. Binary and no-content successes require different body handling from JSON errors, and network
failures or non-JSON errors are not represented by this union. Uploads can also return per-file errors in a successful
response.

### Errors

Types for all error maps are described in [errors.types.ts](types/errors.types.ts). The error union is discriminated
by `code`. Endpoint response types select the applicable known codes. Future server versions may introduce new codes or
fields, so keep these types up-to-date in your code or handle unexpected errors. File and image download URLs are
outside the API, and their response types describe only successful binary bodies.

### IDs and text IDs

ID aliases such as `BookId` and `PageId` describe relationships between returned objects. `BookRef`, `PageRef`,
`UnitRef`, `EmbedRef`, and `FragmentRef` can represent either an ID or a text ID. Links, maths, files, images, paths,
and path steps do not have text IDs.

A `FragmentTargetRef` can refer to a unit or fragment by ID or text ID, and to a link, path, or path step by ID. These
aliases are plain strings, so TypeScript does not validate them or prevent mixing them. The API detects invalid or
wrong-type IDs and returns an error.

### Timestamps

Timestamps are UTC ISO 8601 strings, typed as `UtcTimestamp`. This type is just an alias for `string`, so the type does
not validate the timestamp. Timestamps only appear in server responses.

## JSON Schema

The [API JSON Schema](schema/api.schema.json) contains reusable `$defs` for the documented JSON request and response
bodies, shared data objects, and the common API error envelope. Reference a definition such
as `api.schema.json#/$defs/CreateOrgPageRequest` from a JSON Schema draft 2020-12-compatible validator. It references
the operations schema for `OperationsRequest`.

The [operations JSON Schema](schema/ops.schema.json) describes the JSON body for `POST /api/v1/o/{orgpage-id}/ops` and
the equivalent short-link endpoint. Its root describes an array of operations, and `$defs` contains the individual
operation schemas.

No JSON schema exists for successful no-body routes, uploads (`multipart/form-data`), or binary successful responses
(downloads, screenshots, and prints). Their JSON error and dry-run bodies are still described by the API types.

## Malli Schemas

The [public Malli schemas](schema/schema.cljc) describe EDN request and response bodies. Request entry points
include `create-orgpage`, `op`, `ops`, `file-rename`, `image-rename`, `permission-op`, `screenshot-orgpage`,
and `print-orgpage`. The `op` schema describes a single operation; `ops` describes the array of operations in a request.
Response schemas include `orgpage`, `orgpage-metadata`, `orgpage-objects`, `api-error`, and named route responses such
as `read-orgpage-response` and `operations-response`.

`request-schemas` and `response-schemas` map route identifiers to their Malli schemas. No-body routes use `no-body`;
multipart upload requests and binary successful responses are deliberately not represented as EDN values.
See [Input and output formats](formats.md) for the differences between EDN and JSON.

## Scope and Limitations

The schemas and types specify the shape of sent and received data. There are further checks and validations done by
OrgPad server. For example, schemas and types do not establish that IDs exist, that the caller has permission, or that
relationships and content are valid. See [Operations](ops.md), [Unit content in operations](ops_content.md),
and [Errors](errors.md) for those checks. TypeScript types do not validate values at runtime or enforce string patterns
and numeric ranges.

The TypeScript types distinguish text content from Hiccup and exclude simultaneous replacement and append. The
operations JSON Schema remains broader for content because the Malli schema uses `:any` for those fields. The Hiccup
types describe its general structure, not every supported tag or attribute.

Returned operations may be expanded by the server and are more restrictive in typing, so their type is separate from the
request operation type. Partial-read response types and other incompletely specified shapes are deliberately
conservative.

## Related Pages

| Page                                         | When to use it                                          |
|----------------------------------------------|---------------------------------------------------------|
| [Input and output formats](formats.md)       | Choose JSON, EDN, Transit, and binary response formats. |
| [Operations](ops.md)                         | Send changes to an OrgPage.                             |
| [Unit content in operations](ops_content.md) | Send HTML, Markdown, Hiccup, and content helper tags.   |
| [OrgPage data](orgpage.md)                   | Understand the objects returned by read endpoints.      |
| [Errors](errors.md)                          | Handle API errors and their codes.                      |
| [Authentication and API keys](auth.md)       | Authenticate API requests and choose key permissions.   |
