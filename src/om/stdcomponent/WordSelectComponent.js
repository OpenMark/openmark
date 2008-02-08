// to be added word select component

function wordOnClick(checkboxID,idPrefix)
{
	var label = document.getElementById(idPrefix+'label_wordselectword_'+checkboxID);
	var checkbox=document.getElementById(idPrefix+'wordselectword_'+checkboxID);

	if (checkbox.checked) {
		label.className = label.className + " selectedhilight";
	} else {
		label.className = label.className.replace("selectedhilight", "");
	}
}

function wordOnFocus(checkboxID,idPrefix)
{
	var label = document.getElementById(idPrefix+'label_wordselectword_'+checkboxID);
	var checkbox=document.getElementById(idPrefix+'wordselectword_'+checkboxID);

		label.className = label.className + " borderfocus";
}

function wordOnBlur(checkboxID,idPrefix)
{
	var label = document.getElementById(idPrefix+'label_wordselectword_'+checkboxID);
	var checkbox=document.getElementById(idPrefix+'wordselectword_'+checkboxID);

		label.className = label.className.replace("borderfocus", "");
}