import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import { NCashConstants } from '../helpers/ncash-constants';
import 'rxjs/add/operator/map';


@Injectable()
export class ChangePasswordService {
    
    constructor(public http: Http) {
        this.http = http;
    }

    public updatePassword(form) : any{
        let headers: Headers = new Headers;
        headers.append('Content-Type', 'application/json');
        var link = NCashConstants.CHANGE_PASSWORD_URL;        
        var userCredentials = { "oldPwd": form.oldPassword.value, "newPwd": form.newPassword.value };
        var data = JSON.stringify(userCredentials);

        return this.http.post(link, data, { headers: headers })
            .map(res => res.json());
    }
}
