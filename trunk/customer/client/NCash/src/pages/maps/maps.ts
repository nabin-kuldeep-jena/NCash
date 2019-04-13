import { Component, ViewChild, ElementRef } from '@angular/core';
import { NavController, Platform } from 'ionic-angular';
import { Geolocation } from 'ionic-native';

/*
  Generated class for the Maps page.

  See http://ionicframework.com/docs/v2/components/#navigation for more info on
  Ionic pages and navigation.
*/

declare var google: any;

@Component({
  selector: 'page-maps',
  templateUrl: 'maps.html'
})
export class MapsPage {
  @ViewChild('map') mapElement: ElementRef;
  map: any;
  constructor(public navCtrl: NavController, platform: Platform) {
    platform.ready().then(() => {
      this.loadMap();
    });
  }

  ionViewDidLoad() {
    //this.loadMap();
  }

  loadMap() {

    var hypericty = {
      info: '<strong>HyperCITY<strong><br>\
           Munnekollal',
      lat: 12.957899,
      lon: 77.716255
    };

    var namdharifresh = {
      info: '<strong>Namdhari\'s Fresh<strong><br>\
           Munnekollal',
      lat: 12.956661,
      lon: 77.714628
    };

    var starExtra = {
      info: '<strong>Star Extra<strong><br>\
           Munnekollal',
      lat: 12.955234,
      lon: 77.712437
    };
    var locations = [
      [hypericty.info, hypericty.lat, hypericty.lon],
      [namdharifresh.info, namdharifresh.lat, namdharifresh.lon],
      [starExtra.info, starExtra.lat, starExtra.lon]
    ];
    var infowindow = new google.maps.InfoWindow({});
    var marker, i;
    var options = {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    };
    
    Geolocation.getCurrentPosition(options).then((position) => {
      let latLng = new google.maps.LatLng(13.011096, 77.554990);
      //let latLng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
      let mapOptions = {
        center: latLng,
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      }
      this.map = new google.maps.Map(this.mapElement.nativeElement, mapOptions);
      for (i = 0; i < locations.length; i++) {
        marker = new google.maps.Marker({
          position: new google.maps.LatLng(locations[i][1], locations[i][2]),
          map: this.map
        });
        google.maps.event.addListener(marker, 'click', (function (marker, i) {
          return function name() {
            infowindow.setContent(locations[i][0]);
            infowindow.open(this.map, marker);
          }
        })(marker, i));
      }
    }, (err) => {
      console.log(err);
    });
//,options

    // for (i = 0; i < locations.length; i++) {
    //   marker = new google.maps.Marker({
    //     position: new google.maps.LatLng(locations[i][1], locations[i][2]),
    //     map: this.map
    //   });
    //   google.maps.event.addListener(marker, 'click', (function (marker, i) {
    //     return function name() {
    //       infowindow.setContent(locations[i][0]);
    //       infowindow.open(this.map, marker);
    //     }
    //   })(marker,i));
    // }
  }
}
