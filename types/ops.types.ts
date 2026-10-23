// JSON operations accepted by the API and the expanded operations returned by the server. See ../ops.md.

import type {
  AccessToken, BookId, BookRef, PageId, PageRef, UnitRef, LinkId,
  MathId, EmbedId, EmbedRef, FileId, ImageId, FragmentId, FragmentRef,
  PathId, PathStepId, TextId, UnitId, UnitProps, LinkProps, FileAttachment, ImageAttachment, SpectrumColor,
} from "./data.types";

export type TextContentType = "html" | "markdown" | "plain";

/** JSON representation of Hiccup; the server validates supported tags and attributes. */
export type HiccupPrimitive = null | boolean | number | string;
export type HiccupAttributes = {
  [key: string]: HiccupPrimitive | { [key: string]: HiccupPrimitive };
};
export type HiccupNode =
  | HiccupPrimitive
  | [tag: string, ...children: HiccupNode[]]
  | [tag: string, attributes: HiccupAttributes, ...children: HiccupNode[]];
export type HiccupContent = HiccupNode[];

/** If contentType is omitted, JSON content defaults to HTML. */
export type CreateContent =
  | { content?: string | null; contentType?: TextContentType }
  | { content?: HiccupContent | null; contentType: "hiccup" };

/** An update can replace or append content, but not both. */
export type UpdateContent =
  | { content?: string | null; appendedContent?: never; contentType?: TextContentType }
  | { content?: never; appendedContent?: string; contentType?: TextContentType }
  | { content?: HiccupContent | null; appendedContent?: never; contentType: "hiccup" }
  | { content?: never; appendedContent?: HiccupContent; contentType: "hiccup" };

export type OrgpageUpdateMetaOperation = ["orgpage/update-meta", {
  "title"?: string;
  "description"?: string;
  "tags"?: Array<string>;
  "color"?: SpectrumColor;
  "initFragments"?: {
    "default"?: FragmentRef | null;
    "smallScreen"?: FragmentRef | null;
    "screenshot"?: FragmentRef | null;
  };
}];

export type OrgpageRemoveImageOperation = ["orgpage/remove-image", {
  "id": ImageId;
}];

export type OrgpageRemoveFileOperation = ["orgpage/remove-file", {
  "id": FileId;
}];

export type OrgpageAddImageOperation = ["orgpage/add-image", {
  "id": ImageId;
  "token"?: AccessToken;
}];

export type OrgpageAddFileOperation = ["orgpage/add-file", {
  "id": FileId;
  "token"?: AccessToken;
}];

export type UnitCreateOperation = ["unit/create", ({
  "id"?: BookId;
  "type": "unit/book";
  "pos": [number, number];
  "childUnitIds": Array<PageRef>;
  "title"?: string;
  "props"?: Partial<UnitProps>;
  "textId"?: TextId;
}) | (CreateContent & {
  "id"?: PageId;
  "type": "unit/page";
  "parentId": BookRef;
  "textId"?: TextId;
}) | (CreateContent & {
  "id"?: BookId;
  "pageId"?: PageId;
  "pos": [number, number];
  "title"?: string;
  "props"?: Partial<UnitProps>;
  "textId"?: TextId;
  "pageTextId"?: TextId;
})];

export type UnitUpdateOperation = ["unit/update", UpdateContent & {
  "id"?: UnitRef;
  "title"?: string | null;
  "textId"?: TextId | null;
}];

export type UnitRemoveOperation = ["unit/remove", {
  "id": UnitRef;
}];

export type UnitMoveOperation = ["unit/move", {
  "id": BookRef;
  "pos": [number, number];
}];

export type UnitChangePropsOperation = ["unit/change-props", {
  "id": BookRef;
  "props": Partial<UnitProps>;
}];

export type UnitAddChildUnitOperation = ["unit/add-child-unit", {
  "id": BookRef;
  "childId": PageRef;
  "index"?: number;
}];

export type UnitRemoveChildUnitOperation = ["unit/remove-child-unit", {
  "id": BookRef;
  "childId": PageRef;
}];

export type UnitMoveChildUnitOperation = ["unit/move-child-unit", {
  "id": BookRef;
  "childId": PageRef;
  "newIndex": number;
}];

