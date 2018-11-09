var canvasList=new Array();

function canvasInit(canvasId,idPrefix,enabled,imageOffsetX,imageOffsetY,keyStep,fg,bg,labelSize)
{
  canvas=new Object()
  canvas.enabled=enabled;
  canvas.imageOffsetX=imageOffsetX;
  canvas.imageOffsetY=imageOffsetY;
  canvas.keyStep=keyStep;
  canvas.fg=fg;
  canvas.bg=bg;
  canvas.labelSize=labelSize;
  canvas.img=document.getElementById(idPrefix+canvasId+'_img');
  canvas.markers=new Array();
  canvas.lines=new Array();
  canvas.dynamic=document.getElementById(idPrefix+canvasId+'_dynamic');

  // Record the background position, so we can detect if it changes later.
  canvas.img.setAttribute("data-top",canvas.img.offsetTop);

  canvasList.push(idPrefix+canvasId,canvas);
}

function getCanvas(canvasId,idPrefix)
{
  for(var index=0;index<canvasList.length;index+=2)
  {
    if(canvasList[index]==idPrefix+canvasId) return canvasList[index+1];
  }
}

function canvasMarkerInit(canvasId,idPrefix,labelJS,originX,originY,factorX,factorY)
{
  // Get marker, canvas, and x/y fields and init marker object
  var canvas=getCanvas(canvasId,idPrefix);
  var markerNum=canvas.markers.length;
  var marker=document.getElementById(idPrefix+canvasId+'_marker'+markerNum);
  marker.mCanvas=canvas;
  marker.mCanvas.markers.push(marker);
  marker.mX=document.getElementById(idPrefix+"canvasmarker_"+canvasId+"_"+markerNum+"x");
  marker.mY=document.getElementById(idPrefix+"canvasmarker_"+canvasId+"_"+markerNum+"y");
  marker.mLines=new Array();
  marker.e=document.createElement("div");
  canvas.dynamic.appendChild(marker.e);

  marker.updateLabel=function()
  {
    // Get co-ordinates
    var
      px=canvasGetMarkerX(marker),py=canvasGetMarkerY(marker);

    // Evaluate label
    var x=(px-originX) / factorX,y=(py-originY) / factorY;
    var label=null;
    eval(labelJS);

    if(marker.labelDiv) // Get rid of existing label
    {
      var parent=marker.labelDiv.parentNode;
      if(parent) parent.removeChild(marker.labelDiv);
    }

    canvas.img.trueoffsetLeft=trueoffsetleft(canvas.img);
    canvas.img.trueoffsetTop=trueoffsettop(canvas.img);
    
    // Add label
    if(label)
    {
      var div=document.createElement("div");
      marker.e.appendChild(div);
      marker.labelDiv=div;

      div.style.fontSize=canvas.labelSize+"px";
      div.style.lineHeight=canvas.labelSize+"px";
      div.style.position="absolute";
      div.style.top=(py+canvas.img.trueoffsetTop-(canvas.labelSize+6))+"px";
      div.style.left=(px+canvas.img.trueoffsetLeft+canvas.labelSize)+"px";
      div.style.width="200px";
      div.style.textAlign="left";

      var span=document.createElement("span");
      div.appendChild(span);

      span.style.color=canvas.fg;
      span.style.backgroundColor=canvas.bg;
      span.style.border="1px solid "+canvas.fg;
      span.style.padding="2px";

      span.appendChild(document.createTextNode(label));
    }
  };

  // Move marker into position and display it
  marker.style.display="block";
  canvasSetPos(marker,marker.mX.value,marker.mY.value);

  if(marker.mCanvas.enabled)
  {
    // Add events
    marker.onmousedown=function(e) { return canvasMarkerDown(fixEvent(e),marker); };
    marker.ontouchstart=function(e) { return canvasMarkerDown(fixEvent(e),marker); };
    marker.onkeydown=function(e) { return canvasMarkerKeydown(fixEvent(e),marker); };
  }

  if(isIE)
  {
	// Stop IE from dragging the damn image
	marker.ondragstart=function() { return false; };
  }
}
function canvasGetMarkerX(marker)
{
  return marker.offsetLeft-trueoffsetleft(marker.mCanvas.img)+marker.mCanvas.imageOffsetX;
}
function canvasGetMarkerY(marker)
{
  return marker.offsetTop-trueoffsettop(marker.mCanvas.img)+marker.mCanvas.imageOffsetY;
}


