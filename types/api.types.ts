// Request and response bodies. Route comments show the OrgPage-ID form where a short-link form also exists.
// See the API reference for path and query parameters, headers, and errors.

import type {
  Id, OrgPageId, OperationId, UserId, UserGroupId, FileId, ImageId, PathId, AccessToken,
  BookRef, PageRef, FragmentTargetRef, UtcTimestamp, Permission, SpectrumColor,
  User, OrgPage, OrgPageMetadata, OrgPageObjects, FileAttachment, ImageAttachment, Fragment,
} from "./data.types";
import type { Operation, ReturnedOperation } from "./ops.types";
import type { ApiErrorCode, ApiErrorDetails, ApiErrorResponse, CommonErrorCode, ScopedKeyErrorCode, OrgPageTargetErrorCode,
  BodyErrorCode, OperationsErrorCode, SharingErrorCode, ScreenshotErrorCode, PrintErrorCode } from "./errors.types";

/** Either the successful body or a structured API error body. */
export type ApiResponse<T, C extends ApiErrorCode = ApiErrorCode> = T | ApiErrorResponse<C>;

/** GET /api/v1/info */
export type ApiKeyInfoRequest = undefined;
/** GET /api/v1/info */
export type ApiKeyInfoResponse = ApiResponse<{
  id: string;
  title: string;
  userId: UserId;
  orgpageId?: OrgPageId;
  permission: Permission;
  creationTime: UtcTimestamp;
  lastUsed?: UtcTimestamp;
}, CommonErrorCode>;

export type ListedOrgPage = Pick<OrgPageMetadata,
  "id" | "title" | "description" | "tags" | "color" | "owner" | "creationTime" | "lastEditTime"
> & { viewToken: AccessToken; permission: Permission };
type OrgPageListBody = {
  orgpages: ListedOrgPage[];
  owners: User[];
  usergroups?: Array<{ id: UserGroupId; name: string; orgpageIds?: OrgPageId[] }>;
  sharedOrgpageIds?: OrgPageId[];
};
/** GET /api/v1/o */
export type OrgPageListRequest = undefined;
/** GET /api/v1/o */
export type OrgPageListResponse = ApiResponse<OrgPageListBody, ScopedKeyErrorCode>;

/** GET /api/v1/public */
export type PublicOrgPageListRequest = undefined;
/** GET /api/v1/public */
export type PublicOrgPageListResponse = ApiResponse<Pick<OrgPageListBody, "orgpages" | "owners">, CommonErrorCode>;

/** POST /api/v1/o; an omitted body and JSON null both use defaults. */
export type CreateOrgPageRequest = {
  title?: string;
  description?: string;
  tags?: string[];
  color?: SpectrumColor;
} | null | undefined;
/** POST /api/v1/o */
export type CreateOrgPageResponse = ApiResponse<{ id: OrgPageId }, ScopedKeyErrorCode | BodyErrorCode>;

/** POST /api/v1/o/{orgpage-id}/copy */
export type CopyOrgPageRequest = undefined;
/** POST /api/v1/o/{orgpage-id}/copy */
export type CopyOrgPageResponse = ApiResponse<{ id: OrgPageId }, OrgPageTargetErrorCode>;

/** DELETE /api/v1/o/{orgpage-id} */
export type DeleteOrgPageRequest = undefined;
/** DELETE /api/v1/o/{orgpage-id}; success returns 204 with no body. */
export type DeleteOrgPageResponse = ApiResponse<undefined, OrgPageTargetErrorCode>;

/** GET /api/v1/o/{orgpage-id} */
export type ReadOrgPageRequest = undefined;
/** GET /api/v1/o/{orgpage-id} */
export type ReadOrgPageResponse = ApiResponse<OrgPage, OrgPageTargetErrorCode>;

/** GET /api/v1/o/{orgpage-id}/meta */
export type ReadOrgPageMetadataRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/meta */
export type ReadOrgPageMetadataResponse = ApiResponse<OrgPageMetadata, OrgPageTargetErrorCode>;

/** GET /api/v1/o/{orgpage-id}/unit/{unit-id-or-text-id} */
export type ReadUnitRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/unit/{unit-id-or-text-id} */
export type ReadUnitResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "unit-not-found">;