export type LinkCreateOperation = ["link/create", {
  "id"?: LinkId;
  "endpointIds": [BookRef, BookRef];
  "props"?: Partial<LinkProps>;
}];

export type LinkRemoveOperation = ["link/remove", {
  "id": LinkId;
}];

export type LinkStraightenOperation = ["link/straighten", {
  "id": LinkId;
}];

export type LinkFlipOperation = ["link/flip", {
  "id": LinkId;
}];

export type LinkChangePropsOperation = ["link/change-props", {
  "id": LinkId;
  "props": Partial<LinkProps>;
}];

export type MathCreateOperation = ["math/create", {
  "id"?: MathId;
  "pageId": PageRef;
  "source": string;
  "block"?: boolean | null;
  "type": "math/math" | "math/chemistry";
}];

export type MathUpdateOperation = ["math/update", {
  "id": MathId;
  "source"?: string;
  "block"?: boolean | null;
  "type"?: "math/math" | "math/chemistry";
}];

export type MathRemoveOperation = ["math/remove", {
  "id": MathId;
}];

export type MathSetMacrosOperation = ["math/set-macros", {
  "macros": string | null;
}];

export type EmbedCreateOperation = ["embed/create", {
  "id"?: EmbedId;
  "pageId": PageRef;
  "source"?: string;
  "fileId"?: FileId;
  "fileToken"?: AccessToken;
  "textId"?: TextId;
}];

export type EmbedUpdateOperation = ["embed/update", {
  "id": EmbedRef;
  "source"?: string;
  "fileId"?: FileId;
  "fileToken"?: AccessToken;
  "textId"?: TextId | null;
}];

export type EmbedRemoveOperation = ["embed/remove", {
  "id": EmbedRef;
}];

export type PathCreateOperation = ["path/create", {
  "id"?: PathId;
  "title"?: string;
}];

export type PathUpdateTitleOperation = ["path/update-title", {
  "id": PathId;
  "title": string | null;
}];

export type PathRemoveOperation = ["path/remove", {
  "id": PathId;
}];

export type PathAddStepOperation = ["path/add-step", {
  "id": PathId;
  "stepId"?: PathStepId;
  "index"?: number;
  "audioId"?: FileId;
  "openedPageIds"?: Array<PageRef>;
  "closedBookIds"?: Array<BookRef>;
  "focusedBookIds"?: Array<BookRef>;
  "shownBookIds"?: Array<BookRef>;
  "hiddenBookIds"?: Array<BookRef>;
}];

export type PathRemoveStepOperation = ["path/remove-step", {
  "id": PathStepId;
}];

export type PathUpdateUnitsOperation = ["path/update-units", {
  "id": PathStepId;
  "addOpenedPageIds"?: Array<PageRef>;
  "removeOpenedPageIds"?: Array<PageRef>;
  "addClosedBookIds"?: Array<BookRef>;
  "removeClosedBookIds"?: Array<BookRef>;
  "addFocusedBookIds"?: Array<BookRef>;
  "removeFocusedBookIds"?: Array<BookRef>;
  "addShownBookIds"?: Array<BookRef>;
  "removeShownBookIds"?: Array<BookRef>;
  "addHiddenBookIds"?: Array<BookRef>;
  "removeHiddenBookIds"?: Array<BookRef>;
}];

export type PathSetAudioOperation = ["path/set-audio", {
  "id": PathStepId;
  "audioId": FileId | null;
}];

export type PathMoveStepOperation = ["path/move-step", {
  "id": PathStepId;
  "newIndex": number;
}];

export type FragmentCreateOperation = ["fragment/create", {
  "id"?: FragmentId;
  "textId": TextId;
  "title"?: string;
  "openedPageIds"?: Array<PageRef>;
  "closedBookIds"?: Array<BookRef>;
  "focusedBookIds"?: Array<BookRef>;
  "shownBookIds"?: Array<BookRef>;
  "hiddenBookIds"?: Array<BookRef>;
}];

export type FragmentUpdateOperation = ["fragment/update", {
  "id": FragmentRef;
  "title"?: string | null;
  "textId"?: TextId;
}];

