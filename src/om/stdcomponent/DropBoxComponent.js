// Array of all dragboxes
var dragboxArray=new Array();
function dragboxInform(id,idPrefix,enabled,group,infinite)
{
	var dragbox=document.getElementById(idPrefix+id);
	dragbox.isEnabled=enabled;
	dragbox.isInfinite=infinite;
	dragbox.atHome=true;
	dragbox.inGroup=idPrefix+group;
	dragbox.valueId=id;

	dragboxArray.push(idPrefix+id);
}

// Array of all dropboxes
var dropboxArray=new Array();

function dropboxFix(id,idPrefix,enabled,group,bgcolour)
{
	// Check maximum size of all dragboxes
	var maxWidth=0,maxHeight=0;
	for(var i=0;i<dragboxArray.length;i++)
	{
		var db=document.getElementById(dragboxArray[i]);
		if(db.inGroup!=idPrefix+group) continue;
		if(db.offsetWidth>maxWidth) maxWidth=db.offsetWidth;
		if(db.offsetHeight>maxHeight) maxHeight=db.offsetHeight;
	}

	// Init size of dropbox
	var img=document.getElementById(idPrefix+id+"img");
	var box=document.getElementById(idPrefix+id+"box");
	img.width=maxWidth;
	img.height=maxHeight+2;
	box.style.width=(maxWidth-2)+"px";
	box.style.height=(maxHeight-2)+"px";
	box.inGroup=idPrefix+group;

	// Init all dragboxes not already inited
	for(var i=0;i<dragboxArray.length;i++)
	{
		var db=document.getElementById(dragboxArray[i]);
		if(!db.isInited && db.inGroup==idPrefix+group)
		{
			var img2=document.getElementById(dragboxArray[i]+"img");
			img2.width=maxWidth;
			img2.height=maxHeight;

			var dbInner=document.getElementById(dragboxArray[i]+"inner");
			dbInner.style.marginTop=(((maxHeight-6) - dbInner.offsetHeight)/2)+"px";
	
			db.style.width=(maxWidth-6) +"px";
			db.style.height=(maxHeight-6)+"px";

			db.isInited=true;

		}
	}

	box.valueField=document.getElementById(idPrefix+"omval_"+id);
	if(enabled) dropboxArray.push(box);
	addPostLoad( function() { dropboxFix2(id,idPrefix); } );

	// Add keydown
	box.onkeydown=function(e) { return dropboxKeyDown(id,idPrefix,fixEvent(e)); };
	box.onfocus=function(e) { box.style.border="1px dotted black"; }
	box.onblur=function(e) {
		if(box.filledBorder)
			box.style.border="1px solid "+box.filledBorder;
		else
			box.style.border="1px solid "+bgcolour;
	}
}

// Keyboard support for dropbox
function dropboxKeyDown(id,idPrefix,e)
{
	var dropbox=document.getElementById(idPrefix+id+"box");
	var newDragbox;

	if(e.mKey==27) // Escape
	{
		// Get rid of anything already in dropbox
		if(dropbox.draggedItem)
		{
			dragboxUnplace(dropbox.draggedItem,true);
		}
		return false;
	}
	else if(e.mKey==38 || e.mKey==37) // Up or left
	{
		var startSeek=false;
		if(!(dropbox.draggedItem)) startSeek=true;

		for(var i=dragboxArray.length-1;;i--)
		{
			if(i<0)
			{
				// Looping past the end of the list counts as 'space'
				if(dropbox.draggedItem)
				{
					dragboxUnplace(dropbox.draggedItem,true);
				}
				//return false to override the default IE pagedown action on space bar
				return false;
			}

			if(startSeek)
			{
				var thisDragbox=document.getElementById(dragboxArray[i]);
				if(thisDragbox.isEnabled && !thisDragbox.inDropbox && thisDragbox.inGroup==dropbox.inGroup)
				{
					newDragbox=thisDragbox;
					break;
				}
			}

			if((dropbox.draggedItem) && dragboxArray[i]==dropbox.draggedItem.id)
			{
				if(startSeek) break; // Give up (avoid endless loop if failure)
				// Use next one
				startSeek=true;
			}
		}
	}
	else if(e.mKey==40 || e.mKey==39 || e.mKey==32 || e.mKey==13) // Down or right or space or return
	{
		var startSeek=false;
		if(!(dropbox.draggedItem)) startSeek=true;

		for(var i=0;;i++)
		{
			if(i>dragboxArray.length-1)
			{
				// Looping past the end of the list counts as 'space'
				if(dropbox.draggedItem)
				{
					dragboxUnplace(dropbox.draggedItem,true);
				}
				return false;
			}

			if(startSeek)
			{
				var thisDragbox=document.getElementById(dragboxArray[i]);
				if(thisDragbox.isEnabled && !thisDragbox.inDropbox && thisDragbox.inGroup==dropbox.inGroup)
				{
					newDragbox=thisDragbox;
					break;
				}
			}

			if((dropbox.draggedItem) && dragboxArray[i]==dropbox.draggedItem.id)
			{
				if(startSeek) break; // Give up (avoid endless loop if failure)
				// Use next one
				startSeek=true;
			}
		}
	}

	if(newDragbox)
	{
		resolvePageXY(newDragbox);

		// Get rid of anything already in dropbox
		if(dropbox.draggedItem)
		{
			dragboxUnplace(dropbox.draggedItem,true);
		}

		// Put new dragbox in dropbox
		dragboxPlace(newDragbox,dropbox);

		// Tell browser not to handle key
		return false;
	}
	else
		// Wasn't a target keypress, ignore
		return true;
}

