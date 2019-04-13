import { Component } from '@angular/core';
//import { HTTP } from 'ionic-native';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';

@Component({
	templateUrl: 'payment.html'	
})

export class PaymentPage {
	payment_modes: any = [];

	constructor(public http: Http) {
		this.fetchPaymentOptions();
	}

	fetchPaymentOptions() {
		/*HTTP.get("json/payment_options.json", {}, {})
			.then(data => {
				this.payment_modes = data;
			})
			.catch(error => {
				console.log("Fetching Payment Modes failed!");
			});*/

		this.http.get("json/payment_options.json")
			.map(res => res.json())
			.subscribe(data => {
				this.payment_modes = data;
				console.log(JSON.stringify(this.payment_modes));
			})
	}
}