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
  parent_content: string;
  revision_content: string;
  parent_line_num: number | undefined;
  revision_line_num: number | undefined;
  highlight_parent: boolean;
  highlight_revision: boolean;
}
