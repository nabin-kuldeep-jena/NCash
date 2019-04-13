import { Component } from '@angular/core';
import { AlertController, Platform, NavController, LoadingController, PopoverController, ModalController, ViewController, NavParams } from 'ionic-angular';
import { HTTP, BarcodeScanner, CardIO, Network } from 'ionic-native';

import { Helper } from '../../helpers/helper';
import { NCashConstants } from '../../helpers/ncash-constants';
import { OrdersummaryPage } from '../ordersummary/ordersummary';
//import { Product } from '../../interfaces/product';
import { PopoverPage } from './popover';
import { PaymentPage } from '../payment/payment';
import { TodoListPage } from '../todolist/todolist';
import { ItemService } from '../../providers/item-service';
import { ProductModel } from '../../common/product-model';

@Component({
	templateUrl: 'item.html',
	providers: [ ItemService ]
})

export class ItemPage {
	searchbar: boolean = true;
	total: number = 0;
	loading: any;
	/* stores product ids. useful to show duplicate items */
	productIds: string[];
	helper: Helper;
	duplicateProducts: ProductModel[] = [];
	itemService: ItemService;
	products: ProductModel[] = [ 
		// {
		// 	prdid: "111",
		// 	name: "ABC",
		// 	price: 100,
		// 	img: "b2",
		// 	count: 3,
		// 	mfgDate: "12 Dec 2016",
		// 	isExpired: false
		// },
		// {
		// 	id: "222",
		// 	name: "DEF",
		// 	price: 200,
		// 	img: "b3",
		// 	count: 5,
		// 	mfgDate: "13 Dec 2016",
		// 	isExpired: false
		// },
		// {
		// 	id: "333",
		// 	name: "EFG",
		// 	price: 300,
		// 	img: "b25",
		// 	count: 1,
		// 	mfgDate: "14 Dec 2016",
		// 	isExpired: false
		// }
	];

  	constructor(public platform: Platform, public navParams: NavParams, public navCtrl: NavController, public alertCtrl: AlertController, public popoverCtrl: PopoverController, public modalCtrl: ModalController, public itemServiceTemp: ItemService, public loader: LoadingController) {
  		this.platform = platform;
  		this.navCtrl = navCtrl;
  		this.helper = new Helper();
  		this.itemService = itemServiceTemp;
  		this.helper.copyProducts(this.products,this.duplicateProducts);
  	}
	ionViewDidLoad(){
	let nikeShoes=new ProductModel();
	nikeShoes.prdCode='NIKE1002',
	nikeShoes.prdPrice=4500,
	nikeShoes.prdName='Nike Shoes'
	this.products[0]=nikeShoes;

	let goodDayBiscuits=new ProductModel();
	goodDayBiscuits.prdCode='GDB1106',
	goodDayBiscuits.prdPrice=30,
	goodDayBiscuits.prdName='Good Day Biscuits',
	this.products[1]=goodDayBiscuits;
	}

  	scanProduct() {
        BarcodeScanner.scan().then((result) => {
        	this.mask();
	        if (result != null && result.text != "") {
	        	alert(result.text);
	        	let storeId = 1;
	        	let itemId = 1;
	        	this.itemService.getItem(this.navParams.get('IP'), this.navParams.get('port'), storeId, itemId)
	        		.subscribe(data => {
                    	if (data.length > 0){
                   		   console.log(data.prdName);
                   		   for (var datum in data)
                   		   {
                   		   		let productModel: ProductModel = new ProductModel();
                   		   		productModel.prdId = data[datum].id;
                   		   		productModel.prdName = data[datum].properties.prdName;
                   		   		productModel.prdCode = data[datum].properties.prdCode;
								productModel.prdDesc = data[datum].properties.prdDesc;
								productModel.prdPrice = data[datum].properties.prdPrice;
								productModel.prdMfgDate = data[datum].properties.prdMfgDate;
								productModel.prdExpiryDate = data[datum].properties.prdExpiryDate;

								this.products[datum] = productModel;
                   		   }
                    	}
                    this.unmask();
                },
                err => {
                    console.log("Error while Fetching Item Details!!!")
                    this.unmask();
                });

	        	let isExist: boolean = false;

	        	this.products.forEach((product, index) => {
	    			if (result.text === product.prdId) {
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
	        		let scannedProduct: ProductModel = new ProductModel();
					scannedProduct.prdId=1;
					scannedProduct.prdCode='Sample';
					scannedProduct.prdDesc='Testing';
					scannedProduct.prdPrice=12;
					scannedProduct.prdName='Sample';

	        		this.total += scannedProduct.prdPrice;
	        		this.products.push(scannedProduct);
	        		this.duplicateProducts.push(scannedProduct);
	    		}
        	}
		}).catch((err) => {
			console.log(err);
		})
    }

    showProductDetails(item) {
    	let detail = this.modalCtrl.create(ItemDetails, { item: item });
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

    deleteItem(item: ProductModel) {
    	let confirm = this.alertCtrl.create({
    		message: NCashConstants.DELETE_ITEM_FROM_CART,
    		buttons: [
        		{
          			text: 'Remove',
          			handler: () => {	
          				let i = this.products.indexOf(item);
				    	this.products.splice(i,1);
				    	this.duplicateProducts.splice(i,1);
				    	this.total -= item.prdPrice;
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
    			return (item.prdName.trim().toLowerCase().indexOf(val.trim().toLowerCase()) > -1);
    		});
    	}
    }

    showHideSearchBar() {
    	return !this.searchbar;
    }

    increaseCount(event, item) {
    	item.count++;
    	return false;
    }

    decreaseCount(event, item) {
    	event.stopPropagation();

    	if (item.count > 0) {
    		item.count--;
    	}
    }

    mask() {
        this.loading = this.loader.create({
            content: `<ion-spinner name='crescent'></ion-spinner>`
        });

        this.loading.present();
    }

    unmask() {
        this.loading.dismiss();
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

	proceedToOrderSummary() {
		this.navCtrl.push(OrdersummaryPage,{
			products:this.products
		});
	}

	scanCardIO() {
		CardIO.canScan()
  .then(
    (res: boolean) => {
      if(res){
        let options = {
          requireExpiry: true,
          requireCCV: false,
          requirePostalCode: false
        };
        CardIO.scan(options);
      }
    }
  );
	}
}

@Component({
	templateUrl: 'itemDetails.html'
})

export class ItemDetails {
	item;

	constructor(public params: NavParams, public viewCtrl: ViewController) {
		this.item = this.params.get('item');
	}

	dismiss() {
   		this.viewCtrl.dismiss();
 	}
}