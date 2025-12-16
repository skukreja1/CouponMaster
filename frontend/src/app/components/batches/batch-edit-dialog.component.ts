import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, CouponBatch, BatchUpdate } from '../../services/api.service';

@Component({
  selector: 'app-batch-edit-dialog',
  standalone: false,
  templateUrl: './batch-edit-dialog.component.html'
})
export class BatchEditDialogComponent {
  form: FormGroup;
  loading = false;
  minDate = new Date();

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<BatchEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CouponBatch
  ) {
    this.form = this.fb.group({
      posCode: [data.posCode || '', Validators.maxLength(50)],
      atgCode: [data.atgCode || '', Validators.maxLength(50)],
      startDate: [data.startDate ? new Date(data.startDate) : null],
      expiryDate: [data.expiryDate ? new Date(data.expiryDate) : null],
      maxUsages: [data.maxUsages, [Validators.required, Validators.min(1)]]
    });
  }

  formatDate(date: Date | null): string | undefined {
    if (!date) return undefined;
    return date.toISOString().split('T')[0];
  }

  save(): void {
    if (this.form.invalid) return;

    this.loading = true;
    const formValue = this.form.value;
    
    const update: BatchUpdate = {
      posCode: formValue.posCode || undefined,
      atgCode: formValue.atgCode || undefined,
      startDate: formValue.startDate ? this.formatDate(formValue.startDate) : undefined,
      expiryDate: formValue.expiryDate ? this.formatDate(formValue.expiryDate) : undefined,
      maxUsages: formValue.maxUsages
    };

    this.apiService.updateBatch(this.data.id!, update).subscribe({
      next: () => {
        this.snackBar.open('Batch updated successfully', 'Close', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message || 'Failed to update batch', 'Close', { duration: 3000 });
      }
    });
  }
}
