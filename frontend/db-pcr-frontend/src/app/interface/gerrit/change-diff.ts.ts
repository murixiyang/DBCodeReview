export interface ChangeDiff {
  oldPath: string;
  newPath: string;
  diff: string; // unified‐diff body from Gerrit
}
