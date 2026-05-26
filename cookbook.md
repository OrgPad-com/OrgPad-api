# API Cookbook

The API cookbook shows small end-to-end examples for common OrgPad API tasks. The examples use JSON and `curl`.

Before running them, create an API key in [OrgPad API settings](https://orgpad.info/settings/api) and export it:

```bash
export ORGPAD_API_KEY="orgpad_…"
```

Most examples also use `jq` to read IDs from JSON responses.

You need an active subscription with API access. Creating, copying, and deleting OrgPages requires an admin API key.
Applying operations and uploading attachments requires edit permission for the target OrgPage.

## Contents

- [Create an OrgPage](#create-an-orgpage)
- [Add Cells and a Link](#add-cells-and-a-link)
- [Update Cell Content](#update-cell-content)
- [Upload Files and Images](#upload-files-and-images)
- [Use an Uploaded Image in a Cell](#use-an-uploaded-image-in-a-cell)
- [Attach an Uploaded File](#attach-an-uploaded-file)
- [Embed an Uploaded PDF](#embed-an-uploaded-pdf)
- [Use Markdown Content](#use-markdown-content)
- [Copy an OrgPage](#copy-an-orgpage)
- [Delete an OrgPage](#delete-an-orgpage)
- [Related Pages](#related-pages)

## Create an OrgPage

Create a new OrgPage and store its ID in `ORGPAGE_ID`:

```bash
ORGPAGE_ID=$(
  curl -s -X POST "https://orgpad.info/api/v1/o" \
    -H "Authorization: Bearer $ORGPAD_API_KEY" \
    -H "Content-Type: application/json" \
    --data '{
      "title": "Cookbook example",
      "description": "Created with the OrgPad API",
      "tags": ["api", "cookbook"],
      "color": "color/blue"
    }' \
  | jq -r '.id'
)

echo "$ORGPAGE_ID"
```

To create an OrgPage with default metadata, omit the request body:

```bash
curl -X POST "https://orgpad.info/api/v1/o" \
  -H "Authorization: Bearer $ORGPAD_API_KEY"
```

## Add Cells and a Link

Most OrgPage edits are made with the operations endpoint. This example creates two cells and connects them with a link.
The `textId` values are stored aliases. They can be used instead of generated UUIDs in this request and in later
requests.

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[
    ["unit/create", {
      "textId": "intro-cell",
      "pos": [0, 0],
      "title": "Introduction",
      "content": "<p>This cell was created through the API.</p>"
    }],
    ["unit/create", {
      "textId": "next-cell",
      "pos": [360, 0],
      "title": "Next step",
      "content": "<p>The API can create cells, links, and rich content.</p>"
    }],
    ["link/create", {
      "endpointIds": ["intro-cell", "next-cell"],
      "props": {
        "color": "color/orange",
        "arrowhead": "props/single"
      }
    }]
  ]'
```

EDN operation body:

```clojure
[[:unit/create {:unit/text-id "intro-cell"
                :unit/pos     [0 0]
                :unit/title   "Introduction"
                :unit/content [[:p "This cell was created through the API."]]}]
 [:unit/create {:unit/text-id "next-cell"
                :unit/pos     [360 0]
                :unit/title   "Next step"
                :unit/content [[:p "The API can create cells, links, and rich content."]]}]
 [:link/create {:link/endpoint-ids ["intro-cell" "next-cell"]
                :link/props        {:props/color     :color/orange
                                    :props/arrowhead :props/single}}]]
```

To preview the expanded operations without changing the OrgPage, add `?dry-run=true` to the URL.

## Update Cell Content

Use `unit/update` to update an existing cell. If the cell has only one page, you can use the book ID or text ID and the
API updates that page.

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[
    ["unit/update", {
      "id": "intro-cell",
      "title": "Updated introduction",
      "content": "<p>The content was replaced.</p><p>HTML is the default JSON content format.</p>"
    }]
  ]'
```

To append content instead of replacing it, use `appendedContent`:

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[
    ["unit/update", {
      "id": "intro-cell",
      "appendedContent": "<p>Appended paragraph.</p>"
    }]
  ]'
```

## Upload Files and Images

Upload one image and one file into the OrgPage:

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/upload" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -F 'files=@"diagram.png";type=image/png' \
  -F 'files=@"report.pdf";type=application/pdf' \
  -o upload.json
```

Extract the uploaded IDs and tokens:

```bash
IMAGE_ID=$(jq -r '.images[0].id' upload.json)
IMAGE_TOKEN=$(jq -r '.images[0].token' upload.json)
FILE_ID=$(jq -r '.files[0].id' upload.json)
FILE_TOKEN=$(jq -r '.files[0].token' upload.json)
```

The upload response separates recognized images from generic files:

```json
{
  "files": [
    {
      "id": "72b84fee-fa08-450c-81b7-76c873a64f92",
      "token": "d063bd8a-06b2-4bc5-b663-20f15e98c622",
      "filename": "report.pdf",
      "contentType": "application/pdf",
      "size": 198630,
      "lastModified": "2026-05-14T13:46:12.863945Z"
    }
  ],
  "images": [
    {
      "id": "f443bb93-800a-435e-af2d-35774daea301",
      "token": "0dedef67-a8b3-4fa0-bb77-6e2e50132a55",
      "filename": "diagram.png",
      "width": 289,
      "height": 175,
      "format": "png",
      "thumbnailFormat": "png",
      "size": 5867,
      "lastModified": "2026-05-14T13:46:12.867321Z",
      "thumbnailSizes": [
        {
          "width": 289,
          "height": 175,
          "size": 5867
        }
      ]
    }
  ],
  "errors": []
}
```

## Use an Uploaded Image in a Cell

After uploading an image, insert it into a new cell with an `img` tag. Since the image is already attached to this
OrgPage, the image token is optional.

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[
    ["unit/create", {
      "textId": "image-cell",
      "pos": [0, 260],
      "title": "Uploaded image",
      "content": "<p><img id='$IMAGE_ID' width=400></p>"
    }]
  ]'
```

If you reuse an image from another OrgPage, include the image token:

```html
<img id="f443bb93-800a-435e-af2d-35774daea301"
     token="0dedef67-a8b3-4fa0-bb77-6e2e50132a55"
     width="400">
```

EDN content equivalent:

```clojure
[[:p [:img {:image/id    #uuid "f443bb93-800a-435e-af2d-35774daea301"
            :image/token #uuid "0dedef67-a8b3-4fa0-bb77-6e2e50132a55"
            :width       400}]]]
```

## Attach an Uploaded File

The simplest API input is the custom `file` helper tag. The API replaces it with a normal hyperlink containing the
file-type icon and filename.

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[
    ["unit/create", {
      "textId": "file-cell",
      "pos": [360, 260],
      "title": "Uploaded file",
      "content": "<p><file id='$FILE_ID'></file></p>"
    }]
  ]'
```

If you reuse a file from another OrgPage, include the file token:

```html
<file id="72b84fee-fa08-450c-81b7-76c873a64f92"
      token="d063bd8a-06b2-4bc5-b663-20f15e98c622"></file>
```

EDN content equivalent:

```clojure
[[:p [:file {:file/id    #uuid "72b84fee-fa08-450c-81b7-76c873a64f92"
             :file/token #uuid "d063bd8a-06b2-4bc5-b663-20f15e98c622"}]]]
```

## Embed an Uploaded PDF

PDF, Word, Excel, and PowerPoint files can be embedded. Other uploaded files can still be linked.

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[
    ["unit/create", {
      "textId": "pdf-cell",
      "pos": [720, 260],
      "title": "Embedded PDF",
      "content": "<p><embed file-id='$FILE_ID' width=700 height=450></embed></p>"
    }]
  ]'
```

## Use Markdown Content

JSON content defaults to HTML. To send Markdown, set `contentType` to `markdown`:

```bash
curl -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[
    ["unit/create", {
      "textId": "markdown-cell",
      "pos": [0, 560],
      "title": "Markdown",
      "contentType": "markdown",
      "content": "## Heading\n\nThis paragraph contains **bold text** and a [link](https://orgpad.info)."
    }]
  ]'
```

Markdown is parsed and then normalized to OrgPad unit content.

## Copy an OrgPage

Copy an existing OrgPage into your account:

```bash
COPIED_ORGPAGE_ID=$(
  curl -s -X POST "https://orgpad.info/api/v1/o/$ORGPAGE_ID/copy" \
    -H "Authorization: Bearer $ORGPAD_API_KEY" \
  | jq -r '.id'
)

echo "$COPIED_ORGPAGE_ID"
```

Files and images are shared with the copied OrgPage. Units, links, maths, embeds, paths, path steps, and fragments get
new IDs.

## Delete an OrgPage

Deleting an OrgPage is permanent. Use an API key with admin permission:

```bash
curl -X DELETE "https://orgpad.info/api/v1/o/$ORGPAGE_ID" \
  -H "Authorization: Bearer $ORGPAD_API_KEY"
```

Successful deletion returns `204 No Content`.

## Related Pages

Use these pages when you need the full reference behind the cookbook examples.

| Page                                          | When to use it                                 |
|-----------------------------------------------|------------------------------------------------|
| [Managing OrgPages](management.md)            | Create, delete, copy, and share OrgPages.      |
| [Operations](ops.md)                          | See all available operations and their fields. |
| [Unit content in operations](ops_content.md)  | Send content formats and helper tags.          |
| [Attachments](attachments.md)                 | Upload, download, and rename files and images. |
| [Authentication and API keys](auth.md)        | Check permissions needed for the examples.     |
| [Input and output formats](formats.md)        | Adapt examples between JSON, EDN, and Transit. |
| [Errors](errors.md#how-to-debug-an-api-error) | Diagnose failed cookbook requests.             |
