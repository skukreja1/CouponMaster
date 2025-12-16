import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Campaign {
  id?: number;
  name: string;
  description?: string;
  posCode?: string;
  atgCode?: string;
  startDate: string;
  expiryDate: string;
  createdAt?: string;
  updatedAt?: string;
  active?: boolean;
  batchCount?: number;
  totalCoupons?: number;
}

export interface CouponBatch {
  id?: number;
  campaignId: number;
  campaignName?: string;
  userPrefix: string;
  prefix?: string;
  couponCount: number;
  maxUsages: number;
  createdAt?: string;
  updatedAt?: string;
  active?: boolean;
  activeCoupons?: number;
  usedCoupons?: number;
  expiredCoupons?: number;
}

export interface BatchUpdate {
  maxUsages?: number;
}

export interface Coupon {
  id: number;
  batchId: number;
  code: string;
  status: string;
  usageCount: number;
  maxUsages: number;
  startDate: string;
  expiryDate: string;
  campaignName: string;
  posCode?: string;
  atgCode?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface CouponSearch {
  code?: string;
  prefix?: string;
  status?: string;
  campaignId?: number;
  batchId?: number;
  createdFrom?: string;
  createdTo?: string;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCampaigns(): Observable<Campaign[]> {
    return this.http.get<Campaign[]>(`${this.apiUrl}/campaigns`);
  }

  getActiveCampaigns(): Observable<Campaign[]> {
    return this.http.get<Campaign[]>(`${this.apiUrl}/campaigns/active`);
  }

  getCampaign(id: number): Observable<Campaign> {
    return this.http.get<Campaign>(`${this.apiUrl}/campaigns/${id}`);
  }

  createCampaign(campaign: Campaign): Observable<Campaign> {
    return this.http.post<Campaign>(`${this.apiUrl}/campaigns`, campaign);
  }

  updateCampaign(id: number, campaign: Campaign): Observable<Campaign> {
    return this.http.put<Campaign>(`${this.apiUrl}/campaigns/${id}`, campaign);
  }

  deleteCampaign(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/campaigns/${id}`);
  }

  reactivateCampaign(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/campaigns/${id}/reactivate`, {});
  }

  getBatches(): Observable<CouponBatch[]> {
    return this.http.get<CouponBatch[]>(`${this.apiUrl}/batches`);
  }

  getBatchesByCampaign(campaignId: number): Observable<CouponBatch[]> {
    return this.http.get<CouponBatch[]>(`${this.apiUrl}/batches/campaign/${campaignId}`);
  }

  getBatch(id: number): Observable<CouponBatch> {
    return this.http.get<CouponBatch>(`${this.apiUrl}/batches/${id}`);
  }

  createBatch(batch: CouponBatch): Observable<CouponBatch> {
    return this.http.post<CouponBatch>(`${this.apiUrl}/batches`, batch);
  }

  updateBatch(id: number, update: BatchUpdate): Observable<CouponBatch> {
    return this.http.put<CouponBatch>(`${this.apiUrl}/batches/${id}`, update);
  }

  deleteBatch(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/batches/${id}`);
  }

  reactivateBatch(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/batches/${id}/reactivate`, {});
  }

  searchCoupons(search: CouponSearch): Observable<PagedResponse<Coupon>> {
    let params = new HttpParams();
    if (search.code) params = params.set('code', search.code);
    if (search.prefix) params = params.set('prefix', search.prefix);
    if (search.status) params = params.set('status', search.status);
    if (search.campaignId) params = params.set('campaignId', search.campaignId.toString());
    if (search.batchId) params = params.set('batchId', search.batchId.toString());
    if (search.createdFrom) params = params.set('createdFrom', search.createdFrom);
    if (search.createdTo) params = params.set('createdTo', search.createdTo);
    if (search.page !== undefined) params = params.set('page', search.page.toString());
    if (search.size !== undefined) params = params.set('size', search.size.toString());

    return this.http.get<PagedResponse<Coupon>>(`${this.apiUrl}/coupons/search`, { params });
  }

  getCouponByCode(code: string): Observable<Coupon> {
    return this.http.get<Coupon>(`${this.apiUrl}/coupons/${code}`);
  }

  exportBatchCSV(batchId: number): void {
    const credentials = localStorage.getItem('credentials');
    const url = `${this.apiUrl}/export/batch/${batchId}`;
    
    fetch(url, {
      headers: {
        'Authorization': `Basic ${credentials}`
      }
    })
    .then(response => response.blob())
    .then(blob => {
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = `batch_${batchId}_coupons.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(downloadUrl);
      a.remove();
    });
  }

  exportAllCSV(): void {
    const credentials = localStorage.getItem('credentials');
    const url = `${this.apiUrl}/export/all`;
    
    fetch(url, {
      headers: {
        'Authorization': `Basic ${credentials}`
      }
    })
    .then(response => response.blob())
    .then(blob => {
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = 'all_coupons.csv';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(downloadUrl);
      a.remove();
    });
  }
}
