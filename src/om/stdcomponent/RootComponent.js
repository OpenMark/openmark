// Adds to a chain of events that occur onload
var onLoadEvents=new Array();
function addOnLoad(handler)
{
	onLoadEvents.push(handler);
}

function myOnLoad()
{
	window.mLoaded=true;
	for(var i=0;i<onLoadEvents.length;i++)
	{
		onLoadEvents[i]();
	}
	setTimeout(myPostLoad,0);
}

//window.addEventListener('load', myOnLoad, false);

if (window.addEventListener){
	window.addEventListener('load', myOnLoad, false);
} else if (window.attachEvent){
	window.attachEvent('onload', myOnLoad);
}


var postLoadEvents=new Array();
function addPostLoad(handler)
{
	postLoadEvents.push(handler);
}
function myPostLoad()
{
	for(var i=0;i<postLoadEvents.length;i++)
	{
		postLoadEvents[i]();
	}
}

// Adds to chain of handles that are called onKeyPress on the specified form
function addFormKeypress(specifiedForm,handler)
{
	if(specifiedForm.onkeypress)
	{
		var previous=specifiedForm.onkeypress;
		specifiedForm.onkeypress=function() { return previous() && handler(); };
	}
	else
		specifiedForm.onkeypress=handler;
}

// Adds to chain of events that occur before submitting the specified form
function addPreSubmit(specifiedForm,handler)
{
	if(specifiedForm.preSubmit)
	{
		var previous=specifiedForm.preSubmit;
		specifiedForm.preSubmit=function() { previous(); handler(); };
		return;
	}

	specifiedForm.preSubmit=handler;
	code = function(){
		// Call pre submit methods
		preSubmit(specifiedForm);
	};
	om_attachEvent('submit', code, specifiedForm);
}

// Must call before submitting specified form
function preSubmit(specifiedForm)
{
	if(specifiedForm.preSubmit) specifiedForm.preSubmit();
}

/**
 * Get form element by id, run presubmit methods then trigger form submit event
 * @param id
 * @return bool
 */
function submitForm(id)
{
	form = document.getElementById(id);

	// trigger submit event
	fireEventById(id,'submit');
	// submit form
	form.submit();
	return true;
}

// Detect whether a DOM node is a particular thing. Can call with one or more
// attribute/value pairs as arrays e.g. isDomElement(n,"input",["type","text"])
function isDomElement(node,tagName)
{
	if(!node.tagName || node.tagName.toLowerCase()!=tagName) return false;

	for(var i=1;i<arguments.length;i++)
	{
		if(node.getAttribute(arguments[i][0])!=arguments[i][1]) return false;
	}

	return true;
}

// Utility for use when debugging: log messages to a 'console'
var logBox;
function log(line)
{
	if(!logBox)
	{
		logBox=document.createElement("div");
		document.body.appendChild(logBox);
		logBox.style.overflow="scroll";
		logBox.style.width="100%";
		if(isIE)
		{
			logBox.style.position="absolute";
			logBox.style.top=(document.documentElement.clientHeight-120)+"px";
			logBox.style.left="0";
		}
		else
		{
			logBox.style.position="fixed";
		logBox.style.bottom="0";
	}
		logBox.style.height="120px";
		logBox.style.fontFamily="Andale Mono, monospace";
		logBox.style.fontSize="11px";
		logBox.style.borderTop="2px solid #888";
	}

	var newLine=document.createElement("div");
	newLine.appendChild(document.createTextNode(line));

	if(!logBox.firstChild)
		logBox.appendChild(newLine);
	else
		logBox.insertBefore(newLine,logBox.firstChild);
}


// We don't really care about KHTML and Opera, they will both be served the
// 'correct' version and are welcome to either work or not. They are detected
// only so we know they're not the 'real' ones they imitate.
var isKHTML=navigator.userAgent.indexOf('KHTML')!=-1;
var isGecko=!isKHTML && navigator.userAgent.indexOf('Gecko/')!=-1;
var isOpera=navigator.userAgent.indexOf('Opera')!=-1;
var isIE=!isOpera && navigator.userAgent.match('.*MSIE.*Windows.*');
var isIE7OrBelow=!isOpera && navigator.userAgent.match('.*MSIE [1-7].*Windows.*');
var isIE8 = navigator.userAgent.match('.*Trident/4.*');
var isIE7=!isOpera && navigator.userAgent.match('.*MSIE 7.*Windows.*') && !isIE8;
var isIE8OrBelow=!isOpera && navigator.userAgent.match('.*Trident/[1-4].*Windows.*');
var isFF=navigator.userAgent.indexOf("Firefox")!=-1;


