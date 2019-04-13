import {FormControl} from '@angular/forms';
import { SignupService } from '../../../providers/signup-service';

export class EmailValidator{
    signupService: SignupService;

    constructor(signupService: SignupService) {
        this.signupService = signupService;
    }
    validateEmailId(email: FormControl): any {
        console.log("In validator");
        if (this.signupService == undefined || email.value.length == 0)
            return null;
        else {
            this.signupService.checkEmail(email.value).subscribe(data => {
                console.log("success");
            }, err => {
                console.log("error");
            });
            return null;
        }
    }
}