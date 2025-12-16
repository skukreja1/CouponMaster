import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService, Campaign } from '../../services/api.service';
import { CampaignDialogComponent } from './campaign-dialog.component';

@Component({
  selector: 'app-campaigns',
  standalone: false,
  templateUrl: './campaigns.component.html',
  styleUrl: './campaigns.component.scss'
})
export class CampaignsComponent implements OnInit {
  campaigns: Campaign[] = [];
  loading = true;
  displayedColumns = ['id', 'name', 'description', 'batches', 'coupons', 'status', 'actions'];

  constructor(
    private apiService: ApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCampaigns();
  }

  loadCampaigns(): void {
    this.loading = true;
    this.apiService.getCampaigns().subscribe({
      next: (campaigns) => {
        this.campaigns = campaigns;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load campaigns', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  openDialog(campaign?: Campaign): void {
    const dialogRef = this.dialog.open(CampaignDialogComponent, {
      width: '500px',
      data: campaign ? { ...campaign } : null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadCampaigns();
      }
    });
  }

  deleteCampaign(id: number): void {
    if (confirm('Are you sure you want to deactivate this campaign?')) {
      this.apiService.deleteCampaign(id).subscribe({
        next: () => {
          this.snackBar.open('Campaign deactivated', 'Close', { duration: 3000 });
          this.loadCampaigns();
        },
        error: () => {
          this.snackBar.open('Failed to deactivate campaign', 'Close', { duration: 3000 });
        }
      });
    }
  }

  reactivateCampaign(id: number): void {
    this.apiService.reactivateCampaign(id).subscribe({
      next: () => {
        this.snackBar.open('Campaign reactivated', 'Close', { duration: 3000 });
        this.loadCampaigns();
      },
      error: () => {
        this.snackBar.open('Failed to reactivate campaign', 'Close', { duration: 3000 });
      }
    });
  }
}
