export class CountryTbl {
    private _ctrId: number; 
    private _ctrName: string;
    private _ctrDialCode: string;
    private _ctrMaxMobileNoLength: number;

    get ctrId(): number {
        return this._ctrId;
    }

    set ctrId(newCtrId: number) {
        this._ctrId = newCtrId;
    }
    get ctrName(): string {
        return this._ctrName;
    }

    set ctrName(newCtrName: string) {
        this._ctrName = newCtrName;
    }
    get ctrDialCode(): string {
        return this._ctrDialCode;
    }

    set ctrDialCode(newCtrDialCode: string) {
        this._ctrDialCode = newCtrDialCode;
    }

    get ctrMaxMobileNoLength(): number {
        return this._ctrMaxMobileNoLength;
    }

    set ctrMaxMobileNoLength(newCtrMaxMobileNoLength: number) {
        this._ctrMaxMobileNoLength = newCtrMaxMobileNoLength;
    }
}
