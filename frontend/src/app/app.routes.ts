import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { guestGuard } from '@core/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('@features/auth/login/login.component')
      .then(m => m.LoginComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    loadComponent: () => import('@features/auth/register/register.component')
      .then(m => m.RegisterComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('@features/dashboard/dashboard.component')
      .then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'practice',
    loadComponent: () => import('@features/practice/practice.component')
      .then(m => m.PracticeComponent),
    canActivate: [authGuard]
  },
  {
    path: 'vocabulary',
    loadComponent: () => import('@features/vocabulary/vocabulary-list/vocabulary-list.component')
      .then(m => m.VocabularyListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'vocabulary/new',
    loadComponent: () => import('@features/vocabulary/vocabulary-form/vocabulary-form.component')
      .then(m => m.VocabularyFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'vocabulary/:id/edit',
    loadComponent: () => import('@features/vocabulary/vocabulary-form/vocabulary-form.component')
      .then(m => m.VocabularyFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'vocabulary/:id',
    loadComponent: () => import('@features/vocabulary/vocabulary-detail/vocabulary-detail.component')
      .then(m => m.VocabularyDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'sentence-builder',
    loadComponent: () => import('@features/sentence-builder/sentence-builder.component')
      .then(m => m.SentenceBuilderComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
