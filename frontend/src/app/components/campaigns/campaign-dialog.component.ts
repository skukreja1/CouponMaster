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
  minDate = new Date();

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
      description: [data?.description || '', Validators.maxLength(2000)],
      posCode: [data?.posCode || '', [Validators.required, Validators.maxLength(50)]],
      atgCode: [data?.atgCode || '', [Validators.required, Validators.maxLength(50)]],
      userPrefix: [data?.userPrefix || '', [Validators.required, Validators.minLength(4), Validators.maxLength(4), Validators.pattern(/^[A-Z0-9]{4}$/)]],
      maxUsages: [data?.maxUsages || 1, [Validators.required, Validators.min(1)]],
      startDate: [data?.startDate ? new Date(data.startDate) : '', Validators.required],
      expiryDate: [data?.expiryDate ? new Date(data.expiryDate) : '', Validators.required]
    });
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  save(): void {
    if (this.form.invalid) return;

    this.loading = true;
    const formValue = this.form.value;

    const campaign: Campaign = {
      name: formValue.name,
      description: formValue.description || null,
      posCode: formValue.posCode || null,
      atgCode: formValue.atgCode || null,
      userPrefix: formValue.userPrefix.toUpperCase(),
      maxUsages: formValue.maxUsages,
      startDate: this.formatDate(formValue.startDate),
      expiryDate: this.formatDate(formValue.expiryDate)
    };

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
