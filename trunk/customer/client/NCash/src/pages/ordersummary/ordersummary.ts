import { Component } from '@angular/core';
import { NavController,NavParams } from 'ionic-angular';
import { PaymentPage } from '../payment/payment';
//import { Product } from '../../interfaces/product';
import { ProductModel } from '../../common/product-model';

/*
  Generated class for the Ordersummary page.

  See http://ionicframework.com/docs/v2/components/#navigation for more info on
  Ionic pages and navigation.
*/
@Component({
  selector: 'page-ordersummary',
  templateUrl: 'ordersummary.html'
})
export class OrdersummaryPage {
  total: number = 0;  
  products: ProductModel[];
  constructor(public navCtrl: NavController, public navParams: NavParams) {
    this.products=navParams.get('products');
  }

  ionViewDidLoad() {
    console.log('Hello OrdersummaryPage Page');
  }
  proceedToPayment() {
		this.navCtrl.push(PaymentPage);
	}
}
