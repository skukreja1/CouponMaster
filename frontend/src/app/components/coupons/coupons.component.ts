import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { ApiService, Coupon, Campaign, CouponSearch, PagedResponse } from '../../services/api.service';

@Component({
  selector: 'app-coupons',
  standalone: false,
  templateUrl: './coupons.component.html',
  styleUrl: './coupons.component.scss'
})
export class CouponsComponent implements OnInit {
  coupons: Coupon[] = [];
  campaigns: Campaign[] = [];
  loading = true;
  filterForm: FormGroup;
  
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  
  displayedColumns = ['code', 'campaign', 'status', 'usages', 'dates', 'codes', 'created'];
  statusOptions = ['ACTIVE', 'INACTIVE', 'EXPIRED', 'MAX_USED'];

  constructor(
    private apiService: ApiService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.filterForm = this.fb.group({
      code: [''],
      prefix: [''],
      status: [''],
      campaignId: [''],
      createdFrom: [''],
      createdTo: ['']
    });
  }

  ngOnInit(): void {
    this.loadCampaigns();
    this.searchCoupons();
  }

  loadCampaigns(): void {
    this.apiService.getCampaigns().subscribe({
      next: (campaigns) => this.campaigns = campaigns
    });
  }

  searchCoupons(): void {
    this.loading = true;
    const formValue = this.filterForm.value;

    const search: CouponSearch = {
      code: formValue.code || undefined,
      prefix: formValue.prefix || undefined,
      status: formValue.status || undefined,
      campaignId: formValue.campaignId || undefined,
      createdFrom: formValue.createdFrom ? this.formatDate(formValue.createdFrom) : undefined,
      createdTo: formValue.createdTo ? this.formatDate(formValue.createdTo) : undefined,
      page: this.pageIndex,
      size: this.pageSize
    };

    this.apiService.searchCoupons(search).subscribe({
      next: (response: PagedResponse<Coupon>) => {
        this.coupons = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Failed to search coupons', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  onSearch(): void {
    this.pageIndex = 0;
    this.searchCoupons();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.searchCoupons();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.searchCoupons();
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'status-active';
      case 'INACTIVE': return 'status-inactive';
      case 'EXPIRED': return 'status-expired';
      case 'MAX_USED': return 'status-max-used';
      default: return '';
    }
  }
}
