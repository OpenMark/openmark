// to be added word select component

var isKHTML=navigator.userAgent.indexOf('KHTML')!=-1;
var isGecko=!isKHTML && navigator.userAgent.indexOf('Gecko/')!=-1;
var isOpera=navigator.userAgent.indexOf('Opera')!=-1;
var isIE=!isOpera && navigator.userAgent.match('.*MSIE.*Windows.*');

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
	var div = document.getElementById(idPrefix+'div_wordselectword_'+checkboxID);

	if(isGecko){
		div.className = div.className + " borderfocus";
	}
}

function wordOnBlur(checkboxID,idPrefix)
{
	var label = document.getElementById(idPrefix+'label_wordselectword_'+checkboxID);
	var checkbox=document.getElementById(idPrefix+'wordselectword_'+checkboxID);
	var div=document.getElementById(idPrefix+'div_wordselectword_'+checkboxID);

		div.className = div.className.replace("borderfocus", "");
}