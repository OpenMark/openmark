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

window.onload=myOnLoad;

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

// Adds to chain of events that occur before submitting the specified form
function addPreSubmit(specifiedForm,handler)
{
  if(specifiedForm.preSubmit)
  {
    var previous=specifiedForm.preSubmit;
    specifiedForm.preSubmit=function() { previous(); handler(); };
  }
  else
    specifiedForm.preSubmit=handler;
}

// Must call before submitting specified form
function preSubmit(specifiedForm)
{
  if(specifiedForm.preSubmit) specifiedForm.preSubmit();
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
  var rootx=imageElement.offsetLeft,rooty=imageElement.offsetTop;
  
  for(var i=1;i<arguments.length;i++)
  {
    var ph=document.getElementById(arguments[i][0]);

 		// Hack for IE positioning editfields one too far down (?!)
		var moveUp=false;
		if(isIE && (ph.childNodes.length==1 || 
		  (ph.childNodes.length==2 && ph.childNodes[1].nodeName.toLowerCase()=="script")) && 
		  isDomElement(ph.childNodes[0],"input",["type","text"]))
		{
		  moveUp=true;
		}
		
    if(isIE && ph.firstChild && ph.firstChild.currentStyle.display=="inline")
    {
      // In IE, if you don't put some text (such as this 0-size NBSP) before the
      // first item in the placeholder, then in some (only some) cases, it adds 
      // a humungous chunk of space before that first item.
      var ieSucks=document.createElement("span");
      ieSucks.style.fontSize="0";
      ieSucks.appendChild(document.createTextNode("\u00a0"));
      // Image placeholders don't have width so check it exists before messing
      if(ph.style.width)
        ph.style.width=(parseInt(ph.style.width.replace(/px/,""),10)+1)+"px";
      ph.style.left=(rootx+arguments[i][1]-1)+'px';
      ph.insertBefore(ieSucks,ph.firstChild);
    }
    else
    {
      ph.style.left=(rootx+arguments[i][1])+'px';
    }
    ph.style.top=(rooty+arguments[i][2]-(moveUp ? 1 : 0))+'px';
    
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


// Keep track of focusable objects
var focusList=new Array();

function addFocusable(id,expr)
{
  var o=new Object();
  o.id=id;
  o.expr=expr;
  
  if(focusList.length==0)
  {
    addOnLoad(function() {
	  setTimeout(o.expr+'.focus();setTimeout("window.scroll(0,0);",0)',100);
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
      setTimeout(focusList[index].expr+".focus();",0);
    }
  }
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
        divs[i].style.height="auto";      }
      }
  });
}