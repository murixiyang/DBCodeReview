export interface DiffInfo {
  meta_a: DiffFileMetaInfo;
  meta_b: DiffFileMetaInfo;
  change_type: string;
  intraline_status: string;
  diff_header: string[];
  content: DiffContent[];
}

export interface DiffFileMetaInfo {
  name: string;
  content_type: string;
  lines: number;
}

export interface DiffContent {
  a: string[];
  b: string[];
  ab: string[];
}

export interface FrontDiffLine {
  originalContent: string;
  changedContent: string;
  originalLineNumber: number | undefined;
  changedLineNumber: number | undefined;
  highlightOriginal: boolean;
  highlightChanged: boolean;
}
