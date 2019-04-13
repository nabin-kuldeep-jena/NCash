import { Injectable } from '@angular/core';
import { Http ,Headers,URLSearchParams} from '@angular/http';
import 'rxjs/add/operator/map';

/*
  Generated class for the LocationService provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
export class LocationService {

  constructor(public http: Http) {
    console.log('Hello LocationService Provider');
  }
  public getCountries(IP, port) {
    var link = 'http://' + IP + ':' + port + '/ncash/services/gen/CountryTbl';
    let headers: Headers = new Headers;
    headers.append('Content-Type', 'application/json');
    headers.append('Accept', 'application/json');
    let params: URLSearchParams = new URLSearchParams();
    return this.http.get(link, { headers: headers });
  }
  public getCitiesBasedOnState(IP, port,steId) {
    var link = 'http://' + IP + ':' + port + '/ncash/services/gen/CityTbl?filter=steId='+steId;
    let headers: Headers = new Headers;
    headers.append('Content-Type', 'application/json');
    headers.append('Accept', 'application/json');
    let params: URLSearchParams = new URLSearchParams();
    return this.http.get(link, { headers: headers });
  }
  public getStatesBasedOnCountry(IP, port, ctrId) {
    //? filter = steId=1
    var link = 'http://' + IP + ':' + port + '/ncash/services/gen/State?filter=ctrId='+ctrId;
    let headers: Headers = new Headers;
    headers.append('Content-Type', 'application/json');
    headers.append('Accept', 'application/json');
    let params: URLSearchParams = new URLSearchParams();
    return this.http.get(link, { headers: headers });
  }
}
