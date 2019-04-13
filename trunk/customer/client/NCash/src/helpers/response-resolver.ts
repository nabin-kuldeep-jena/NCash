import {AlertHelper} from './alert-helper';
import { OTPHelper } from './otp-helper';
import { AlertController } from 'ionic-angular';

export class ResponseResolver {

    static helper: ResponseResolver = null;

    static getInstance() {
        if (this.helper == null)
            this.helper = new ResponseResolver();
        return this.helper;
    }
    
    resolveResponse(err, alertCtrl: AlertController):any {
        switch (err.status) {
            case 406: return this.resolveErr406(err);
            case 500: AlertHelper.getInstance().showAlert(alertCtrl,err.statusText,err._body.message,['Ok']);
                return;
            default: AlertHelper.getInstance().showAlert(alertCtrl, 'Server Error', 'The server is temporarily unavailable. Could not process your request.', ['OK']);
                return;
        }
    }
    resolveErr406(err: any): string {
            var res = JSON.parse(err._body);
            if (Object.keys(res.errorKey).length === 0) {
                return res.message;
            }
            else {
                var obj = res.errorKey;
                for (var key in obj) {
                    {
                        // if (key === NCashConstants.ALERT_KEY) {
                        //     this.showAlert('Login Error', obj[key], ['OK']);
                        //     //this.unSuccesfulLogin=true;
                        // }
                        // else {
                        //     this.showAlert('Login Error', obj[key], ['OK']);
                        // }
                        return obj[key];
                    }
                }
            }
        return null;
    }
}