# Attachments

Attachments are uploaded files and images stored on an OrgPage. Use these endpoints to upload attachments, download
them with attachment URLs, reuse existing attachments in another OrgPage, and rename them.

## Contents

- [Attachment Metadata](#attachment-metadata)
- [Download Files and Images](#download-files-and-images)
- [Upload Files and Images](#upload-files-and-images)
- [Sharing Attachments Between OrgPages](#sharing-attachments-between-orgpages)
- [Rename Files](#rename-files)
- [Rename Images](#rename-images)
- [Related Pages](#related-pages)

## Attachment Metadata

These routes return attachment metadata together with IDs of all accessible OrgPages that contain the attachment.
They require view permission on at least one containing OrgPage. OrgPage-specific API keys only return their target
OrgPage when it contains the attachment.

```http
GET https://orgpad.info/api/v1/file/{file-id}
GET https://orgpad.info/api/v1/img/{image-id}
```

File metadata in JSON format:

```json
{
  "id": "72b84fee-fa08-450c-81b7-76c873a64f92",
  "token": "d063bd8a-06b2-4bc5-b663-20f15e98c622",
  "filename": "Document.pdf",
  "contentType": "application/pdf",
  "size": 142786,
  "lastModified": "2026-05-14T13:46:12.863945Z",
  "orgpageIds": [
    "136a1f33-0113-41ac-aff6-f50542a3228a",
    "4a91e1cc-af31-4b65-a192-895aa19279da"
  ]
}
```

Image metadata in JSON format:

```json
{
  "id": "98585725-07f6-4830-9f65-5110aa75e601",
  "token": "6fe205e7-3686-4331-9f9c-85f84b43d12b",
  "filename": "Laptop image.jpg",
  "width": 1280,
  "height": 853,
  "format": "jpg",
  "thumbnailFormat": "jpg",
  "size": 208764,
  "lastModified": "2026-05-14T13:46:12.863945Z",
  "url": "https://dr282zn36sxxg.cloudfront.net/…",
  "thumbnailSizes": [
    {
      "width": 1280,
      "height": 853,
      "size": 208764
    },
    {
      "width": 500,
      "height": 333,
      "size": 46379
    }
  ],
  "orgpageIds": [
    "136a1f33-0113-41ac-aff6-f50542a3228a"
  ]
}
```

In EDN, the same fields use namespaced keys:

```clojure
{:file/id            #uuid "72b84fee-fa08-450c-81b7-76c873a64f92"
 :file/token         #uuid "d063bd8a-06b2-4bc5-b663-20f15e98c622"
 :file/filename      "Document.pdf"
 :file/content-type  "application/pdf"
 :file/size          142786
 :file/last-modified "2026-05-14T13:46:12.863945Z"
 :file/orgpage-ids   #{#uuid "136a1f33-0113-41ac-aff6-f50542a3228a"
                       #uuid "4a91e1cc-af31-4b65-a192-895aa19279da"}}
```

Image metadata in EDN format:

```clojure
{:image/id               #uuid "98585725-07f6-4830-9f65-5110aa75e601"
 :image/token            #uuid "6fe205e7-3686-4331-9f9c-85f84b43d12b"
 :image/filename         "Laptop image.jpg"
 :image/width            1280
 :image/height           853
 :image/format           "jpg"
 :image/thumbnail-format "jpg"
 :image/size             208764
 :image/last-modified    "2026-05-14T13:46:12.863945Z"
 :image/url              "https://dr282zn36sxxg.cloudfront.net/…"
 :image/thumbnail-sizes  [{:thumbnail/width  1280
                           :thumbnail/height 853
                           :thumbnail/size   208764}
                          {:thumbnail/width  500
                           :thumbnail/height 333
                           :thumbnail/size   46379}]
 :image/orgpage-ids      #{#uuid "136a1f33-0113-41ac-aff6-f50542a3228a"}}
```

Attachment timestamp fields, such as `lastModified` in JSON and `:file/last-modified` or `:image/last-modified` in EDN,
are returned as UTC ISO datetime strings.

If the attachment does not exist or is not contained in any OrgPage accessible to the API key, the API returns
`file-not-found` or `image-not-found`. See
[Image, file, video, and audio errors](errors.md#image-file-video-and-audio-errors).

## Download Files and Images

Files and images are downloaded with their regular OrgPad attachment URLs, not `/api/v1` routes. Use the attachment ID
and token returned by the API.

```http
GET https://orgpad.info/file/{file-id}?token={file-token}
GET https://orgpad.info/img/{image-id}/download?token={image-token}
```
Treat file and image tokens as access credentials. Anyone with the URL and token has access to the attachment.

Example file download:

```bash
curl -L "https://orgpad.info/file/$FILE_ID?token=$FILE_TOKEN" \
  -o "Document.pdf"
```

Example image download:

```bash
curl -L "https://orgpad.info/img/$IMAGE_ID/download?token=$IMAGE_TOKEN" \
  -o "Laptop image.jpg"
```

OrgPad-generated content often uses compact base64 IDs in these URLs, for example `/file/A1wzY1upVBw5SBwTeHWt8k`.
The standard UUIDs returned by the API also work in these URLs. See [OrgPad UUID formats](uuid.md).

## Upload Files and Images

The upload route stores one or more attachments in the target OrgPage. Send multipart form data with one or more
`files` parts. The API key and user account must have edit permission for the OrgPage.

```http
POST https://orgpad.info/api/v1/o/{orgpage-id}/upload
POST https://orgpad.info/api/v1/s/{short-link}/upload
```

OrgPad separates recognized images from generic files in the response:

- `files` contains non-image attachments such as PDFs, documents, audio, or video.
- `images` contains successfully processed images.
- `errors` contains per-file upload errors. The API returns `200` when accepted files were stored, even if some upload
  parts failed and appear here.

Image detection uses both the content type and the file format. Recognized image formats are PNG, JPEG, GIF, SVG,
TIFF, and WebP. A recognized image fails processing when the file is invalid, corrupted, unsupported by the server, or
cannot be resized.

The third part in the example intentionally declares a video as an image to show how per-file upload errors are
returned. Generated IDs, tokens, timestamps, dimensions, and sizes vary between requests.

Example request:

```bash
curl "https://orgpad.info/api/v1/o/$ORGPAGE_ID/upload" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -F "files=@document.pdf;filename=Document.pdf;type=application/pdf" \
  -F "files=@laptop.jpg;filename=Laptop image.jpg;type=image/jpeg" \
  -F "files=@video.mp4;filename=not-an-image.jpg;type=image/jpeg"
```

JSON example response:

```json
{
  "files": [
    {
      "id": "72b84fee-fa08-450c-81b7-76c873a64f92",
      "token": "d063bd8a-06b2-4bc5-b663-20f15e98c622",
      "filename": "Document.pdf",
      "contentType": "application/pdf",
      "size": 142786,
      "lastModified": "2026-05-14T13:46:12.863945Z"
    }
  ],
  "images": [
    {
      "id": "f443bb93-800a-435e-af2d-35774daea301",
      "token": "0dedef67-a8b3-4fa0-bb77-6e2e50132a55",
      "filename": "Laptop image.jpg",
      "width": 1050,
      "height": 700,
      "format": "jpg",
      "thumbnailFormat": "jpg",
      "size": 102117,
      "lastModified": "2026-05-14T13:46:12.867321Z",
      "thumbnailSizes": [
        {
          "width": 1050,
          "height": 700,
          "size": 102117
        },
        {
          "width": 1000,
          "height": 667,
          "size": 39593
        },
        {
          "width": 500,
          "height": 334,
          "size": 14964
        },
        {
          "width": 250,
          "height": 167,
          "size": 6059
        },
        {
          "width": 125,
          "height": 84,
          "size": 2723
        }
      ]
    }
  ],
  "errors": [
    {
      "code": "invalid-image",
      "message": "The uploaded file is not a valid image.",
      "docs": "https://orgpad.info/s/api",
      "filename": "not-an-image.jpg",
      "contentType": "image/jpeg"
    }
  ]
}
```

EDN example response:

```clojure
{:files  [{:file/id            #uuid "72b84fee-fa08-450c-81b7-76c873a64f92"
           :file/token         #uuid "d063bd8a-06b2-4bc5-b663-20f15e98c622"
           :file/filename      "Document.pdf"
           :file/content-type  "application/pdf"
           :file/size          142786
           :file/last-modified "2026-05-14T13:46:12.863945Z"}]
 :images [{:image/id               #uuid "f443bb93-800a-435e-af2d-35774daea301"
           :image/token            #uuid "0dedef67-a8b3-4fa0-bb77-6e2e50132a55"
           :image/filename         "Laptop image.jpg"
           :image/width            1050
           :image/height           700
           :image/format           "jpg"
           :image/thumbnail-format "jpg"
           :image/size             102117
           :image/last-modified    "2026-05-14T13:46:12.867321Z"
           :image/thumbnail-sizes  [{:thumbnail/width  1050
                                     :thumbnail/height 700
                                     :thumbnail/size   102117}
                                    {:thumbnail/width  1000
                                     :thumbnail/height 667
                                     :thumbnail/size   39593}
                                    {:thumbnail/width  500
                                     :thumbnail/height 334
                                     :thumbnail/size   14964}
                                    {:thumbnail/width  250
                                     :thumbnail/height 167
                                     :thumbnail/size   6059}
                                    {:thumbnail/width  125
                                     :thumbnail/height 84
                                     :thumbnail/size   2723}]}]
 :errors [{:code         :invalid-image
           :message      "The uploaded file is not a valid image."
           :docs         "https://orgpad.info/s/api"
           :filename     "not-an-image.jpg"
           :content-type "image/jpeg"}]}
```

[Errors](errors.md#image-file-video-and-audio-errors):

- `storage-quota-exceeded` when the upload exceeds available storage.
- `invalid-image` when an uploaded image is invalid, corrupted, unsupported by the server, or cannot be resized. The
  upload still returns a successful response object. Accepted files appear in `files` or `images`, and failed image
  parts appear as error objects in the `errors` array.

## Sharing Attachments Between OrgPages

Files and images can belong to multiple OrgPages. To attach an existing attachment to another OrgPage, use the
[operations endpoint](ops.md) with `orgpage/add-file` or `orgpage/add-image`.

The API key must have edit permission for the target OrgPage. It must also have access to the existing attachment,
either through an OrgPage where the attachment is already present or by using the attachment token in the operation
body.

Example request:

```bash
curl "https://orgpad.info/api/v1/o/$SECOND_ORGPAGE_ID/ops" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data '[["orgpage/add-file", {"id": "'"$FILE_ID"'"}],
           ["orgpage/add-image", {"id": "'"$IMAGE_ID"'"}]]'
```

After the operation succeeds, the same file or image is attached to both OrgPages. For the full operation schema, see
[Operations](ops.md).

## Rename Files

This route renames a file in every OrgPage where the file is present and editable by both the user account and API key,
or only in selected OrgPages.

```http
POST https://orgpad.info/api/v1/file/{file-id}/rename
```

JSON example body:

```json
{
  "filename": "New filename.pdf",
  "orgpageIds": [
    "c56f99df-cb7d-4ef9-81ef-d664b5333284"
  ]
}
```

EDN example body:

```clojure
{:file/filename    "New filename.pdf"
 :file/orgpage-ids #{#uuid "c56f99df-cb7d-4ef9-81ef-d664b5333284"}}
```

If `orgpageIds` is omitted, the API renames the file in every OrgPage where the file is present and editable by both
the user account and API key. If the API key is restricted to one OrgPage, the file is renamed only in that OrgPage.

If all OrgPages using the file are included, the file keeps its ID and only the file map and visible labels are updated.
If only some OrgPages are included, OrgPad creates a replacement file with a new ID for those OrgPages and updates their
content references to use the replacement.

The rename updates:

- The file map.
- Labels of file hyperlinks in unit content.
- File embeds.
- Video/audio `:source` children.

Use `?dry-run=true` to compute the same response without applying changes. Dry-run responses include `"dryRun": true`.

Example dry-run request:

```bash
curl "https://orgpad.info/api/v1/file/$FILE_ID/rename?dry-run=true" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data "{\"filename\":\"New filename.pdf\",\"orgpageIds\":[\"$SECOND_ORGPAGE_ID\"]}"
```

JSON example dry-run response:

```json
{
  "file": {
    "id": "6407a2b3-0a67-439a-a2b5-d4aec8a68220",
    "token": "ee73ae20-4e69-4bc5-b162-7a30d3b1dc48",
    "filename": "New filename.pdf",
    "contentType": "application/pdf",
    "size": 142786,
    "lastModified": "2026-05-14T13:46:12.863945Z"
  },
  "oldFileId": "72b84fee-fa08-450c-81b7-76c873a64f92",
  "fileOps": [
    [
      "orgpage/remove-file",
      {
        "id": "72b84fee-fa08-450c-81b7-76c873a64f92"
      }
    ],
    [
      "orgpage/add-file",
      {
        "id": "6407a2b3-0a67-439a-a2b5-d4aec8a68220",
        "token": "ee73ae20-4e69-4bc5-b162-7a30d3b1dc48",
        "filename": "New filename.pdf",
        "contentType": "application/pdf",
        "size": 142786,
        "lastModified": "2026-05-14T13:46:12.863945Z"
      }
    ]
  ],
  "orgpageOps": {
    "c56f99df-cb7d-4ef9-81ef-d664b5333284": [
      [
        "unit/update",
        {
          "id": "461b7236-c779-4f8e-910f-7973ab22bbf8",
          "content": "<p><a href=\"/file/BkB6KzCmdDmqK11K7IpoIg\"><img height=\"24\" src=\"/static/img/files/pdf.svg\" width=\"24\" />New filename.pdf</a></p>"
        }
      ],
      [
        "embed/update",
        {
          "id": "fb2d0cb8-61b8-4b57-a42b-99bea2f42f4d",
          "fileId": "6407a2b3-0a67-439a-a2b5-d4aec8a68220"
        }
      ]
    ]
  },
  "dryRun": true
}
```

EDN example dry-run response:

```clojure
{:file        {:file/id            #uuid "6407a2b3-0a67-439a-a2b5-d4aec8a68220"
               :file/token         #uuid "ee73ae20-4e69-4bc5-b162-7a30d3b1dc48"
               :file/filename      "New filename.pdf"
               :file/content-type  "application/pdf"
               :file/size          142786
               :file/last-modified "2026-05-14T13:46:12.863945Z"}
 :old-file-id #uuid "72b84fee-fa08-450c-81b7-76c873a64f92"
 :file-ops    [[:orgpage/remove-file {:file/id #uuid "72b84fee-fa08-450c-81b7-76c873a64f92"}]
               [:orgpage/add-file {:file/id            #uuid "6407a2b3-0a67-439a-a2b5-d4aec8a68220"
                                   :file/token         #uuid "ee73ae20-4e69-4bc5-b162-7a30d3b1dc48"
                                   :file/filename      "New filename.pdf"
                                   :file/content-type  "application/pdf"
                                   :file/size          142786
                                   :file/last-modified "2026-05-14T13:46:12.863945Z"}]]
 :orgpage-ops {#uuid "c56f99df-cb7d-4ef9-81ef-d664b5333284"
               [[:unit/update {:unit/id      #uuid "461b7236-c779-4f8e-910f-7973ab22bbf8"
                               :unit/content [[:p
                                               [:a {:href "/file/BkB6KzCmdDmqK11K7IpoIg"}
                                                [:img {:src    "/static/img/files/pdf.svg"
                                                       :width  24
                                                       :height 24}]
                                                "New filename.pdf"]]]}]
                [:embed/update {:embed/id      #uuid "fb2d0cb8-61b8-4b57-a42b-99bea2f42f4d"
                                :embed/file-id #uuid "6407a2b3-0a67-439a-a2b5-d4aec8a68220"}]]}
 :dry-run     true}
```

Request fields:

- `filename`: new filename.
- `orgpageIds`: optional IDs of OrgPages where the API renames the file. If omitted, the API renames the file in every
  OrgPage where the file is present and editable by both the user account and API key.

Response fields:

- `file`: resulting file object. If a replacement file was created, this is the replacement file.
- `oldFileId`: present only when a replacement file was created. It contains the original file ID.
- `fileOps`: attachment-level operations that were applied, or that dry-run mode lists without applying.
- `orgpageOps`: content operations grouped by OrgPage ID. These update file links, embeds, and video/audio sources.
- `dryRun`: present and `true` only when `?dry-run=true` is used.

[Errors](errors.md#image-file-video-and-audio-errors):

- `invalid-id` when an OrgPage ID cannot be parsed.
- `file-not-found` when the file is not found or no editable target OrgPages contain it.
- `file-missing-in-orgpages` when an OrgPage specified in `orgpageIds` is not editable or does not contain the file.

## Rename Images

This route renames an image in every OrgPage where the image is present and editable by both the user account and API
key, or only in selected OrgPages. It works like [Rename Files](#rename-files), but updates image maps and image
references in unit content.

```http
POST https://orgpad.info/api/v1/img/{image-id}/rename
```

JSON example body:

```json
{
  "filename": "New filename.jpg",
  "orgpageIds": [
    "c56f99df-cb7d-4ef9-81ef-d664b5333284"
  ]
}
```

EDN example body:

```clojure
{:image/filename    "New filename.jpg"
 :image/orgpage-ids #{#uuid "c56f99df-cb7d-4ef9-81ef-d664b5333284"}}
```

If `orgpageIds` is omitted, the API renames the image in every OrgPage where the image is present and editable by both
the user account and API key. If the API key is restricted to one OrgPage, the image is renamed only in that OrgPage.

If all OrgPages using the image are included, the image keeps its ID. If only some OrgPages are included, OrgPad creates
a replacement image with a new ID for those OrgPages and updates image references in unit content.

Use `?dry-run=true` to compute the same response without applying changes. Dry-run responses include `"dryRun": true`.

Example dry-run request:

```bash
curl "https://orgpad.info/api/v1/img/$IMAGE_ID/rename?dry-run=true" \
  -H "Authorization: Bearer $ORGPAD_API_KEY" \
  -H "Content-Type: application/json" \
  --data "{\"filename\":\"New filename.jpg\",\"orgpageIds\":[\"$SECOND_ORGPAGE_ID\"]}"
```

JSON example dry-run response:

```json
{
  "image": {
    "id": "04d8dd0b-d2a4-4022-827e-0aa1ad42645b",
    "token": "f555ba2e-ff27-422f-ba83-ff752cfdf79e",
    "filename": "New filename.jpg",
    "width": 1050,
    "height": 700,
    "format": "jpg",
    "thumbnailFormat": "jpg",
    "size": 102117,
    "lastModified": "2026-05-14T13:46:12.867321Z",
    "thumbnailSizes": [
      {
        "width": 1050,
        "height": 700,
        "size": 102117
      },
      {
        "width": 1000,
        "height": 667,
        "size": 39593
      },
      {
        "width": 500,
        "height": 334,
        "size": 14964
      },
      {
        "width": 250,
        "height": 167,
        "size": 6059
      },
      {
        "width": 125,
        "height": 84,
        "size": 2723
      }
    ]
  },
  "oldImageId": "f443bb93-800a-435e-af2d-35774daea301",
  "imageOps": [
    [
      "orgpage/remove-image",
      {
        "id": "f443bb93-800a-435e-af2d-35774daea301"
      }
    ],
    [
      "orgpage/add-image",
      {
        "id": "04d8dd0b-d2a4-4022-827e-0aa1ad42645b",
        "token": "f555ba2e-ff27-422f-ba83-ff752cfdf79e",
        "filename": "New filename.jpg",
        "width": 1050,
        "height": 700,
        "format": "jpg",
        "thumbnailFormat": "jpg",
        "size": 102117,
        "lastModified": "2026-05-14T13:46:12.867321Z",
        "thumbnailSizes": [
          {
            "width": 1050,
            "height": 700,
            "size": 102117
          },
          {
            "width": 1000,
            "height": 667,
            "size": 39593
          },
          {
            "width": 500,
            "height": 334,
            "size": 14964
          },
          {
            "width": 250,
            "height": 167,
            "size": 6059
          },
          {
            "width": 125,
            "height": 84,
            "size": 2723
          }
        ]
      }
    ]
  ],
  "orgpageOps": {
    "c56f99df-cb7d-4ef9-81ef-d664b5333284": [
      [
        "unit/update",
        {
          "id": "ddbb4471-8c29-409b-bfc4-9fdaab1ecf70",
          "content": "<p><img height=\"500\" src=\"/img/AE2N0L0qRAIoJ-CqGtQmRb\" width=\"750\" /></p>"
        }
      ]
    ]
  },
  "dryRun": true
}
```

EDN example dry-run response:

```clojure
{:image        {:image/id               #uuid "04d8dd0b-d2a4-4022-827e-0aa1ad42645b"
                :image/token            #uuid "f555ba2e-ff27-422f-ba83-ff752cfdf79e"
                :image/filename         "New filename.jpg"
                :image/width            1050
                :image/height           700
                :image/format           "jpg"
                :image/thumbnail-format "jpg"
                :image/size             102117
                :image/last-modified    "2026-05-14T13:46:12.867321Z"
                :image/thumbnail-sizes  [{:thumbnail/width  1050
                                          :thumbnail/height 700
                                          :thumbnail/size   102117}
                                         {:thumbnail/width  1000
                                          :thumbnail/height 667
                                          :thumbnail/size   39593}
                                         {:thumbnail/width  500
                                          :thumbnail/height 334
                                          :thumbnail/size   14964}
                                         {:thumbnail/width  250
                                          :thumbnail/height 167
                                          :thumbnail/size   6059}
                                         {:thumbnail/width  125
                                          :thumbnail/height 84
                                          :thumbnail/size   2723}]}
 :old-image-id #uuid "f443bb93-800a-435e-af2d-35774daea301"
 :image-ops    [[:orgpage/remove-image {:image/id #uuid "f443bb93-800a-435e-af2d-35774daea301"}]
                [:orgpage/add-image {:image/id               #uuid "04d8dd0b-d2a4-4022-827e-0aa1ad42645b"
                                     :image/token            #uuid "f555ba2e-ff27-422f-ba83-ff752cfdf79e"
                                     :image/filename         "New filename.jpg"
                                     :image/width            1050
                                     :image/height           700
                                     :image/format           "jpg"
                                     :image/thumbnail-format "jpg"
                                     :image/size             102117
                                     :image/last-modified    "2026-05-14T13:46:12.867321Z"
                                     :image/thumbnail-sizes  [{:thumbnail/width  1050
                                                               :thumbnail/height 700
                                                               :thumbnail/size   102117}
                                                              {:thumbnail/width  1000
                                                               :thumbnail/height 667
                                                               :thumbnail/size   39593}
                                                              {:thumbnail/width  500
                                                               :thumbnail/height 334
                                                               :thumbnail/size   14964}
                                                              {:thumbnail/width  250
                                                               :thumbnail/height 167
                                                               :thumbnail/size   6059}
                                                              {:thumbnail/width  125
                                                               :thumbnail/height 84
                                                               :thumbnail/size   2723}]}]]
 :orgpage-ops  {#uuid "c56f99df-cb7d-4ef9-81ef-d664b5333284"
                [[:unit/update {:unit/id      #uuid "ddbb4471-8c29-409b-bfc4-9fdaab1ecf70"
                                :unit/content [[:p
                                                [:img {:src    "/img/AE2N0L0qRAIoJ-CqGtQmRb"
                                                       :width  750
                                                       :height 500}]]]}]]}
 :dry-run      true}
```

Request fields:

- `filename`: new filename.
- `orgpageIds`: optional IDs of OrgPages where the API renames the image. If omitted, the API renames the image in
  every OrgPage where the image is present and editable by both the user account and API key.

Response fields:

- `image`: resulting image object. If a replacement image was created, this is the replacement image.
- `oldImageId`: present only when a replacement image was created. It contains the original image ID.
- `imageOps`: attachment-level operations that were applied, or that dry-run mode lists without applying.
- `orgpageOps`: content operations grouped by OrgPage ID.
- `dryRun`: present and `true` only when `?dry-run=true` is used.

[Errors](errors.md#image-file-video-and-audio-errors):

- `invalid-id` when an OrgPage ID cannot be parsed.
- `image-not-found` when the image is not found or no editable target OrgPages contain it.
- `image-missing-in-orgpages` when an OrgPage specified in `orgpageIds` is not editable or does not contain the image.

## Related Pages

Use these pages when attachments connect to operations, content, errors, or response data.

| Page                                                  | When to use it                                                 |
|-------------------------------------------------------|----------------------------------------------------------------|
| [Operations](ops.md)                                  | Attach files and images or remove them from OrgPages.          |
| [Unit content in operations](ops_content.md)          | Use image, file, video, audio, and embed helper tags.          |
| [OrgPage data](orgpage.md)                            | Understand file and image object fields.                       |
| [API cookbook](cookbook.md)                           | See upload and attachment examples with `curl`.                |
| [Errors](errors.md#image-file-video-and-audio-errors) | Resolve upload, quota, missing-file, and missing-image errors. |
