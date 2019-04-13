export class ImageTbl{
    private _imgId:number;
    private _imgURL:string;
    private _imgName: string;
    private _imgPath: string;

    get imgId(): number {
        return this._imgId;
    }
    set imgId(imgId: number) {
        this._imgId = imgId;
    }
    get imgURL(): string {
        return this._imgURL;
    }
    set imgURL(imgURL: string) {
        this._imgURL = imgURL;
    }
    get imgName(): string {
        return this._imgName;
    }
    set imgName(imgName: string) {
        this._imgName = imgName;
    }
    get imgPath(): string {
        return this._imgPath;
    }
    set imgPath(imgPath: string) {
        this._imgPath = imgPath;
    }
}