// Get offset for object co-ordinate system
function getZeroCoords(thing)
{
	var out=new Object();
	out.left=0; out.top=0;
	while(true)
	{
		thing=thing.offsetParent;

		if(thing==null)/* || (thing.nodeName.toLowerCase()!="td" &&
			thing.nodeName.toLowerCase()!='th' && thing.nodeName.toLowerCase()!="table"
			&& (!isIE || thing.className!="layoutgriditem")))*/
		{
			return out;
		}

		out.left+=thing.offsetLeft;
		out.top+=thing.offsetTop;
	}
}

// Obtain absolute co-ordinates of object
function getStyleCoords(thing)
{
	var out=new Object();
	out.left=thing.offsetLeft;
	out.top=thing.offsetTop;
	var zero=getZeroCoords(thing);
	out.left+=zero.left;
	out.top+=zero.top;
	return out;
}

function moveMatch(move,ref,xOffset,yOffset)
{
	var c=getStyleCoords(ref);
	var moveZero=getZeroCoords(move);
	move.style.left=(c.left+xOffset-moveZero.left)+"px";
	move.style.top=(c.top+yOffset-moveZero.top)+"px";
}

function dropboxFix2(id,idPrefix)
{
	// Show all dragboxes not already shown
	for(var i=0;i<dragboxArray.length;i++)
	{
		var db=document.getElementById(dragboxArray[i]);
		if(!db.isShown)
		{
			var img2=document.getElementById(dragboxArray[i]+"img");
			moveMatch(db,img2,0,0);
			db.style.visibility="visible";
			if(db.isEnabled) db.onmousedown=dragboxMouseDown;
			db.homeLeft=db.style.left;
			db.homeTop=db.style.top;
			db.isShown=true;
		}
	}

	var img=document.getElementById(idPrefix+id+"img");
	var box=document.getElementById(idPrefix+id+"box");
	moveMatch(box,img,0,2);
	box.style.visibility="visible";
	resolvePageXY(box);

	if(box.valueField.value!='')
	{
		var db=document.getElementById(idPrefix+box.valueField.value);
		resolvePageXY(db);
		dragboxPlace(db,box);
	}
}

function clearSelection()
{
	if (window.getSelection)
	{
	window.getSelection().removeAllRanges();
	}
	else if (document.selection)
	{
	document.selection.empty();
	}
}

function resolveDragbox(e)
{
	var target=e.mTarget;
	while(target!=null)
	{
		if(target.className.indexOf("dragbox")==0 &&
			(target.className=="dragbox" || target.className.indexOf("dragbox ")==0))
			return target;
		target=target.parentNode;
	}
	return null;
}

var dbMoving;

function dragboxMouseDown(e)
{
	e=fixEvent(e);
	dbMoving=resolveDragbox(e);

	dragboxLeavingHome(dbMoving);

	dbMoving.pageX=e.mPageX;
	dbMoving.pageY=e.mPageY;
	dbMoving.style.zIndex=100;

	dragboxUnplace(dbMoving,false);

	// Get current dropbox locations
	for(var i=0;i<dropboxArray.length;i++)
	{
		resolvePageXY(dropboxArray[i]);
	}

	document.onmouseup=dragboxMouseUp;
	document.onmousemove=dragboxMouseMove;
}

function dragboxMouseMove(e)
{
	e=fixEvent(e);
	// Handle IE thing where it gets stuck on if you go out of
	// the window and let go there
	if(isIE8OrBelow && e.button==0)
			return dragboxMouseUp(e);
	if(!dbMoving) return;


	var deltaX=e.mPageX-dbMoving.pageX, deltaY=e.mPageY-dbMoving.pageY;

	dbMoving.style.left=(Number(dbMoving.style.left.replace("px","")) + deltaX) + "px";
	dbMoving.style.top=(Number(dbMoving.style.top.replace("px","")) + deltaY) + "px";

	dbMoving.pageX=e.mPageX;
	dbMoving.pageY=e.mPageY;

	clearSelection();
	return true;
}

function dragboxReleaseCapture()
{
	document.onmouseup=null;
	document.onmousemove=null;
	dbMoving.style.zIndex=5;
	dbMoving=null;
}

