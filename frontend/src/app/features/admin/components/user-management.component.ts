import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '@core/services/auth.service';
import { Role } from '@core/models/role.enum';
import { User } from '@core/models/auth.model';
import { UserManagementService } from '../services/user-management.service';
import { UserManagement } from '../models/user-management.model';
import { NotificationService } from '@core/services/notification.service';
import { RoleAccessDirective } from '@app/shared/directives/role-access.directive';
import { LayoutComponent } from '@app/shared/components/layout/layout.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RoleAccessDirective, LayoutComponent],
  template: `
    <app-layout>
    <div class="user-management">
      <header class="page-header">
        <h1>👑 User Management</h1>
        <p>Manage user roles and account status</p>
      </header>

      @if (loading()) {
        <div class="loading">Loading users...</div>
      } @else {
        <div class="users-table">
          <table>
            <thead>
              <tr>
                <th>User</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Content</th>
                <th>Joined</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (user of users(); track user.id) {
                <tr>
                  <td>
                    <div class="user-cell">
                      <strong>{{ user.displayName || user.firstName + ' ' + user.lastName }}</strong>
                    </div>
                  </td>
                  <td>{{ user.email }}</td>
                  <td>
                    @if (editingRole() === user.id) {
                      <select [(ngModel)]="selectedRole" class="role-select">
                        <option [value]="Role.USER">USER</option>
                        <option [value]="Role.ADMIN">ADMIN</option>
                        <option [value]="Role.SUPERADMIN">SUPERADMIN</option>
                      </select>
                    } @else {
                      <span [class]="'role-badge role-' + user.role.toLowerCase()">
                        {{ user.role }}
                      </span>
                    }
                  </td>
                  <td>
                    <span [class]="user.enabled ? 'status-active' : 'status-disabled'">
                      {{ user.enabled ? 'Active' : 'Disabled' }}
                    </span>
                  </td>
                  <td>
                    <div class="content-stats">
                      <span>📚 {{ user.vocabularyCount || 0 }}</span>
                      <span>💬 {{ user.sentencePatternCount || 0 }}</span>
                    </div>
                  </td>
                  <td>{{ formatDate(user.createdAt) }}</td>
                  <td>
                    <div class="actions">
                      <!-- Role Edit Actions (SUPERADMIN only) -->
                      @if (editingRole() === user.id) {
                        <button 
                          class="btn btn-sm btn-primary" 
                          (click)="saveRole(user)"
                          [disabled]="auth.currentUser()?.id === user.id">
                          Save
                        </button>
                        <button class="btn btn-sm btn-secondary" (click)="cancelEdit()">
                          Cancel
                        </button>
                      } @else {
                        <button 
                          *appRoleAccess="Role.SUPERADMIN"
                          class="btn btn-sm btn-secondary" 
                          (click)="startEditRole(user)"
                          [disabled]="auth.currentUser()?.id === user.id">
                          Change Role
                        </button>
                      }

                      <!-- Enable/Disable (ADMIN+) -->
                      <button 
                        class="btn btn-sm"
                        [class.btn-warning]="user.enabled"
                        [class.btn-success]="!user.enabled"
                        (click)="toggleStatus(user)"
                        [disabled]="auth.currentUser()?.id === user.id">
                        {{ user.enabled ? 'Disable' : 'Enable' }}
                      </button>

                      <!-- Delete (SUPERADMIN only) -->
                      <button 
                        *appRoleAccess="Role.SUPERADMIN"
                        class="btn btn-sm btn-danger" 
                        (click)="deleteUser(user)"
                        [disabled]="auth.currentUser()?.id === user.id">
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
    </app-layout>
  `,
  styles: [`
    .user-management {
      max-width: 1400px;
      margin: 0 auto;
    }

    .page-header {
      margin-bottom: 2rem;
      
      h1 {
        font-size: 2rem;
        margin-bottom: 0.5rem;
      }
      
      p {
        color: var(--text-secondary);
      }
    }

    .loading {
      text-align: center;
      padding: 3rem;
      color: var(--text-secondary);
    }

    .users-table {
      background: var(--bg-primary);
      border-radius: var(--radius);
      border: 1px solid var(--border);
      overflow: hidden;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    thead {
      background: var(--bg-secondary);
      
      th {
        text-align: left;
        padding: 1rem;
        font-weight: 600;
        color: var(--text-secondary);
        font-size: 0.875rem;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }

    tbody {
      tr {
        border-top: 1px solid var(--border);
        transition: background 0.2s;
        
        &:hover {
          background: var(--bg-secondary);
        }
      }
      
      td {
        padding: 1rem;
      }
    }

    .user-cell {
      strong {
        display: block;
        margin-bottom: 0.25rem;
      }
      
      span {
        font-size: 0.875rem;
        color: var(--text-secondary);
      }
    }

    .role-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }
    
    .role-user { background: #3b82f6; color: white; }
    .role-admin { background: #8b5cf6; color: white; }
    .role-superadmin { background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%); color: white; }

    .status-active {
      color: var(--success);
      font-weight: 500;
    }
    
    .status-disabled {
      color: var(--danger);
      font-weight: 500;
    }

    .content-stats {
      display: flex;
      gap: 1rem;
      font-size: 0.875rem;
    }

    .actions {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .btn {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: var(--radius);
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s;
      
      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
    
    .btn-sm {
      padding: 0.375rem 0.75rem;
      font-size: 0.875rem;
    }

    .btn-primary {
      background: var(--primary);
      color: white;
      &:hover:not(:disabled) { opacity: 0.9; }
    }

    .btn-secondary {
      background: var(--bg-secondary);
      color: var(--text-primary);
      border: 1px solid var(--border);
      &:hover:not(:disabled) { background: var(--bg-tertiary); }
    }

    .btn-success {
      background: var(--success);
      color: white;
      &:hover:not(:disabled) { opacity: 0.9; }
    }

    .btn-warning {
      background: var(--warning);
      color: white;
      &:hover:not(:disabled) { opacity: 0.9; }
    }

    .btn-danger {
      background: var(--danger);
      color: white;
      &:hover:not(:disabled) { opacity: 0.9; }
    }

    .role-select {
      padding: 0.375rem 0.75rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      background: var(--bg-primary);
      color: var(--text-primary);
    }
  `]
})
export class UserManagementComponent implements OnInit {
  private userManagementService = inject(UserManagementService);
  private notification = inject(NotificationService);
  auth = inject(AuthService);

