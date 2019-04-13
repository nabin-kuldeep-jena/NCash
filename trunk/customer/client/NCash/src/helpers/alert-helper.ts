import { AlertController } from 'ionic-angular';

export class AlertHelper {

    static helper: AlertHelper = null;

    static getInstance() {
        if (this.helper == null)
            this.helper = new AlertHelper();
        return this.helper;
    }

    showAlert(alertCtrl: AlertController, alertTitle: string, alertSubTitle: string, alertButtons: string[]) {
        let alert = alertCtrl.create({
            title: alertTitle,
            subTitle: alertSubTitle,
            buttons: alertButtons
        });
        alert.present();
    }
}