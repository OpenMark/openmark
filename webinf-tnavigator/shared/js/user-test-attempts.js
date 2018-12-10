/*
 * This JavaScript manages the UserTestAttemptsReport allowing users
 * to select the rows, and delete them via Ajax after prompting for a password.
 */
var deleteTestAttempts = {
		/* Test instance Object to delete */
		selectedTiObj: Array(),

		/* Get all the selected test attempts*/
		selectAllTestIns: function(isSelected) {
			var cboxes = document.querySelectorAll('td > input[type="checkbox"]');
			cboxes.forEach( function(chkbox) {
				chkbox.checked = isSelected;
				deleteTestAttempts.selectedTiObj.push(chkbox);
			});
		},

		/*Toggle checkboxes*/
		toggleCheckbox : function(e) {
			var e = e || window.event;
			var src = e.target || e.srcElement;
			var isselected = false;
			if (src.getAttribute('name') == 'select_all') {
				if (src.checked) {
					isselected = true
				}
				deleteTestAttempts.selectAllTestIns(isselected);
			} else if (src.getAttribute('type') == 'checkbox') {
				deleteTestAttempts.selectedTiObj.push(src);
			}
		},

		/*Delete the test instance*/
		deleteTestAttempt : function() {
			var result = confirm("Are you sure to delete user test attempts?");
			if (result) {
				var password = prompt("Please enter password to delete user test attempt.");
				if (password != null){
					tis = deleteTestAttempts.getSelTestInstance().join(',');
					deleteTestAttempts.deleteSelTestAttempt(tis, password);
				}
			}
		},

		/* Hide Delete button and select all option when no record is found*/
		hideButtons : function() {
			var table = document.querySelectorAll('table[class="topheaders"] > tbody > tr');
			if (table.length < 1) {
				var deleteButton = document.querySelector('input[name="delete_test"]');
				deleteButton.style.visibility = "hidden";
				var checkbox = document.querySelector('input[name="select_all"]');
				checkbox.style.visibility = "hidden";
			}
		},

		/* Get the Selected test instance*/
		getSelTestInstance : function() {
			var selectedTi = Array();
			var cboxes = deleteTestAttempts.selectedTiObj;
			cboxes.forEach( function(chkbox) {
				if (chkbox.checked) {
					selectedTi.push(chkbox.value);
				}
			});
			return selectedTi;
		},

		/* Get all the Test instance to delete*/
		deleteRows : function(tis) {
			var cboxes = deleteTestAttempts.selectedTiObj;
			cboxes.forEach( function(chkbox) {
				if (chkbox.checked) {
					deleteTestAttempts.deleteRow(chkbox);
				}
			});
			deleteTestAttempts.selectedTiObj = Array();
			var checkbox = document.querySelector('input[name="select_all"]');
			checkbox.checked = false;
		},

		/* Get each test instance tr delete*/
		getRowToDel : function(tagName, root) {
			if (!root) return;
			tagName = tagName.toLowerCase();
			while ((root = root.parentNode)) {
				if (root.tagName && root.tagName.toLowerCase() == tagName) {
					return root;
				}
			}
		},

		/* Remove the test instance row*/
		deleteRow : function(el) {
			var row = deleteTestAttempts.getRowToDel('tr', el);
			if (row && row.parentNode) {
				row.parentNode.removeChild(row);
				return true;
			}
		},

		/* Ajax call to delete the selected test instance*/
		deleteSelTestAttempt : function(selectedBoxs, password) {
			var xmlhttp = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
			reqURL = "deletetestattempt";
			xmlhttp.open('POST', reqURL, true);
			xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf-8');
			xmlhttp.send("ti="+selectedBoxs+"&testDeletePass="+password);
			xmlhttp.onreadystatechange = function (data) {
				if (this.readyState != 4 || this.status != 200) return;
				if (this.responseText == "Wrong password") {
					alert("Please verify the password entered to delete user test attempt.");
				} else if (this.responseText == "SUCCESS") {
					deleteTestAttempts.deleteRows(selectedBoxs);
				} else {
					alert("Could not delete user test attempt, try again later.");
				}
			}
		}
}

/* Event handling to delete test instance*/
document.addEventListener("DOMContentLoaded", function() {
	// Adds compatibility to all Browsers supports
	if (window.NodeList && !NodeList.prototype.forEach) {
		NodeList.prototype.forEach = Array.prototype.forEach;
	}

	var tableTh = document.querySelector('table[class="topheaders"] > thead > tr >th:nth-child(7)');
	var checkBox = document.createElement("input");
	checkBox.type = "checkbox";
	checkBox.name = "select_all";
	tableTh.appendChild(checkBox);

	var deleteButton = document.querySelector('input[name="delete_test"]');
	deleteButton.addEventListener('click', deleteTestAttempts.deleteTestAttempt);
	deleteTestAttempts.hideButtons();

	var checkbox = document.querySelector('input[name="select_all"]');
	checkbox.addEventListener('click', deleteTestAttempts.toggleCheckbox);

	var cboxes = document.querySelectorAll('td > input[type="checkbox"]');
	cboxes.forEach( function(chkbox) {
		chkbox.addEventListener('click', deleteTestAttempts.toggleCheckbox);
	});

});
