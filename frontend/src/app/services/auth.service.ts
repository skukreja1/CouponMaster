import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private loggedIn = new BehaviorSubject<boolean>(false);
  private credentials: string | null = null;

  constructor(private http: HttpClient) {
    const savedCredentials = localStorage.getItem('credentials');
    if (savedCredentials) {
      this.credentials = savedCredentials;
      this.loggedIn.next(true);
    }
  }

  login(username: string, password: string): Observable<any> {
    const credentials = btoa(`${username}:${password}`);
    const headers = new HttpHeaders({
      'Authorization': `Basic ${credentials}`
    });

    return this.http.post<any>(`${environment.apiUrl}/auth/login`, {}, { headers }).pipe(
      tap(response => {
        this.credentials = credentials;
        localStorage.setItem('credentials', credentials);
        this.loggedIn.next(true);
      }),
      catchError(error => {
        this.loggedIn.next(false);
        throw error;
      })
    );
  }

  logout(): void {
    this.credentials = null;
    localStorage.removeItem('credentials');
    this.loggedIn.next(false);
  }

  isLoggedIn(): boolean {
    return this.loggedIn.value;
  }

  getCredentials(): string | null {
    return this.credentials;
  }

  isLoggedIn$(): Observable<boolean> {
    return this.loggedIn.asObservable();
  }
}
