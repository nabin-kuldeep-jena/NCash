import { Component } from '@angular/core';
import { NavController, NavParams, Platform } from 'ionic-angular';
import { SQLite } from 'ionic-native';
import { NCashConstants } from '../../helpers/ncash-constants';
import { OtpService} from '../../providers/otp-service'
import { StoresPage } from '../stores/stores';
//import { SMS } from 'ionic-native';

declare var window: any;

@Component({
    selector: 'page-otp',
    templateUrl: 'otp.html',
	providers: [OtpService]
})

export class OtpPage {
	otpService: OtpService;
	//otp: string;//= new string"######";
	constructor(public navCtrl: NavController,public navParams: NavParams, otpService: OtpService, public platform: Platform) {
		this.otpService = otpService;
		platform.ready().then(() => {
			// document.addEventListener('onSMSArrive',
			// 	event => {
			// 		//var event : any =e;
			// 		var data = (event as any).data;
			// 		var length = data.body.length;
			// 		this.otp = data.body.substring(length - 6, length);
			// 	});
			//this.autoPopulateOTP();
		});

		//put a condition here so that only on successful signup, below function should be executed
		// if (true)
		// 	this.createLocalDB();
	}
	autoPopulateOTP() {
		var filter = {
			box: 'inbox',
			//body: 'your NCASH',
			address: 'HP-NCAOTP',
			indexFrom: 0,
			maxCount: 1,
		};
		var filter2 = {};
		if (window.SMS) {
			setTimeout(() => {
				window.SMS.listSMS(filter,
					(data) => {
						console.log(data);
						var length = data[0].body.length;
						var otp = data[0].body.substring(length - 6, length);
					},
					(err) => { console.log("Error auto populating OTP") });
			}, 6000);

		}
	}
	verifyOtp(otp) {
		this.otpService.verifyOtp(this.navParams.get('IP'),this.navParams.get('port'),otp,this.navParams.get('clientId'),this.navParams.get('usrName'),this.navParams.get('urtId')).subscribe(
			data => {
				//console.log("Success");
				this.navCtrl.push(StoresPage);
			}, err => {
				console.log("error");
			}
		);
    }
	gotoStores() {
		this.navCtrl.push(StoresPage);
	}

	createLocalDB() {
        let db = new SQLite();

        db.openDatabase({
            name: 'ncash.db',
            location: 'default'
        }).then(() => {
			db.executeSql("DROP TABLE IF EXISTS " + NCashConstants.TODO_LIST_TBL, {})
				.then((data) => {
					console.log("Dropped table: " + NCashConstants.TODO_LIST_TBL);

					db.executeSql('CREATE TABLE IF NOT EXISTS ' + NCashConstants.TODO_LIST_TBL + ' (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(256), isDone BOOLEAN)', {})
						.then((data) => {
							console.log('Table created: ' + data);
						}, (error) => {
							console.error('Unable to execute SQL: ', error);
						})
				});
		}, (error) => {
			console.error('Unable to Open DB: ', error);
		});
    }
}
