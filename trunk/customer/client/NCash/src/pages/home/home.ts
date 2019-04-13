import { Component } from '@angular/core';
import { AlertController, Platform, NavController, PopoverController, ModalController, ViewController, NavParams } from 'ionic-angular';
import {  BarcodeScanner } from 'ionic-native';

import { Helper } from '../../helpers/helper';
import { NCashConstants } from '../../helpers/ncash-constants';

import { Product } from '../../interfaces/product';
import { PopoverPage } from './popover';
import { TodoListPage } from '../todolist/todolist';

@Component({
  	selector: 'page-home',
	templateUrl: 'home.html'
})

export class HomePage {
	searchbar: boolean = true;
	/* stores product ids. useful to show duplicate items */
	productIds: string[];
	helper: Helper;
	duplicateProducts: Product[] = [];
	products: Product[] = [ 
		{
			id: "111",
			name: "ABC",
			price: 100,
			img: "b2",
			count: 3,
			mfgDate: "12 Dec 2016",
			isExpired: false
		},
		{
			id: "222",
			name: "DEF",
			price: 200,
			img: "b3",
			count: 5,
			mfgDate: "13 Dec 2016",
			isExpired: false
		},
		{
			id: "333",
			name: "EFG",
			price: 300,
			img: "b25",
			count: 1,
			mfgDate: "14 Dec 2016",
			isExpired: false
		}
	];

  	constructor(public platform: Platform, public navCtrl: NavController, public alertCtrl: AlertController, public popoverCtrl: PopoverController, public modalCtrl: ModalController) {
  		this.platform = platform;
  		this.navCtrl = navCtrl;
  		this.helper = new Helper();
  		this.helper.copyProducts(this.products,this.duplicateProducts);
  	}

  	scanProduct() {
        BarcodeScanner.scan().then((result) => {
	        if (result != null && result.text != "") {
	        	/*HTTP.get('http://ionic.io', {}, {})
	        		.then(data => {
	        			console.log(data.status);
	        			console.log(data.data);
	        		})
	        		.catch(error => {
	        			console.log(error.status);
	        			console.log(error.error);
	        		});*/

	        	let isExist: boolean = false;

	        	this.products.forEach((product, index) => {
	    			if (result.text === product.id) {
	    				let existsAlert = this.alertCtrl.create({
	    					subTitle: NCashConstants.ITEM_EXIST,
	    					buttons: ['OK']
	    				});
	    				existsAlert.present();
	    				isExist = true;
	    				return;
	    			}
    			});

	        	if (!isExist) {
	        		let scannedProduct: Product = {
	        			id: result.text,
	        			name: "Test",
	        			price: 10,
	        			img: "lion",
	        			count: 0,
	        			mfgDate: "",
	        			isExpired: false
	        		}

	        		this.products.push(scannedProduct);
	        		this.duplicateProducts.push(scannedProduct);
	    		}
        	}
		}).catch((err) => {
			console.log(err);
		})
    }

    showProductDetails(item) {
    	let detail = this.modalCtrl.create(ProductDetails, { item: item });
    	detail.present();
    }

    clearCart() {
    	let clearCart = this.alertCtrl.create({
    		message: NCashConstants.CLEAR_CART,
    		buttons: [
    			{
    				text: 'Clear',
    				handler: () => {
    					this.products = [];
    					this.duplicateProducts = [];
    				}
    			},
    			{
    				text: 'Cancel',
    				handler: () => {

    				}
    			}
    		]
    	});

    	clearCart.present();
    }

    deleteItem(item) {
    	let confirm = this.alertCtrl.create({
    		message: NCashConstants.DELETE_ITEM_FROM_CART,
    		buttons: [
        		{
          			text: 'Remove',
          			handler: () => {	
          				let i = this.products.indexOf(item);
				    	this.products.splice(i,1);
				    	this.duplicateProducts.splice(i,1);
          			}
		        },
		        {
		        	text: 'Cancel',
		          	handler: () => {
		            	
		          	}
		        }
      		]
    	});

    	confirm.present();
    }

    searchItems(eve: any) {
    	this.products = [];
    	this.helper.copyProducts(this.duplicateProducts,this.products);

    	let val = eve.target.value;
    	if (val && val.trim() != '') {
    		this.products = this.products.filter((item) => {
    			return (item.name.trim().toLowerCase().indexOf(val.trim().toLowerCase()) > -1);
    		});
    	}
    }

    showHideSearchBar() {
    	return !this.searchbar;
    }

    increaseCount(event, item) {
    	item.count++;
    	event.stopPropagation();
    }

    decreaseCount(event, item) {
    	if (item.count > 0) {
    		item.count--;
    	}
    	event.stopPropagation();
    }


    moreAction(myEvent) {
    	let popover = this.popoverCtrl.create(PopoverPage);
	    popover.present({
	      ev: myEvent
	    });

	    popover.onDidDismiss(data => {
	    	switch (data) {
	    		case NCashConstants.SEARCH_VISIBLE:
	    			this.searchbar = this.showHideSearchBar();
	    			break;

	    		case NCashConstants.CLEAR:
	    			this.clearCart();
	    			break;

	    		case NCashConstants.TODO_LIST:
	    			this.navCtrl.push(TodoListPage);
	    			break;

	    		default:
	    			break;
	    	}
		});
	}

}

@Component({
	templateUrl: 'productDetails.html'
})

export class ProductDetails {
	item;

	constructor(public params: NavParams, public viewCtrl: ViewController) {
		this.item = this.params.get('item');
	}

	dismiss() {
   		this.viewCtrl.dismiss();
 	}
}