/** GET /api/v1/o/{orgpage-id}/link/{link-id} */
export type ReadLinkRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/link/{link-id} */
export type ReadLinkResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "link-not-found">;

/** GET /api/v1/o/{orgpage-id}/file/{file-id} */
export type ReadFileRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/file/{file-id} */
export type ReadFileResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "file-not-found">;

/** GET /api/v1/o/{orgpage-id}/image/{image-id} */
export type ReadImageRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/image/{image-id} */
export type ReadImageResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "image-not-found">;

/** GET /api/v1/o/{orgpage-id}/math/{math-id} */
export type ReadMathRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/math/{math-id} */
export type ReadMathResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "math-not-found">;

/** GET /api/v1/o/{orgpage-id}/embed/{embed-id-or-text-id} */
export type ReadEmbedRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/embed/{embed-id-or-text-id} */
export type ReadEmbedResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "embed-not-found">;

/** GET /api/v1/o/{orgpage-id}/path/{path-id} */
export type ReadPathRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/path/{path-id} */
export type ReadPathResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "path-not-found">;

/** GET /api/v1/o/{orgpage-id}/fragment/{fragment-id-or-text-id} */
export type ReadFragmentRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/fragment/{fragment-id-or-text-id} */
export type ReadFragmentResponse = ApiResponse<OrgPageObjects, OrgPageTargetErrorCode | "fragment-not-found">;

/** POST /api/v1/o/{orgpage-id}/ops */
export type OperationsRequest = Operation[];
/** POST /api/v1/o/{orgpage-id}/ops */
export type OperationsResponse = ApiResponse<{ id: OperationId; list: ReturnedOperation[]; dryRun?: true },
  OrgPageTargetErrorCode | BodyErrorCode | OperationsErrorCode>;

export type SharingRequest =
  | ["permission/set-public-permission", "permission/none" | "permission/view"]
  | ["permission/reset-tokens"]
  | ["permission/add-user", idOrEmail: string, permission: Permission]
  | ["permission/set-user-permission", idOrEmail: string, permission: Permission]
  | ["permission/remove-user", idOrEmail: string];
type SharingBody = {
  owner: User;
  users?: Array<(User | { id: Id; name: string }) & { permission: Permission }>;
  public: "permission/none" | "permission/view";
  viewToken: AccessToken;
  commentToken: AccessToken;
  editToken: AccessToken;
  viewLink: string;
  commentLink: string;
  editLink: string;
  fragments?: Array<Pick<Fragment, "id" | "textId" | "title">>;
  paths?: Array<{ id: PathId; title?: string; viewLink: string; commentLink: string; editLink: string; numSteps: number }>;
};

/** GET /api/v1/o/{orgpage-id}/share */
export type ReadSharingRequest = undefined;
/** GET /api/v1/o/{orgpage-id}/share */
export type ReadSharingResponse = ApiResponse<SharingBody, OrgPageTargetErrorCode>;

/** POST /api/v1/o/{orgpage-id}/share */
export type UpdateSharingRequest = SharingRequest;
/** POST /api/v1/o/{orgpage-id}/share */
export type UpdateSharingResponse = ApiResponse<SharingBody, OrgPageTargetErrorCode | BodyErrorCode | SharingErrorCode>;

/** GET /api/v1/file/{file-id} */
export type FileMetadataRequest = undefined;
/** GET /api/v1/file/{file-id} */
export type FileMetadataResponse = ApiResponse<FileAttachment & { orgpageIds: OrgPageId[] }, ScopedKeyErrorCode | "file-not-found">;

/** GET /api/v1/img/{image-id} */
export type ImageMetadataRequest = undefined;
/** GET /api/v1/img/{image-id} */
export type ImageMetadataResponse = ApiResponse<ImageAttachment & { orgpageIds: OrgPageId[] }, ScopedKeyErrorCode | "image-not-found">;

/** POST /api/v1/o/{orgpage-id}/upload uses multipart/form-data with one or more `files` parts. */
export type UploadRequest = FormData;
/** POST /api/v1/o/{orgpage-id}/upload */
export type UploadResponse = ApiResponse<{
  files?: FileAttachment[];
  images?: ImageAttachment[];
  errors?: ApiErrorDetails<"invalid-image">[];
}, OrgPageTargetErrorCode | "storage-quota-exceeded">;

