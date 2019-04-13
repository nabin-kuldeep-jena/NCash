import { Injectable } from '@angular/core';
import { Http, URLSearchParams, Headers } from '@angular/http';
import 'rxjs/add/operator/map';
import { Observable } from 'rxjs/Rx';
import { NCashConstants } from '../helpers/ncash-constants';

@Injectable()
export class StoreService {
    stores: any;

    constructor(public http: Http) {}

    getStoresForPreferredCity(IP, port,preferredCity) {
        console.log("Store service");
        //var api = "http://" + IP + ":" + port + "/ncash/services/store/getAllPreferedStores?city=bangalore";
        var link = 'http://' + IP + ':' + port + '/ncash/services/gen/StoreBranch?filter=address.cityTbl.ctyId='+preferredCity+'&cmp=SOCDMC';
        let headers: Headers = new Headers;
        headers.append('Content-Type', 'application/json');
        headers.append('Accept', 'application/json');
        let params: URLSearchParams = new URLSearchParams();
        
        return this.http.get(link, { headers: headers }).map(res => res.json());
    }

    // getStoresForNewCity(IP, port, city) {
    //     var link = 'http://' + IP + ':' + port + '/ncash/services/store/getAllPreferedStores';
    //     let headers: Headers = new Headers;
    //     headers.append('Content-Type', 'application/json');
    //     let params: URLSearchParams = new URLSearchParams();
    //     return this.http.get(link, {
    //         headers: headers
    //     }).map(res => res.json());
    // }

    // getPreferredCity(IP, port) {
    //     console.log("Store service");
    //     var link = 'http://' + IP + ':' + port + '/ncash/services/gen/StoreTbl?filter=address.city.ctyId=';
    //     let headers: Headers = new Headers;
    //     headers.append('Content-Type', 'application/json');
    //     headers.append('Accept', 'application/json');
    //     let params: URLSearchParams = new URLSearchParams();
        
    //     return this.http.get(link, { headers: headers }).map(res => res.json());
    // }

    getCitiesBasedOnPreferredState(ip, port, stateId) {
        console.log("Fetching all cities...");
        var api = 'http://' + ip + ':' + port + '/ncash/services/gen/CityTbl?filter=steId=' + stateId;
        let headers: Headers = new Headers;
        headers.append('Content-Type', 'application/json');
        headers.append('Accept', 'application/json');
        let params: URLSearchParams = new URLSearchParams();
        
        return this.http.get(api, { headers: headers }).map(res => res.json());        
    }

}