export interface DiffInfo {
  meta_a: DiffFileMetaInfo;
  meta_b: DiffFileMetaInfo;
  change_type: string;
  intraline_status: string;
  diff_header: string[];
  content: DiffContentModel;
  web_links: string[];
  edit_web_links: string[];
  binary: boolean;
}

export interface DiffFileMetaInfo {
  name: string;
  content_type: string;
  lines: number;
}

export interface DiffContentModel {
  a: string;
  b: string;
  ab: string;
  edit_a: DiffIntralineInfoModel;
  edit_b: DiffIntralineInfoModel;
  due_to_move: boolean;
  skip: number;
  common: boolean;
}

export interface DiffIntralineInfoModel {
  skip_length: number[];
  edit_length: number[];
}
