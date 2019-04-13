import {CityTbl} from './city-tbl'

export class UserTbl {
    private _usrId: number;
    private _usrName: string;
    private _cityTbl: CityTbl;
    private _usrMobNo: string;
    private _usrEmailAddress: string;
    private _usrForename: string;
    private _usrSurname: string;
    private _usrPassword: string;
    private _usrEmailVerifyFl: boolean;

    get usrId(): number {
        return this._usrId;
    }

    set usrId(usrId: number) {
        this._usrId = usrId;
    }

    get usrName(): string {
        return this._usrName;
    }

    set usrName(usrName: string) {
        this._usrName = usrName;
    }

    get cityTbl(): CityTbl {
        return this._cityTbl;
    }

    set cityTbl(cityTbl: CityTbl) {
        this._cityTbl = cityTbl;
    }

    get usrMobNo(): string {
        return this._usrMobNo;
    }

    set usrMobNo(usrMobNo: string) {
        this._usrMobNo = usrMobNo;
    }

    get usrEmailAddress(): string {
        return this._usrEmailAddress;
    }

    set usrEmailAddress(usrEmailAddress: string) {
        this._usrEmailAddress = usrEmailAddress;
    }

     get usrForename(): string {
        return this._usrForename;
    }

    set usrForename(usrForename: string) {
        this._usrForename = usrForename;
    }

    get usrSurname(): string {
        return this._usrSurname;
    }

    set usrSurname(usrSurname: string) {
        this._usrSurname = usrSurname;
    }

    get usrPassword(): string {
        return this._usrPassword;
    }

    set usrPassword(usrPassword: string) {
        this._usrPassword = usrPassword;
    }

    set usrEmailVerifyFl(usrEmailVerifyFl: boolean) {
        this._usrEmailVerifyFl = usrEmailVerifyFl;
    }

    get usrEmailVerifyFl(): boolean {
        return this._usrEmailVerifyFl;
    }
}
