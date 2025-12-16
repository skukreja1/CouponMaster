import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, Campaign } from '../../services/api.service';

@Component({
  selector: 'app-campaign-dialog',
  standalone: false,
  templateUrl: './campaign-dialog.component.html'
})
export class CampaignDialogComponent {
  form: FormGroup;
  isEdit: boolean;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<CampaignDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Campaign | null
  ) {
    this.isEdit = !!data;
    this.form = this.fb.group({
      name: [data?.name || '', [Validators.required, Validators.maxLength(255)]],
      description: [data?.description || '', Validators.maxLength(2000)]
    });
  }

  save(): void {
    if (this.form.invalid) return;

    this.loading = true;
    const campaign: Campaign = this.form.value;

    const request = this.isEdit
      ? this.apiService.updateCampaign(this.data!.id!, campaign)
      : this.apiService.createCampaign(campaign);

    request.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEdit ? 'Campaign updated' : 'Campaign created',
          'Close',
          { duration: 3000 }
        );
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message || 'Operation failed', 'Close', { duration: 3000 });
      }
    });
  }
}
