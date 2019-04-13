import {State} from './state';

export class CityTbl {
    private _ctyId: number;
    private _ctyName: string;
    private _state: State;

    get ctyId(): number {
        return this._ctyId;
    }

    set ctyId(newCtyId: number) {
        this._ctyId = newCtyId;
    }

    get ctyName(): string {
        return this._ctyName;
    }

    set ctyName(newCtyName: string) {
        this._ctyName = newCtyName;
    }

    get state(): State {
        return this._state;
    }

    set state(state: State) {
        this._state = state;
    }
}
