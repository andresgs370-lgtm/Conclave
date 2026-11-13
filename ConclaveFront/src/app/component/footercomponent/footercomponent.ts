import { Component } from '@angular/core';
/**
 * Componente que tiene la informacion de contacto y que se visualiza en cualquier componente.
 */
@Component({
  selector: 'app-footercomponent',
  standalone: false,
  templateUrl: './footercomponent.html',
  styleUrl: './footercomponent.css',
})
export class Footercomponent {
  anio = new Date().getFullYear();
}
