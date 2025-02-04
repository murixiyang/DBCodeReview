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
  original_content: string;
  changed_content: string;
  original_line_num: number | undefined;
  changed_line_num: number | undefined;
  highlight_original: boolean;
  highlight_changed: boolean;
}
