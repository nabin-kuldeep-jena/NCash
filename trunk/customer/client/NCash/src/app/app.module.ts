import { NgModule, ErrorHandler } from '@angular/core';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';
//import {ToolTipModule} from 'angular2-tooltip'

import { MyApp } from './app.component';
import { AboutPage } from '../pages/about/about';
import { ContactPage } from '../pages/contact/contact';
import { ItemPage } from '../pages/item/item';
import { ItemDetails } from '../pages/item/item';
import { TabsPage } from '../pages/tabs/tabs';
import { LoginPage } from '../pages/login/login';
import { SignupPage } from '../pages/signup/signup';
import { StoresPage} from '../pages/stores/stores';
import { TodoListPage } from '../pages/todolist/todolist';
import { OtpPage} from '../pages/otp/otp';
import { ProfilePage } from '../pages/profile/profile';
import { SearchPage } from '../pages/search/search';
import { PaymentPage } from '../pages/payment/payment';
import { VerifyMobNumPage } from '../pages/verifymobnum/verifymobnum';
import { ForgotPasswordPage } from '../pages/forgotpassword/forgotpassword';
//import { PopoverPage } from '../pages/item/popover';
import { ChangePasswordPage } from '../pages/profile/changePassword';
import { DeactivateAccountPage } from '../pages/deactivateAccount/deactivateAccount';
import { PopoverPage } from '../pages/popover/popover';
import { OrdersummaryPage } from '../pages/ordersummary/ordersummary'

@NgModule({
  	declarations: [ MyApp, AboutPage, ContactPage, ItemPage, TabsPage, LoginPage, SignupPage, StoresPage, TodoListPage, OtpPage, ProfilePage, SearchPage, ItemDetails, PaymentPage, VerifyMobNumPage, ForgotPasswordPage ,PopoverPage, ChangePasswordPage, DeactivateAccountPage,OrdersummaryPage],
  	imports: [ IonicModule.forRoot(MyApp,
  		{
	        scrollAssist: false, 
	        autoFocusAssist: false
	    })
	],
  	bootstrap: [ IonicApp ],
  	entryComponents: [ MyApp, AboutPage, ContactPage, ItemPage, TabsPage, LoginPage, SignupPage, StoresPage, TodoListPage, OtpPage, ProfilePage, SearchPage, ItemDetails, PaymentPage, VerifyMobNumPage, ForgotPasswordPage ,PopoverPage, ChangePasswordPage, DeactivateAccountPage,OrdersummaryPage],
   	providers: [ { provide: ErrorHandler, useClass: IonicErrorHandler } ]
})

export class AppModule {}
