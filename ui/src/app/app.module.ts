import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HTTP_INTERCEPTORS, HttpClientModule, HttpClientXsrfModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { AppService } from './app.service';
import { AppHttpInterceptorService } from './http-interceptor.service';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { FormComponent } from './form/form.component';
import { SummaryComponent } from './summary/summary.component';
import { FormsModule } from '@angular/forms';

const routes: Routes = [
  {
    path: 'order',
    component: FormComponent,
  },
  {
    path: 'summary',
    component: SummaryComponent,
  },
  {
    path: '**',
    redirectTo: '/order',
  }
];

@NgModule({
  declarations: [
    AppComponent,
    FormComponent,
    SummaryComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'Csrf-Token',
      headerName: 'Csrf-Token',
    }),
    RouterModule.forRoot(routes),
    BsDropdownModule.forRoot(),
    FormsModule
  ],
  providers: [
    AppService,
    {
      multi: true,
      provide: HTTP_INTERCEPTORS,
      useClass: AppHttpInterceptorService
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
