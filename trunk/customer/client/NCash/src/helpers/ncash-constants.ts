export class NCashConstants {
	static ITEM_EXIST: string = "Item Already Added!";
	static CLEAR_CART: string = "Are you sure you want to clear cart?";
	static DELETE_ITEM_FROM_CART: string = "Are you sure you want to remove this item from cart?";
	static SEARCH_VISIBLE: string = "SEARCH_VISIBLE";
	static CLEAR: string = "CLEAR";
	static TODO_LIST: string = "TODO_LIST";

	static LOCAL_STORAGE_DB = 'ncash.db';
	static TODO_LIST_TBL = 'todolist_tbl';
	
	//API Related constants
	static IMAGES_BASE_URL='http://localhost:8080/ncash';
	static BASE_URL = 'http://localhost:8080/ncash/services';
	static CHANGE_PASSWORD_URL = '/changePassword';
	static HTTP = "http://";
	
	//Login constants
	static ALERT_KEY = "ALERT";	
}