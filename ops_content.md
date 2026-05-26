# Unit Content in Operations

Unit content operations send content through `unit/create` and `unit/update`. Read this page when you need to choose a
`contentType`, append to existing content, use API helper tags, understand generated operations, or debug
content-specific errors.

For the normalized content stored in units and returned by read endpoints, see [Unit content](content.md). That page
documents supported stored tags, attributes, CSS styles, and stored HTML/Hiccup examples. The sections below cover
accepted API input and how OrgPad converts that input before storage.

During operation processing, OrgPad parses the selected input format, normalizes content, replaces API-only helper tags,
and generates derived image, file, math, or embed operations. The operations endpoint returns the expanded operation
list. To inspect that list without changing the OrgPage, send the same request with `?dry-run=true`.

## Contents

- [API Input Guidance](#api-input-guidance)
- [Processing and Normalization](#processing-and-normalization)
- [Content Formats](#content-formats)
    - [HTML](#html)
    - [Hiccup](#hiccup)
    - [Plain text](#plain-text)
    - [Markdown](#markdown)
- [Appended Content](#appended-content)
- [API Helpers for Content](#api-helpers-for-content)
    - [Text highlighting helpers](#text-highlighting-helpers)
    - [Image helpers](#image-helpers)
    - [File helpers](#file-helpers)
    - [Embed helpers](#embed-helpers)
    - [OrgPage embed helpers](#orgpage-embed-helpers)
    - [Video and audio helpers](#video-and-audio-helpers)
    - [YouTube helpers](#youtube-helpers)
    - [Math and chemistry helpers](#math-and-chemistry-helpers)
    - [Code helpers](#code-helpers)
- [Generated Operations and Dry Run](#generated-operations-and-dry-run)
- [Errors and Troubleshooting](#errors-and-troubleshooting)
- [Related Pages](#related-pages)

## API Input Guidance

When sending content through the API, prefer simple semantic HTML or Hiccup that matches the structures described in
[Unit content](content.md). Use paragraphs, lists, tables, links, uploaded images and files, embeds, math objects, and
code blocks as the primary building blocks. Avoid treating unit content as a custom HTML layout surface.

As a useful mental model, think of OrgPad unit content as basic Markdown-like document content with a few additional
OrgPad objects embedded into it: uploaded images and files, videos, audio, external embeds, YouTube videos, embedded
OrgPages, math, and chemistry.

OrgPad normalizes submitted content before storing it. Most pasted HTML formatting is discarded and only essential
structure is kept, such as bold text, lists, tables, images, and hyperlinks. Some styles are converted instead of
preserved exactly. For example, `background` is converted to `background-color`, non-white background colors becomes
OrgPad `mark` highlights or is removed, tables receive OrgPad table defaults, and table cells receive default padding.
Styles and attributes that are common in pasted HTML but not meaningful in OrgPad, such as CSS classes, font families,
font sizes, arbitrary margins, overflow rules, and text colors, are removed.

API input is currently less restrictive than stored OrgPad content: allowed attributes and styles are not validated by
value. Future OrgPad versions may restrict these values more and replace some HTML patterns with custom tags. OrgPad
will migrate existing data when these changes are introduced.

## Processing and Normalization

OrgPad processes submitted content before it is stored. The result is normalized Hiccup internally and HTML in JSON
responses. Processing uses these rules:

- `b` is converted to `strong`, and `i` is converted to `em`.
- `h1`, `h2`, `h3`, `h5`, and `h6` are converted to `h4`.
- Nested paragraphs are flattened.
- Top-level inline content is wrapped in a paragraph.
- `div` blocks are converted to paragraphs. They may appear in imported or submitted HTML, but they are not kept as
  stored unit content.
- Empty wrapper elements are removed when possible.
- Newlines in normal text are converted to spaces, while newlines inside code become line breaks.
- Hyperlinks and sources are normalized to HTTPS where possible.
- Helper tags such as `file`, `chem`, and `iframe` are expanded to stored tags and related operations.
- Everything except [supported stored tags](content.md#supported-tags),
  [supported attributes](content.md#supported-attributes), and
  [supported CSS styles](content.md#supported-css-styles) is cleaned.

API helpers are accepted input syntax. Stored content contains only the normalized tags documented in
[Unit content](content.md).

## Content Formats

Unit content can be sent in four formats and the format is specified in `contentType`. HTML and Hiccup can express all
[supported stored unit content](content.md#list-of-supported-content) and all API helper tags. Plain text and Markdown
are convenient for simple text, but they cannot directly express OrgPad helper tags or exact stored attributes.

### HTML

Set `contentType` to `html` in JSON and `:html` in EDN. In JSON requests, when `content` or `appendedContent` is
present and `contentType` is omitted, the API defaults to HTML. HTML content must be a string; otherwise the API returns
the `string-html-content` [error](errors.md#content-format-errors). JSON responses always return unit `content`
as HTML.

Example input:

```html
<p>
  This is a single paragraph with <b>some bold text</b>.
</p>
<p>
  This is another paragraph.
</p>
```

### Hiccup

[Hiccup](https://github.com/weavejester/hiccup) is the standard Clojure data representation of HTML. Set `contentType`
to `hiccup` in JSON and `:hiccup` in EDN. In EDN requests, when `:unit/content` or `:unit/appended-content` is present
and `:unit/content-type` is omitted, the API defaults to Hiccup. Hiccup content must be a vector of Hiccup nodes.

For example, the HTML above looks like this in Hiccup:

```clojure
[[:p "This is a single paragraph with "
  [:b "some bold text"] "."]
 [:p "This is another paragraph."]]
```

The `style` attribute can be represented as a string or as a nested map:

```clojure
[[:p {:style "background-color: red"} "Red background."]
 [:p {:style {:background-color "blue"}} "Blue background."]]
```

Invalid Hiccup returns the `hiccup-content-schema-error` [error](errors.md#content-format-errors).

### Plain text

Set `contentType` to `plain` in JSON and `:plain` in EDN. Use plain text when you want unformatted paragraphs.
As in Markdown, paragraphs are separated by one or more empty lines. The content must be a string; otherwise the API
returns the `string-plain-content` [error](errors.md#content-format-errors).

```text
This is a single paragraph without any formatting.

This is another paragraph.
```

### Markdown

Set `contentType` to `markdown` in JSON and `:markdown` in EDN. OrgPad uses
[markdown-clj](https://github.com/yogthos/markdown-clj) for parsing Markdown and supports basic Markdown formatting.
Markdown is good for basic headings, paragraphs, emphasis, lists, links, and code fences. At the moment, math and OrgPad
helper tags cannot be inserted directly in Markdown. The content must be a string; otherwise the API returns the
`string-markdown-content` [error](errors.md#content-format-errors).

```markdown
## Heading

This is a single paragraph with **some bold text**.

This is another paragraph.
```

Markdown is parsed first and then normalized like any other unit content. For example, the heading is normalized to
`h4`:

```html
<h4>Heading</h4>
<p>
  This is a single paragraph with <strong>some bold text</strong>.
</p>
<p>
  This is another paragraph.
</p>
```

## Appended Content

Use `appendedContent` in [`unit/update`](ops.md#updating-units) to append to an existing page without resending the whole
content. API expands this operation into `unit/update` where existing `content` is merged with added `appendedContent`.
Inspect that expanded operation with `?dry-run=true` when you need to confirm exactly where the appended nodes were
inserted.

A request must not contain both `content` and `appendedContent`. Doing so returns the `content-and-appended-content`
[error](errors.md#content-format-errors).

Append behavior:

- If appended content starts with `li` tags and existing content ends with `ul` or `ol`, list items are inserted into
  that list. The rest of the appended content is added after the list.
- If appended content starts with inline/non-paragraph nodes and existing content ends with `p`, nodes are inserted
  into the last paragraph. The rest is appended after that paragraph.
- Otherwise, the appended content is added after the existing content.

## API Helpers for Content

You can send the final stored form documented in [Unit content](content.md#list-of-supported-content), or use the API
helper tags below. Helper tags are input-only shorthands. OrgPad expands them to stored content and, when needed,
generates related operations such as `math/create`, `embed/create`, `orgpage/add-file`, or `orgpage/add-image`.

When a helper references an existing file, image or OrgPage, the referenced object must already exist. Access must be
allowed by the API key, a supplied token, or a token parsed from the referenced URL.

### Text highlighting helpers

OrgPad does not support changing text color in unit content. The CSS `color` style is ignored. It supports text
highlighting by changing allows the background color to one of [spectrum OrgPad colors](orgpage.md#colors).

Direct text highlighting uses the stored `mark` [tag](content.md#text-highlighting). This helper does not reference any
external object and does not generate additional operations. In HTML, the `color` attribute can use either the short
color name or the `color/…` form:

```html
<mark color="orchid">Orchid text</mark>
<mark color="color/blueberry">Blueberry text</mark>
```

In Hiccup, `:color` accepts a color string and `:mark/color` accepts a namespaced color keyword:

```clojure
[:mark {:color "orange"} "Orange text"]
[:mark {:mark/color :color/pink} "Pink text"]
```

Mark colors must be [OrgPad spectrum colors](orgpage.md#colors). Invalid mark colors return the `invalid-mark-color`
[error](errors.md#content-format-errors).

The API also converts detected background styles into `mark` tags. It reads `background-color` and `background` styles,
parses the color, and replaces the styled element with a `mark` whose color is the nearest OrgPad unit background color
in light mode.

The background color parser accepts:

- HTML color names, such as `red`, `blue`, or `lightgreen`.
- Hex colors in `#RRGGBB` form, such as `#3e56c9`.
- RGB colors in `rgb(R,G,B)` form, such as `rgb(255, 0, 0)`.

Short hex colors such as `#f00`, `rgba(…)`, `hsl(…)`, `transparent`, other formats, and unparseable CSS expressions are
ignored. White backgrounds are ignored as well.

```html
<p style="background-color:red;">Red highlighting.</p>
<p style="background: #3e56c9;">Blue highlighting.</p>
```

is converted into:

```html
<p><mark color="orange">Red highlighting.</mark></p>
<p><mark color="teal">Blue highlighting.</mark></p>
```

### Image helpers

Use the `img` [tag](content.md#image) to place an image that is already uploaded to OrgPad. The API finds the image,
checks access, and when the image is not attached to the edited OrgPage, automatically generates an `orgpage/add-image`
operation. External image URLs are not allowed in unit content.

The image can be referenced by ID, by ID with token, or by an existing OrgPad image URL from which the ID and token are
parsed. In HTML, the same image can be inserted in multiple ways:

```html
<img id="c40700c0-7b5b-4cf6-8983-31b4483f531b"
     width="370" />
<img id="c40700c0-7b5b-4cf6-8983-31b4483f531b"
     token="8b52d311-9b92-4d27-9777-477787b9efe2" />
<img src="/img/DEBwDAe1tM9omDMbRIP1Mb" />
<img src="/img/DEBwDAe1tM9omDMbRIP1Mb/download?token=CLUtMRm5JNJ5d3R3eHue_i" />
<img src="https://orgpad.info/img/DEBwDAe1tM9omDMbRIP1Mb" />
```

In Hiccup, ID and token can be written either with unqualified keys or with namespaced `:image/…` keys:

```clojure
[:img {:id    #uuid "c40700c0-7b5b-4cf6-8983-31b4483f531b"
       :width 370}]
[:img {:id    #uuid "c40700c0-7b5b-4cf6-8983-31b4483f531b"
       :token #uuid "8b52d311-9b92-4d27-9777-477787b9efe2"}]
[:img {:image/id    #uuid "c40700c0-7b5b-4cf6-8983-31b4483f531b"
       :image/token #uuid "8b52d311-9b92-4d27-9777-477787b9efe2"}]
[:img {:src "/img/DEBwDAe1tM9omDMbRIP1Mb"}]
[:img {:src "/img/DEBwDAe1tM9omDMbRIP1Mb/download?token=CLUtMRm5JNJ5d3R3eHue_i"}]
[:img {:src "https://orgpad.info/img/DEBwDAe1tM9omDMbRIP1Mb"}]
```

The image must exist and be viewable by the API key. When referencing an image from another OrgPage, include its token,
either as `token` / `:image/token` or as a token query parameter in the OrgPad image URL.

A separate dark-mode image can be supplied for the same `img` tag. It is used when the OrgPage is viewed in dark mode.
The dark-mode image uses the same ID, token, and URL forms as the main image. In HTML:

```html
<img id="c40700c0-7b5b-4cf6-8983-31b4483f531b"
     dm-id="89a5e8eb-9d06-4515-834c-d41e00fc31de"
     width="370" height="370" />
<img id="c40700c0-7b5b-4cf6-8983-31b4483f531b"
     token="8b52d311-9b92-4d27-9777-477787b9efe2"
     dm-id="89a5e8eb-9d06-4515-834c-d41e00fc31de"
     dm-token="fb20d6e5-dbbd-4ce7-8d78-59bd9a7b8807" />
<img src="/img/DEBwDAe1tM9omDMbRIP1Mb"
     dm-src="/img/CJpejrnQZFFYNM1B4A_DHe" />
```

In Hiccup:

```clojure
[:img {:id    #uuid "c40700c0-7b5b-4cf6-8983-31b4483f531b"
       :dm-id #uuid "89a5e8eb-9d06-4515-834c-d41e00fc31de"
       :width 370
       :height 370}]
[:img {:id       #uuid "c40700c0-7b5b-4cf6-8983-31b4483f531b"
       :token    #uuid "8b52d311-9b92-4d27-9777-477787b9efe2"
       :dm-id    #uuid "89a5e8eb-9d06-4515-834c-d41e00fc31de"
       :dm-token #uuid "fb20d6e5-dbbd-4ce7-8d78-59bd9a7b8807"}]
[:img {:image/id       #uuid "c40700c0-7b5b-4cf6-8983-31b4483f531b"
       :image/token    #uuid "8b52d311-9b92-4d27-9777-477787b9efe2"
       :image/dm-id    #uuid "89a5e8eb-9d06-4515-834c-d41e00fc31de"
       :image/dm-token #uuid "fb20d6e5-dbbd-4ce7-8d78-59bd9a7b8807"}]
[:img {:src    "/img/DEBwDAe1tM9omDMbRIP1Mb"
       :dm-src "/img/CJpejrnQZFFYNM1B4A_DHe"}]
```

Dark-mode image attributes are optional, but they cannot be used alone. Every `img` tag must also specify the main image
using `id` or `src`. The dark-mode image must have the same pixel dimensions as the main image.

The `width` and `height` attributes may be supplied. They can be numbers or pixel values such as `370px`. If only one
dimension is supplied, OrgPad preserves the original image ratio and computes the other dimension. If both dimensions
are omitted, OrgPad uses the stored image size and scales it down to fit into `500 x 500` pixels.

Possible image [errors](errors.md#image-file-video-and-audio-errors):

- `invalid-image-source`: `src` is present, but it is not an OrgPad image URL.
- `invalid-image-dm-source`: `dm-src` is present, but it is not an OrgPad image URL.
- `invalid-image-id-or-token`: the tag does not contain a usable image ID or image URL.
- `image-not-found`: the image does not exist or cannot be accessed with the API key or supplied token.
- `dm-image-not-found`: the dark-mode image does not exist or cannot be accessed with the API key or supplied token.
- `dark-mode-image-only`: the tag specifies only a dark-mode image and no main image.
- `dark-mode-different-size`: the dark-mode image has different pixel dimensions than the main image.

### File helpers

Uploaded files are normally referenced in units as [attached files](content.md#attached-file). You can send that final
attached-file form directly in unit content.

Alternatively, use the custom `file` tag to insert a hyperlink to an uploaded OrgPad file. The API replaces the tag with
a normal hyperlink containing the file icon and the filename. The file does not have to be attached to the edited
OrgPage; if it is accessible and missing from the OrgPage, the API automatically generates an `orgpage/add-file`
operation.

The file can be referenced by ID, by ID with token, or by an existing OrgPad file URL from which the ID and token are
parsed. In HTML:

```html
<file id="35c33635-ba95-41c3-9481-c137875adf24"></file>
<file id="35c33635-ba95-41c3-9481-c137875adf24"
      token="2498ced3-60be-48b7-b86a-9b5b3a79316e"></file>
<file src="/file/A1wzY1upVBw5SBwTeHWt8k"></file>
<file src="/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"></file>
<file src="https://orgpad.info/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"></file>
```

In Hiccup, ID and token can be written either with unqualified keys or with namespaced `:file/…` keys:

```clojure
[:file {:id #uuid "35c33635-ba95-41c3-9481-c137875adf24"}]
[:file {:id    #uuid "35c33635-ba95-41c3-9481-c137875adf24"
        :token #uuid "2498ced3-60be-48b7-b86a-9b5b3a79316e"}]
[:file {:file/id    #uuid "35c33635-ba95-41c3-9481-c137875adf24"
        :file/token #uuid "2498ced3-60be-48b7-b86a-9b5b3a79316e"}]
[:file {:src "/file/A1wzY1upVBw5SBwTeHWt8k"}]
[:file {:src "/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"}]
[:file {:src "https://orgpad.info/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"}]
```

The file must already exist and be viewable by the API key. When referencing a file from another OrgPage, include its
token either as `token` / `:file/token` or as a token query parameter in the OrgPad file URL. If the file is not already
attached to the edited OrgPage, the API generates `orgpage/add-file`.

Possible file [errors](errors.md#image-file-video-and-audio-errors):

- `invalid-file-source`: `src` is present, but it is not an OrgPad file URL.
- `invalid-file-id`: the tag does not contain a usable file ID or file URL.
- `file-not-found`: the file does not exist or cannot be accessed with the API key or supplied token.

### Embed helpers

Use the custom `embed` tag or a regular `iframe` tag to create [embedded content](content.md#embed). For ordinary
embeds, the API stores an object in the OrgPage `embeds` collection and leaves an `embed` reference in unit content.
For `embed`, the URL can be supplied as either `src` or `source`. In Hiccup, the namespaced `:embed/source` key is also
accepted. For `iframe`, use `src`.

If the URL points to YouTube or an OrgPad OrgPage, the helper is converted to the more specific
[`youtube`](content.md#youtube-video) or [`orgpage`](content.md#embedded-orgpage) tag instead.

When an `embed` or `iframe` source points to an OrgPad file URL, the API parses the file ID and token from the URL and
creates a file embed. This is equivalent to using `file-id` and `file-token` directly. The file must already exist and
be viewable by the API key or token. If it is not attached to the edited OrgPage, the API generates
`orgpage/add-file`.

Otherwise, the helper creates an external embed. The target site must allow embedding. See the general
[Embed](content.md#embed) section for embedding limitations, including external PDF files.

If an `embed` tag supplies only an existing `id`, it references an existing embed object. Embed objects are
page-specific and must stay consistent with the edited page content.

In HTML:

```html
<embed src="https://example.com/report"></embed>
<embed source="https://example.com/report" width="600" height="400"></embed>
<embed src="/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"></embed>
<iframe src="https://example.com/report" width="600" height="400"></iframe>
<iframe src="https://www.youtube.com/watch?v=YYcJ49dIVEo"></iframe>
<iframe src="/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"
        width="700" height="450"></iframe>
<embed file-id="35c33635-ba95-41c3-9481-c137875adf24"
       file-token="2498ced3-60be-48b7-b86a-9b5b3a79316e"></embed>
<embed id="7a4cd0cf-33a8-46a7-8056-9b23a1db6e49"></embed>
```

In Hiccup:

```clojure
[:embed {:src "https://example.com/report"}]
[:embed {:source "https://example.com/report"
         :width  600
         :height 400}]
[:embed {:embed/source "https://example.com/report"}]
[:embed {:src "/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"}]
[:iframe {:src    "https://example.com/report"
          :width  600
          :height 400}]
[:iframe {:src "https://www.youtube.com/watch?v=YYcJ49dIVEo"}]
[:iframe {:src    "/file/A1wzY1upVBw5SBwTeHWt8k?token=AkmM7TYL5It7hqm1s6eTFu"
          :width  700
          :height 450}]
[:embed {:file-id    #uuid "35c33635-ba95-41c3-9481-c137875adf24"
         :file-token #uuid "2498ced3-60be-48b7-b86a-9b5b3a79316e"}]
[:embed {:embed/file-id    #uuid "35c33635-ba95-41c3-9481-c137875adf24"
         :embed/file-token #uuid "2498ced3-60be-48b7-b86a-9b5b3a79316e"}]
[:embed {:embed/id #uuid "7a4cd0cf-33a8-46a7-8056-9b23a1db6e49"}]
```

For generic embeds, width and height must be supplied together. If either dimension is missing, the API uses the default
embed size, `600 x 400` pixels. Uploaded files can be embedded only when they are PDF, Word, Excel, or PowerPoint
documents. Other files can still be linked with the custom [file helper](#file-helpers).

Possible embed [errors](errors.md#math-embed-youtube-and-orgpage-embed-errors):

- `invalid-embed-params`: the tag does not contain a usable embed ID, source URL, or file ID.
- `missing-embed-source`: a new embed ID appears in content without a source URL or file ID.
- `embed-source-or-file`: both a source URL and file ID are specified for the same embed.
- `embed-file-not-found`: the embedded file does not exist or cannot be accessed with the API key or supplied token.
- `unsupported-embed-file-format`: the embedded file exists, but its content type is not supported for embedding.
- `duplicate-embed-id`, `multiple-embed-sources`, `missing-embed-in-content`, `removed-embed-still-in-content`, and
  `embed-linked-to-different-page`: the embed ID and page content are inconsistent with the generated embed operations.

### OrgPage embed helpers

Use the custom [`orgpage` tag](content.md#embedded-orgpage) to embed another OrgPage. The embedded OrgPage can be
referenced by an OrgPad URL, by ID and token, or by a short link. The embedded OrgPage must already exist, and the API
checks that it can be viewed with the supplied token or short link. OrgPage embed helpers do not generate additional
operations.

In HTML:

```html
<orgpage src="https://orgpad.info/o/Cx0toaAblKpKUSZasDxsxK?token=DtN36_XBJGqKhdJk2pwl1Z"></orgpage>
<orgpage src="https://orgpad.info/s/orgpage-data-example#pascal-video"></orgpage>
<orgpage id="b1d2da1a-01b9-4aa4-a512-65ab03c6cc4a"
         token="ed377ebf-5c12-46a8-a85d-264da9c25d59"></orgpage>
<orgpage short-link="orgpage-data-example"></orgpage>
```

In Hiccup, ID and token can be written either with unqualified keys or with namespaced `:orgpage/…` keys:

```clojure
[:orgpage {:src "https://orgpad.info/o/Cx0toaAblKpKUSZasDxsxK?token=DtN36_XBJGqKhdJk2pwl1Z"}]
[:orgpage {:src "https://orgpad.info/s/orgpage-data-example#pascal-video"}]
[:orgpage {:id    #uuid "b1d2da1a-01b9-4aa4-a512-65ab03c6cc4a"
           :token #uuid "ed377ebf-5c12-46a8-a85d-264da9c25d59"}]
[:orgpage {:orgpage/id    #uuid "b1d2da1a-01b9-4aa4-a512-65ab03c6cc4a"
           :orgpage/token #uuid "ed377ebf-5c12-46a8-a85d-264da9c25d59"}]
[:orgpage {:short-link "orgpage-data-example"}]
[:orgpage {:orgpage/short-link "orgpage-data-example"
           :orgpage/fragment   "pascal-video"}]
```

For embedded OrgPages, width and height must be supplied together. If either dimension is missing, the API uses the
default embedded OrgPage size, `700 x 395` pixels. With `src`, OrgPad parses the path, fragment, token, short link, and
query parameters from the URL.

Possible OrgPage embed [errors](errors.md#math-embed-youtube-and-orgpage-embed-errors):

- `invalid-orgpage-params`: the tag does not contain a usable OrgPage URL, ID and token pair, or short link.
- `invalid-orgpage-source`: `src` is present, but it is not a valid OrgPad OrgPage URL or does not contain enough
  information to embed the OrgPage.
- `short-link-not-found`: the referenced short link does not exist.
- `orgpage-not-found`: the OrgPage does not exist or cannot be accessed with the supplied token.

### Video and audio helpers

Use [`video`](content.md#video) and [`audio`](content.md#audio) tags to play uploaded OrgPad files directly in the unit.
The file object must already exist. The API resolves the referenced file, checks access, checks the file content type,
and rewrites the media tag to contain a generated `source` child. If the file is not already attached to the edited
OrgPage, the API generates `orgpage/add-file`.

The file can be referenced directly on the media tag or on a single nested `source` tag. A nested `source` tag accepts
the same ID, token, and URL forms. In HTML:

```html
<video controls="controls"
       id="2464c009-f7d9-4eb2-88d3-c1410d1083f7"
       token="49031bc7-a81c-4b03-8c9a-f5a32e6596e4"
       width="600"></video>
<video>
  <source src="/file/AkZMAJ99lOsojTwUENEIP3?token=BJAxvHqBxLA4ya9aMuZZbk" />
</video>
<video>
  <source id="2464c009-f7d9-4eb2-88d3-c1410d1083f7"
          token="49031bc7-a81c-4b03-8c9a-f5a32e6596e4" />
</video>

<audio controls="controls"
       id="9c1a83fd-e7b2-430e-a007-69b650d5d396"
       token="3b6a9f3d-ca9b-4ea8-8174-502fd5aced8d"></audio>
<audio>
  <source src="/file/CcGoP957JDDqAHabZQ1dOW?token=A7ap89yptOqIF0UC_VrO2N" />
</audio>
```

In Hiccup, ID and token can be written either with unqualified keys or with namespaced `:video/…` or `:audio/…` keys:

```clojure
[:video {:controls true
         :id       #uuid "2464c009-f7d9-4eb2-88d3-c1410d1083f7"
         :token    #uuid "49031bc7-a81c-4b03-8c9a-f5a32e6596e4"
         :width    600}]
[:video {:controls true
         :video/id    #uuid "2464c009-f7d9-4eb2-88d3-c1410d1083f7"
         :video/token #uuid "49031bc7-a81c-4b03-8c9a-f5a32e6596e4"}]
[:video [:source {:src "/file/AkZMAJ99lOsojTwUENEIP3?token=BJAxvHqBxLA4ya9aMuZZbk"}]]
[:video [:source {:id    #uuid "2464c009-f7d9-4eb2-88d3-c1410d1083f7"
                  :token #uuid "49031bc7-a81c-4b03-8c9a-f5a32e6596e4"}]]

[:audio {:controls true
         :id       #uuid "9c1a83fd-e7b2-430e-a007-69b650d5d396"
         :token    #uuid "3b6a9f3d-ca9b-4ea8-8174-502fd5aced8d"}]
[:audio {:controls true
         :audio/id    #uuid "9c1a83fd-e7b2-430e-a007-69b650d5d396"
         :audio/token #uuid "3b6a9f3d-ca9b-4ea8-8174-502fd5aced8d"}]
[:audio [:source {:src "/file/CcGoP957JDDqAHabZQ1dOW?token=A7ap89yptOqIF0UC_VrO2N"}]]
```

The referenced file must be viewable by the API key or supplied token and playable for the tag type. Playable video
content types are `video/mp4` and `video/webm`. Playable audio content types are `audio/mpeg`, `audio/wav`,
`audio/ogg`, `audio/flac`, `audio/mp4`, `audio/x-m4a`, and `audio/aac`.

Video width and height are computed from metadata when missing. If only one dimension is supplied, OrgPad preserves the
video ratio and computes the other dimension. If both dimensions are omitted, OrgPad uses stored video metadata and
scales the video down to fit within `600 x 600` pixels. When dimensions are not available in video metadata, OrgPad
falls back to `560 x 314` pixels.

The API stores the media tag with a generated `source` child that points to the uploaded OrgPad file. Supported media
attributes are described in the [video](content.md#video) and [audio](content.md#audio) content overview.

Possible video [errors](errors.md#image-file-video-and-audio-errors):

- `invalid-video-source`: `src` is present, but it is not an OrgPad file URL.
- `invalid-video-id`: the tag does not contain a usable file ID or file URL.
- `video-not-found`: the file does not exist or cannot be accessed with the API key or supplied token.
- `unsupported-video-format`: the file exists, but its content type is not playable as video.

Possible audio [errors](errors.md#image-file-video-and-audio-errors):

- `invalid-audio-source`: `src` is present, but it is not an OrgPad file URL.
- `invalid-audio-id`: the tag does not contain a usable file ID or file URL.
- `audio-not-found`: the file does not exist or cannot be accessed with the API key or supplied token.
- `unsupported-audio-format`: the file exists, but its content type is not playable as audio.

### YouTube helpers

Use the custom [`youtube` tag](content.md#youtube-video) to embed a YouTube video. The API accepts a full YouTube URL or
an 11-character YouTube video ID. Full URLs can use `youtube.com`, `m.youtube.com`, `music.youtube.com`,
`youtube-nocookie.com`, `youtu.be`, or `yout-ube.com`, including normal watch URLs, embed URLs, and shorts URLs.
YouTube helpers do not create an `embed` object and do not generate an `embed/create` operation.

In HTML:

```html
<youtube>YYcJ49dIVEo</youtube>
<youtube>https://www.youtube.com/watch?v=YYcJ49dIVEo</youtube>
<youtube src="https://www.youtube.com/watch?v=YYcJ49dIVEo"></youtube>
<youtube src="https://www.youtube.com/watch?v=YYcJ49dIVEo&amp;start=60&amp;end=120&amp;autoplay=1"></youtube>
<youtube id="YYcJ49dIVEo"></youtube>
<youtube id="YYcJ49dIVEo" query-params="start:60;end:120;autoplay:1"></youtube>
<youtube id="YYcJ49dIVEo" width="560" height="314"></youtube>
```

In Hiccup, the ID can be written either with `:id` or with the namespaced `:youtube/id` key. Query parameters can be
written either with `:query-params` or `:youtube/query-params`:

```clojure
[:youtube "YYcJ49dIVEo"]
[:youtube "https://www.youtube.com/watch?v=YYcJ49dIVEo"]
[:youtube {:src "https://www.youtube.com/watch?v=YYcJ49dIVEo"}]
[:youtube {:src "https://www.youtube.com/watch?v=YYcJ49dIVEo&start=60&end=120&autoplay=1"}]
[:youtube {:id "YYcJ49dIVEo"}]
[:youtube {:id           "YYcJ49dIVEo"
           :query-params {:start    60
                          :end      120
                          :autoplay 1}}]
[:youtube {:youtube/id     "YYcJ49dIVEo"
           :youtube/width  560
           :youtube/height 314}]
[:youtube {:youtube/id           "YYcJ49dIVEo"
           :youtube/query-params {:start    60
                                  :end      120
                                  :autoplay 1}}]
```

When width and height are omitted, normal YouTube videos default to `560 x 314` pixels. YouTube Shorts default to
`314 x 560` pixels. If only one dimension is supplied, the supplied dimension is used and the missing dimension falls
back to the corresponding default. Query parameters from the URL are preserved except parameters that OrgPad does not
use for embeds, such as `v`, `ab_channel`, and `feature`. Playback parameters are described in the
[YouTube content overview](content.md#youtube-video).

Possible YouTube [errors](errors.md#math-embed-youtube-and-orgpage-embed-errors):

- `invalid-youtube-source`: source text or `src` is present, but it is not a supported YouTube URL.
- `invalid-youtube-id`: `id` is present, but it is not an 11-character YouTube video ID.
- `missing-youtube-source`: the tag does not contain a YouTube URL or video ID.
- `youtube-video-unavailable`: OrgPad could not access the video thumbnail. Check that the video ID is correct and the
  video is not private.

### Math and chemistry helpers

Use the custom [`math` tag](content.md#math-and-chemistry) for LaTeX math and the API-only `chem` tag for chemistry. The
source can be written as the only text child or in a `source` attribute. If no ID is supplied, the API generates a new
math ID and creates a `math/create` operation. If an ID is supplied without a source, the tag references an existing
math object. The `chem` tag is shorthand for `math` with the `chemistry` type.

In HTML:

```html
<math id="bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294"></math>
<math>x,y \in \mathbb R</math>
<math source="x,y \in \mathbb R"></math>
<math block="true">(x+y)^n = \sum_{k=0}^n {n \choose k} x^k y^{n-k}</math>
<chem>H2O</chem>
<math type="chemistry">NaCl + H2SO4 -> NaHSO4 + HCl</math>
```

In Hiccup:

```clojure
[:math {:math/id #uuid "bf9dfb8f-e3c6-4d5b-93e6-2006dec8e294"}]
[:math "x,y \\in \\mathbb R"]
[:math {:source "x,y \\in \\mathbb R"}]
[:math {:math/source "x,y \\in \\mathbb R"}]
[:math {:math/block true}
 "(x+y)^n = \\sum_{k=0}^n {n \\choose k} x^k y^{n-k}"]
[:chem "H2O"]
[:math {:type "chemistry"} "NaCl + H2SO4 -> NaHSO4 + HCl"]
[:math {:math/source "H2O"
        :math/type   :math/chemistry}]
```

The `math` tag creates math formulas by default. The `chem` tag creates chemistry formulas. For `math`, the type can
also be supplied explicitly with `type` in HTML or `:math/type` in Hiccup.

Block math and block code are split out so they can stand as block-level content. For example, this HTML input:

```html
<p>
  Binomial theorem states
  <math block>(x+y)^n = \sum_{k=0}^n {n \choose k} x^k y^{n-k}</math>
  for every <math>x,y \in \mathbb R</math> and every <math>n \in \mathbb N</math>
</p>
```

is automatically split into:

```html
<p>
  Binomial theorem states
</p>
<p>
  <math block>(x+y)^n = \sum_{k=0}^n {n \choose k} x^k y^{n-k}</math>
</p>
<p>
  for every <math>x,y \in \mathbb R</math> and every <math>n \in \mathbb N</math>
</p>
```

After splitting, `math/create` operations are generated, and the stored `math` tags reference the generated math IDs.

Possible math [errors](errors.md#math-embed-youtube-and-orgpage-embed-errors):

- `missing-math-source`: a new math tag does not contain a source.
- `invalid-math-source`: OrgPad could not render the supplied LaTeX source.
- `duplicate-math-id`, `multiple-math-sources`, `missing-math-in-content`, `removed-math-still-in-content`, and
  `math-linked-to-different-page`: the math ID and page content are inconsistent with the generated math operations.

### Code helpers

Use the normal [`code` tag](content.md#code) for inline code. For block code, use `code` with the `block` attribute,
`pre`, or `pre` with nested `code`. Newline characters inside code are converted to `<br>` line breaks. A `code` tag is
also treated as block code when its content contains multiple lines. Block code is split out so it can stand as
block-level stored content. Code helpers do not reference external objects and do not generate additional operations.

See the [list of supported programming languages](content.md#supported-code-highlighting-languages). In HTML, the API
parses the language from the `class`, `lang`, or `data-code-lang` attribute. If the language name is not recognized, the
stored code has no language. In Hiccup, `:code/lang` uses a supported `:code-lang/…` keyword; an unsupported keyword
returns the `unsupported-code-lang` [error](errors.md#content-format-errors).

In HTML:

```html
<code>inline code</code>
<pre><code class="language-clojure">(map inc [1 2 3])</code></pre>
<pre lang="clojure">(filter odd? [1 2 3])</pre>
<pre><code lang="c">int choose(int n, int k) {
  if (k > n) return 0;
  if (k > n - k) k = n - k;

  int r = 1;
  for (int i = 1; i <= k; i++)
    r = r * (n - k + i) / i;

  return r;
}</code></pre>
<pre>Preformatted text without a language.</pre>
<code block>Also preformatted text
with multiple lines</code>
<code>Code containing a newline
is automatically treated as block code.</code>
```

In Hiccup:

```clojure
[:code "inline code"]
[:pre [:code {:class "language-clojure"} "(map inc [1 2 3])"]]
[:code {:code/block true
        :code/lang  :code-lang/clojure} "(map inc [1 2 3])"]
[:pre {:lang "clojure"} "(filter odd? [1 2 3])"]
[:pre [:code {:lang "c"} "int choose(int n, int k) {
  if (k > n) return 0;
  if (k > n - k) k = n - k;

  int r = 1;
  for (int i = 1; i <= k; i++)
    r = r * (n - k + i) / i;

  return r;
}"]]
[:pre "Preformatted text without a language."]
[:code {:block true} "Also preformatted text
with multiple lines"]
[:code "Code containing a newline
is automatically treated as block code."]
```

Block code is split out so it can stand as block-level content. For example, this HTML input

```html
<p>To print statements, use<pre lang="clojure">(println "Hello World")</pre> in Clojure.</p>
```

is automatically transformed into

```html
<p>To print statements, use</p>
<code block lang="clojure">(println "Hello World")</code>
<p>in Clojure.</p>
```

Code can contain regular inline formatting, math formulas, and chemistry formulas; see [Code](content.md#code) and the
[supported OrgPad code language keywords](content.md#supported-code-highlighting-languages).

## Generated Operations and Dry Run

During content processing, OrgPad expands helper tags into stored content and generated operations. Use `?dry-run=true`
on the operations endpoint to inspect the expanded list without saving changes. The dry-run response has the normal
response shape and includes `"dryRun": true` in JSON or `:ops/dry-run true` in EDN.

- `appendedContent` is merged with the existing page content and expanded into a normal `unit/update` containing full
  `content`.
- New `math` tags with source text can generate `math/create`; existing math tags with changed source, type, or block
  mode can generate `math/update`.
- New URL or file embeds can generate `embed/create`; existing embeds with changed source, file, or text ID can generate
  `embed/update`.
- Images used in content generate `orgpage/add-image` when the image is accessible but not yet attached to the edited
  OrgPage.
- Files used by file helpers, media helpers, or file embeds generate `orgpage/add-file` when the file is accessible but
  not yet attached to the edited OrgPage.
- Replacing page content can generate `math/remove` or `embed/remove` for math and embed objects no longer referenced
  from that page.

Check dry-run output when helper input is accepted but the stored content or generated object IDs are not what you
expected.

## Errors and Troubleshooting

Content-specific errors are listed near the helper that can return them. General HTTP, authentication, and
endpoint errors are documented in [Errors](errors.md#http-status-codes).

Use these checks when a content operation fails:

- Confirm the request format first. JSON content keys are `content`, `contentType`, and `appendedContent`; EDN keys are
  `:unit/content`, `:unit/content-type`, and `:unit/appended-content`.
- Confirm the default content type. JSON defaults to HTML when content is present; EDN defaults to Hiccup.
- If an HTML or Markdown request returns a `string-…-content` error, send the content as a string. Use
  `contentType: "hiccup"` only when the request body contains Hiccup data.
- If a helper references a missing file, image, or OrgPage, include the required token or use a URL that
  contains the token.
- If a math or embed error mentions a missing, duplicate, removed, or differently linked object, check the sent
  operation list.

## Related Pages

Use these pages when content input connects to stored content, operations, attachments, or errors.

| Page                                      | When to use it                                                          |
|-------------------------------------------|-------------------------------------------------------------------------|
| [Operations](ops.md)                      | See where `content`, `appendedContent`, and `contentType` are used.     |
| [Unit content](content.md)                | Understand stored unit content and supported tags.                      |
| [Attachments](attachments.md)             | Upload and download files and images used in content.                   |
| [OrgPage data](orgpage.md)                | Inspect generated units, files, images, embeds, and math objects.       |
| [API cookbook](cookbook.md)               | See practical examples using HTML, Markdown, files, images, and embeds. |
| [Errors](errors.md#content-format-errors) | Understand content-specific errors.                                     |
