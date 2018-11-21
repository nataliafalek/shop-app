import {Component} from '@angular/core';
import {AppService} from "../app.service";
import * as _ from 'lodash';

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent {
  orderSummary: OrderSummaryDTO[];
  visibleOrders = new Set();

  constructor(private appService: AppService) {
    this.fetchData();
  }

  private fetchData() {
    this.appService.gerOrdersSummary().subscribe((data: any) => {
      const orderSummary = data as OrderSummaryDTO[];
      this.orderSummary = _.orderBy(orderSummary, 'date', 'desc')
    });
  }

  orderShouldBeVisible(orderId: number) {
    return this.visibleOrders.has(orderId)
  }

  makeOrderDetails(orderId: number) {
    if (this.visibleOrders.has(orderId)) {
      this.visibleOrders.delete(orderId)
    } else {
      this.visibleOrders.add(orderId)
    }
  }
}

class OrderSummaryDTO {
  orderId: number;
  date: string;
  name: string;
  age: number;
  orderProducts: OrderProducts[];
}

class OrderProducts {
  id: number;
  color: string;
  size: string;
  quantity: number;
}
