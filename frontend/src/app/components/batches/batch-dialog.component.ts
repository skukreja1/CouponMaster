import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, Campaign, CouponBatch } from '../../services/api.service';

@Component({
  selector: 'app-batch-dialog',
  standalone: false,
  templateUrl: './batch-dialog.component.html'
})
export class BatchDialogComponent {
  form: FormGroup;
  campaigns: Campaign[];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<BatchDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { campaigns: Campaign[] }
  ) {
    this.campaigns = data.campaigns;
    
    this.form = this.fb.group({
      campaignId: ['', Validators.required],
      userPrefix: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4), Validators.pattern(/^[A-Z0-9]{4}$/)]],
      couponCount: [1000, [Validators.required, Validators.min(1), Validators.max(3000000)]],
      maxUsages: [1, [Validators.required, Validators.min(1)]]
    });
  }

  save(): void {
    if (this.form.invalid) return;

    this.loading = true;
    const formValue = this.form.value;
    
    const batch: CouponBatch = {
      campaignId: formValue.campaignId,
      userPrefix: formValue.userPrefix.toUpperCase(),
      couponCount: formValue.couponCount,
      maxUsages: formValue.maxUsages
    };

    this.apiService.createBatch(batch).subscribe({
      next: () => {
        this.snackBar.open('Batch created successfully! Coupons are being generated.', 'Close', { duration: 5000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message || 'Failed to create batch', 'Close', { duration: 3000 });
      }
    });
  }
}
