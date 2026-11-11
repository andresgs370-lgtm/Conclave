import { Routes } from '@angular/router';
import { Paginaprincipalcomponent } from './component/paginaprincipalcomponent/paginaprincipalcomponent';
import { Modeloscomponent } from './component/modeloscomponent/modeloscomponent';

export const routes: Routes = [
  { path: '', component: Paginaprincipalcomponent },
  { path: 'modelo', component: Modeloscomponent },
];
