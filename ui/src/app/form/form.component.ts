import {Component, OnInit} from '@angular/core';
import {AppService} from "../app.service";
import * as moment from 'moment'
import * as _ from 'lodash';

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})
export class FormComponent implements OnInit {
  colors = ['czerwony', 'zielony', 'niebieski'];
  sizes = ['S', 'M', 'L', 'XL'];
  products: Product[];
  chosenProducts: any = [];
  submitted = false;
  model = new OrderForm(null, null, "default", null);
  errorOrdersQuantity: ErrorOrderQuantity[];
  isErrorOrdersQuantity = false;
  isErrorOrder = false;

  constructor(private appService: AppService) {
    this.fetchData();
  }

  fetchData() {
    this.appService.getProducts().subscribe((data: any) => {
      this.products = data as Product[];
    });
  }

  ngOnInit() {
  }

  saveProduct() {
    this.chosenProducts.push({color: this.model.color, size: this.model.size});
    this.model.color = "default";
    this.model.size = null;
  }

  isQuantityMoreThanZero() {
    if (this.model.color && this.model.size) {
      return this.products.filter(product => product.color == this.model.color)
        .filter(product => product.size == this.model.size)
        .some(product => product.quantity > 0);
    } else {
      return false;
    }
  }

  isSizeAndColorSelected() {
    const color = this.model.color;
    const size = this.model.size;
    return (color !== undefined && color && color !== "default") && (size !== undefined && size);
  }

  isValidForm(form) {
    return form.valid && this.chosenProducts.length > 0;
  }

  onSubmit() {
    const groupedProductsByColorAndSize = this.groupProductsByColorAndSize(this.chosenProducts);
    const order = new OrderProductDTO(this.model.name, this.model.age, moment().format("YYYY-MM-DD HH:mm:ss"), groupedProductsByColorAndSize);
    this.appService.sendOrder(JSON.stringify(order))
      .subscribe(
        data => {
          this.clearOrderAfterSubmit();
          this.fetchData();
        },
        error => {
          if (error.status === 400) {
            this.handleBadRequest(error);
          } else {
            this.isErrorOrder = true;
          }
          this.chosenProducts = [];
        }
      );
  }

  handleBadRequest(error) {
    this.isErrorOrdersQuantity = true;
    this.errorOrdersQuantity = error.error.map(e =>
      this.createErrorOrderQuantity(e.productId, e.quantity))
  }

  clearOrderAfterSubmit() {
    this.submitted = true;
    this.chosenProducts = [];
  }

  visualizeSelectedColorAndSize() {
    const colorMap: Map<string, string> = new Map<string, string>();
    const sizeMap: Map<string, string> = new Map<string, string>();
    colorMap.set("czerwony", "firebrick");
    colorMap.set("zielony", "#44534C");
    colorMap.set("niebieski", "skyblue");
    sizeMap.set("S", "25px");
    sizeMap.set("M", "50px");
    sizeMap.set("L", "75px");
    sizeMap.set("XL", "100px");
    if(this.isSizeAndColorSelected()) {
      return {
        height: sizeMap.get(this.model.size),
        width: sizeMap.get(this.model.size),
        backgroundColor: colorMap.get(this.model.color)
      }
    } else {
      return {
        height: '',
        width: '',
        backgroundColor: ''
      }
    }
  }

  groupProductsByColorAndSize(chosenProducts: any[]) {
    const groupedProducts = _.groupBy(chosenProducts, (p) => `${p.color}--${p.size}`);
    return _.values(groupedProducts).map(groupedProduct => {
      const oneProduct = groupedProduct[0];
      const productId = this.products.filter(product =>
        product.color == oneProduct.color && product.size == oneProduct.size)[0].id;
      const productsQuantity = groupedProduct.length;
      return new ChosenProductIdAndQuantity(productId, productsQuantity);
    })
  }

  createErrorOrderQuantity(productId: number, orderQuantity: number) {
    const product = this.products.find(product => product.id === productId);
    return new ErrorOrderQuantity(product.color, product.size, product.quantity, orderQuantity);
  }
}

class OrderForm {
  constructor(public name: string,
              public age: number,
              public color: string,
              public size: string) {
  }
}

class Product {
  id: number;
  color: string;
  size: string;
  quantity: number;
}

class OrderProductDTO {
  constructor(public name: string,
              public age: number,
              public date: string,
              public orderProducts: ChosenProductIdAndQuantity[]) {
  }
}

class ChosenProductIdAndQuantity {
  constructor(public productId: number,
              public quantity: number) {
  }
}

class ErrorOrderQuantity {
  constructor(public color: string,
              public size: string,
              public productQuantity: number,
              public orderQuantity: number) {
  }
}
