// JSON API error codes, endpoint-specific error groups, and error response details. See ../errors.md.

import type { JsonValue } from "./data.types";

/** Content helper failures include the original tag and its surrounding page. */
type ContentHelperErrorCode =
  | "invalid-mark-color" | "unsupported-code-lang" | "missing-math-source"
  | "invalid-embed-params" | "missing-embed-source" | "image-not-found" | "dm-image-not-found"
  | "dark-mode-different-size" | "dark-mode-image-only" | "invalid-image-source"
  | "invalid-image-dm-source" | "invalid-image-id-or-token" | "file-not-found"
  | "invalid-file-source" | "invalid-file-id" | "video-not-found" | "invalid-video-source"
  | "invalid-video-id" | "unsupported-video-format" | "audio-not-found" | "invalid-audio-source"
  | "invalid-audio-id" | "unsupported-audio-format" | "invalid-orgpage-params" | "orgpage-not-found"
  | "short-link-not-found" | "invalid-orgpage-source" | "invalid-youtube-source" | "invalid-youtube-id"
  | "missing-youtube-source";

/** Common API middleware errors. OrgPage-targeted routes can also use short links. */
export type CommonErrorCode = "unsupported-response-format" | "invalid-authorization" | "invalid-api-key"
  | "expired-subscription" | "key-permission-denied";
export type ScopedKeyErrorCode = CommonErrorCode | "key-for-single-orgpage";
export type OrgPageTargetErrorCode = ScopedKeyErrorCode | "unknown-short-link" | "orgpage-not-found" | "user-permission-denied";
export type BodyErrorCode = "unsupported-content-type" | "invalid-request-body" | "body-schema-error";
export type IdErrorCode = "invalid-id" | "unknown-id" | "wrong-id-type";
export type SharingErrorCode = "invalid-user-or-usergroup" | "cannot-change-owner-permission"
  | "already-has-permission" | "permission-not-found" | "owner-share-limit-reached";
export type ScreenshotErrorCode = IdErrorCode | "screenshot-target-conflict" | "screenshot-failed"
  | "screenshot-timeout" | "screenshot-fragment-not-found";
export type PrintErrorCode = IdErrorCode | "print-target-conflict" | "print-failed"
  | "print-timeout" | "print-fragment-not-found";

/** Operations validation includes content, attachment, math and embed helper errors. */
export type OperationsErrorCode = ContentHelperErrorCode | IdErrorCode
  | "duplicate-text-id" | "id-already-exists" | "op-for-removed-id"
  | "specify-edited-page" | "added-page-out-of-bounds" | "moved-page-out-of-bounds" | "page-not-in-book"
  | "missing-parent" | "missing-child-in-parent" | "missing-child" | "parent-mismatch" | "empty-children"
  | "same-link-endpoints" | "empty-path-steps" | "added-step-out-of-bounds" | "moved-step-out-of-bounds"
  | "content-and-appended-content" | "string-plain-content" | "string-markdown-content" | "string-html-content"
  | "hiccup-content-schema-error" | "video-poster" | "invalid-math-source" | "duplicate-math-id" | "multiple-math-sources"
  | "multiple-math-macros" | "multiple-math-operations" | "missing-math-in-content"
  | "removed-math-still-in-content" | "math-linked-to-different-page" | "embed-source-or-file"
  | "duplicate-embed-id" | "multiple-embed-sources" | "missing-embed-in-content"
  | "removed-embed-still-in-content" | "embed-linked-to-different-page" | "embed-file-not-found"
  | "unsupported-embed-file-format" | "youtube-video-unavailable";

export type ApiErrorCode =
  | OrgPageTargetErrorCode | BodyErrorCode | SharingErrorCode | ScreenshotErrorCode | PrintErrorCode | OperationsErrorCode
  | "unit-not-found" | "link-not-found" | "math-not-found" | "embed-not-found" | "path-not-found" | "fragment-not-found"
  | "image-missing-in-orgpages" | "invalid-image" | "file-missing-in-orgpages" | "storage-quota-exceeded";

type SharedContentHelperErrorCode = "missing-math-source" | "missing-embed-source" | "image-not-found"
  | "file-not-found" | "orgpage-not-found";
type ContentHelperFields<C extends ApiErrorCode> = C extends ContentHelperErrorCode ? C extends SharedContentHelperErrorCode ? {
  pageId?: string;
  tag?: string;
  attributes?: JsonValue;
  children?: JsonValue;
} : {
  pageId: string;
  tag: string;
  attributes: JsonValue;
  children: JsonValue;
} : {};

