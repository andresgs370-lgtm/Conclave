import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-perfilcomponent',
  standalone: false,
  templateUrl: './perfilcomponent.html',
  styleUrl: './perfilcomponent.css',
})
export class Perfilcomponent implements OnInit {
  profile: any = null;
  loading = true;
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.profile().subscribe({
      next: data => { this.profile = data; this.loading = false; },
      error: err => { this.error = err.error?.message || 'No fue posible cargar el perfil.'; this.loading = false; }
    });
  }
}