// Intersect 2 elements. Must have pageX, pageY resolved.
// Returns area of overlap
function intersects(e1,e2)
{
	// Check X overlap
	if(e1.pageX2<=e2.pageX) return 0;
	if(e1.pageX>e2.pageX2) return 0;
	var xStart = Math.max(e1.pageX,e2.pageX);
	var xEnd = Math.min(e1.pageX2,e2.pageX2);

	// Check Y overlap
	if(e1.pageY2<=e2.pageY) return 0;
	if(e1.pageY>e2.pageY2) return 0;
	var yStart = Math.max(e1.pageY,e2.pageY);
	var yEnd = Math.min(e1.pageY2,e2.pageY2);

	// Return product of overlaps
	return (xEnd-xStart) * (yEnd-yStart);
}

function dragboxMouseUp(e)
{
	if(!dbMoving) return;
	e=fixEvent(e);

	// See if it hit something
	resolvePageXY(dbMoving);
	var found=null,bestIntersect=0,thisIntersect;
	for(var i=0;i<dropboxArray.length;i++)
	{
		var dropbox=dropboxArray[i];
		if(dropbox.inGroup!=dbMoving.inGroup) continue;
		var thisIntersect=intersects(dbMoving,dropbox);
		if(thisIntersect > bestIntersect)
		{
			bestIntersect=thisIntersect;
			found=dropbox;
			break;
		}
	}
	if(found)
	{
		// Does found dropbox already have something? Get rid of it
		if(found.draggedItem)
		{
			dragboxUnplace(found.draggedItem,true);
		}

		dragboxPlace(dbMoving,found);
	}
	else
	{
		dragboxUnplace(dbMoving,true);
	}

	dragboxReleaseCapture();
}

function dragboxLeavingHome(db)
{
	if(db.isInfinite && db.atHome)
	{
		// Make another copy which stays at home
		var newDragbox=db.cloneNode(true);
		newDragbox.isEnabled=db.isEnabled;
		newDragbox.isInfinite=db.isInfinite;
		newDragbox.atHome=db.atHome;
		newDragbox.inGroup=db.inGroup;
		newDragbox.isInited=db.isInited;
		newDragbox.isShown=db.isShown;
		newDragbox.homeLeft=db.homeLeft;
		newDragbox.homeTop=db.homeTop;
		newDragbox.onmousedown=db.onmousedown;
		newDragbox.valueId=db.valueId;

		db.parentNode.insertBefore(newDragbox,db);
	}
	db.atHome=false;
}

function getBorderColor(e)
{
	if(document.defaultView && document.defaultView.getComputedStyle)
	{
		return document.defaultView.getComputedStyle(e,"").getPropertyValue(
			"border-left-color");
	}
	else if(e.currentStyle)
	{
		return e.currentStyle['borderLeftColor'];
	}
	else
	{
		throw new Exception('Unsupported browser');
	}
}

function dragboxPlace(dragbox,dropbox)
{
	dragboxLeavingHome(dragbox);

	// Place in middle of dropbox
	var
		middleX=(dropbox.pageX+dropbox.pageX2)/2,
		middleY=(dropbox.pageY+dropbox.pageY2)/2;
	var
		deltaX=middleX - (dragbox.pageX+dragbox.offsetWidth/2),
		deltaY=middleY - (dragbox.pageY+dragbox.offsetHeight/2);

	dragbox.style.left=(Number(dragbox.style.left.replace("px","")) + deltaX +1 ) + "px";
	dragbox.style.top=(Number(dragbox.style.top.replace("px","")) + deltaY +1 ) + "px";
	dragbox.inDropbox=dropbox;
	// Remove border (the +1 we added above was to account for this)
	// and put it on dropbox if needed
	dropbox.oldBorderColor=getBorderColor(dropbox);
	var newBorder=getBorderColor(dragbox);
	dragbox.style.border="none";

	dropbox.style.borderLeftColor=newBorder;
	dropbox.style.borderRightColor=newBorder;
	dropbox.style.borderTopColor=newBorder;
	dropbox.style.borderBottomColor=newBorder;
	dropbox.filledBorder=newBorder;


	dragbox.atHome=false;

	dropbox.draggedItem=dragbox;
	dropbox.valueField.value=dragbox.valueId;
}

function dragboxUnplace(dragbox,resetPosition)
{
	if(dragbox.inDropbox)
	{
		var dropbox=dragbox.inDropbox;
		dragbox.inDropbox=null;

		dropbox.draggedItem=null;
		dropbox.valueField.value="";

		dragbox.style.border="1px solid black";

		dropbox.style.borderLeftColor=dropbox.oldBorderColor;
		dropbox.style.borderRightColor=dropbox.oldBorderColor;
		dropbox.style.borderTopColor=dropbox.oldBorderColor;
		dropbox.style.borderBottomColor=dropbox.oldBorderColor;
		dropbox.filledBorder=false;

		if(!resetPosition)
		{
			dragbox.style.left=(Number(dragbox.style.left.replace("px","")) - 1 ) + "px";
			dragbox.style.top=(Number(dragbox.style.top.replace("px","")) - 1) + "px";
		}
	}

	if(resetPosition)
	{
		if(dragbox.isInfinite)
		{
			dragbox.parentNode.removeChild(dragbox);
		}
		else
		{
			dragbox.style.left=dragbox.homeLeft;
			dragbox.style.top=dragbox.homeTop;
		}
	}


}

