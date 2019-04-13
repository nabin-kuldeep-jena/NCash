import { Component } from '@angular/core';
import { Validators, FormBuilder } from '@angular/forms';
import { NavController, AlertController } from 'ionic-angular';
import { SignupPage } from '../signup/signup';
//import { ItemPage } from '../item/item';
import { StoresPage } from '../stores/stores';
import { LoginService } from '../../providers/login-service';
//import { AlertController } from 'ionic-angular';
//import { AlertHelper } from '../helper/AlertHelper';
import { ResponseResolver } from '../../helpers/response-resolver';
//import { MapsPage } from '../maps/maps';
//import { FloormapsPage } from '../floormaps/floormaps';
import { VerifyMobNumPage } from '../verifymobnum/verifymobnum';

@Component({
    selector: 'page-login',
    templateUrl: 'login.html',
    providers: [LoginService]
})

export class LoginPage {
    unSuccesfulLogin: boolean;
    loginForm: {
        mobileNumber?: string,
        password?: string,
        IP?: string,
        port?: number
    } = {};
    loginService: LoginService;
    responseErrMessage: string;
    constructor(public navCtrl: NavController, public formBuilder: FormBuilder, loginService: LoginService, public alertCtrl: AlertController) {

        this.loginForm = formBuilder.group({
            //min length for mobile no.
            mobileNumber: ['', Validators.compose([Validators.required, Validators.pattern('[0-9]+')])],
            password: ['', Validators.compose([Validators.minLength(6), Validators.required])],
            IP: ['127.0.0.1'],//['192.168.1.33'],
            port: ['8083']
            // email: [''],
            //password: ['']
        });
        this.loginService = loginService;
        this.unSuccesfulLogin = false;
    }

    doLogin(form) {
        /*this.navCtrl.push(StoresPage, {
            // clientId:uidToken,
            // usrName:form.controls.firstName.value,
            // urtId:data.responseData.urtId,
            IP: form.controls.IP.value,
            port: form.controls.port.value
           // mobileNo: form.controls.mobileNumber.value
        });*/
        // if (form.controls.mobileNumber.errors || form.controls.password.errors)
        //     return;
        // else {
        //     this.loginService.login(form).subscribe(
        //         data => {
        //             //console.log(data[0].usrCtyId);
        //             //console.log(data[0].usrSteId);
        //             this.navCtrl.push(StoresPage, {loggedInUsrId : data[0] });
        //             //preferredCityId: .usrCtyId, preferredStateId: data[0].usrSteId
        //         },
        //         err => {
        //             this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
        //             this.unSuccesfulLogin = true;
        //         }
        //     );
        // }
        
         this.navCtrl.push(StoresPage, {loggedInUsrId:1, preferredCityId:1,preferredStateId:1, IP: form.controls.IP.value,
              port: form.controls.port.value });
    }

    public signUp(form) {
        this.navCtrl.push(SignupPage,{
            IP:form.controls.IP.value,
            port:form.controls.port.value
        });
    }

    onFocus(event) {
        this.unSuccesfulLogin = false;
    }

    forgotPassword(form) {
        this.navCtrl.push(VerifyMobNumPage,
            {
                // clientId:uidToken,
                // usrName:form.controls.firstName.value,
                // urtId:data.responseData.urtId,
                IP: form.controls.IP.value,
                port: form.controls.port.value
               // mobileNo: form.controls.mobileNumber.value
            });
    }

}
