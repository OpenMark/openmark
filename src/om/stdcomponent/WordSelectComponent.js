// to be added word select component

function wordOnClick(checkboxID,idPrefix)
{
	var label = document.getElementById(idPrefix+'label'+checkboxID);
	var checkbox=document.getElementById(idPrefix+checkboxID);
	
	if (document.getElementById(idPrefix+checkboxID).checked) {
		label.className = label.className + " chkdColor";
	} 
	else {
		label.className = label.className.replace("chkdColor", "");
	}
	
	
}	