import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { 
  UserManagement, 
  UpdateUserRoleRequest, 
  UpdateUserStatusRequest 
} from '../models/user-management.model';

/**
 * Service for admin user management operations
 */
@Injectable({
  providedIn: 'root'
})
export class UserManagementService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/users`;

  /**
   * List all users with pagination
   */
  listUsers(page: number = 0, size: number = 50): Observable<UserManagement[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<UserManagement[]>(this.apiUrl, { params });
  }

  /**
   * Update user role (SUPERADMIN only)
   */
  updateUserRole(userId: string, request: UpdateUserRoleRequest): Observable<UserManagement> {
    return this.http.put<UserManagement>(`${this.apiUrl}/${userId}/role`, request);
  }

  /**
   * Enable/disable user account (ADMIN+)
   */
  updateUserStatus(userId: string, request: UpdateUserStatusRequest): Observable<UserManagement> {
    return this.http.put<UserManagement>(`${this.apiUrl}/${userId}/status`, request);
  }

  /**
   * Delete user (SUPERADMIN only)
   */
  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`);
  }
}