var isGecko18;
if(isGecko)
{
	var re=/^.*rv:([0-9]+).([0-9]+)[^0-9].*$/;
	var matches=re.exec(navigator.userAgent);
	if(matches)
	{
		isGecko18 = (matches[1]==1) ? (matches[2]>=8) : (matches[1]>1);
	}
}


// Fix position of placeholders relative to an image. First argument is image Id,
// following arguments are arrays of [id, x, y].
function inlinePositionFix(imageID)
{
	var imageElement=document.getElementById(imageID);
	resolvePageXY(imageElement);

	for(var i=1;i<arguments.length;i++)
	{
		var ph=document.getElementById(arguments[i][0]);
		resolvePageXY(ph);
		var deltaX = imageElement.pageX + arguments[i][1] - ph.pageX;
		var deltaY = imageElement.pageY + arguments[i][2] - ph.pageY;

		// Hack for IE positioning editfields one too far down (?!)
		var moveUp=0;
		if(isIE && (ph.childNodes.length==1 ||
			(ph.childNodes.length==2 && ph.childNodes[1].nodeName.toLowerCase()=="script")) &&
			isDomElement(ph.childNodes[0],"input",["type","text"]))
		{
			moveUp=1;
		}

		// There used to be some very hacky code here to fix layout problems in IE when
		// the placeholder contained an element, but it now seems to do more harm than good.
		// I think it is the same issue that is now handled by the workaround in resolvePageXY.
		ph.style.left=(Number(ph.style.left.replace("px","")) + deltaX)+'px';
		ph.style.top=(Number(ph.style.top.replace("px","")) + deltaY - moveUp)+'px';
		ph.style.visibility='visible';
	}
}

function checkEnter(e)
{
	var target = e.target ? e.target : e.srcElement;
	var keyCode = e.keyCode ? e.keyCode : e.which;

	// Stifle Enter on anything except submit buttons and textareas
	if(keyCode==13 && (!target.type || (target.type!='submit' && target.type!='textarea')))
		return false;
	else
		return true;
}

// Convert event object so it works with both browsers
function fixEvent(e)
{
	if(!e) e=window.event;

	if(e.pageX)
		e.mPageX=e.pageX;
	else if(e.clientX)
		e.mPageX=e.clientX+document.body.scrollLeft;
	if(e.pageY)
		e.mPageY=e.pageY;
	else if(e.clientY)
		e.mPageY=e.clientY+document.body.scrollTop;
	if(e.target)
		e.mTarget=e.target;
	else if(e.srcElement)
		e.mTarget=e.srcElement;
	if(e.keyCode)
		e.mKey=e.keyCode;
	else if(e.which)
		e.mKey=e.which;

	return e;
}

function getUrlParameter(name)
{
	var regexStr = "[\\?&]"+name+"=([^&#]*)";  
	var regex = new RegExp(regexStr);  
	var match = regex.exec(window.location.href);  
	if(match==null)    
		return "";  
	else    
		return match[1];
}

function isAutoFocusOn()
{
	var focusOn = true;
	if(getUrlParameter('autofocus')=='off')
		focusOn = false;
	return focusOn;
}

// Keep track of focusable objects
var focusList=new Array();

function addFocusable(id,expr)
{
    var o=new Object();
    o.id=id;
    o.expr=expr;

    if(focusList.length==0 && isAutoFocusOn())
    {
        addOnLoad(function() {
            setTimeout('try{'+o.expr+'.focus()}catch(e){};setTimeout("window.scroll(0,0);",0)',100);
        });
    }

    focusList.push(o);
}

