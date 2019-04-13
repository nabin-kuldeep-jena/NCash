import { Component } from '@angular/core';
import { ViewController } from 'ionic-angular';

@Component({
    selector: 'popover',
    template: `
        <ion-list>
            <button ion-item (click)="close('SEARCH_VISIBLE')">show/hide search</button>
            <button ion-item (click)="close('CLEAR')">Clear Cart</button>
            <button ion-item (click)="close('TODO_LIST')">Todo List</button>
        </ion-list>
    `
})

export class PopoverPage {
    constructor(public viewCtrl: ViewController) {
    }

    close(val) {
        this.viewCtrl.dismiss(val);
    }
}