export type FragmentUpdateUnitsOperation = ["fragment/update-units", {
  "id": FragmentRef;
  "addOpenedPageIds"?: Array<PageRef>;
  "removeOpenedPageIds"?: Array<PageRef>;
  "addClosedBookIds"?: Array<BookRef>;
  "removeClosedBookIds"?: Array<BookRef>;
  "addFocusedBookIds"?: Array<BookRef>;
  "removeFocusedBookIds"?: Array<BookRef>;
  "addShownBookIds"?: Array<BookRef>;
  "removeShownBookIds"?: Array<BookRef>;
  "addHiddenBookIds"?: Array<BookRef>;
  "removeHiddenBookIds"?: Array<BookRef>;
}];

export type FragmentRemoveOperation = ["fragment/remove", {
  "id": FragmentRef;
}];

export type Operation =
  | OrgpageUpdateMetaOperation
  | OrgpageRemoveImageOperation
  | OrgpageRemoveFileOperation
  | OrgpageAddImageOperation
  | OrgpageAddFileOperation
  | UnitCreateOperation
  | UnitUpdateOperation
  | UnitRemoveOperation
  | UnitMoveOperation
  | UnitChangePropsOperation
  | UnitAddChildUnitOperation
  | UnitRemoveChildUnitOperation
  | UnitMoveChildUnitOperation
  | LinkCreateOperation
  | LinkRemoveOperation
  | LinkStraightenOperation
  | LinkFlipOperation
  | LinkChangePropsOperation
  | MathCreateOperation
  | MathUpdateOperation
  | MathRemoveOperation
  | MathSetMacrosOperation
  | EmbedCreateOperation
  | EmbedUpdateOperation
  | EmbedRemoveOperation
  | PathCreateOperation
  | PathUpdateTitleOperation
  | PathRemoveOperation
  | PathAddStepOperation
  | PathRemoveStepOperation
  | PathUpdateUnitsOperation
  | PathSetAudioOperation
  | PathMoveStepOperation
  | FragmentCreateOperation
  | FragmentUpdateOperation
  | FragmentUpdateUnitsOperation
  | FragmentRemoveOperation;

type BookCreate = Extract<UnitCreateOperation[1], { type: "unit/book" }>;
type PageCreate = Extract<UnitCreateOperation[1], { type: "unit/page" }>;

/** Shorthand book creation is expanded into separate book and page operations. */
type ReturnedUnitCreate = ["unit/create",
  | (Omit<BookCreate, "id"> & { id: BookId })
  | (Omit<PageCreate, "id" | "content" | "contentType" | "appendedContent"> & {
      id: PageId;
      content?: string;
    })
];

/** Content and appended content are returned as normalized HTML. */
type ReturnedUnitUpdate = ["unit/update",
  Omit<UnitUpdateOperation[1], "id" | "content" | "contentType" | "appendedContent"> & {
    id: UnitId;
    content?: string;
  }
];

type ExpandedOperationName = "unit/create" | "unit/update" | "orgpage/add-file" | "orgpage/add-image";
type CreatedIds = {
  "link/create": LinkId;
  "math/create": MathId;
  "embed/create": EmbedId;
  "path/create": PathId;
  "fragment/create": FragmentId;
};

/** Preserve the operation-name/parameters relationship when adding generated IDs. */
type NormalizeOperation<T> = T extends [infer Name, infer Params]
  ? Name extends keyof CreatedIds
    ? [Name, Omit<Params, "id"> & { id: CreatedIds[Name] }]
    : Name extends "path/add-step"
      ? [Name, Omit<Params, "stepId"> & { stepId: PathStepId }]
      : T
  : never;

/** The server expands operations and can add operations that were not in the request. */
export type ReturnedOperation =
  | NormalizeOperation<Exclude<Operation, [ExpandedOperationName, unknown]>>
  | ReturnedUnitCreate
  | ReturnedUnitUpdate
  | ["orgpage/add-file", Pick<FileAttachment, "id"> & Partial<Omit<FileAttachment, "id">>]
  | ["orgpage/add-image", Pick<ImageAttachment, "id"> & Partial<Omit<ImageAttachment, "id">>]
  | ["orgpage/rename-file", { id: FileId; filename: string }]
  | ["orgpage/rename-image", { id: ImageId; filename: string }];
