import { Injectable } from '@angular/core';
import { Http, URLSearchParams, Headers } from '@angular/http';
import 'rxjs/add/operator/map';
import { NCashConstants } from '../helpers/ncash-constants';

/*
  Generated class for the SignupService provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
export class SignupService {

  constructor(public http: Http) {
  }

  public checkMobileNo(mobileNo) {
    let link = NCashConstants.BASE_URL + '/user/validateMobileNo?mobNo=' + mobileNo;
    //var link = 'http://' + form.controls.IP.value + ':' + form.controls.port.value + '/ncash/services/user/validateMobileNo?mobNo=' + mobileNo;
    let signUpheader: Headers = new Headers;
    signUpheader.append('Content-Type', 'application/json');
    let params: URLSearchParams = new URLSearchParams();
    params.set('usrMobNo', mobileNo);
    return this.http.get(link, { headers: signUpheader }).map(res => res.json);
  }

  public checkEmail(email) {
    //TODO: link
    let link = NCashConstants.BASE_URL + '/user/validateEmail?emailId=' + email;
    //TODO: params
    //var link = 'http://' + form.controls.IP.value + ':' + form.controls.port.value + '/ncash/services/user/validateEmail?mobNo=' + email;
    let signUpheader: Headers = new Headers;
    signUpheader.append('Content-Type', 'application/json');
    return this.http.get(link, { headers: signUpheader }).map(res => res.json);
  }

  public registerUser(myObj, IP, port) {
    let headers: Headers = new Headers;
    headers.append('Content-Type', 'application/json');
    var link2 = NCashConstants.BASE_URL + '/user/registerUser';
    //'http://localhost:8080/ncash/services'
    var link = 'http://' +IP + ':' + port + '/ncash/services/user/registerUser';
    var data = JSON.stringify(myObj);
    return this.http.post(link, data, { headers: headers })
      .map(res => res.json());
  }
 

  
  
}
