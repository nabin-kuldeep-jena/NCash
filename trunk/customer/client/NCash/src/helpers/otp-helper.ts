export class OTPHelper {

    static helper: OTPHelper = null;

    static getInstance() {
        if (this.helper == null)
            this.helper = new OTPHelper();
        return this.helper;
    }
    generateClientUID(mobNum): string {
        // let timestamp = new Date().getUTCMilliseconds();
        // let otp: string = (timestamp * mobNum).t]oString().substring(0, 6);
        let randomKeyMap = new Map();
        randomKeyMap.set(1, 'A');
        randomKeyMap.set(2, 'B');
        randomKeyMap.set(3, 'C');
        randomKeyMap.set(4, 'D');
        randomKeyMap.set(5, 'E');
        randomKeyMap.set(6, 'F');
        randomKeyMap.set(7, 'G');
        randomKeyMap.set(8, 'H');
        randomKeyMap.set(9, 'I');
        randomKeyMap.set(10, 'J');
        randomKeyMap.set(11, 'K');
        randomKeyMap.set(12, 'L');
        randomKeyMap.set(13, 'M');
        randomKeyMap.set(14, 'N');
        randomKeyMap.set(15, 'O');
        randomKeyMap.set(16, 'P');
        randomKeyMap.set(17, 'Q');
        randomKeyMap.set(18, 'R');
        randomKeyMap.set(19, 'S');
        randomKeyMap.set(20, 'T');
        randomKeyMap.set(21, 'U');
        randomKeyMap.set(22, 'V');
        randomKeyMap.set(23, 'W');
        randomKeyMap.set(24, 'X');
        randomKeyMap.set(25, 'Y');
        randomKeyMap.set(26, 'Z');
        randomKeyMap.set(27, '2');
        randomKeyMap.set(28, '3');
        randomKeyMap.set(29, '4');
        randomKeyMap.set(30, '5');
        randomKeyMap.set(31, '6');
        randomKeyMap.set(32, '7');
        //Math.floor for int
        //below stmt for min and max inclusion(max-min+1)+min
        // let randomNo=Math.floor(Math.random()*(7-5+1))+2;
        // let otp = randomKeyMap.get(randomNo);
        // randomNo=Math.floor(Math.random()*(7-5+1))+2;
        // otp=otp+randomKeyMap.get(randomNo);
        let otp: string;
        for (var i = 0; i < 4; i++) {
            let randomNo = Math.floor(Math.random() * (31 + i)) + 1;
            if (i == 0)
                otp = randomKeyMap.get(randomNo);
            else
                otp = otp + randomKeyMap.get(randomNo);
        }
        // let otp: number = Math.floor(Math.random() * 90000) + 10000;
        return otp;
    }
}