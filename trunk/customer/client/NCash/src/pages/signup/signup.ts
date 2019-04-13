import { Component } from '@angular/core';
import { Validators, FormBuilder } from '@angular/forms';
import { NavController, Platform, NavParams,AlertController } from 'ionic-angular';
//import { OtpPage } from '../otp/otp';
import { Http } from '@angular/http';
import { SignupService } from '../../providers/signup-service'
import { ResponseResolver } from '../../helpers/response-resolver';
import { OTPHelper } from '../../helpers/otp-helper';
import { CityTbl } from '../../common/city-tbl';
import { State } from '../../common/state';
import { CountryTbl } from '../../common/country-tbl';
import { OtpService} from '../../providers/otp-service'
import { LocationService} from '../../providers/location-service'
import { StoresPage } from '../stores/stores';
import {PreferredLocationHelper} from '../../helpers//preferred-location-helper'

@Component({
    selector: 'page-signup',
    templateUrl: 'signup.html',
    providers: [SignupService,OtpService,LocationService]
})

export class SignupPage {
    ctrDialCode: string;   //To be removed once used internationally
    countries: CountryTbl[];
    cities: CityTbl[];
    states: State[];
    signUpForm: {
        firstName?: string,
        lastName?: string,
        mobileNumber?: number,
        email?: string,
        password?: string,
        confirmPassword?: string,
        preferredCity?: CityTbl,
        preferredState?: State,
        preferredCountry?: CountryTbl
    } = {};

    isSignedUp: boolean;
    signupService: SignupService;
    otpService: OtpService;
    locationService: LocationService;
    responseErrMessage: string;
    unSuccesfulRegistration: boolean;
    selectedCtyId:number;
    selectedSteId:number;
    selectedCtrId:number;

    isStateDisabled:boolean;
    isCityDisabled:boolean;
    isCountryDisabled:boolean;

    maxMobileNoLength: number;
    constructor(public navCtrl: NavController, public navParams: NavParams, public formBuilder: FormBuilder, public http: Http, signupService: SignupService,otpService: OtpService,locationService: LocationService, public alertCtrl: AlertController, public platform: Platform) {
        this.http = http;
        this.isSignedUp = false;
        this.signupService = signupService;
        this.otpService = otpService;
        this.locationService=locationService;
        this.unSuccesfulRegistration = false;
        this.maxMobileNoLength=0;
        this.signUpForm = formBuilder.group({
            firstName: ['Sonali', Validators.compose([Validators.required, Validators.pattern('[a-zA-Z]+([ ]?[a-zA-Z]+[ ]*)*'), Validators.maxLength(30)])],
            lastName: ['Tulaskar', Validators.compose([Validators.required, Validators.pattern('[a-zA-Z]+([ ]?[a-zA-Z]+[ ]*)*'), Validators.maxLength(30)])],
            mobileNumber: [9743922776, Validators.compose([Validators.required, Validators.minLength(10), Validators.maxLength(10)])],
            email: ['sonali.subash21@gmail.com', Validators.compose([Validators.required, Validators.pattern('^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+[.][a-zA-Z0-9-.]+$')])],
            password: ['welcome', Validators.compose([Validators.required, Validators.minLength(6)])],
            confirmPassword: ['welcome', Validators.compose([Validators.required, Validators.minLength(6)])],
            preferredCity: ['', Validators.required],
            preferredState: ['', Validators.required],
            preferredCountry: ['', Validators.required]
        });
        this.cities = new Array<CityTbl>();
        this.states = new Array<State>();
        this.countries = new Array<CountryTbl>();
        this.ctrDialCode="+91";
        this.selectedSteId=0;
         //this.selectedCtyId=1; 
         this.selectedCtrId=0; 
         this.isStateDisabled=true;
         this.isCityDisabled=true;
         this.isCountryDisabled=true;
    }
    ionViewDidLoad() {
        this.locationService.getCountries(this.navParams.get('IP'), this.navParams.get('port')).subscribe(
            data => {
                PreferredLocationHelper.getInstance().initialiseCountries(data,this.countries);
                this.isCountryDisabled=this.countries.length==0;
            },
            err => {
                 this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
            }
        );
    }

