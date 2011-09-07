
var OBJECT_TO_FOCUS;
var mysubtype = new Array();
var suppress = false;

function delayedFocus(obj)
{
	OBJECT_TO_FOCUS=obj;
	setTimeout(function() {OBJECT_TO_FOCUS.focus() },0);
}

function suppressEvent(e)
{
	suppress=true;
	e.returnValue=false;
	if(e.preventDefault) e.preventDefault();
	return false;
}

function toChemHtml(s)
{
	var s1=s.replace(/<[^<]+>/g,"");
	var s2=s1.replace(/([a-zA-Z])([0-9]+)/g,"$1<sub>$2</sub>");
	s2=s2.replace(/ /g,"&nbsp;");
	return s2;
}

function formatChem(win)
{
	var r0,r1; // ranges
	var sel;
	var root=win.document.body;
	var c0 = root.innerHTML;
	var c1=c0.replace(/<[^<]+>/g,"");
	var c2=c1.replace(/([a-zA-Z])([0-9]+)/g,"$1<sub>$2</sub>");
	c2=c2.replace(/ /g,"&nbsp;");

	if(isIE)
	{
		if (c0.toLowerCase()==c2.toLowerCase()) return false;
		sel = win.document.selection;
		r0= sel.createRange().duplicate();
		r0.moveStart("character",-99);
		var caretPos=r0.text.length;
		root.innerHTML = c2;
		r1=root.createTextRange();
		r1.moveStart("character",caretPos);
		r1.collapse();
		r1.select();
	}
	else if (isGecko)
	{
		if (c0.toLowerCase()==c2.toLowerCase()) return false;
		var caretNode, node, subnode;

		r0=win.getSelection().getRangeAt(0);
		r0.setStart(root,0);
		var caretOffset=r0.toString().length;

		root.innerHTML=c2;
		var na = root.childNodes;
		var j, jj;
		for (j=0;j<na.length && !caretNode;j++)
		{
			if (na[j].nodeType == 3) // text
			{
				if (caretOffset <= na[j].length) caretNode=na[j];
				else caretOffset -= na[j].length;
			}
			else
			{
				var sna = na[j].childNodes;
				for(jj=0;jj<sna.length && !caretNode;jj++)
				{
					if (caretOffset <= sna[jj].length) caretNode=sna[jj];
					else caretOffset -= sna[jj].length;
				}
			}
		}

		if (caretNode)
		{
			r1 = win.document.createRange();
			r1.setStart(caretNode,caretOffset);
			sel = win.getSelection();
			sel.removeAllRanges();
			sel.addRange(r1);
		}
		delayedFocus(win);
	}
}

function advancedfieldKeyFilter(fieldName,idPrefix,e)
{
	var myframe=document.getElementById(idPrefix+"om_"+fieldName+"_iframe");
	var mydoc=myframe.contentWindow.document;
	var doSuppress=true;
	var reFocus = true;
	if(!e) var e=myframe.contentWindow.event;
	var k = e.keyCode;
	suppress=false; // global

	if (k==9)
	{
		reFocus=false;
		if (isIE) focusFromList(idPrefix+fieldName,1); // cludge to clear
														// Iframe focus in IE

		if(e.shiftKey)focusFromList(idPrefix+fieldName,-1);
		else
		{
			if (mysubtype[idPrefix+fieldName]=="chem") focusFromList(idPrefix+fieldName,1);
			else // has sup or sub boxes
			{
				var sup=document.getElementById(idPrefix+"om_"+fieldName+"_sup");
				if (sup) delayedFocus(sup);
				else
				{
					var sub=document.getElementById(idPrefix+"om_"+fieldName+"_sub");
					if (sub) delayedFocus(sub);
				}
			}
		}
	}
	else if(k==13)
	{
	}
	else if(k==38) // arrow up
	{
		if (mydoc.queryCommandState("subscript"))
			mydoc.execCommand("subscript",false,null);
		else if( (mysubtype[idPrefix+fieldName]=="superscript" || mysubtype[idPrefix+fieldName]=="both")
				&& (!mydoc.queryCommandState("superscript")) )
		mydoc.execCommand("superscript",false,null);
	}
	else if (k==40) // arrow down
	{
		if (mydoc.queryCommandState("superscript"))
			mydoc.execCommand("superscript",false,null);
		else if( (mysubtype[idPrefix+fieldName]=="subscript" || mysubtype[idPrefix+fieldName]=="both")
				&& (!mydoc.queryCommandState("subscript")) )
			mydoc.execCommand("subscript",false,null);
	}
	else doSuppress = false;

	if (reFocus) delayedFocus(myframe.contentWindow);

	if (doSuppress) {
		suppressEvent(e);
		return false;
	}
	return true;
}

