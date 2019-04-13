import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import { NCashConstants } from '../helpers/ncash-constants';
import 'rxjs/add/operator/map';


@Injectable()
export class LoginService {

  constructor(public http: Http) {
    this.http = http;
  }

  public login(form) : any{
    let headers: Headers = new Headers;
    headers.append('Content-Type', 'application/json');
    var link2 = NCashConstants.BASE_URL + '/security/authenticateUser';
    var link = 'http://' + form.controls.IP.value + ':' + form.controls.port.value + '/ncash/services/security/authenticateUser';
    var myObj = { "usrName": form.controls.mobileNumber.value, "password": form.controls.password.value };
    var data = JSON.stringify(myObj);

    return this.http.post(link, data, { headers: headers })
      .map(res => res.json());
  }
}
