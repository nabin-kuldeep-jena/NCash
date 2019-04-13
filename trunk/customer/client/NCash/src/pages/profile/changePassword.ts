import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { NavController, AlertController, NavParams } from 'ionic-angular';
import { ResponseResolver } from '../../helpers/response-resolver';
import { ProfilePage } from './profile';
import { ChangePasswordService } from '../../providers/change-password-service';

@Component({
    selector: 'page-change-password',
    templateUrl: 'changePassword.html',
    providers: [ChangePasswordService]
})

export class ChangePasswordPage {

    validOldPwd: boolean = true;
    changePasswordService: ChangePasswordService;
    responseErrMessage: string;
    failedTxn: boolean = false;
    passwordUpdateForm: {
    	oldPassword?: String,
    	newPassword?: String,
    	confirmPassword?: String
    } = {};

    constructor(public navCtrl: NavController, public navParams: NavParams, public formBuilder: FormBuilder, public changePwddService: ChangePasswordService, public alertCtrl: AlertController) {
        this.changePasswordService = changePwddService;
        this.passwordUpdateForm = formBuilder.group({
        	oldPassword: ['', Validators.compose([ Validators.required, Validators.pattern('[a-zA-Z]+([ ]?[a-zA-Z]+[ ]*)*'), Validators.minLength(6) ]) ],
        	newPassword: ['', Validators.compose([ Validators.required, Validators.pattern('[a-zA-Z]+([ ]?[a-zA-Z]+[ ]*)*'), Validators.minLength(6) ]) ],
        	confirmPassword: ['', Validators.compose([ Validators.required, Validators.pattern('[a-zA-Z]+([ ]?[a-zA-Z]+[ ]*)*'), Validators.minLength(6) ]) ]
        });
    }

    updatePassword(form)
    {
        // api for password verification        
        this.changePasswordService.updatePassword(form).subscribe(
            data => {
                    this.navCtrl.push(ProfilePage);
                },
                err => {
                    this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
                    this.failedTxn = true;
                }
        )
    }

    isValid(oldPwd, newPwd, confirmPwd)
    {
        if (newPwd != confirmPwd)
            return true;
    }

    verifyOldPassword(oldPwd)
    {
        var oldPassword = this.navParams.get('oldPwd');
        console.log(oldPwd);

        if ( oldPassword != oldPwd)
            this.validOldPwd = false;
        else
            this.validOldPwd = true;
    }
}
