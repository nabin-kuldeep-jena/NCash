import { FormControl } from '@angular/forms';
//import { Control } from '@angular/common';
import { SignupService } from '../../../providers/signup-service';

export class PhoneNoValidator {

    signupService: SignupService;

    constructor(signupService: SignupService) {
        this.signupService = signupService;
    }
    validatePhoneNo(control: FormControl): any {
        console.log("In validator");
        if (this.signupService == undefined || control.value.length == 0)
            return null;
        else {
            this.signupService.checkMobileNo(control.value).subscribe(data => {
                console.log("success");
            }, err => {
                console.log("error");
            });
            return null;
        }
    }
}