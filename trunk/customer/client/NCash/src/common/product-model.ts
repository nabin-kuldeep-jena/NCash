export class ProductModel {
	private _prdId: number;
	private _prdName: string;
	private _prdCode: string;
	private _prdDesc: string;
	private _prdPrice: number;
	private _prdMfgDate: string;
	private _prdExpiryDate: string;

	get prdId(): number {
		return this._prdId;
	}

	set prdId(prdId: number){
		this._prdId = prdId;
	}

	get prdName(): string {
		return this._prdName;
	}

	set prdName(prdName: string){
		this._prdName = prdName;
	}

	get prdCode(): string {
		return this._prdCode;
	}

	set prdCode(prdCode: string){
		this._prdCode = prdCode;
	}

	get prdDesc(): string {
		return this._prdDesc;
	}

	set prdDesc(prdDesc: string){
		this._prdDesc = prdDesc;
	}

	get prdPrice(): number {
		return this._prdPrice;
	}

	set prdPrice(prdPrice: number){
		this._prdPrice = prdPrice;
	}

	get prdMfgDate(): string {
		return this._prdMfgDate;
	}

	set prdMfgDate(prdMfgDate: string){
		this._prdMfgDate = prdMfgDate;
	}

	get prdExpiryDate(): string {
		return this._prdExpiryDate;
	}

	set prdExpiryDate(expiry: string){
		this._prdExpiryDate = expiry;
	}

}