/** Details can vary between call sites that use the same error code. */
type ErrorFields<C extends ApiErrorCode> =
  C extends "invalid-authorization" | "invalid-api-key" | "expired-subscription" ? { url: string } :
  C extends "unsupported-response-format" ? { supportedFormats: string[] } :
  C extends "unsupported-content-type" ? { supportedFormats: string[]; contentType: string } :
  C extends "body-schema-error" ? { schemaError: JsonValue } :
  C extends "invalid-request-body" ? { exception: string | null } :
  C extends "invalid-id" ? { invalidId: string } | { orgpageId: string } :
  C extends "unknown-id" ? { id: string; op: string | null; idKey: string | string[]; expectedType: string } :
  C extends "wrong-id-type" ? { id: string; op: string | null; idKey: string | string[]; expectedType: string; detectedType: string } :
  C extends "id-already-exists" ? { existingId: string } :
  C extends "op-for-removed-id" ? { id: string; op: string } :
  C extends "duplicate-text-id" ? { duplicateId: string } :
  C extends "unknown-short-link" ? { shortLink: string } :
  C extends "orgpage-not-found" ? { orgpageId?: string } :
  C extends "key-for-single-orgpage" ? { orgpageId: string } :
  C extends "user-permission-denied" ? { requiredPermission: string; userPermission: string } :
  C extends "key-permission-denied" ? { requiredPermission: string; apiKeyPermission: string } :
  C extends "specify-edited-page" ? { id: string; childUnitIds: string[] } :
  C extends "added-page-out-of-bounds" ? { id: string; pageId: string; index: number; maxIndex: number; childUnitIds: string[] | null } :
  C extends "moved-page-out-of-bounds" ? { id: string; pageId: string; newIndex: number; maxIndex: number; childUnitIds: string[] } :
  C extends "page-not-in-book" ? { id: string; pageId: string; childUnitIds: string[] | null } :
  C extends "missing-parent" ? { id: string; parentId: string } :
  C extends "missing-child-in-parent" ? { id: string; parentId: string; childUnitIds: string[] } :
  C extends "missing-child" ? { id: string; childId: string } :
  C extends "parent-mismatch" ? { id: string; childId: string; parentId: string } :
  C extends "empty-children" ? { id: string } :
  C extends "added-step-out-of-bounds" ? { pathId: string; stepId: string; stepIds: string[] | null; index: number; maxIndex: number } :
  C extends "moved-step-out-of-bounds" ? { pathId: string; stepId: string; stepIds: string[]; newIndex: number; maxIndex: number } :
  C extends "empty-path-steps" ? { id: string } :
  C extends "same-link-endpoints" ? { linkId: string; endpointIds: [string, string] } :
  C extends "multiple-math-operations" ? { mathId: string } :
  C extends "content-and-appended-content" | "string-plain-content" | "string-markdown-content" | "string-html-content" ? { id: string } :
  C extends "hiccup-content-schema-error" ? { id: string; schemaError: JsonValue } :
  C extends "storage-quota-exceeded" ? { uploadSize: number; availableSpace: number } :
  C extends "invalid-image" ? { filename: string | null; contentType: string | null } :
  C extends "file-not-found" ? { fileId?: string; id?: string; token?: string | null } :
  C extends "image-not-found" ? { imageId?: string; id?: string; token?: string | null } :
  C extends "file-missing-in-orgpages" | "image-missing-in-orgpages" ? { orgpageIds: string[] } :
  C extends "video-poster" ? { imageId: string; fileId: string } :
  C extends "invalid-mark-color" ? { allowedColors: string[] } :
  C extends "unsupported-code-lang" ? { supportedLangs: string[] } :
  C extends "dark-mode-different-size" ? { size: [number | null, number | null]; dmSize: [number | null, number | null] } :
  C extends "unsupported-video-format" | "unsupported-audio-format" ? { contentType: string | null; supportedContentTypes: string[] } :
  C extends "invalid-math-source" ? { latexError: string; latexSource?: string; mathId?: string; pageId?: string; generated?: boolean } :
  C extends "duplicate-math-id" ? { mathId: string; pageIds: string[] } :
  C extends "duplicate-embed-id" ? { embedId: string; pageIds: string[] } :
  C extends "multiple-math-sources" ? { mathId: string; pageId: string; contentSource: string; opSource: string } :
  C extends "missing-math-source" ? { mathId?: string; pageId?: string } :
  C extends "multiple-embed-sources" ? { embedId: string; pageId: string; contentSource: string; opSource: string } :
  C extends "missing-embed-source" ? { embedId?: string; pageId?: string } :
  C extends "missing-math-in-content" ? { mathId: string; pageId: string; contentMathIds: string[] | null } :
  C extends "removed-math-still-in-content" ? { mathId: string; pageId: string } :
  C extends "math-linked-to-different-page" ? { mathId: string; pageId: string; mathPageId: string | null } :
  C extends "missing-embed-in-content" ? { embedId: string; pageId: string; contentEmbedIds: string[] | null } :
  C extends "removed-embed-still-in-content" ? { embedId: string; pageId: string } :
  C extends "embed-linked-to-different-page" ? { embedId: string; pageId: string; embedPageId: string | null } :
  C extends "embed-source-or-file" ? { id: string; fileId: string; source: string } :
  C extends "embed-file-not-found" ? { id: string; fileId: string; fileToken: string | null } :
  C extends "unsupported-embed-file-format" ? { id: string; fileId: string; contentType: string | null; supportedContentTypes: string[] } :
  C extends "screenshot-fragment-not-found" | "print-fragment-not-found" ? { fragmentId: string } :
  C extends "invalid-user-or-usergroup" ? { idOrEmail: string } :
  C extends "cannot-change-owner-permission" | "already-has-permission" | "permission-not-found" ? { id: string } :
  C extends "youtube-video-unavailable" ? { youtubeId: string } : {};

/** An error code discriminates the error and its known details. */
export type ApiErrorDetails<C extends ApiErrorCode = ApiErrorCode> = C extends ApiErrorCode ? {
  code: C;
  message: string;
  docs: string;
} & ErrorFields<C> & ContentHelperFields<C> : never;
export type ApiErrorResponse<C extends ApiErrorCode = ApiErrorCode> = { error: ApiErrorDetails<C> };
