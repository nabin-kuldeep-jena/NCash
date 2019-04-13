import { Component } from '@angular/core';
import { NavController, MenuController, AlertController, NavParams, LoadingController, PopoverController } from 'ionic-angular';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
//import { StoreTbl } from '../../common/store-tbl';
import { StoreBranch } from '../../common/store-branch';
import { CityTbl } from '../../common/city-tbl';
import { ImageTbl } from '../../common/image-tbl';
import { ItemPage } from '../item/item';
import { ProfilePage } from '../profile/profile';
import { PopoverPage } from '../popover/popover';
import { StoreService } from '../../providers/store-service';
import { ResponseResolver } from '../../helpers/response-resolver';

@Component({
    selector: 'page-stores',
    templateUrl: 'stores.html',
    providers: [StoreService]
})

export class StoresPage {
    loading: any;
    queryText: string = "";
    selectedCity: string = "Bangalore";
    preferredCity: number;
    preferredState: number;
    storeBranches: StoreBranch[];
    temp_stores: StoreBranch[];
    cities: CityTbl[] = [];
    storeService: StoreService;
    isServerDown: boolean=false;

    constructor(public navCtrl: NavController, public navParams: NavParams, public menuCtrl: MenuController, public alertCtrl: AlertController, public http: Http, storeService: StoreService, public loader: LoadingController,public popoverCtrl: PopoverController) {
        this.storeBranches = new Array<StoreBranch>();
        this.temp_stores = new Array<StoreBranch>();
        this.storeService = storeService;
        // this.preferredCity = this.navParams.get('loggedInUsrId').usrCtyId;
        // this.preferredState = this.navParams.get('loggedInUsrId').usrSteId;
        this.preferredCity = this.navParams.get('preferredCityId');
        this.preferredState = this.navParams.get('preferredStateId');
    }

    ionViewDidLoad() {
       // this.fetchPreferredCity();
        this.fetchStores();
        this.fetchCities();
    }
    // fetchPreferredCity(){
    //    // this.mask()
    //     this.storeService.getPreferredCity(this.navParams.get('IP'), this.navParams.get('port'))
    //         .subscribe(data => {
    //             console.log("Returned data " + JSON.stringify(data));
    //             // this.stores = data;
    //             // this.temp_stores = this.stores;
    //             // this.mapResponseObjectsToClientOjects(data);
    //             // this.unmask();
    //         },
    //         err => {
    //             console.log("Error while Fetching Stores!!!")
    //             //this.unmask();
    //            this.handleAsyncCallError(err);
    //         });
    // }
    handleAsyncCallError(err) {
        if (!this.isServerDown) {
            ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
            this.isServerDown = true;
        }
    }
    fetchStores()
    {
        ///this.mask();
        this.storeBranches = new Array<StoreBranch>();
        this.temp_stores = new Array<StoreBranch>();
        this.storeService.getStoresForPreferredCity(this.navParams.get('IP'), this.navParams.get('port'), this.preferredCity)
            .subscribe(data => {
                //console.log("Returned data " + JSON.stringify(data));
                // this.stores = data;
                // this.temp_stores = this.stores;
                if (data.length > 0)
                    this.mapResponseObjectsToClientOjects(data);
                ///this.unmask();
            },
            err => {
                console.log("Error while Fetching Stores!!!")
               /// this.unmask();
                //ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl)
                this.handleAsyncCallError(err);
            });
    }
    mapResponseObjectsToClientOjects(data: any) {
        //var stores = JSON.parse(data._body);
      var isStoreInitialised:boolean=false;
        for (var index in data) {
            let storeBranch: StoreBranch = new StoreBranch();
            //let imageTbl: ImageTbl = new ImageTbl();
            storeBranch.storeTbl = data[index].properties.storeTbl;
            // if(!isStoreInitialised)
            // {
            //     isStoreInitialised=true;
            //     storeBranch.storeTbl.imageTbl.imgURL="http"
            // }
            //'http://' + IP + ':' + port + '/ncash/services/gen/StoreBranch?filter=address.cityTbl.ctyId='
           // storeBranch.storeTbl.imageTbl=imageTbl;
            storeBranch.storeTbl.imageTbl.imgURL='http://'+ this.navParams.get('IP') + ':' + this.navParams.get('port')+'/ncash'+storeBranch.storeTbl.imageTbl.imgPath;//+data[index].properties.storeTbl.logoImageTbl.imgPath;
            storeBranch.address=data[index].properties.address;
            storeBranch.stbId = data[index].id;
            storeBranch.stbOpeningHours = data[index].properties.stbOpeningHours;
            storeBranch.stbClosingHours = data[index].properties.stbClosingHours;
            storeBranch.isStoreOpenNow=data[index].properties.isStoreOpenNow;
            storeBranch.showDetails=false;
            storeBranch.detailIconName='md-arrow-dropdown';
            //.ctrDialCode=countries[index].properties.ctrDialCode
            this.storeBranches[index] = storeBranch;
            this.temp_stores=this.storeBranches;
        }
    }

