import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, CouponBatch, Campaign, ExportJob } from '../../services/api.service';
import { BatchDialogComponent } from './batch-dialog.component';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-batches',
  standalone: false,
  templateUrl: './batches.component.html',
  styleUrl: './batches.component.scss'
})
export class BatchesComponent implements OnInit, OnDestroy {
  batches: CouponBatch[] = [];
  campaigns: Campaign[] = [];
  loading = true;
  selectedCampaignId: number | null = null;
  displayedColumns = ['id', 'campaign', 'prefix', 'count', 'usages', 'status', 'actions'];

  exportJobs: Map<number, ExportJob> = new Map();
  pendingExports: Set<number> = new Set();
  private pollSubscription?: Subscription;

  constructor(
    private apiService: ApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCampaigns();
    this.loadBatches();
  }

  ngOnDestroy(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
    }
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
      width: '500px',
      data: { campaigns: this.campaigns }
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

  exportBatch(batchId: number): void {
    this.pendingExports.add(batchId);
    
    this.apiService.submitBatchExport(batchId).subscribe({
      next: (job) => {
        this.exportJobs.set(batchId, job);
        this.snackBar.open('Export started. File will be ready shortly.', 'Close', { duration: 3000 });
        this.startPolling(batchId, job.id);
      },
      error: () => {
        this.pendingExports.delete(batchId);
        this.snackBar.open('Failed to start export', 'Close', { duration: 3000 });
      }
    });
  }

  private startPolling(batchId: number, jobId: number): void {
    const poll = interval(2000).subscribe(() => {
      this.apiService.getExportJobStatus(jobId).subscribe({
        next: (job) => {
          this.exportJobs.set(batchId, job);
          
          if (job.status === 'COMPLETED') {
            this.pendingExports.delete(batchId);
            poll.unsubscribe();
            this.snackBar.open('Export ready! Click download to get your file.', 'Close', { duration: 5000 });
          } else if (job.status === 'FAILED') {
            this.pendingExports.delete(batchId);
            poll.unsubscribe();
            this.snackBar.open('Export failed: ' + (job.errorMessage || 'Unknown error'), 'Close', { duration: 5000 });
          }
        }
      });
    });
  }

  getExportJob(batchId: number): ExportJob | undefined {
    return this.exportJobs.get(batchId);
  }

  isExporting(batchId: number): boolean {
    return this.pendingExports.has(batchId);
  }

  downloadExport(batchId: number): void {
    const job = this.exportJobs.get(batchId);
    if (job && job.status === 'COMPLETED') {
      this.apiService.downloadExport(job.id);
    }
  }

  clearExportJob(batchId: number): void {
    this.exportJobs.delete(batchId);
  }
}
