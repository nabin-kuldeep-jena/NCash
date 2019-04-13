import { Component } from '@angular/core';
import { NavController, AlertController, NavParams } from 'ionic-angular';
import { UserTbl } from '../../common/user-tbl';
import { CityTbl } from '../../common/city-tbl';
import { State } from '../../common/state';
import { CountryTbl } from '../../common/country-tbl';
import { ChangePasswordPage } from './changePassword';
import { DeactivateAccountPage } from '../deactivateAccount/deactivateAccount';
import { ProfileService} from '../../providers/profile-service';
import { LocationService} from '../../providers/location-service';
import {PreferredLocationHelper} from '../../helpers//preferred-location-helper';
import { ResponseResolver } from '../../helpers/response-resolver';

@Component({
    selector: 'page-profile',
    templateUrl: 'profile.html',
    providers: [ProfileService,LocationService]
})

export class ProfilePage {
    loggedInUser: UserTbl;
    profileService: ProfileService;
    cityTbl: CityTbl;
    state :State;
    countryTbl: CountryTbl;
    selectedCtrId:number;
    selectedSteId:number;
    selectedCtyId:number;
    locationService: LocationService;
    countries: CountryTbl[];
    states: State[];
    cities: CityTbl[];
    responseErrMessage: string;
    isEmailFieldInEditMode: boolean;

    constructor(public navCtrl: NavController, public navParams: NavParams,public alertCtrl: AlertController, profileService: ProfileService, locationService: LocationService) {
        this.locationService=locationService;

        this.loggedInUser=new UserTbl();
        
        this.countryTbl=new CountryTbl();
        this.countryTbl.ctrId=1;
        this.countryTbl.ctrName='India';       

        this.state=new State();
        this.state.steName='Karnataka';
        this.state.steId=1;
        this.state.countryTbl=this.countryTbl;
        
        this.cityTbl=new CityTbl;
        this.cityTbl.ctyId=1;
        this.cityTbl.ctyName='Bangalore';
        this.cityTbl.state=this.state;

        this.loggedInUser.usrEmailAddress='sonali.subash21@gmail.com',
        this.loggedInUser.usrForename='Sonali',
        this.loggedInUser.usrSurname='Tulaskar',
        this.loggedInUser.usrMobNo='9743922776',
        this.loggedInUser.cityTbl=this.cityTbl,
        this.loggedInUser.usrPassword='welcome',
        this.loggedInUser.usrEmailVerifyFl=false;
        this.profileService=profileService;

        this.selectedCtrId=this.loggedInUser.cityTbl.state.countryTbl.ctrId; 
        this.selectedSteId=this.loggedInUser.cityTbl.state.steId;
        this.selectedCtyId=this.loggedInUser.cityTbl.ctyId;
         this.countries = new Array<CountryTbl>();
         this.states=new Array<State>();
         this.cities=new Array<CityTbl>();

         this.isEmailFieldInEditMode=false;
    }

    ionViewDidLoad() {
        this.locationService.getCountries(this.navParams.get('IP'), this.navParams.get('port')).subscribe(
            data => {
                PreferredLocationHelper.getInstance().initialiseCountries(data, this.countries);
            },
            err => {
                this.responseErrMessage=ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
            }
        );
        this.fetchStates();
        this.fetchCities();
    }

    saveSettings() {
        
    }

    changePassword()
    {
        this.navCtrl.push(ChangePasswordPage, {oldPwd: this.loggedInUser.usrPassword});
        // let pwdAlert=this.alertCtrl.create();
        // pwdAlert.setTitle('Change Password');
        // pwdAlert.addButton('Cancel');
        // pwdAlert.addButton('Change');
        
        // pwdAlert.addInput({
        //     type:'password',
        //     placeholder:'Old Password'
        // });
        // pwdAlert.addInput({
        //     type:'password',
        //     placeholder:'New Password'
        // });
        // pwdAlert.addInput({
        //     type:'password',
        //     placeholder:'Confirm Password'
        // });
        // pwdAlert.present();
    }

    deactivateAccount()
    {
    	this.navCtrl.push(DeactivateAccountPage);
    }
   
    onChangeCountry() {
        this.states = new Array<State>();
        this.cities=new Array<CityTbl>();
        this.fetchStates();
    }
    fetchStates(){
        this.locationService.getStatesBasedOnCountry(this.navParams.get('IP'), this.navParams.get('port'), this.selectedCtrId).subscribe(
            data => {
                
                PreferredLocationHelper.getInstance().initialiseStates(data, this.states);
            },
            err => {
                this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
            }
        );
    }
    onChangeState() {
        this.fetchCities();
    }
    fetchCities(){
         this.locationService.getCitiesBasedOnState(this.navParams.get('IP'), this.navParams.get('port'), this.selectedSteId).subscribe(
             data => { 
                 this.cities = new Array<CityTbl>();
                 PreferredLocationHelper.getInstance().initialiseCities(data, this.cities);
             },
             err => {
                 console.log("Get cities errors")
                 this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
             }
         );
    }
    changeCountry(){

    }
    editEmail(){
        this.isEmailFieldInEditMode=true;
        // let alert=this.alertCtrl.create();
        // alert.setTitle('New Email');
        // alert.addButton('Cancel');
        // alert.addButton('Ok');
        
        
        // pwdAlert.addInput({
        //     type:'password',
        //     placeholder:'Confirm Password'
        // });
        // pwdAlert.present();
    }
    updateEmail(){
        this.isEmailFieldInEditMode=false;
    }
}