    onChangeCountry() {
        for(var index in this.countries){
            if(this.countries[index].ctrId==this.selectedCtrId)
            {
                this.ctrDialCode = this.countries[index].ctrDialCode;
                this.signUpForm.preferredCountry= this.countries[index];/////
                this.maxMobileNoLength=this.countries[index].ctrMaxMobileNoLength;///////
            }
        }
        this.locationService.getStatesBasedOnCountry(this.navParams.get('IP'), this.navParams.get('port'), this.selectedCtrId).subscribe(
            data => {
                 this.states = new Array<State>();
                 PreferredLocationHelper.getInstance().initialiseStates(data,this.states);
                this.isStateDisabled=this.states.length==0;
            },
            err => {
                console.log("Get states errors")
                 this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
            }
        );
    }
   
    //  initialiseStates(data: any) {
    //      this.states = new Array<State>();
    //      this.cities=new Array<CityTbl>();
    //      this.isCityDisabled=true;
    //      var states = JSON.parse(data._body);
    //      for (var index in states) {
    //          let state: State = new State();
    //          state.steName = states[index].properties.steName;
    //          state.steId = states[index].id;
    //          this.states[index] = state;
    //      }
    // }
     onChangeState() {
        // this.isStateNull = this.selectedSteId == 0;
         this.locationService.getCitiesBasedOnState(this.navParams.get('IP'), this.navParams.get('port'), this.selectedSteId).subscribe(
             data => {
                 this.cities = new Array<CityTbl>();
                 PreferredLocationHelper.getInstance().initialiseCities(data, this.cities);
                 this.isCityDisabled = this.cities.length == 0;
             },
             err => {
                 console.log("Get cities errors")
                 this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
             }
         );
     }
    
    doSignUp(form) {
        if (form.controls.email.errors || form.controls.password.errors)
            return;
        else {
            let mobileNum = form.controls.mobileNumber.value;
            let uidToken: string = OTPHelper.getInstance().generateClientUID(mobileNum);
            var myObj = {
                "urtName": form.controls.mobileNumber.value,
                "urtPassword": form.controls.password.value,
                "urtForename": form.controls.firstName.value,
                "urtSurname": form.controls.lastName.value,
                "urtEmailAddress": form.controls.email.value,
                "urtMobNo": form.controls.mobileNumber.value,
                //"urtCountryNoCode": this.ctrDialCode,
                "ctyId":form.controls.preferredCity.value,
                "uidToken": uidToken
            };
            this.signupService.registerUser(myObj,this.navParams.get('IP'), this.navParams.get('port') ).subscribe(
                data => {
                    // this.navCtrl.push(OtpPage,
                    //     {
                    //         clientId: uidToken,
                    //         usrName: form.controls.firstName.value,
                    //         urtId: data.responseData.urtId,
                    //         IP: this.navParams.get('IP'),
                    //         port: this.navParams.get('port')
                    //     });

                     let otpAlert=this.alertCtrl.create();
                     otpAlert.setTitle('OTP');
                     otpAlert.addInput({
                         type:'text',
                         value:''
                     });
                    // otpAlert.setBackButtonText('Close');
                     otpAlert.addButton({
                         text:'Resend',
                        handler:value=>{
                             this.otpService.resendOtp(this.navParams.get('IP'), this.navParams.get('port'),  uidToken, form.controls.firstName.value,data.responseData.urtId).subscribe(
                                 data => {
                                     this.navCtrl.push(StoresPage);
                                 }, err => {
                                     this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
                                     this.unSuccesfulRegistration = true;
                                 }
                             );
                        }});
                     otpAlert.addButton({
                         text: 'Verify',
                         handler: value => {
                             this.otpService.verifyOtp(this.navParams.get('IP'), this.navParams.get('port'), value, uidToken, form.controls.firstName.value,data.responseData.urtId).subscribe(
                                 data => {
                                     this.navCtrl.push(StoresPage);
                                 }, err => {
                                     this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
                                     this.unSuccesfulRegistration = true;
                                 }
                             );
                         }
                        });
                     otpAlert.present();
                }, err => {
                    this.responseErrMessage = ResponseResolver.getInstance().resolveResponse(err, this.alertCtrl);
                    this.unSuccesfulRegistration = true;
                }
            );
        }
    }
    onFocus(event) {
        this.unSuccesfulRegistration = false;
    }
    onMobileNoFocusOut(event){
        if(event.target.value.length>3)
       // this.signUpForm.mobileNumber.{}
       {}

    }
}