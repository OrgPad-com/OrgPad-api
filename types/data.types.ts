// Shared JSON data types, including OrgPage objects, identifiers, and attachments.

export type JsonValue = null | boolean | number | string | JsonValue[] | { [key: string]: JsonValue };

export type Id = string;
export type OrgPageId = Id;
export type OperationId = Id;
export type UserId = Id;
export type UserGroupId = Id;
export type BookId = Id;
export type PageId = Id;
export type UnitId = BookId | PageId;
export type LinkId = Id;
export type MathId = Id;
export type EmbedId = Id;
export type FileId = Id;
export type ImageId = Id;
export type FragmentId = Id;
export type PathId = Id;
export type PathStepId = Id;

/** Text IDs are custom readable identifiers. They are supported for units, embeds, and fragments. */
export type TextId = string;
export type BookRef = BookId | TextId;
export type PageRef = PageId | TextId;
export type UnitRef = UnitId | TextId;
export type EmbedRef = EmbedId | TextId;
export type FragmentRef = FragmentId | TextId;
/** All fragments, including implicit fragments. */
export type FragmentTargetRef = FragmentRef | UnitRef | LinkId | PathId | PathStepId;

/** UTC ISO 8601 timestamp, for example "2026-05-14T13:46:12.863945Z". */
export type UtcTimestamp = string;
export type AccessToken = string;
export type Permission = "permission/view" | "permission/comment" | "permission/edit" | "permission/admin";
export type SpectrumColor =
  | "color/red" | "color/orange" | "color/yellow" | "color/lime"
  | "color/green" | "color/mint" | "color/teal" | "color/blue"
  | "color/blueberry" | "color/purple" | "color/orchid" | "color/pink";
export type Color = SpectrumColor | "color/white" | "color/light-gray" | "color/gray";

export interface User {
  id: UserId;
  firstName?: string;
  lastName?: string;
}

export interface OrgPageMetadata {
  id: OrgPageId;
  title?: string;
  description?: string;
  tags?: string[];
  color: SpectrumColor;
  owner: UserId;
  creationTime: UtcTimestamp;
  lastLoadTime?: UtcTimestamp;
  lastEditTime?: UtcTimestamp;
  mathMacros?: string;
  initFragments?: { default?: FragmentId; smallScreen?: FragmentId; screenshot?: FragmentId };
}

export interface UnitProps {
  color: Color;
  titleSize: "props/h1" | "props/h2" | "props/h3";
}

export interface Book {
  id: BookId;
  textId?: TextId;
  type: "unit/book";
  title?: string;
  pos: [number, number];
  childUnitIds: PageId[];
  props: UnitProps;
}

export interface Page {
  id: PageId;
  textId?: TextId;
  type: "unit/page";
  parentId: BookId;
  /** JSON responses return HTML. */
  content?: string;
}
export type Unit = Book | Page;

export interface LinkProps {
  color: Color;
  weight?: "props/none" | "props/strong";
  arrowhead: "props/none" | "props/single" | "props/double";
}

export interface Link {
  id: LinkId;
  endpointIds: [BookId, BookId];
  props?: LinkProps;
}

export interface MathObject {
  id: MathId;
  pageId: PageId;
  type: "math/math" | "math/chemistry";
  source: string;
  block?: boolean;
  width: number;
  height: number;
  verticalAlign: number;
  svg: string;
}

export type Embed = { id: EmbedId; pageId: PageId; textId?: TextId } & (
  | { source: string; fileId?: never }
  | { fileId: FileId; source?: never }
);

export interface FileAttachment {
  id: FileId;
  token: AccessToken;
  filename: string;
  contentType: string;
  size: number;
  lastModified: UtcTimestamp;
}

export interface ImageAttachment {
  id: ImageId;
  token: AccessToken;
  filename: string;
  width: number;
  height: number;
  format: string;
  thumbnailFormat?: string;
  size: number;
  lastModified: UtcTimestamp;
  url?: string;
  thumbnailSizes: Array<{ width: number; height: number; size: number }>;
}

export interface UnitSelection {
  openedPageIds?: PageId[];
  closedBookIds?: BookId[];
  focusedBookIds?: BookId[];
  shownBookIds?: BookId[];
  hiddenBookIds?: BookId[];
}
export interface Fragment extends UnitSelection { id: FragmentId; textId?: TextId; title?: string }
export interface Path { id: PathId; title?: string; stepIds: PathStepId[] }
export interface PathStep extends UnitSelection { id: PathStepId; audioId?: FileId }

/** Collections are omitted when there are no corresponding objects. */
export interface OrgPageObjects {
  units?: Unit[];
  links?: Link[];
  maths?: MathObject[];
  embeds?: Embed[];
  files?: FileAttachment[];
  images?: ImageAttachment[];
  fragments?: Fragment[];
  paths?: Path[];
  pathSteps?: PathStep[];
}
export type OrgPage = OrgPageMetadata & OrgPageObjects;