function canvasMarkerKeydown(e,marker)
{
  // Get movement offset and update
  var xOffset=0,yOffset=0;
  switch(e.mKey)
  {
  case 37: // left
    xOffset=-marker.mCanvas.keyStep;
    break;
  case 38: // up
    yOffset=-marker.mCanvas.keyStep;
    break;
  case 39: // right
    xOffset=marker.mCanvas.keyStep;
    break;
  case 40: // down
    yOffset=marker.mCanvas.keyStep;
    break;
  case 13: // return (just make sure it's ignored)
    return false;
  default:
    return;
  }

  // Get current pos
  canvasSetPos(marker,
    canvasGetMarkerX(marker)+xOffset,canvasGetMarkerY(marker)+yOffset);
  return false;
}

function canvasMarkerDown(e,marker)
{
  // Initialise marker data
  marker.mDown=true;
  marker.mStartX=e.mPageX;
  marker.mStartY=e.mPageY;
  marker.mStartLeft=marker.offsetLeft;
  marker.mStartTop=marker.offsetTop;

  // Listen to events across whole document
  document.onmouseup=function() { canvasMarkerUp(marker); };
  document.ontouchend=function() { canvasMarkerUp(marker); };
  document.onmousemove=function(e) { return canvasMarkerMove(fixEvent(e),marker); };
  document.ontouchmove=function(e) { return canvasMarkerMove(fixEvent(e),marker); };

  // Set focus too
  marker.focus();

  return false;
}

function canvasMarkerUp(marker)
{
  if(!marker.mDown) return;
  marker.mDown=false;

  // Release document events
  document.onmouseup = null;
  document.ontouchend = null;

  document.onmousemove = null;
  document.ontouchmove = null;
}

function canvasMarkerMove(e,marker)
{
  if(!marker.mDown) return;
  if (typeof e.touches !== 'undefined' && e.touches.length !== 1)
  {
    return canvasMarkerUp(e);
  }

  // Get x/y relative to canvas
  var canvasX=(e.mPageX-marker.mStartX)+
    marker.mStartLeft-trueoffsetleft(marker.mCanvas.img)+marker.mCanvas.imageOffsetX;
  var canvasY=(e.mPageY-marker.mStartY)+
    marker.mStartTop-trueoffsettop(marker.mCanvas.img)+marker.mCanvas.imageOffsetY;

  canvasSetPos(marker,canvasX,canvasY);

  if (e.preventDefault) e.preventDefault();
  return false;
}

function canvasSetPos(marker,canvasX,canvasY)
{
  // Restrict to canvas
  canvasX= (canvasX<0 ? 0 :
	(canvasX>marker.mCanvas.img.offsetWidth-1 ? marker.mCanvas.img.offsetWidth-1 : canvasX));
  canvasY= (canvasY<0 ? 0 :
	(canvasY>marker.mCanvas.img.offsetHeight-1 ? marker.mCanvas.img.offsetHeight-1 : canvasY));

  // Update hidden fields
  marker.mX.value=canvasX;
  marker.mY.value=canvasY;

  // Update position
// the -8 and -18 are fudges
marker.style.left=(1*canvasX+trueoffsetleft(marker.mCanvas.img)-marker.mCanvas.imageOffsetX)+"px";
marker.style.top=(1*canvasY+trueoffsettop(marker.mCanvas.img)-marker.mCanvas.imageOffsetY)+"px";


  // Update label
  marker.updateLabel();

  // Update any lines
  for(var i=0;i<marker.mLines.length;i++)
  {
    marker.mLines[i].update();
  }
}

