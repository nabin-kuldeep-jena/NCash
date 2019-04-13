import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import 'rxjs/add/operator/map';
import { NCashConstants } from '../helpers/ncash-constants';

@Injectable()
export class PasswordService {

    constructor(public http: Http) {
        console.log('Hello PasswordService Provider');
    }

    public sendUserNameAndReceiveOTP(ip, port, mobileNo, clientUid): any {
        let verifyMobNoApi = '/ncash/services/user/forgotPassword';
        var api = 'http://' + ip + ':' + port + verifyMobNoApi;

        var userObj = {
            "usrName": mobileNo,
            "clientUid": clientUid
        };

        var body = JSON.stringify(userObj);

        let headers: Headers = new Headers;
        headers.append('Content-Type', 'application/json');
        return this.http.post(api, body, {
            headers: headers
        }).map(res => res.json());
    }

    public verifyOTPAndUpdatePassword(ip, port, clientUid, otp, mobileNo, newPassword): any {
        let api = 'http://' + ip + ':' + port + '/ncash/services' + NCashConstants.CHANGE_PASSWORD_URL;
        var userObj = {
            "clientUid": clientUid,
            "otpToken": otp,
            "usrName": mobileNo,
            "newPassword": newPassword
        };

        var body = JSON.stringify(userObj);

        let headers: Headers = new Headers;
        headers.append('Content-Type', 'application/json');
        return this.http.post(api, body, {
            headers: headers
        }).map(res => res.json());
    }
}