    fetchCities()
    {
       /// this.mask();
        //let stateId: number = 1; // for 'Karnataka'
        this.storeService.getCitiesBasedOnPreferredState(this.navParams.get('IP'), this.navParams.get('port'), this.navParams.get('preferredStateId'))
            .subscribe(data => {
                console.log(JSON.stringify(data));
                if (data.length > 0)
                {
                	for ( var index in data)
                	{
                		//console.log(data[index].id);
                		console.log(data[index].properties.ctyName);
                		let city: CityTbl = new CityTbl();
                		city.ctyId = data[index].id;
                		city.ctyName = data[index].properties.ctyName;

                		this.cities.push(city);
                	}	
                }
                ///this.unmask();
            },
            err => {
                console.log("Error while Fetching Cities!!!")
                ///this.unmask();
            });
    }

    initializeStores() {
        this.storeBranches = this.temp_stores;
    }

    //Search method
    getItems(event: any) {
        this.initializeStores();

        let val = event.target.value;
        console.log("Search: " + val);

        if (val && val.trim() != '') {
            this.storeBranches = this.storeBranches.filter((storeBranch: StoreBranch) => {
                //return (storeBranch.storeTbl.strName.toLowerCase().indexOf(val.toLowerCase()) > -1);
                return (storeBranch.storeTbl.strName.toLowerCase().startsWith(val.toLowerCase()) || storeBranch.address.adrStreet4.toLowerCase().startsWith(val.toLowerCase()));
            })
        }
    }

    showCities() {
        if ( this.cities.length > 0)
        {
            console.log("In showCities()");
            this.queryText = "";
            let alert = this.alertCtrl.create();
            
            alert.setTitle('Choose City');

            for (let city of this.cities) {
                alert.addInput({
                    type: 'radio',
                    label: city.ctyName,
                    value: city.ctyId.toString(),
                    checked: this.selectedCity == city.ctyName ? true : false
                });
            }

            alert.addButton('Cancel');

            alert.addButton({
                text: 'OK',
                handler: data => {
                    console.log(data);
                    for(var city of this.cities)
                    {
                        if (city.ctyId == data) {
                            this.selectedCity = city.ctyName;
                            this.preferredCity = city.ctyId;
                        }
                    }
                    
                    this.fetchStores();
                }
            });

            alert.present();
        }        

    }

    gotoItemPage() {
        this.navCtrl.push(ItemPage);
    }

    mask()
    {
        this.loading = this.loader.create({
            content: `<ion-spinner name='crescent'></ion-spinner>`
        });

        this.loading.present();
    } 

    unmask()
    {
        this.loading.dismiss();
    }

    toggleDetails(storeBranch) {
    if (storeBranch.showDetails) {
        storeBranch.showDetails = false;
        storeBranch.detailIconName = 'md-arrow-dropdown';
    } else {
        storeBranch.showDetails = true;
        storeBranch.detailIconName = 'md-arrow-dropup';
    }
  }

  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(PopoverPage,{IP:this.navParams.get('IP'), port:this.navParams.get('port')});
    popover.present({
      ev: myEvent
    });
  }
}