function canvasLineInit(canvasId,idPrefix,from,to,labelJS,originX,originY,factorX,factorY)
{
  var line=new Object();
  line.canvas=getCanvas(canvasId,idPrefix);
  line.canvas.lines.push(line);
  line.from=line.canvas.markers[from];
  line.to=line.canvas.markers[to];
  line.from.mLines.push(line);
  line.to.mLines.push(line);
  line.e=document.createElement("div");
  line.canvas.dynamic.appendChild(line.e);
  line.labelJS=labelJS;

  line.e.style.fontSize="0";
  line.e.style.lineHeight="0";

  line.update=function()
  {
    // Get co-ordinates
    var
      px1=canvasGetMarkerX(line.from),px2=canvasGetMarkerX(line.to),
      py1=canvasGetMarkerY(line.from),py2=canvasGetMarkerY(line.to);

    // Decide which angle to draw
    if(Math.abs(px1-px2) < Math.abs(py1-py2))
      line.drawVert(px1,py1,px2,py2);
    else
      line.drawHoriz(px1,py1,px2,py2);

    // Evaluate label
    var x1=(px1-originX) / factorX,x2=(px2-originX) / factorX;
    var y1=(py1-originY) / factorY,y2=(py2-originY) / factorY;
    var label=null;
    eval(line.labelJS);

    // Add label
    if(label)
    {
      if(line.labelDiv) // Get rid of existing label
      {
        var parent=line.labelDiv.parentNode;
        if(parent) parent.removeChild(line.labelDiv);
      }

      var div=document.createElement("div");
      line.e.appendChild(div);
      line.labelDiv=div;


      div.style.fontSize=line.canvas.labelSize+"px";
      div.style.lineHeight=line.canvas.labelSize+"px";
      div.style.position="absolute";
      div.style.top=((py1+py2)/2+trueoffsettop(line.canvas.img)-line.canvas.labelSize/2)+"px";
      div.style.left=((px1+px2)/2 - 100+trueoffsetleft(line.canvas.img))+"px";
      div.style.width="200px";
      div.style.textAlign="center";

      var span=document.createElement("span");
      div.appendChild(span);

      span.style.color=line.canvas.fg;
      span.style.backgroundColor=line.canvas.bg;
      span.style.border="1px solid "+line.canvas.fg;
      span.style.padding="2px";

      span.appendChild(document.createTextNode(label));
    }
  };

  // If innerHTML is available it's faster so use that
  if(!(line.e.innerHTML == undefined) && (!isGecko || isGecko18))
  {
    line.drawHoriz=function(x1,y1,x2,y2)
    {
      var xStart,xEnd,yStart,yEnd;
      if(x1<x2)
      {
        xStart=x1; xEnd=x2; yStart=y1; yEnd=y2;
      }
      else
      {
        xStart=x2; xEnd=x1; yStart=y2; yEnd=y1;
      }
      var drawnX=xStart,drawnY=yStart,y=yStart;
      var dY=(yEnd-yStart)/(xEnd-xStart);

      var h="";
      for(var x=xStart+1;x<=xEnd+1;x+=2)
      {
        if(x==xEnd+1)
        {
          y+=dY;
          x=xEnd;
        }
        else
        {
          y+=dY*2;
        }
        if(Math.round(y)!=drawnY || x==xEnd)
        {
          h+="<div style='"+
            "position:absolute;"+
            "background:"+line.canvas.fg+";"+
            "left:"+(drawnX+trueoffsetleft(line.canvas.img))+"px;"+
            "top:"+(drawnY+trueoffsettop(line.canvas.img))+"px;"+
            "width:"+(x-drawnX)+"px;"+
    	    "height:2px;"+
    	    "'></div>";

          drawnY=Math.round(y);
          drawnX=x;
        }
      }
      line.e.innerHTML=h;
    };
    line.drawVert=function(x1,y1,x2,y2)
    {
      var xStart,xEnd,yStart,yEnd;
      if(y1<y2)
      {
        xStart=x1; xEnd=x2; yStart=y1; yEnd=y2;
      }
      else
      {
        xStart=x2; xEnd=x1; yStart=y2; yEnd=y1;
      }
      var drawnX=xStart,drawnY=yStart,x=xStart;
      var dX=(xEnd-xStart)/(yEnd-yStart);

      var h="";
      for(var y=yStart+1;y<=yEnd+1;y+=2)
      {
        if(y==yEnd+1)
        {
          x+=dX;
          y=yEnd;
        }
        else
        {
          x+=dX*2;
        }
        if(Math.round(x)!=drawnX || y==yEnd)
        {
          h+="<div style='"+
            "position:absolute;"+
            "background:"+line.canvas.fg+";"+
            "left:"+(drawnX+trueoffsetleft(line.canvas.img))+"px;"+
            "top:"+(drawnY+trueoffsettop(line.canvas.img))+"px;"+
            "width:2px;"+
    	    "height:"+(y-drawnY)+"px;"+
    	    "'></div>";

          drawnY=y;
          drawnX=Math.round(x);
        }
      }
      line.e.innerHTML=h;
    };
  }
  else // innerHTML not available, use DOM standard version
  {
    line.bits=new Array();

    line.drawHoriz=function(x1,y1,x2,y2)
    {
      var xStart,xEnd,yStart,yEnd;
      if(x1<x2)
      {
        xStart=x1; xEnd=x2; yStart=y1; yEnd=y2;
      }
      else
      {
        xStart=x2; xEnd=x1; yStart=y2; yEnd=y1;
      }
      var drawnX=xStart,drawnY=yStart,y=yStart;
      var dY=(yEnd-yStart)/(xEnd-xStart);

      var lineBit=0;
      for(var x=xStart+1;x<=xEnd+1;x+=2)
      {
        if(x==xEnd+1)
        {
          y+=dY;
          x=xEnd;
        }
        else
        {
          y+=dY*2;
        }
        if(Math.round(y)!=drawnY || x==xEnd)
        {
          var s;
          if(line.bits.length<=lineBit)
          {
            s=document.createElement("div");
            s.style.position="absolute";
            s.style.background=line.canvas.fg;
            line.e.appendChild(s);
            line.bits.push(s);
          }
          else s=line.bits[lineBit];

          s.style.left=(drawnX+trueoffsetleft(line.canvas.img))+"px";
          s.style.top=(drawnY+trueoffsettop(line.canvas.img))+"px";
          s.style.width=(x-drawnX)+"px";
          s.style.height="2px";
          s.style.visibility="visible";

          drawnY=Math.round(y);
          drawnX=x;

          lineBit++;
        }
      }
      for(;lineBit<line.bits.length;lineBit++)
      {
        line.bits[lineBit].style.visibility="hidden";
      }
    };
    line.drawVert=function(x1,y1,x2,y2)
    {
      var xStart,xEnd,yStart,yEnd;
      if(y1<y2)
      {
        xStart=x1; xEnd=x2; yStart=y1; yEnd=y2;
      }
      else
      {
        xStart=x2; xEnd=x1; yStart=y2; yEnd=y1;
      }
      var drawnX=xStart,drawnY=yStart,x=xStart;
      var dX=(xEnd-xStart)/(yEnd-yStart);

      var lineBit=0;
      for(var y=yStart+1;y<=yEnd+1;y+=2)
      {
        if(y==yEnd+1)
        {
          x+=dX;
          y=yEnd;
        }
        else
        {
          x+=dX*2;
        }
        if(Math.round(x)!=drawnX || y==yEnd)
        {
          var s;
          if(line.bits.length<=lineBit)
          {
            s=document.createElement("div");
            s.style.position="absolute";
            s.style.background=line.canvas.fg;
            line.e.appendChild(s);
            line.bits.push(s);
          }
          else s=line.bits[lineBit];

          s.style.left=(drawnX+trueoffsetleft(line.canvas.img))+"px";
          s.style.top=(drawnY+trueoffsettop(line.canvas.img))+"px";
          s.style.width="2px";
          s.style.height=(y-drawnY)+"px";
          s.style.visibility="visible";

          drawnY=y;
          drawnX=Math.round(x);

          lineBit++;
        }
      }
      for(;lineBit<line.bits.length;lineBit++)
      {
        line.bits[lineBit].style.visibility="hidden";
      }
    };
  }

  line.update();
}

function canvasHandleResize() {
  for(var i=1;i<canvasList.length;i+=2)
  {
    var canvas=canvasList[i],
    prevtop=parseFloat(canvas.img.getAttribute('data-top')),
    newtop=canvas.img.offsetTop;

    if(prevtop===null||prevtop===newtop)
    {
      // No change.
      continue;
    }

    // Canvas has moved. Save the new top.
    canvas.img.setAttribute("data-top",newtop);

    // Reposition all markers.
    for(var j=0;j<canvas.markers.length;j++)
    {
      var marker=canvas.markers[j];
      canvasSetPos(marker,marker.mX.value,marker.mY.value);
    }
  }
}
window.addEventListener('resize',canvasHandleResize);
