import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService, CsvRowError, CsvUploadResponse } from '@core/services/api.service';
import { NotificationService } from '@core/services/notification.service';

/**
 * Component for importing vocabulary items from CSV file.
 * Supports file upload, preview, and detailed error reporting.
 */
@Component({
  selector: 'app-vocabulary-import',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './vocabulary-import.component.html',
  styleUrl: './vocabulary-import.component.css'
})
export class VocabularyImportComponent {
  selectedFile: File | null = null;
  csvPreview: string[][] = [];
  isDragging = false;
  isUploading = false;
  uploadResult: CsvUploadResponse | null = null;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  /**
   * Handle file selection from input
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
    }
  }

  /**
   * Handle drag over event
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  /**
   * Handle drag leave event
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  /**
   * Handle file drop
   */
  onDropFile(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.handleFile(event.dataTransfer.files[0]);
    }
  }

  /**
   * Process the selected file
   */
  private handleFile(file: File): void {
    // Validate file type
    if (!file.name.endsWith('.csv')) {
      this.notificationService.error('Please select a CSV file');
      return;
    }

    // Validate file size (5MB)
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      this.notificationService.error('File too large. Maximum size is 5MB');
      return;
    }

    this.selectedFile = file;
    this.uploadResult = null;
    this.loadPreview(file);
  }

  /**
   * Load CSV preview (first 5 rows)
   */
  private loadPreview(file: File): void {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result as string;
      const lines = text.split('\n').slice(0, 6); // Header + 5 rows
      this.csvPreview = lines.map(line => line.split(','));
    };
    reader.readAsText(file);
  }

  /**
   * Upload the CSV file
   */
  uploadCsv(): void {
    if (!this.selectedFile) {
      return;
    }

    this.isUploading = true;
    this.uploadResult = null;

    this.apiService.importCsvVocabulary(this.selectedFile).subscribe({
      next: (result) => {
        this.isUploading = false;
        this.uploadResult = result;

        if (result.errorCount === 0) {
          this.notificationService.success(
            `Successfully imported ${result.successCount} vocabulary items`
          );
        } else {
          this.notificationService.warning(
            `Imported ${result.successCount} items with ${result.errorCount} errors`
          );
        }
      },
      error: (error) => {
        this.isUploading = false;
        this.notificationService.error(
          error.error?.message || 'Failed to upload CSV file'
        );
      }
    });
  }

  /**
   * Download CSV template
   */
  downloadTemplate(): void {
    const link = document.createElement('a');
    link.href = '/assets/vocabulary-template.csv';
    link.download = 'vocabulary-template.csv';
    link.click();
  }

  /**
   * Clear selection
   */
  clearFile(): void {
    this.selectedFile = null;
    this.csvPreview = [];
    this.uploadResult = null;
  }

  /**
   * Navigate to vocabulary list
   */
  viewVocabulary(): void {
    this.router.navigate(['/vocabulary']);
  }
}
