import { ImageTbl } from './image-tbl';

export class StoreTbl {
    private _strId: number; 
    private _strName: string;
    private _imageTbl: ImageTbl;

    get strId(): number {
        return this._strId;
    }
    set strId(newStrId: number) {
        this._strId = newStrId;
    }

    get strName(): string {
        return this._strName;
    }
    set strName(newStrName: string) {
        this._strName = newStrName;
    }

    get imageTbl(): ImageTbl {
        return this._imageTbl;
    }
    set imageTbl(imageTbl: ImageTbl) {
        this._imageTbl = imageTbl;
    }

}