function advancedfieldUpdateState(fieldName,idPrefix)
{
	var myframe=document.getElementById(idPrefix+"om_"+fieldName+"_iframe");
	var mydoc=myframe.contentWindow.document;

	var sup=document.getElementById(idPrefix+"om_"+fieldName+"_sup");
	var sub=document.getElementById(idPrefix+"om_"+fieldName+"_sub");

	if(sup) sup.checked = mydoc.queryCommandState("superscript") ? true : false;
	if(sub) sub.checked = mydoc.queryCommandState("subscript") ? true : false;
	if(mysubtype[idPrefix+fieldName]=="chem") formatChem(myframe.contentWindow);
	return false;
}

function setCursor(myWin)
{
	if (isIE)
	{
		var r1=myWin.document.body.createTextRange();
		r1.moveEnd("character",200);
		r1.collapse(false);
		r1.select();
	}
	else if (isGecko)
	{
		log("SC");
		// myWin.body.selectall();
		var sel=myWin.getSelection();
		sel.selectAllChildren(sel.anchorNode);
		sel.collapseToEnd();

		delayedFocus(myWin);

	}
}

function advancedfieldFix(fieldName,idPrefix,enabled,type,dZoom, sfg,sbg) {
	var nameForIFrame = idPrefix+"om_"+fieldName+"_iframe";
	var myframe=document.getElementById(nameForIFrame);
	
	var tryThis = nameForIFrame + "_ifr";
	
	var gotIt = document.getElementById(tryThis);
	
	alert("Got access to frame : " + gotIt);

//	var mydoc=myframe.contentWindow.document;
	mysubtype[idPrefix+fieldName]=type;
	var sContent = document.getElementById(idPrefix+"omval_"+fieldName).value;
	
//	if (type=="chem") sContent=toChemHtml(sContent);
//
//	if(enabled) {
//		mydoc.designMode="on";
//		setTimeout(function() { advancedfieldInit(fieldName,idPrefix) },0); // IE
//																			// needs
//																			// this.
//		// setTimeout(function() { setCursor(myframe.contentWindow);},200);
//	}
	
	
}

function advancedfieldInit(fieldName,idPrefix)
{
	var myframe=document.getElementById(idPrefix+"om_"+fieldName+"_iframe");
	var mydoc=myframe.contentWindow.document;

	mydoc.onkeyup=function(e) {advancedfieldUpdateState(fieldName,idPrefix); delayedFocus(myframe.contentWindow);return false;};

	if (isIE) {
		mydoc.onkeydown=function(e) { return advancedfieldKeyFilter(fieldName,idPrefix,e); };
	}
	else if(isGecko)
	{
		mydoc.addEventListener('keyup',mydoc.onkeyup,false);
		mydoc.onkeypress=function(e) { return advancedfieldKeyFilter(fieldName,idPrefix,e); };
		mydoc.addEventListener('keypress',mydoc.onkeypress,false);
	}
	addPreSubmit(document.getElementById(idPrefix+"omval_"+fieldName).form,
			function() { advancedfieldPreSubmit(fieldName,idPrefix); });
}

function advancedfieldSub(fieldName,idPrefix)
{
	var myframe=document.getElementById(idPrefix+"om_"+fieldName+"_iframe");
	myframe.contentWindow.focus();

	var mydoc=myframe.contentWindow.document;
	var sub=document.getElementById(idPrefix+"om_"+fieldName+"_sub");

	if (mydoc.queryCommandState("superscript"))
		mydoc.execCommand("superscript",false,null);
	if (mydoc.queryCommandState("subscript") != sub.checked)
		mydoc.execCommand("subscript",false,null);

	var sup=document.getElementById(idPrefix+"om_"+fieldName+"_sup");
	if (sup) sup.checked=false;
}

function advancedfieldSup(fieldName,idPrefix)
{
	var myframe=document.getElementById(idPrefix+"om_"+fieldName+"_iframe");
	myframe.contentWindow.focus();

	var mydoc=myframe.contentWindow.document;
	var sup=document.getElementById(idPrefix+"om_"+fieldName+"_sup");

	if (mydoc.queryCommandState("subscript"))
		mydoc.execCommand("subscript",false,null);

	if (mydoc.queryCommandState("superscript") != sup.checked)
		mydoc.execCommand("superscript",false,null);

	var sub=document.getElementById(idPrefix+"om_"+fieldName+"_sub");
	if (sub) sub.checked=false;
}

function advancedfieldPreSubmit(fieldName,idPrefix)
{
	var myframe=document.getElementById(idPrefix+"om_"+fieldName+"_iframe");
	document.getElementById(idPrefix+"omval_"+fieldName).value=myframe.contentWindow.document.body.innerHTML;
}
