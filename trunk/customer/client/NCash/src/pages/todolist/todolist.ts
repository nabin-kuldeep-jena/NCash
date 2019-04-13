import { Component } from '@angular/core';
import { SQLite } from 'ionic-native';
import { AlertController } from 'ionic-angular';

import { Task } from '../../interfaces/Task';
import { NCashConstants } from '../../helpers/ncash-constants';

@Component({
	templateUrl: 'todolist.html'
})

export class TodoListPage {
	todoList: Task[] = [];
	db: SQLite = null;

	constructor(public alertCtrl: AlertController) {
		this.getSQLiteDBHandler();
	}

	getSQLiteDBHandler() {
		//Helper.getSQLiteDBHandler(this.db, NCashConstants.LOCAL_STORAGE_DB);
		console.log("Opening new DB connection");

		this.db = new SQLite();	
		
		this.db.openDatabase({
			name: NCashConstants.LOCAL_STORAGE_DB,
			location: 'default'
		}).then(() => {
			console.log("DB connection opened successfully")
			this.getTodoListItems();
		});
	}

	getTodoListItems() {
		console.log("Fetching data from: " + NCashConstants.TODO_LIST_TBL);
		let query: string = "SELECT * FROM " + NCashConstants.TODO_LIST_TBL;
		console.log(query);

		this.db.executeSql(query , [])
			.then((data) => {
				console.log("fetched successfully!")
				console.log("fetched: " + JSON.stringify(data));

				for (let i = 0; i < data.rows.length; i++) {
					let task: Task = {
						name: data.rows.item(i).name,
						isDone: data.rows.item(i).isDone
					}
					this.todoList.push(task);
				}
			}, (error) => {
				console.log("Error: " + JSON.stringify(error));
			});
	}

	addTask() {
		let prompt = this.alertCtrl.create({
      		title: 'New Task',
		    inputs: [
        		{
          			name: 'Task',
          			placeholder: 'Task'
        		},
      		],
      		buttons: [
        		{
          			text: 'Cancel',
          			handler: data => {
            			
          			}
        		},
        		{
          			text: 'Save',
          			handler: data => {
          				if (data.Task.trim() != "") {
	          				let task: Task = {
	      						name: data.Task.trim(),
	      						isDone: false
	      					}

	            			this.todoList.push(task);
	            			this.insertToLocalTbl(NCashConstants.TODO_LIST_TBL, task);
          				}
          			}
        		}
      		]
  		});

  		prompt.present();
	}

	deleteTodoItems() {
		let tempTodo: Task[] = [];
		let tasks: string[] = [];

		for ( let task of this.todoList ) {
			if ( !task.isDone ) {
				tempTodo.push(task);
			} else {
				tasks.push(task.name);
			}
		}

		this.todoList = tempTodo;
		this.deleteFromLocalTbl(NCashConstants.TODO_LIST_TBL, tasks);
	}

	insertToLocalTbl(tbl, task) {
		let query: string = "INSERT INTO " + tbl + " (name, isDone) VALUES('" + task.name + "'," + 0 + ')';
		console.log("query is: " + query);

		this.db.executeSql(query, [])
			.then((data) => {
				console.log("Inserted new task: " + JSON.stringify(data));
			}, (error) => {
				console.log("Cannot insert: " + JSON.stringify(error));
			})
	}

	deleteFromLocalTbl(tbl, tasks: string[]) {
		let query: string = "DELETE from " + tbl + " WHERE name in (";
		for (let i = 0; i < tasks.length; ) {
			query += "'" + tasks[i] +"'";
			i++;
			if (i < tasks.length)
				query += ",";
		}
		query += ")";
		console.log(query);

		this.db.executeSql(query, [])
			.then((data) => {
				console.log("Deleted successfully");
			}, (error) => {
				console.log("Failed to Delete Tasks!");
			});
	}
}