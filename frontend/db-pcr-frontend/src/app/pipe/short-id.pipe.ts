import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'shortId' })
export class ShortIdPipe implements PipeTransform {
  transform(value: string, length: number = 8): string {
    return value ? value.substring(0, length) : '';
  }
}
