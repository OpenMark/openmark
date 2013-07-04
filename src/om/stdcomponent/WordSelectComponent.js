// to be added word select component
function wordOnClick(checkboxID,idPrefix)
{
	var label = document.getElementById(idPrefix+'label_wordselectword_'+checkboxID);
	var checkbox = document.getElementById(idPrefix+'wordselectword_'+checkboxID);

	if (checkbox.checked) {
		label.className = label.className + " selectedhilight";
	} else {
		label.className = label.className.replace(" selectedhilight", "");
	}
	if (isKHTML) {
		// Fix some Chrome layout glitches.
		khtmlForceRepaint(label.parentNode);
		checkbox.focus();
	}
}

function wordOnFocus(checkboxID,idPrefix)
{
	var div = document.getElementById(idPrefix+'div_wordselectword_'+checkboxID);	
	// Firefox, IE8 and other browsers don't automatically create a focus rectangle so have to draw one in.
	// IE7 and below versions do create a focus rectangle automatically.
	if(!isIE7OrBelow){
		div.className = div.className + " borderfocus";
	}
	if (isKHTML) {
		// Fix some Chrome layout glitches.
		khtmlForceRepaint(div);
	}
}

function wordGeckoBorder(checkboxID,idPrefix)
{
	// This is called onLoad and ulimately adds a transparent border around the
	//the displayed word. This stops the words from moving around as the focus
	//rectangle moves. The border cannot be added directly as any left or right
	//borders stuff the layout of the component in IE7 or below
	var div = document.getElementById(idPrefix+'div_wordselectword_'+checkboxID);
	if(!isIE7OrBelow){
		div.className = div.className + " geckoselectworddiv";
	}
}

function wordOnBlur(checkboxID,idPrefix)
{
	var div = document.getElementById(idPrefix+'div_wordselectword_'+checkboxID);
	div.className = div.className.replace(" borderfocus", "");
	if (isKHTML) {
		// Fix some Chrome layout glitches.
		khtmlForceRepaint(div);
	}
}
