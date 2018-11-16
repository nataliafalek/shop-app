import { Component, OnInit } from '@angular/core';
import {AppService} from "../app.service";

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent {
  public products: Product[];

  constructor(private appService: AppService) {
    this.appService.getProducts().subscribe((data: any) => {
      this.products = data[1] as Product[];
    });
  }
}

interface Product {
  color: string;
  size: string;
}
