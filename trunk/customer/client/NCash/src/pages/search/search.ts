import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { ItemPage } from '../item/item';

/*
  Generated class for the Search page.

  See http://ionicframework.com/docs/v2/components/#navigation for more info on
  Ionic pages and navigation.
*/
@Component({
  selector: 'page-search',
  templateUrl: 'search.html'
})
export class SearchPage {
  searchQuery: string = '';
  public storeBranches:any;

  constructor(public navCtrl: NavController) {
    //this.storeBranches = new Array<Object>();
  }

  ionViewDidLoad() {
    console.log('Hello SearchPage Page');
    this.initialiseAccounts();
  }
  initialiseAccounts(){
     this.storeBranches = [
      { strName: 'Hypercity', strBranch: 'Kundalhalli gate', strBranchImage: 'img/16.jpg', strBranchOpenTime: '10:00 AM', strBranchCloseTime: '10:00 PM', strDistance: '3Km' },
      { strName: 'More', strBranch: 'Marathalli', strBranchImage: 'img/b6.png', strBranchOpenTime: '10:00 AM', strBranchCloseTime: '10:00 PM', strDistance: '4Km' },
      { strName: 'Hypercity', strBranch: 'InOrbit Mall', strBranchImage: 'img/b25.jpg', strBranchOpenTime: '10:00 AM', strBranchCloseTime: '10:00 PM', strDistance: '4.5Km' },
      { strName: 'More', strBranch: 'Mahadevpura', strBranchImage: 'img/b29.jpg', strBranchOpenTime: '10:00 AM', strBranchCloseTime: '10:00 PM', strDistance: '4.53Km' }
    ];
    }
  searchStores(event) {
    var val = event.target.value;
    if( val && val.trim()!='')
    {
      this.storeBranches=this.storeBranches.filter((storeBranch)=>{
        return (storeBranch.strName.toLowerCase().indexOf(val.toLowerCase())>-1);
      })
    }
    else
    {
      this.initialiseAccounts();
    }
  }
  onCancel(event) {

  }
  onStoreCardClick()
  {
    this.navCtrl.push(ItemPage);
  }
}
