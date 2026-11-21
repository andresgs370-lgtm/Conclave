import { Component } from '@angular/core';
import { GeneracionService } from '../../services/generacionservice';

/** Muestra los avisos de generación terminada en la esquina de la pantalla. */
@Component({
  selector: 'app-toastcomponent',
  standalone: false,
  templateUrl: './toastcomponent.html',
  styleUrl: './toastcomponent.css',
})
export class Toastcomponent {

  /**
   * Inyecta el servicio de generación, que contiene los avisos visibles.
   *
   * @param generacion servicio global de generación
   */
  constructor(public generacion: GeneracionService) {}
}
