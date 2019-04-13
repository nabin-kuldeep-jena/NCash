import { Injectable } from '@angular/core';
import { Http,Headers} from '@angular/http';
import 'rxjs/add/operator/map';
import { NCashConstants } from '../helpers/ncash-constants';

/*
  Generated class for the ProfileService provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
export class ProfileService {

  constructor(public http: Http) {
    console.log('Hello ProfileService Provider');
  }

  public getLoggedInUser(ip, port,usrId) {
    // let name: string = 'Sonali';
    // let clientUid: string = '1234';
    let headers = new Headers({ 'Content-Type': 'application/json' });
   // let options = new RequestOptions({ headers: headers });
    let link2 = NCashConstants.BASE_URL + '/user/verifyOtpToken';
    //var link = 'http://' + IP + ':' + port + '/ncash/services/gen/StoreBranch?filter=address.cityTbl.ctyId='+preferredCity+'&cmp=SOCDMC';
    var link = 'http://' + ip + ':' + port + '/ncash/services/gen/UserTbl?filter=usrId=1';
    //var body = 'otpToken=' + otp + '&clientUid=' + clientId + '&userName=' + usrName + '&urtId=' + urtId;
    return this.http.get(link, { headers: headers }).map(res => res.json());
  }
}