  users = signal<UserManagement[]>([]);
  loading = signal(true);
  editingRole = signal<string | null>(null);
  selectedRole: Role = Role.USER;
  Role = Role;

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading.set(true);
    this.userManagementService.listUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: (error) => {
        this.notification.error('Failed to load users');
        this.loading.set(false);
      }
    });
  }

  startEditRole(user: UserManagement) {
    this.editingRole.set(user.id);
    this.selectedRole = user.role;
  }

  saveRole(user: UserManagement) {
    this.userManagementService.updateUserRole(user.id, { role: this.selectedRole })
      .subscribe({
        next: (updated) => {
          // Update local state
          this.users.update(users => 
            users.map(u => u.id === user.id ? updated : u)
          );
          this.notification.success(`Role updated to ${this.selectedRole}`);
          this.editingRole.set(null);
        },
        error: () => this.notification.error('Failed to update role')
      });
  }

  cancelEdit() {
    this.editingRole.set(null);
  }

  toggleStatus(user: UserManagement) {
    const newStatus = !user.enabled;
    this.userManagementService.updateUserStatus(user.id, { enabled: newStatus })
      .subscribe({
        next: (updated) => {
          this.users.update(users => 
            users.map(u => u.id === user.id ? updated : u)
          );
          this.notification.success(`User ${newStatus ? 'enabled' : 'disabled'}`);
        },
        error: () => this.notification.error('Failed to update status')
      });
  }

  deleteUser(user: UserManagement) {
    if (!confirm(`Are you sure you want to delete ${user.email}? This cannot be undone.`)) {
      return;
    }

    this.userManagementService.deleteUser(user.id).subscribe({
      next: () => {
        this.users.update(users => users.filter(u => u.id !== user.id));
        this.notification.success('User deleted');
      },
      error: () => this.notification.error('Failed to delete user')
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
