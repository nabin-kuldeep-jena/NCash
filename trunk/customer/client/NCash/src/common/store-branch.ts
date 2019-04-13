import { StoreTbl } from './store-tbl';
import { Address } from './address';

export class StoreBranch {
    private _stbId: number;
    private _storeTbl: StoreTbl;
    private _address: Address;
    private _stbOpeningHours: number;
    private _stbClosingHours: number;
    private _isStoreOpenNow: boolean;
    private _showDetails: boolean;
    private _detailIconName:string; 

    get stbId(): number {
        return this._stbId;
    }

    set stbId(stbId: number) {
        this._stbId = stbId;
    }
    get storeTbl(): StoreTbl {
        return this._storeTbl;
    }

    set storeTbl(storeTbl: StoreTbl) {
        this._storeTbl = storeTbl;
    }

    get address(): Address {
        return this._address;
    }

    set address(address: Address) {
        this._address = address;
    }
    get stbOpeningHours(): number {
        return this._stbOpeningHours;
    }

    set stbOpeningHours(stbOpeningHours: number) {
        this._stbOpeningHours = stbOpeningHours;
    }
    get stbClosingHours(): number {
        return this._stbClosingHours;
    }

    set stbClosingHours(stbClosingHours: number) {
        this._stbClosingHours = stbClosingHours;
    }
     get isStoreOpenNow(): boolean {
        return this._isStoreOpenNow;
    }

    set isStoreOpenNow(isStoreOpenNow: boolean) {
        this._isStoreOpenNow = isStoreOpenNow;
    }

     get showDetails(): boolean {
        return this._showDetails;
    }

    set showDetails(showDetails: boolean) {
        this._showDetails = showDetails;
    }

     get detailIconName(): string {
        return this._detailIconName;
    }

    set detailIconName(detailIconName: string) {
        this._detailIconName = detailIconName;
    }
}
