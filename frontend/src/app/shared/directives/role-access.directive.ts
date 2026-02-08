import { Directive, Input, TemplateRef, ViewContainerRef, effect, inject } from '@angular/core';
import { AuthService } from '@core/services/auth.service';
import { Role } from '@core/models/role.enum';

/**
 * Structural directive to conditionally render content based on user role.
 * 
 * Usage:
 * ```html
 * <div *appRoleAccess="'ADMIN'">Admin only content</div>
 * <div *appRoleAccess="['ADMIN', 'SUPERADMIN']">ADMIN or SUPERADMIN content</div>
 * ```
 */
@Directive({
  selector: '[appRoleAccess]',
  standalone: true
})
export class RoleAccessDirective {
  private authService = inject(AuthService);
  private templateRef = inject(TemplateRef<any>);
  private viewContainer = inject(ViewContainerRef);
  
  private allowedRoles: Role[] = [];

  @Input() set appRoleAccess(roles: Role | Role[]) {
    this.allowedRoles = Array.isArray(roles) ? roles : [roles];
    this.updateView();
  }

  constructor() {
    // React to role changes
    effect(() => {
      const currentRole = this.authService.userRole();
      this.updateView();
    });
  }

  private updateView(): void {
    const currentRole = this.authService.getUserRole();
    const hasAccess = this.allowedRoles.includes(currentRole);

    this.viewContainer.clear();
    if (hasAccess) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }
}
