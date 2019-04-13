export class Helper {
	//static tempData: any = null;
	static helper: Helper= null;

	static getInstance() {
		if ( this.helper == null)
			this.helper = new Helper();
		return this.helper;
	}

	copyProducts(original, clone) {
  		for (let item of original) {
  			clone.push(item);
  		}
    }

    /*static getSQLiteDBHandler(db:SQLite, dbName) {
		console.log("Opening new DB connection");

		db = new SQLite();	
		
		db.openDatabase({
			name: dbName,
			location: 'default'
		}).then(() => {
			console.log("DB connection opened successfully")
		});
	}

	static fetchData(db: SQLite, tbl) {

		console.log("Fetching data from: " + tbl);
		let query: string = "SELECT * FROM " + tbl;
		console.log(query);

		db.executeSql(query , [])
			.then((data) => {
				console.log("fetched successfully!")
				console.log("fetched: " + JSON.stringify(data));
				this.tempData = data;
			}, (error) => {
				console.log("Error: " + JSON.stringify(error));
			});
			
		return this.tempData;
	}*/
}