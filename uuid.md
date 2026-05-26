# OrgPad UUID Formats

OrgPad uses UUIDv4 values to identify OrgPages, units, links, files, images, maths, embeds, paths, fragments, and other
objects.

The API accepts standard UUID strings and OrgPad compact UUID strings. These two examples represent the same UUID:

```text
0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd
AHE9K_jnBB4ouByMo-9KjN
```

Use standard UUID strings in most integrations. They are also returned in API responses, while compact UUIDs are mainly
used in OrgPad URL path segments.

## Contents

- [Accepted ID Formats](#accepted-id-formats)
- [Where Compact IDs Appear](#where-compact-ids-appear)
- [Compact UUID Encoding](#compact-uuid-encoding)
    - [JavaScript](#javascript)
    - [Python](#python)
- [Related Pages](#related-pages)

## Accepted ID Formats

Public API routes and supported ID fields accept two UUID string formats:

- Standard UUID hex format, 36 characters with hyphens:
  `0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd`
- OrgPad compact UUID format, 22 URL-safe base64 characters:
  `AHE9K_jnBB4ouByMo-9KjN`

The compact form uses this URL-safe base64 alphabet:

```text
ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_
```

It does not use `+`, `/`, or `=` padding, so it can be used directly in URL path segments.

## Where Compact IDs Appear

OrgPad URLs use the compact UUID format. You may see compact IDs in image URLs, file links, embedded OrgPage URLs, and
other OrgPad-generated links.

For example, a file link inside unit content may contain a compact file ID:

```html
<a href="/file/A1wzY1upVBw5SBwTeHWt8k">Example.pdf</a>
```

Most API response fields do not use compact IDs. They return standard UUID strings in JSON, or native UUID values in EDN
and Transit.

## Compact UUID Encoding

The compact form is a URL-safe base64 encoding of the 16 UUID bytes with two leading zero bytes used for alignment.

To encode:

1. Parse the UUID into 16 bytes.
2. Prepend two zero bytes.
3. Base64url-encode the resulting 18 bytes.
4. Remove the first two encoded characters.

To decode:

1. Prepend `AA` to the 22-character compact ID.
2. Base64url-decode the resulting 24-character string.
3. Drop the first two decoded bytes.
4. Interpret the remaining 16 bytes as a UUID.

### JavaScript

This example uses Node.js `Buffer`.

```javascript
function uuidToBase64(uuid) {
  const hex = uuid.replace(/-/g, "");
  const uuidBytes = hex.match(/.{2}/g).map((byte) => parseInt(byte, 16));
  const bytes = Uint8Array.from([0, 0, ...uuidBytes]);
  return Buffer.from(bytes)
    .toString("base64")
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .slice(2);
}

function base64ToUuid(id) {
  const base64 = ("AA" + id).replace(/-/g, "+").replace(/_/g, "/");
  const hex = Buffer.from(base64, "base64").subarray(2).toString("hex");
  return [
    hex.slice(0, 8),
    hex.slice(8, 12),
    hex.slice(12, 16),
    hex.slice(16, 20),
    hex.slice(20)
  ].join("-");
}

console.log(uuidToBase64("0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd"));
// AHE9K_jnBB4ouByMo-9KjN

console.log(base64ToUuid("AHE9K_jnBB4ouByMo-9KjN"));
// 0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd
```

In browsers, use `crypto.randomUUID()` to create standard UUIDv4 values and a base64url helper or small conversion
function following the same rule.

### Python

Python's standard `uuid` and `base64` modules are enough.

```python
import base64
import uuid

def uuid_to_base64(value: str) -> str:
    raw = b"\x00\x00" + uuid.UUID(value).bytes
    return base64.urlsafe_b64encode(raw).decode("ascii")[2:]

def base64_to_uuid(value: str) -> str:
    raw = base64.urlsafe_b64decode("AA" + value)[2:]
    return str(uuid.UUID(bytes=raw))

print(uuid_to_base64("0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd"))
# AHE9K_jnBB4ouByMo-9KjN

print(base64_to_uuid("AHE9K_jnBB4ouByMo-9KjN"))
# 0713d2bf-8e70-41e2-8b81-c8ca3ef4a8cd
```

## Related Pages

Use these pages when IDs connect to request formats, operations, response data, or errors.

| Page                                        | When to use it                                                          |
|---------------------------------------------|-------------------------------------------------------------------------|
| [Text IDs](formats.md#text-ids)             | Understand text IDs and when they can replace generated UUIDs.          |
| [Operations](ops.md#ids-and-text-ids)       | Use UUIDs, compact IDs, native UUIDs, and text IDs in operations.       |
| [Read endpoints](read.md)                   | See where IDs appear in OrgPage response data.                          |
| [OrgPage data](orgpage.md)                  | Understand IDs on units, links, files, images, embeds, and paths.       |
| [Input and output formats](formats.md)      | Understand JSON, EDN, Transit, naming rules, and headers.               |
| [Errors](errors.md#id-and-operation-errors) | Fix invalid IDs, unknown IDs, and short-link errors.                    |
