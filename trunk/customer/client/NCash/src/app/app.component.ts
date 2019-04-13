import { Component, ViewChild } from '@angular/core';
import { Nav, Platform } from 'ionic-angular';
import { StatusBar, Splashscreen } from 'ionic-native';

import { LoginPage } from '../pages/login/login';
import { ItemPage } from '../pages/item/item';
import { ProfilePage } from '../pages/profile/profile';
import { PaymentPage } from '../pages/payment/payment';
import { AboutPage } from '../pages/about/about';
import { TodoListPage } from '../pages/todolist/todolist';

@Component({
    templateUrl: 'app.html'
})

export class MyApp {
    @ViewChild(Nav) nav: Nav;
    rootPage: any = LoginPage;
    pages: Array<{title: string, component: any}>;

    constructor(public platform: Platform) {
        this.initializeApp();

        this.pages = [
            { title: 'Items', component: ItemPage },
            { title: 'Payment', component: PaymentPage },
            { title: 'To-Do List', component: TodoListPage },
            { title: 'My Account', component: ProfilePage },
            { title: 'About', component: AboutPage }
        ];
    }

    initializeApp() {
        this.platform.ready().then(() => {
            // Okay, so the platform is ready and our plugins are available.
            // Here you can do any higher level native things you might need.
            StatusBar.styleDefault();
            Splashscreen.hide();
        });
    }

    openPage(page) {
        // Reset the content nav to have just this page
        // we wouldn't want the back button to show in this scenario
        this.nav.setRoot(page.component);
    }
}
