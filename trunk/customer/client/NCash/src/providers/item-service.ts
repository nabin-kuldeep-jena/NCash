import { Injectable } from '@angular/core';
import { Http, URLSearchParams, Headers } from '@angular/http';
import 'rxjs/add/operator/map';
import { Observable } from 'rxjs/Rx';
import { NCashConstants } from '../helpers/ncash-constants';

@Injectable()
export class ItemService {
    stores: any;

    constructor(public http: Http) {}
  
    getItem(ip, port, storeId, itemId) {
        console.log("Fetching Item details...");
        var api = 'http://' + ip + ':' + port + '/ncash/services/product/' + storeId + '/' + itemId;
        let headers: Headers = new Headers;
        headers.append('Content-Type', 'application/json');
        headers.append('Accept', 'application/json');
        let params: URLSearchParams = new URLSearchParams();
        
        return this.http.get(api, { headers: headers }).map(res => res.json());        
    }

}