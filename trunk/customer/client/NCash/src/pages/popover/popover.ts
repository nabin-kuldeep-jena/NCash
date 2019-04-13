import { Component } from '@angular/core';
import { NavController,ViewController,NavParams } from 'ionic-angular';
import { ProfilePage } from '../profile/profile';
import { AboutPage } from '../about/about';

/*
  Generated class for the Popover page.

  See http://ionicframework.com/docs/v2/components/#navigation for more info on
  Ionic pages and navigation.
*/
@Component({
  selector: 'page-popover',
  templateUrl: 'popover.html'
})
export class PopoverPage {

  constructor(public navCtrl: NavController, public viewCtrl: ViewController,public navParams: NavParams) {}

  ionViewDidLoad() {
    console.log('Hello PopoverPage Page');
  }
  navigateToProfilePage() {
    this.navCtrl.push(ProfilePage,{ IP: this.navParams.get('IP'),
                             port: this.navParams.get('port')});
    this.viewCtrl.dismiss();
  }
  navigateToAboutPage() {
    this.navCtrl.push(AboutPage);
    this.viewCtrl.dismiss();
  }
  signOut(){

  }
}
