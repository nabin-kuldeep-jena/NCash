import { Component } from '@angular/core';
import { Validators, FormBuilder, FormGroup } from '@angular/forms';
import { NavController, NavParams, AlertController } from 'ionic-angular';
import { PasswordService } from '../../providers/password-service';
import { ResponseResolver } from '../../helpers/response-resolver';
import { StoresPage } from '../stores/stores';
import { VerifyMobNumPage } from '../verifymobnum/verifymobnum';

@Component({
	selector: 'forgot-password',
	templateUrl: 'forgotpassword.html',
	providers: [PasswordService]
})

export class ForgotPasswordPage {
	isAnyErrorInUpdatePassword: boolean = true;//false
	passwordService: PasswordService;
	responseErrMessage: string;
	clientUid: string;
	forgotPasswordForm: FormGroup;

	constructor(public navCtrl: NavController, public navParams: NavParams, public formBuilder: FormBuilder, passwordService: PasswordService, public alertCtrl: AlertController) {
		this.passwordService = passwordService;
		this.forgotPasswordForm = this.formBuilder.group({
			otp: ['', Validators.compose([ Validators.pattern('[0-9]{6}'), Validators.required ]) ],
			newPassword: ['', Validators.compose([ Validators.minLength(6), Validators.maxLength(20),Validators.pattern('[a-zA-Z]+([ ]?[a-zA-Z0-9]+[ ]*)*'), Validators.required ]) ],
			confirmPassword: ['', Validators.compose([ Validators.minLength(6), Validators.maxLength(20),Validators.pattern('[a-zA-Z]+([ ]?[a-zA-Z0-9]+[ ]*)*'), Validators.required ]) ]
		});

	}

	redirectToVerifyNumPage()
	{
		this.navCtrl.push(VerifyMobNumPage);
	}

	changePassword(otp, password) {
		//this.navCtrl.push(StoresPage);

		this.passwordService.verifyOTPAndUpdatePassword(this.navParams.get('ip'), this.navParams.get('port'), this.navParams.get('clientUid'), otp, this.navParams.get('mobileNum'), password).subscribe(
			data => {
				this.navCtrl.push(StoresPage);
			},
			err => {
				this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
				this.isAnyErrorInUpdatePassword = true;
			});
	}

	isValid(form) {
		if (form.valid && form.controls.newPassword.value == form.controls.confirmPassword.value)
			return true;
		else
			return false;
	}

	passwordMatchError(form)
	{
		if ( form.controls.newPassword.value != form.controls.confirmPassword.value)
			return true;
		else
			return false;
	}
}
