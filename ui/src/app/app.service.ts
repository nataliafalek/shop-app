import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs/index';

@Injectable()
export class AppService {
  api = "http://localhost:9000/api";
  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PATCH, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Origin, Content-Type, X-Auth-Token'
    })
  };

  constructor(private http: HttpClient) {
  }

  public getProducts() {
    return this.http.get(this.api + '/products').pipe(
      map(response => response)
    );
  }

  public gerOrdersSummary() {
    return this.http.get(this.api + '/orderSummary').pipe(
      map(response => response)
    )
  }
  public sendOrder(object): Observable<any> {
     return this.http.post(this.api + '/sendOrder', object, this.httpOptions);
  }
}
