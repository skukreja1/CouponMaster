import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, CouponBatch, Campaign } from '../../services/api.service';
import { BatchDialogComponent } from './batch-dialog.component';
import { BatchEditDialogComponent } from './batch-edit-dialog.component';

@Component({
  selector: 'app-batches',
  standalone: false,
  templateUrl: './batches.component.html',
  styleUrl: './batches.component.scss'
})
export class BatchesComponent implements OnInit {
  batches: CouponBatch[] = [];
  campaigns: Campaign[] = [];
  loading = true;
  selectedCampaignId: number | null = null;
  displayedColumns = ['id', 'campaign', 'prefix', 'count', 'codes', 'dates', 'usages', 'status', 'actions'];

  constructor(
    private apiService: ApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCampaigns();
    this.loadBatches();
  }

  loadCampaigns(): void {
    this.apiService.getActiveCampaigns().subscribe({
      next: (campaigns) => this.campaigns = campaigns
    });
  }

  loadBatches(): void {
    this.loading = true;
    const request = this.selectedCampaignId
      ? this.apiService.getBatchesByCampaign(this.selectedCampaignId)
      : this.apiService.getBatches();

    request.subscribe({
      next: (batches) => {
        this.batches = batches;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load batches', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onCampaignFilterChange(): void {
    this.loadBatches();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(BatchDialogComponent, {
      width: '600px',
      data: { campaigns: this.campaigns }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadBatches();
      }
    });
  }

  openEditDialog(batch: CouponBatch): void {
    const dialogRef = this.dialog.open(BatchEditDialogComponent, {
      width: '500px',
      data: batch
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadBatches();
      }
    });
  }

  deleteBatch(id: number): void {
    if (confirm('Are you sure you want to deactivate this batch? All coupons will be set to INACTIVE.')) {
      this.apiService.deleteBatch(id).subscribe({
        next: () => {
          this.snackBar.open('Batch deactivated', 'Close', { duration: 3000 });
          this.loadBatches();
        },
        error: () => {
          this.snackBar.open('Failed to deactivate batch', 'Close', { duration: 3000 });
        }
      });
    }
  }

  reactivateBatch(id: number): void {
    this.apiService.reactivateBatch(id).subscribe({
      next: () => {
        this.snackBar.open('Batch reactivated', 'Close', { duration: 3000 });
        this.loadBatches();
      },
      error: () => {
        this.snackBar.open('Failed to reactivate batch', 'Close', { duration: 3000 });
      }
    });
  }

  exportBatch(id: number): void {
    this.apiService.exportBatchCSV(id);
  }
}
