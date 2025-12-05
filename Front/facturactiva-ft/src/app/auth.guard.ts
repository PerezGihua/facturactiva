import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {

  const router = inject(Router);

  const isLoggedIn =
    typeof window !== 'undefined' &&
    !!localStorage.getItem('nombreUser');

  if (isLoggedIn) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }

};
