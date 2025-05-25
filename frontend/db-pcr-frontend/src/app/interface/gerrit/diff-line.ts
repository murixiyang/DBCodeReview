export interface DiffLine {
  oldNumber: number | null;
  newNumber: number | null;
  oldText: string;
  newText: string;
  type: 'equal' | 'insert' | 'delete';
}