export interface RenameAttachmentRequest { filename: string; orgpageIds?: OrgPageId[] }
interface RenameFileResponseFields {
  oldFileId?: FileId;
  oldPosterId?: ImageId;
  fileOps: ReturnedOperation[];
  orgpageOps: Record<OrgPageId, ReturnedOperation[]>;
}

/** POST /api/v1/file/{file-id}/rename */
export type RenameFileRequest = RenameAttachmentRequest;
/** POST /api/v1/file/{file-id}/rename; replacement file and poster tokens may be absent in dry runs. */
export type RenameFileResponse = ApiResponse<RenameFileResponseFields & (
  | { dryRun: true; file: Omit<FileAttachment, "token"> & { token?: AccessToken };
      poster?: Omit<ImageAttachment, "token"> & { token?: AccessToken } }
  | { dryRun?: never; file: FileAttachment; poster?: ImageAttachment }
), ScopedKeyErrorCode | BodyErrorCode | "invalid-id" | "file-not-found" | "file-missing-in-orgpages">;

/** POST /api/v1/img/{image-id}/rename */
export type RenameImageRequest = RenameAttachmentRequest;
/** POST /api/v1/img/{image-id}/rename */
export type RenameImageResponse = ApiResponse<{
  image: ImageAttachment;
  oldImageId?: ImageId;
  imageOps: ReturnedOperation[];
  orgpageOps: Record<OrgPageId, ReturnedOperation[]>;
  dryRun?: true;
}, ScopedKeyErrorCode | BodyErrorCode | "invalid-id" | "image-not-found" | "image-missing-in-orgpages">;

type ScreenshotTarget =
  | { fragmentId: FragmentTargetRef; open?: never; openedPageIds?: never; focusedBookIds?: never; hiddenBookIds?: never }
  | { fragmentId?: never; open: "all"; openedPageIds?: never; focusedBookIds?: never; hiddenBookIds?: never }
  | { fragmentId?: never; open?: never; openedPageIds?: PageRef[]; focusedBookIds?: BookRef[]; hiddenBookIds?: BookRef[] };
type PrintTarget =
  | (ScreenshotTarget & { pathId?: never })
  | {
      pathId: PathId;
      fragmentId?: never;
      open?: never;
      openedPageIds?: never;
      focusedBookIds?: never;
      hiddenBookIds?: never;
    };
type ScreenshotDryRunBody = Exclude<ScreenshotRequest, null> & { dryRun: true; format: string };
type PrintDryRunBody = Exclude<PrintRequest, null> & { dryRun: true };

/** POST /api/v1/o/{orgpage-id}/screenshot */
export type ScreenshotRequest = ({ resolution?: [number, number]; theme?: "light" | "dark" } & ScreenshotTarget) | null;
/** POST /api/v1/o/{orgpage-id}/screenshot */
export type ScreenshotResponse = ApiResponse<ArrayBuffer | ScreenshotDryRunBody,
  OrgPageTargetErrorCode | BodyErrorCode | ScreenshotErrorCode>;

/** POST /api/v1/o/{orgpage-id}/print */
export type PrintRequest = ({
  color?: "white" | "gray" | "dm";
  size?: "a4" | "letter" | "ratio-4-to-3" | "ratio-16-to-9";
  orientation?: "landscape" | "portrait";
  padding?: number | [number, number];
} & PrintTarget) | null;
/** POST /api/v1/o/{orgpage-id}/print */
export type PrintResponse = ApiResponse<ArrayBuffer | PrintDryRunBody,
  OrgPageTargetErrorCode | BodyErrorCode | PrintErrorCode>;

/** GET /file/{file-id}?token={file-token} */
export type DownloadFileRequest = undefined;
/** GET /file/{file-id}?token={file-token}; successful body. Non-API errors are not modeled. */
export type DownloadFileResponse = ArrayBuffer;

/** GET /img/{image-id}/download?token={image-token} */
export type DownloadImageRequest = undefined;
/** GET /img/{image-id}/download?token={image-token}; successful body. Non-API errors are not modeled. */
export type DownloadImageResponse = ArrayBuffer;
