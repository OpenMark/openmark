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

function editadvancedfieldFix(fieldName,idPrefix,enabled,type,dZoom, sfg,sbg) {
	if(enabled) {
		setTimeout(function() { editadvancedfieldInit(fieldName,idPrefix, type) },0);
	}
}

function editadvancedfieldInit(fieldName, idPrefix, type) {	
	addPreSubmit(document.getElementById(idPrefix+"omval_"+fieldName).form,
		function() { editadvancedfieldPreSubmit(fieldName, idPrefix, type); });
}

function editadvancedfieldPreSubmit(fieldName, idPrefix, type) {
    var id = idPrefix+"om_" + fieldName + "_iframe";
    var editor = tinyMCE.get(id);
    var content = editor.getContent();
    if (type=="chem") content=toChemHtml(content);
    document.getElementById(idPrefix+"omval_"+fieldName).value = content;
}
