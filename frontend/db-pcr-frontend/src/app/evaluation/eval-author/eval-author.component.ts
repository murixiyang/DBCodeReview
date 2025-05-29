import { CommonModule, NgFor, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { EvaluationService } from '../../http/evaluation.service';
import { Router } from '@angular/router';
import { FilePayload } from '../../interface/eval/file-paylod';

@Component({
  selector: 'app-eval-author',
  imports: [FormsModule, NgFor, ReactiveFormsModule, CommonModule, NgIf],
  templateUrl: './eval-author.component.html',
  styleUrl: './eval-author.component.css',
})
export class EvalAuthorComponent {
  languages = ['Java', 'Python', 'Other'];
  fileForm: FormGroup;
  locked = false;

  constructor(
    private fb: FormBuilder,
    private evalSvc: EvaluationService,
    private router: Router
  ) {
    this.fileForm = this.fb.group({
      language: ['Java', Validators.required],
      otherLang: [''],
      files: this.fb.array([]),
    });
  }

  ngOnInit(): void {
    // initialize with one blank file
    this.addFile();
  }

  get files(): FormArray {
    return this.fileForm.get('files') as FormArray;
  }

  downloadTemplate(): void {
    const lang = this.fileForm.get('language')?.value;
    if (lang !== 'Java' && lang !== 'Python') return;
    this.evalSvc.getTemplateDownloaded(lang).subscribe(
      (blob) => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = lang.toLowerCase() + '.zip';
        a.click();
        URL.revokeObjectURL(a.href);
      },
      (err) => {
        console.error(err);
        alert('Template download failed');
      }
    );
  }

  onPublish(): void {
    if (this.fileForm.invalid || this.files.length === 0) {
      this.fileForm.markAllAsTouched();
      return;
    }

    const confirmed = window.confirm(
      'Once you publish, you will not be able to modify your code. Proceed?'
    );
    if (!confirmed) {
      return;
    }

    this.locked = true;

    // Prepare payload
    const language =
      this.fileForm.value.language === 'Other'
        ? this.fileForm.value.otherLang
        : this.fileForm.value.language;

    // In onPublish(), before calling the service:
    const rawFiles = this.fileForm.value.files; // [{name, content}, …]
    const files: FilePayload[] = rawFiles.map((f: any) => ({
      name: f.name,
      content: f.content,
    }));

    this.evalSvc.publishToGerrit(language, files).subscribe({
      next: (res) => {
        console.log('Submitted files', res);
        // Navigate to review after a short delay or immediately
        this.router.navigate(['/eval/review-round']);
      },
      error: (err) => {
        console.error('Submission failed', err);
        alert('Could not publish your code. Please try again.');
        this.locked = false;
      },
    });
  }

  addFile(): void {
    if (this.locked) return;
    const fileGroup = this.fb.group({
      name: ['', Validators.required],
      content: ['', Validators.required],
    });
    this.files.push(fileGroup);
  }

  removeFile(index: number): void {
    if (this.locked) return;
    this.files.removeAt(index);
  }

  onFileSelect(event: Event, index: number): void {
    const target = event.target as HTMLInputElement;
    if (!target.files?.length) return;
    const file = target.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      const content = reader.result as string;
      this.files.at(index).patchValue({ name: file.name, content });
    };
    reader.readAsText(file);
  }

  skipAuthor(): void {
    // navigate to review step
    console.log('Authoring skipped');
    this.router.navigate(['/eval/review-round']);
  }
}
