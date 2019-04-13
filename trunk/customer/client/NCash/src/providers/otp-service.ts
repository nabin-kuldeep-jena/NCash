import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import { Http, Headers, RequestOptions} from '@angular/http';
import { NCashConstants } from '../helpers/ncash-constants';

/*
  Generated class for the OtpService provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
export class OtpService {

  constructor(public http: Http) {
  }
  public verifyOtp(ip,port,otp,clientId,usrName,urtId) {
    // let name: string = 'Sonali';
    // let clientUid: string = '1234';
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    let link2 = NCashConstants.BASE_URL + '/user/verifyOtpToken';
    var link = 'http://' + ip + ':' + port + '/ncash/services/user/verifyOtpToken';
    var body = 'otpToken='+otp+'&clientUid='+clientId+'&userName='+usrName+'&urtId='+urtId;
    return this.http.post(link, body, options).map(res => res.json());
  }
  public resendOtp(ip,port,clientId,usrName,urtId) {
    // let name: string = 'Sonali';
    // let clientUid: string = '1234';
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    let link2 = NCashConstants.BASE_URL + '/user/resendOtp';
    var link = 'http://' + ip + ':' + port + '/ncash/services/user/resendOtp';
    var body = '&clientUid='+clientId+'&userName='+usrName+'&urtId='+urtId;
    return this.http.post(link, body, options).map(res => res.json());
  }
}
