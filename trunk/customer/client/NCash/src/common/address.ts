export class Address {
    private _adrId: number;
    private _adrStreet1: string;
    private _adrStreet2: string;
    private _adrStreet3: string;
    private _adrStreet4: string;
    private _adrPostcode: string; 

    get adrId(): number {
        return this._adrId;
    }

    set adrId(newAdrId: number) {
        this._adrId = newAdrId;
    }

    get adrStreet1(): string {
        return this._adrStreet1;
    }

    set adrStreet1(newAdrStreet1: string) {
        this._adrStreet1 = newAdrStreet1;
    }

     get adrStreet2(): string {
        return this._adrStreet2;
    }

    set adrStreet2(newAdrStreet2: string) {
        this._adrStreet2= newAdrStreet2;
    }

     get adrStreet3(): string {
        return this._adrStreet3;
    }

    set adrStreet3(newAdrStreet3: string) {
        this._adrStreet3 = newAdrStreet3;
    }

     get adrStreet4(): string {
        return this._adrStreet4;
    }

    set adrStreet4(newAdrStreet4: string) {
        this._adrStreet4 = newAdrStreet4;
    }

     get adrPostcode(): string {
        return this._adrPostcode;
    }

    set adrPostcode(newAdrPostcode: string) {
        this._adrPostcode = newAdrPostcode;
    }

}