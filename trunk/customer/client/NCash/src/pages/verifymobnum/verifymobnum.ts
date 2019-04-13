import { Component } from '@angular/core';
import { NavController, AlertController, NavParams, LoadingController } from 'ionic-angular';
import { PasswordService } from '../../providers/password-service';
import { OTPHelper } from '../../helpers/otp-helper';
import { ResponseResolver } from '../../helpers/response-resolver';
import { ForgotPasswordPage } from '../forgotpassword/forgotpassword';

@Component({
	templateUrl: 'verifymobnum.html',
	providers: [PasswordService]
})

export class VerifyMobNumPage {
	passwordService: PasswordService;
	clientUid: string;
	responseErrMessage: string;
	userNotRegistered: boolean = false;
	loading: any;

	constructor(public navCtrl: NavController, public alertCtrl: AlertController, public navParams: NavParams, passwordService: PasswordService, public loader: LoadingController) {
		this.passwordService = passwordService;
	}

	verifyNumSendOtp(mobileNo) {
		this.mask();
		//this.navCtrl.push(ForgotPasswordPage, { ip: this.navParams.get('IP'), port: this.navParams.get('port'), mobileNum: mobileNo });
		this.clientUid = OTPHelper.getInstance().generateClientUID(mobileNo);
		console.log(this.clientUid);

		this.passwordService.sendUserNameAndReceiveOTP(this.navParams.get('IP'), this.navParams.get('port'), mobileNo, this.clientUid).subscribe(
			data => {
				this.unmask();
				this.navCtrl.push(ForgotPasswordPage, { ip: this.navParams.get('IP'), port: this.navParams.get('port'), mobileNum: mobileNo, clientUid: this.clientUid});
			},
			err => {
				this.unmask();
				this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
				this.userNotRegistered = true;
			});
	}

	mask()
	{
		this.loading = this.loader.create({
			content: `<ion-spinner name='crescent'></ion-spinner>`
		});

		this.loading.present();
	} 

	unmask()
	{
		this.loading.dismiss();
	}

}
