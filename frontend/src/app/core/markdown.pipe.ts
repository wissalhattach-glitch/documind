import { Pipe, PipeTransform } from '@angular/core';
import { marked } from 'marked';

// transforme le markdown (gras, listes, titres…) en HTML ; Angular nettoie le résultat dans [innerHTML]
@Pipe({ name: 'markdown' })
export class MarkdownPipe implements PipeTransform {
  transform(texte: string | null | undefined): string {
    return marked.parse(texte ?? '', { async: false, breaks: true }) as string;
  }
}