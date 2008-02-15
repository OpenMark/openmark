// to be added word select component
function wordOnClick(checkboxID,idPrefix)
{
	var label = document.getElementById(idPrefix+'label_wordselectword_'+checkboxID);
	var checkbox = document.getElementById(idPrefix+'wordselectword_'+checkboxID);

	if (checkbox.checked) {
		label.className = label.className + " selectedhilight";
	} else {
		label.className = label.className.replace("selectedhilight", "");
	}
}

function wordOnFocus(checkboxID,idPrefix)
{
	var div = document.getElementById(idPrefix+'div_wordselectword_'+checkboxID);
	// Firefox doesn't automatically create a focus rectangle so have to draw one in.
	if(isGecko){
		div.className = div.className + " borderfocus";
	}
}

function wordOnBlur(checkboxID,idPrefix)
{
	var div = document.getElementById(idPrefix+'div_wordselectword_'+checkboxID);
	div.className = div.className.replace("borderfocus", "");
}