// Focus the next (offset=1) or previous (offset=-1) one
function focusFromList(idThis,offset)
{
    for(var i=0;i<focusList.length;i++)
    {
        if(focusList[i].id==idThis)
        {
            var index=i+offset;
            while(index<0) index+=focusList.length;
            while(index>=focusList.length) index-=focusList.length;
            setTimeout('try{'+focusList[index].expr+".focus()}catch(e){};",0);
        }
    }
}

// Adds pageX and pageY to the element
function resolvePageXY(e)
{
	e.pageX=e.offsetLeft;
	e.pageY=e.offsetTop;

	var parent=e.offsetParent;
	while(parent!=null)
	{
		e.pageX+=parent.offsetLeft;
		e.pageY+=parent.offsetTop;
		parent=parent.offsetParent;
	}

	// Bug fix for IE7. When an eplace containing a text field is in an equation
	// in an indented line of text, then IE7 was applying the left margin from the
	// line of text to the input. In this case, add the offsetLeft from the first 
	// child to the position of this element, so the input ends up in the right place.
	if (e.firstChild && e.firstChild.nodeType==1 &&
			e.firstChild.tagName.toLowerCase() == 'input' &&
			e.firstChild.offsetLeft != 0) {
		e.pageX += e.firstChild.offsetLeft;
	}

	e.pageX2=e.pageX+e.offsetWidth;
	e.pageY2=e.pageY+e.offsetHeight;
}

// overflow hidden is set on the divs just so floats don't bounce
// around while loading, turn it off now
if(window.isDevServlet===undefined || !window.isDevServlet)
{
	addPostLoad(function()
	{
		var divs=document.getElementsByTagName("div");
		for(var i=0;i<divs.length;i++)
		{
			if(divs[i].className!="gridcontainer") continue;
			divs[i].style.overflow="visible";
			if(!isIE)
			{
				divs[i].style.minHeight=divs[i].style.height;
				divs[i].style.height="auto";
			}
		}
	});
}

function getTrueOffset(El, position)
{
    var offsetPosition = 'offsetLeft';// Element offset position name
    if(position=='top'){
        offsetPosition = 'offsetTop';
    }
    
    if(!isIE7){
        return(El[offsetPosition]);
    }

    var last=0;
    var currentLast=0;
    var current=0;
    
    e=El;
    if(e.offsetParent) {
        current = e[offsetPosition];
        
        while (e = e.offsetParent) {
            last=currentLast;
            if(e == document.body){
                break;
            }
            current += e[offsetPosition];  
            currentLast=e[offsetPosition];
        }
    }  

    var trueOffset = (current-last);
    if(El[offsetPosition] == 0 && (El[offsetPosition] <= trueOffset)){
        return trueOffset;
    }
    
    return El[offsetPosition]; 
    
}

function trueoffsetleft(El)
{
    return getTrueOffset(El, 'left');
}

function trueoffsettop(El)
{
    return getTrueOffset(El, 'top');
}

function cleanstring(val)
{
	return(val);
}

/**
 * Fire a dom event on a given element from its id
 * @param id element id
 * @param event name of event
 * @return bool
 */
function fireEventById(id,event)
{
	element = document.getElementById(id);
	if(!element){
		return false;
	}

	return fireEvent(element,event);
}

/**
 * Fire a dom event on a given element
 * @param element dom element
 * @param e name of event
 * @return bool
 */
function fireEvent(element,e)
{
	if (!document.createEventObject){
		// dispatch for firefox + others
		var evt = document.createEvent("HTMLEvents");
		evt.initEvent(e, true, true ); // event type,bubbling,cancelable
		return !element.dispatchEvent(evt);
	}
	// dispatch for IE
	var evt = document.createEventObject();
	return element.fireEvent('on'+e,evt);
}

function om_attachEvent(e, code, element)
{
	element = element || window;
	if (element.addEventListener){
		element.addEventListener(e, code, false);
	} else if (window.attachEvent){
		element.attachEvent('on'+e, code);
	}
}

function khtmlForceRepaint(element)
{
	var oldDisplay = element.style.display;
	element.style.display = "none";
	element.offsetHeight;
	element.style.display = oldDisplay;
}
