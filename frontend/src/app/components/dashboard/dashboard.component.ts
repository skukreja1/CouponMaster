import { Component, OnInit } from '@angular/core';
import { ApiService, Campaign, CouponBatch } from '../../services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  campaigns: Campaign[] = [];
  batches: CouponBatch[] = [];
  loading = true;

  totalCampaigns = 0;
  totalBatches = 0;
  totalCoupons = 0;
  activeCoupons = 0;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    
    this.apiService.getCampaigns().subscribe({
      next: (campaigns) => {
        this.campaigns = campaigns;
        this.totalCampaigns = campaigns.length;
        this.totalCoupons = campaigns.reduce((sum, c) => sum + (c.totalCoupons || 0), 0);
      },
      error: () => this.loading = false
    });

    this.apiService.getBatches().subscribe({
      next: (batches) => {
        this.batches = batches;
        this.totalBatches = batches.length;
        this.activeCoupons = batches.reduce((sum, b) => sum + (b.activeCoupons || 0), 0);
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  exportAll(): void {
    this.apiService.exportAllCSV();
  }
}
